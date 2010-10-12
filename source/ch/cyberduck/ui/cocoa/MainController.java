package ch.cyberduck.ui.cocoa;

/*
 *  Copyright (c) 2005 David Kocher. All rights reserved.
 *  http://cyberduck.ch/
 *
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 2 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  Bug fixes, suggestions and comments should be sent to:
 *  dkocher@cyberduck.ch
 */

import ch.cyberduck.core.*;
import ch.cyberduck.core.Collection;
import ch.cyberduck.core.aquaticprime.License;
import ch.cyberduck.core.aquaticprime.LicenseFactory;
import ch.cyberduck.core.i18n.Locale;
import ch.cyberduck.core.importer.*;
import ch.cyberduck.core.serializer.HostReaderFactory;
import ch.cyberduck.core.threading.AbstractBackgroundAction;
import ch.cyberduck.core.threading.DefaultMainAction;
import ch.cyberduck.ui.cocoa.application.*;
import ch.cyberduck.ui.cocoa.delegate.*;
import ch.cyberduck.ui.cocoa.foundation.*;
import ch.cyberduck.ui.cocoa.urlhandler.URLSchemeHandlerConfiguration;
import ch.cyberduck.ui.growl.Growl;

import org.rococoa.Foundation;
import org.rococoa.ID;
import org.rococoa.cocoa.foundation.NSInteger;
import org.rococoa.cocoa.foundation.NSUInteger;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.charset.Charset;
import java.text.MessageFormat;
import java.util.*;

/**
 * Setting the main menu and implements application delegate methods
 *
 * @version $Id$
 */
public class MainController extends BundleController implements NSApplication.Delegate {
    private static Logger log = Logger.getLogger(MainController.class);

    /**
     * Apple event constants<br>
     * **********************************************************************************************<br>
     * <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/AvailabilityMacros.h:117</i>
     */
    public static final int kInternetEventClass = 1196773964;
    /**
     * Apple event constants<br>
     * **********************************************************************************************<br>
     * <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/AvailabilityMacros.h:118</i>
     */
    public static final int kAEGetURL = 1196773964;
    /**
     * Apple event constants<br>
     * **********************************************************************************************<br>
     * <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/AvailabilityMacros.h:119</i>
     */
    public static final int kAEFetchURL = 1179996748;

    /// 0x2d2d2d2d
    public static final int keyAEResult = 757935405;

    public MainController() {
        this.loadBundle();
    }

    @Override
    public void awakeFromNib() {
        NSAppleEventManager.sharedAppleEventManager().setEventHandler_andSelector_forEventClass_andEventID(
                this.id(), Foundation.selector("handleGetURLEvent:withReplyEvent:"), kInternetEventClass, kAEGetURL);

        super.awakeFromNib();
    }

    /**
     * Extract the URL from the Apple event and handle it here.
     *
     * @param event
     * @param reply
     */
    public void handleGetURLEvent_withReplyEvent(NSAppleEventDescriptor event, NSAppleEventDescriptor reply) {
        log.debug("Received URL from Apple Event:" + event);
        final NSAppleEventDescriptor param = event.paramDescriptorForKeyword(keyAEResult);
        if(null == param) {
            log.error("No URL parameter");
            return;
        }
        final String url = param.stringValue();
        if(StringUtils.isEmpty(url)) {
            log.error("URL parameter is empty");
            return;
        }
        final Host h = Host.parse(url);
        if(StringUtils.isNotEmpty(h.getDefaultPath())) {
            if(!h.getDefaultPath().endsWith(String.valueOf(Path.DELIMITER))) {
                final Session s = SessionFactory.createSession(h);
                final Path p = PathFactory.createPath(s, h.getDefaultPath(), Path.FILE_TYPE);
                if(StringUtils.isNotBlank(p.getExtension())) {
                    TransferController.instance().startTransfer(new DownloadTransfer(p));
                    return;
                }
            }
        }
        for(BrowserController controller : MainController.getBrowsers()) {
            if(controller.isMounted()) {
                if(controller.getSession().getHost().toURL().equals(h.toURL())) {
                    // Handle browser window already connected to the same host. #4215
                    controller.window().makeKeyAndOrderFront(null);
                    return;
                }
            }
        }
        BrowserController doc = newDocument();
        doc.mount(h);
    }

    @Outlet
    private NSMenu applicationMenu;

    public void setApplicationMenu(NSMenu menu) {
        this.applicationMenu = menu;
        String name = LicenseFactory.find().toString();
        this.applicationMenu.insertItemWithTitle_action_keyEquivalent_atIndex(name,
                null, "", new NSInteger(5));
        NSDictionary KEY_FONT_ATTRIBUTES = NSDictionary.dictionaryWithObjectsForKeys(
                NSArray.arrayWithObjects(NSFont.userFontOfSize(NSFont.smallSystemFontSize()), NSColor.darkGrayColor()),
                NSArray.arrayWithObjects(NSAttributedString.FontAttributeName, NSAttributedString.ForegroundColorAttributeName)
        );
        this.applicationMenu.itemWithTitle(name).setAttributedTitle(
                NSAttributedString.attributedStringWithAttributes(name, KEY_FONT_ATTRIBUTES)
        );
    }

    @Outlet
    private NSMenu encodingMenu;

    public void setEncodingMenu(NSMenu encodingMenu) {
        this.encodingMenu = encodingMenu;
        for(String charset : availableCharsets()) {
            this.encodingMenu.addItemWithTitle_action_keyEquivalent(charset, Foundation.selector("encodingMenuClicked:"), "");
        }
    }

    @Outlet
    private NSMenu columnMenu;

    public void setColumnMenu(NSMenu columnMenu) {
        this.columnMenu = columnMenu;
        Map<String, String> columns = new HashMap<String, String>();
        columns.put("browser.columnKind", Locale.localizedString("Kind"));
        columns.put("browser.columnSize", Locale.localizedString("Size"));
        columns.put("browser.columnModification", Locale.localizedString("Modified"));
        columns.put("browser.columnOwner", Locale.localizedString("Owner"));
        columns.put("browser.columnGroup", Locale.localizedString("Group"));
        columns.put("browser.columnPermissions", Locale.localizedString("Permissions"));
        Iterator<String> identifiers = columns.keySet().iterator();
        int i = 0;
        for(Iterator iter = columns.values().iterator(); iter.hasNext(); i++) {
            NSMenuItem item = this.columnMenu.addItemWithTitle_action_keyEquivalent((String) iter.next(),
                    Foundation.selector("columnMenuClicked:"), "");
            final String identifier = identifiers.next();
            item.setState(Preferences.instance().getBoolean(identifier) ? NSCell.NSOnState : NSCell.NSOffState);
            item.setRepresentedObject(identifier);
        }
    }

    @Action
    public void columnMenuClicked(final NSMenuItem sender) {
        final String identifier = sender.representedObject();
        final boolean enabled = !Preferences.instance().getBoolean(identifier);
        sender.setState(enabled ? NSCell.NSOnState : NSCell.NSOffState);
        Preferences.instance().setProperty(identifier, enabled);
        BrowserController.updateBrowserTableColumns();
    }

    @Outlet
    private NSMenu editMenu;
    private EditMenuDelegate editMenuDelegate;

    public void setEditMenu(NSMenu editMenu) {
        this.editMenu = editMenu;
        this.editMenuDelegate = new EditMenuDelegate() {
            @Override
            protected Local getSelectedFile() {
                final List<BrowserController> browsers = MainController.getBrowsers();
                for(BrowserController controller : browsers) {
                    if(controller.window().isKeyWindow()) {
                        return controller.getSelectedFile();
                    }
                }
                return null;
            }
        };
        this.editMenu.setDelegate(editMenuDelegate.id());
    }

    @Outlet
    private NSMenu urlMenu;
    private URLMenuDelegate urlMenuDelegate;

    public void setUrlMenu(NSMenu urlMenu) {
        this.urlMenu = urlMenu;
        this.urlMenuDelegate = new CopyURLMenuDelegate() {
            @Override
            protected List<Path> getSelected() {
                final List<BrowserController> browsers = MainController.getBrowsers();
                for(BrowserController controller : browsers) {
                    if(controller.window().isKeyWindow()) {
                        List<Path> selected = controller.getSelectedPaths();
                        if(selected.isEmpty()) {
                            if(controller.isMounted()) {
                                selected.add(controller.workdir());
                            }
                        }
                        return selected;
                    }
                }
                return Collections.emptyList();
            }
        };
        this.urlMenu.setDelegate(urlMenuDelegate.id());
    }

    @Outlet
    private NSMenu openUrlMenu;
    private URLMenuDelegate openUrlMenuDelegate;

    public void setOpenUrlMenu(NSMenu openUrlMenu) {
        this.openUrlMenu = openUrlMenu;
        this.openUrlMenuDelegate = new OpenURLMenuDelegate() {
            @Override
            protected List<Path> getSelected() {
                final List<BrowserController> browsers = MainController.getBrowsers();
                for(BrowserController controller : browsers) {
                    if(controller.window().isKeyWindow()) {
                        List<Path> selected = controller.getSelectedPaths();
                        if(selected.isEmpty()) {
                            if(controller.isMounted()) {
                                selected.add(controller.workdir());
                            }
                        }
                        return selected;
                    }
                }
                return Collections.emptyList();
            }
        };
        this.openUrlMenu.setDelegate(openUrlMenuDelegate.id());
    }

    @Outlet
    private NSMenu archiveMenu;
    private ArchiveMenuDelegate archiveMenuDelegate;

    public void setArchiveMenu(NSMenu archiveMenu) {
        this.archiveMenu = archiveMenu;
        this.archiveMenuDelegate = new ArchiveMenuDelegate();
        this.archiveMenu.setDelegate(archiveMenuDelegate.id());
    }

    @Outlet
    private NSMenu bookmarkMenu;
    private BookmarkMenuDelegate bookmarkMenuDelegate;

    public void setBookmarkMenu(NSMenu bookmarkMenu) {
        this.bookmarkMenu = bookmarkMenu;
        this.bookmarkMenuDelegate = new BookmarkMenuDelegate();
        this.bookmarkMenu.setDelegate(bookmarkMenuDelegate.id());
    }

    @Outlet
    private NSMenu historyMenu;
    private HistoryMenuDelegate historyMenuDelegate;

    public void setHistoryMenu(NSMenu historyMenu) {
        this.historyMenu = historyMenu;
        this.historyMenuDelegate = new HistoryMenuDelegate();
        this.historyMenu.setDelegate(historyMenuDelegate.id());
    }

    @Outlet
    private NSMenu rendezvousMenu;
    private RendezvousMenuDelegate rendezvousMenuDelegate;

    public void setRendezvousMenu(NSMenu rendezvousMenu) {
        this.rendezvousMenu = rendezvousMenu;
        this.rendezvousMenuDelegate = new RendezvousMenuDelegate();
        this.rendezvousMenu.setDelegate(rendezvousMenuDelegate.id());
    }

    @Action
    public void historyMenuClicked(NSMenuItem sender) {
        HistoryCollection.defaultCollection().open();
    }

    @Action
    public void bugreportMenuClicked(final ID sender) {
        openUrl(Preferences.instance().getProperty("website.bug"));
    }

    @Action
    public void helpMenuClicked(final ID sender) {
        openUrl(Preferences.instance().getProperty("website.help"));
    }

    @Action
    public void faqMenuClicked(final ID sender) {
        LocalFactory.createLocal(NSBundle.mainBundle().pathForResource_ofType("Cyberduck FAQ", "rtfd")).open();
    }

    @Action
    public void licenseMenuClicked(final ID sender) {
        LocalFactory.createLocal(NSBundle.mainBundle().pathForResource_ofType("License", "txt")).open();
    }

    @Action
    public void acknowledgmentsMenuClicked(final ID sender) {
        LocalFactory.createLocal(NSBundle.mainBundle().pathForResource_ofType("Acknowledgments", "rtf")).open();
    }

    @Action
    public void websiteMenuClicked(final ID sender) {
        openUrl(Preferences.instance().getProperty("website.home"));
    }

    @Action
    public void forumMenuClicked(final ID sender) {
        openUrl(Preferences.instance().getProperty("website.forum"));
    }

    @Action
    public void donateMenuClicked(final ID sender) {
        openUrl(Preferences.instance().getProperty("website.donate"));
    }

    @Action
    public void aboutMenuClicked(final ID sender) {
        NSDictionary dict = NSDictionary.dictionaryWithObjectsForKeys(
                NSArray.arrayWithObjects(NSBundle.mainBundle().objectForInfoDictionaryKey("CFBundleShortVersionString").toString(), ""),
                NSArray.arrayWithObjects("ApplicationVersion", "Version")
        );
        NSApplication.sharedApplication().orderFrontStandardAboutPanelWithOptions(dict);
    }

    @Action
    public void feedbackMenuClicked(final ID sender) {
        openUrl(Preferences.instance().getProperty("mail.feedback")
                + "?subject=" + Preferences.instance().getProperty("application.name") + "-" + Preferences.instance().getProperty("application.version"));
    }

    @Action
    public void preferencesMenuClicked(final ID sender) {
        PreferencesController controller = PreferencesController.instance();
        controller.window().makeKeyAndOrderFront(null);
    }

    @Action
    public void newDownloadMenuClicked(final ID sender) {
        this.showTransferQueueClicked(sender);
        SheetController c = new DownloadController(TransferController.instance());
        c.beginSheet();
    }

    @Action
    public void newBrowserMenuClicked(final ID sender) {
        this.openDefaultBookmark(MainController.newDocument(true));
    }

    @Action
    public void showTransferQueueClicked(final ID sender) {
        TransferController c = TransferController.instance();
        c.window().makeKeyAndOrderFront(null);
    }

    @Action
    public void showActivityWindowClicked(final ID sender) {
        ActivityController c = ActivityController.instance();
        if(c.isVisible()) {
            c.window().close();
        }
        else {
            c.window().orderFront(null);
        }
    }

    /**
     * @param app
     * @param filename
     * @return
     */
    public boolean application_openFile(NSApplication app, String filename) {
        log.debug("applicationOpenFile:" + filename);
        Local f = LocalFactory.createLocal(filename);
        if(f.exists()) {
            if("duck".equals(f.getExtension())) {
                final Host host = HostReaderFactory.instance().read(f);
                MainController.newDocument().mount(host);
                return true;
            }
            if("cyberducklicense".equals(f.getExtension())) {
                final License l = LicenseFactory.create(f);
                if(l.verify()) {
                    final NSAlert alert = NSAlert.alert(
                            l.toString(),
                            Locale.localizedString("Thanks for your support! Your contribution helps to further advance development to make Cyberduck even better.", "License")
                                    + "\n\n"
                                    + Locale.localizedString("Your donation key has been copied to the Application Support folder.", "License"),
                            Locale.localizedString("Continue", "License"), //default
                            null, //other
                            null);
                    alert.setAlertStyle(NSAlert.NSInformationalAlertStyle);
                    if(alert.runModal() == SheetCallback.DEFAULT_OPTION) {
                        f.copy(LocalFactory.createLocal(Preferences.instance().getProperty("application.support.path"), f.getName()));
                        for(BrowserController c : MainController.getBrowsers()) {
                            c.getDonateButton().removeFromSuperview();
                        }
                    }
                }
                else {
                    final NSAlert alert = NSAlert.alert(
                            Locale.localizedString("Not a valid donation key", "License"),
                            Locale.localizedString("This donation key does not appear to be valid.", "License"),
                            Locale.localizedString("Continue", "License"), //default
                            null, //other
                            null);
                    alert.setAlertStyle(NSAlert.NSWarningAlertStyle);
                    alert.runModal(); //alternate
                }
                return true;
            }
            for(BrowserController controller : MainController.getBrowsers()) {
                if(controller.isMounted()) {
                    final Path workdir = controller.workdir();
                    final Session session = controller.getTransferSession();
                    final Transfer q = new UploadTransfer(
                            PathFactory.createPath(session, workdir.getAbsolute(), f)
                    );
                    controller.transfer(q, workdir);
                    break;
                }
            }
        }
        return false;
    }

    /**
     * Sent directly by theApplication to the delegate. The method should attempt to open the file filename,
     * returning true if the file is successfully opened, and false otherwise. By design, a
     * file opened through this method is assumed to be temporary its the application's
     * responsibility to remove the file at the appropriate time.
     *
     * @param app
     * @param filename
     * @return
     */
    public boolean application_openTempFile(NSApplication app, String filename) {
        log.debug("applicationOpenTempFile:" + filename);
        return this.application_openFile(app, filename);
    }

    /**
     * Invoked immediately before opening an untitled file. Return false to prevent
     * the application from opening an untitled file; return true otherwise.
     * Note that applicationOpenUntitledFile is invoked if this method returns true.
     *
     * @param sender
     * @return
     */
    public boolean applicationShouldOpenUntitledFile(NSApplication sender) {
        log.debug("applicationShouldOpenUntitledFile");
        return Preferences.instance().getBoolean("browser.openUntitled");
    }

    /**
     * @return true if the file was successfully opened, false otherwise.
     */
    public boolean applicationOpenUntitledFile(NSApplication app) {
        log.debug("applicationOpenUntitledFile");
        return false;
    }

    /**
     * Mounts the default bookmark if any
     *
     * @param controller
     */
    private void openDefaultBookmark(BrowserController controller) {
        String defaultBookmark = Preferences.instance().getProperty("browser.defaultBookmark");
        if(null == defaultBookmark) {
            log.info("No default bookmark configured");
            return; //No default bookmark given
        }
        Host bookmark = BookmarkCollection.defaultCollection().lookup(defaultBookmark);
        if(null == bookmark) {
            log.info("Default bookmark no more available");
            return;
        }
        for(BrowserController browser : getBrowsers()) {
            if(browser.hasSession()) {
                if(browser.getSession().getHost().equals(bookmark)) {
                    log.debug("Default bookmark already mounted");
                    return;
                }
            }
        }
        log.debug("Mounting default bookmark " + bookmark);
        controller.mount(bookmark);
    }

    /**
     * These events are sent whenever the Finder reactivates an already running application
     * because someone double-clicked it again or used the dock to activate it. By default
     * the Application Kit will handle this event by checking whether there are any visible
     * NSWindows (not NSPanels), and, if there are none, it goes through the standard untitled
     * document creation (the same as it does if theApplication is launched without any document
     * to open). For most document-based applications, an untitled document will be created.
     * The application delegate will also get a chance to respond to the normal untitled document
     * delegations. If you implement this method in your application delegate, it will be called
     * before any of the default behavior happens. If you return true, then NSApplication will
     * go on to do its normal thing. If you return false, then NSApplication will do nothing.
     * So, you can either implement this method, do nothing, and return false if you do not
     * want anything to happen at all (not recommended), or you can implement this method,
     * handle the event yourself in some custom way, and return false.
     *
     * @param app
     * @param visibleWindowsFound
     * @return
     */
    public boolean applicationShouldHandleReopen_hasVisibleWindows(NSApplication app, boolean visibleWindowsFound) {
        log.debug("applicationShouldHandleReopen");
        // While an application is open, the Dock icon has a symbol below it.
        // When a user clicks an open application’s icon in the Dock, the application
        // becomes active and all open unminimized windows are brought to the front;
        // minimized document windows remain in the Dock. If there are no unminimized
        // windows when the user clicks the Dock icon, the last minimized window should
        // be expanded and made active. If no documents are open, the application should
        // open a new window. (If your application is not document-based, display the
        // application’s main window.)
        if(MainController.getBrowsers().isEmpty() && !TransferController.instance().isVisible()) {
            this.openDefaultBookmark(MainController.newDocument());
        }
        NSWindow miniaturized = null;
        for(BrowserController controller : MainController.getBrowsers()) {
            if(!controller.window().isMiniaturized()) {
                return false;
            }
            if(null == miniaturized) {
                miniaturized = controller.window();
            }
        }
        if(null == miniaturized) {
            return false;
        }
        miniaturized.deminiaturize(null);
        return false;
    }

    /**
     * Sent by the default notification center after the application has been launched and initialized but
     * before it has received its first event. aNotification is always an
     * ApplicationDidFinishLaunchingNotification. You can retrieve the NSApplication
     * object in question by sending object to aNotification. The delegate can implement
     * this method to perform further initialization. If the user started up the application
     * by double-clicking a file, the delegate receives the applicationOpenFile message before receiving
     * applicationDidFinishLaunching. (applicationWillFinishLaunching is sent before applicationOpenFile.)
     *
     * @param notification
     */
    public void applicationDidFinishLaunching(NSNotification notification) {
        log.info("Running Java " + System.getProperty("java.version") + " on " + System.getProperty("os.arch"));
        if(log.isInfoEnabled()) {
            log.info("Available localizations:" + NSBundle.mainBundle().localizations());
            log.info("Current locale:" + java.util.Locale.getDefault());
        }
        if(Preferences.instance().getBoolean("browser.openUntitled")) {
            MainController.newDocument();
        }
        if(Preferences.instance().getBoolean("queue.openByDefault")) {
            this.showTransferQueueClicked(null);
        }
        if(Preferences.instance().getBoolean("browser.serialize")) {
            this.background(new AbstractBackgroundAction() {
                public void run() {
                    sessions.load();
                }

                @Override
                public void cleanup() {
                    for(Host host : sessions) {
                        MainController.newDocument().mount(host);
                    }
                    sessions.clear();
                }
            });
        }
        this.background(new AbstractBackgroundAction() {
            public void run() {
                BookmarkCollection.defaultCollection().load();
            }

            @Override
            public void cleanup() {
                if(Preferences.instance().getBoolean("browser.openUntitled")) {
                    if(MainController.getBrowsers().isEmpty()) {
                        openDefaultBookmark(MainController.newDocument());
                    }
                }
            }
        });
        this.background(new AbstractBackgroundAction() {
            public void run() {
                HistoryCollection.defaultCollection().load();
            }
        });
        this.background(new AbstractBackgroundAction() {
            public void run() {
                TransferCollection.defaultCollection().load();
            }
        });
        this.background(new AbstractBackgroundAction() {
            public void run() {
                // Make sure we register to Growl first
                Growl.instance().register();
            }
        });
        if(Preferences.instance().getBoolean("rendezvous.enable")) {
            RendezvousFactory.instance().addListener(new RendezvousListener() {
                public void serviceResolved(final String identifier, final String hostname) {
                    if(Preferences.instance().getBoolean("rendezvous.loopback.supress")) {
                        try {
                            if(InetAddress.getByName(hostname).equals(InetAddress.getLocalHost())) {
                                log.info("Supressed Rendezvous notification for " + hostname);
                                return;
                            }
                        }
                        catch(UnknownHostException e) {
                            ; //Ignore
                        }
                    }
                    invoke(new DefaultMainAction() {
                        public void run() {
                            Growl.instance().notifyWithImage("Bonjour", RendezvousFactory.instance().getDisplayedName(identifier), "rendezvous");
                        }
                    });
                }

                public void serviceLost(String servicename) {
                    ;
                }
            });
        }
        if(Preferences.instance().getBoolean("defaulthandler.reminder")
                && Preferences.instance().getInteger("uses") > 0) {
            if(!URLSchemeHandlerConfiguration.instance().isDefaultHandlerForURLScheme(
                    new String[]{Protocol.FTP.getScheme(), Protocol.FTP_TLS.getScheme(), Protocol.SFTP.getScheme()})) {
                final NSAlert alert = NSAlert.alert(
                        Locale.localizedString("Set Cyberduck as default application for FTP and SFTP locations?", "Configuration"),
                        Locale.localizedString("As the default application, Cyberduck will open when you click on FTP or SFTP links in other applications, such as your web browser. You can change this setting in the Preferences later.", "Configuration"),
                        Locale.localizedString("Change", "Configuration"), //default
                        Locale.localizedString("Don't Ask Again", "Configuration"), //other
                        Locale.localizedString("Cancel", "Configuration"));
                alert.setAlertStyle(NSAlert.NSInformationalAlertStyle);
                int choice = alert.runModal(); //alternate
                if(choice == SheetCallback.DEFAULT_OPTION) {
                    URLSchemeHandlerConfiguration.instance().setDefaultHandlerForURLScheme(
                            new String[]{Protocol.FTP.getScheme(), Protocol.FTP_TLS.getScheme(), Protocol.SFTP.getScheme()},
                            NSBundle.mainBundle().infoDictionary().objectForKey("CFBundleIdentifier").toString()
                    );
                }
                if(choice == SheetCallback.ALTERNATE_OPTION) {
                    Preferences.instance().setProperty("defaulthandler.reminder", false);
                }
            }
        }
        // NSWorkspace notifications are posted to a notification center provided by
        // the NSWorkspace object, instead of going through the application’s default
        // notification center as most notifications do. To receive NSWorkspace notifications,
        // your application must register an observer with the NSWorkspace notification center.
        NSWorkspace.sharedWorkspace().notificationCenter().addObserver(this.id(),
                Foundation.selector("workspaceWillPowerOff:"),
                NSWorkspace.WorkspaceWillPowerOffNotification,
                null);
        NSWorkspace.sharedWorkspace().notificationCenter().addObserver(this.id(),
                Foundation.selector("workspaceWillLogout:"),
                NSWorkspace.WorkspaceSessionDidResignActiveNotification,
                null);
        NSWorkspace.sharedWorkspace().notificationCenter().addObserver(this.id(),
                Foundation.selector("workspaceWillSleep:"),
                NSWorkspace.WorkspaceWillSleepNotification,
                null);
        NSNotificationCenter.defaultCenter().addObserver(this.id(),
                Foundation.selector("applicationWillRestartAfterUpdate:"),
                "SUUpdaterWillRestartNotificationName",
                null);
        if(Preferences.instance().getBoolean("rendezvous.enable")) {
            this.background(new AbstractBackgroundAction() {
                public void run() {
                    RendezvousFactory.instance().init();
                }
            });
        }
        // Import thirdparty bookmarks.
        for(ThirdpartyBookmarkCollection c : this.getThirdpartyBookmarks()) {
            if(!Preferences.instance().getBoolean(c.getConfiguration())) {
                if(!c.isInstalled()) {
                    log.info("No application installed for " + c.getBundleIdentifier());
                    continue;
                }
                c.load();
                if(!c.isEmpty()) {
                    final NSAlert alert = NSAlert.alert(
                            MessageFormat.format(Locale.localizedString("Import {0} Bookmarks", "Configuration"), c.getName()),
                            MessageFormat.format(Locale.localizedString("{0} bookmarks found. Do you want to add these to your bookmarks?", "Configuration"), c.size()),
                            Locale.localizedString("Import", "Configuration"), //default
                            null, //other
                            Locale.localizedString("Cancel", "Configuration"));
                    alert.setShowsSuppressionButton(true);
                    alert.suppressionButton().setTitle(Locale.localizedString("Don't Ask Again", "Configuration"));
                    alert.setAlertStyle(NSAlert.NSInformationalAlertStyle);
                    int choice = alert.runModal(); //alternate
                    if(alert.suppressionButton().state() == NSCell.NSOnState) {
                        // Never show again.
                        Preferences.instance().setProperty(c.getConfiguration(), true);
                    }
                    if(choice == SheetCallback.DEFAULT_OPTION) {
                        BookmarkCollection.defaultCollection().addAll(c);
                        // Flag as imported
                        Preferences.instance().setProperty(c.getConfiguration(), true);
                    }
                }
            }
        }
    }

    private List<ThirdpartyBookmarkCollection> getThirdpartyBookmarks() {
        return Arrays.asList(new TransmitBookmarkCollection(), new FilezillaBookmarkCollection(), new FetchBookmarkCollection(),
                new FlowBookmarkCollection(), new InterarchyBookmarkCollection());
    }

    /**
     * Saved browsers
     */
    private HistoryCollection sessions = new HistoryCollection(
            LocalFactory.createLocal(Preferences.instance().getProperty("application.support.path"), "Sessions"));

    /**
     * Display donation reminder dialog
     */
    private boolean donationPrompt = true;
    private WindowController donationController;


    /**
     * Invoked from within the terminate method immediately before the
     * application terminates. sender is the NSApplication to be terminated.
     * If this method returns false, the application is not terminated,
     * and control returns to the main event loop.
     *
     * @param app
     * @return Return true to allow the application to terminate.
     */
    public NSUInteger applicationShouldTerminate(final NSApplication app) {
        log.debug("applicationShouldTerminate");
        // Determine if there are any running transfers
        NSUInteger result = TransferController.applicationShouldTerminate(app);
        if(!result.equals(NSApplication.NSTerminateNow)) {
            return result;
        }
        // Determine if there are any open connections
        for(BrowserController controller : MainController.getBrowsers()) {
            if(Preferences.instance().getBoolean("browser.serialize")) {
                if(controller.isMounted()) {
                    // The workspace should be saved. Serialize all open browser sessions
                    final Host serialized = new Host(controller.getSession().getHost().getAsDictionary());
                    serialized.setWorkdir(controller.workdir().getAbsolute());
                    sessions.add(serialized);
                }
            }
            if(controller.isConnected()) {
                if(Preferences.instance().getBoolean("browser.confirmDisconnect")) {
                    final NSAlert alert = NSAlert.alert(Locale.localizedString("Quit"),
                            Locale.localizedString("You are connected to at least one remote site. Do you want to review open browsers?"),
                            Locale.localizedString("Quit Anyway"), //default
                            Locale.localizedString("Cancel"), //other
                            Locale.localizedString("Review..."));
                    alert.setAlertStyle(NSAlert.NSWarningAlertStyle);
                    int choice = alert.runModal(); //alternate
                    if(choice == SheetCallback.OTHER_OPTION) {
                        // Review if at least one window reqested to terminate later, we shall wait.
                        // This will iterate over all mounted browsers.
                        result = BrowserController.applicationShouldTerminate(app);
                        if(NSApplication.NSTerminateNow.equals(result)) {
                            return this.applicationShouldTerminateAfterDonationPrompt(app);
                        }
                        return result;
                    }
                    if(choice == SheetCallback.ALTERNATE_OPTION) {
                        // Cancel. Quit has been interrupted. Delete any saved sessions so far.
                        sessions.clear();
                        return NSApplication.NSTerminateCancel;
                    }
                    if(choice == SheetCallback.DEFAULT_OPTION) {
                        // Quit immediatly
                        return this.applicationShouldTerminateAfterDonationPrompt(app);
                    }
                }
                else {
                    controller.unmount();
                }
            }
        }
        return this.applicationShouldTerminateAfterDonationPrompt(app);
    }

    public NSUInteger applicationShouldTerminateAfterDonationPrompt(final NSApplication app) {
        log.debug("applicationShouldTerminateAfterDonationPrompt");
        if(!donationPrompt) {
            // Already displayed
            return NSApplication.NSTerminateNow;
        }
        final License l = LicenseFactory.find();
        if(!l.verify()) {
            final String lastversion = Preferences.instance().getProperty("donate.reminder");
            if(NSBundle.mainBundle().infoDictionary().objectForKey("Version").toString().equals(lastversion)) {
                // Do not display if same version is installed
                return NSApplication.NSTerminateNow;
            }
            final Calendar nextreminder = Calendar.getInstance();
            nextreminder.setTimeInMillis(Preferences.instance().getLong("donate.reminder.date"));
            // Display donationPrompt every n days
            nextreminder.add(Calendar.DAY_OF_YEAR, Preferences.instance().getInteger("donate.reminder.interval"));
            log.debug("Next reminder:" + nextreminder.getTime().toString());
            // Display after upgrade
            if(nextreminder.getTime().after(new Date(System.currentTimeMillis()))) {
                // Do not display if shown in the reminder interval
                return NSApplication.NSTerminateNow;
            }
            final int uses = Preferences.instance().getInteger("uses");
            donationController = new WindowController() {
                @Override
                protected String getBundleName() {
                    return "Donate";
                }

                @Outlet
                private NSButton neverShowDonationCheckbox;

                public void setNeverShowDonationCheckbox(NSButton neverShowDonationCheckbox) {
                    this.neverShowDonationCheckbox = neverShowDonationCheckbox;
                    this.neverShowDonationCheckbox.setTarget(this.id());
                    this.neverShowDonationCheckbox.setState(
                            Preferences.instance().getProperty("donate.reminder").equals(
                                    NSBundle.mainBundle().infoDictionary().objectForKey("Version").toString())
                                    ? NSCell.NSOnState : NSCell.NSOffState);
                }

                @Override
                public void awakeFromNib() {
                    this.window().setTitle(this.window().title() + " (" + uses + ")");
                    this.window().center();
                    this.window().makeKeyAndOrderFront(null);

                    super.awakeFromNib();
                }

                public void closeDonationSheet(final NSButton sender) {
                    if(sender.tag() == SheetCallback.DEFAULT_OPTION) {
                        openUrl(Preferences.instance().getProperty("website.donate"));
                    }
                    this.terminate();
                }

                @Override
                public void windowWillClose(NSNotification notification) {
                    this.terminate();
                    super.windowWillClose(notification);
                }

                private void terminate() {
                    if(neverShowDonationCheckbox.state() == NSCell.NSOnState) {
                        Preferences.instance().setProperty("donate.reminder",
                                NSBundle.mainBundle().infoDictionary().objectForKey("Version").toString());
                    }
                    // Remeber this reminder date
                    Preferences.instance().setProperty("donate.reminder.date", System.currentTimeMillis());
                    donationPrompt = false;
                    // Quit again
                    app.replyToApplicationShouldTerminate(true);
                }
            };
            donationController.loadBundle();
            // Delay application termination. Dismissing the donation dialog will reply to quit.
            return NSApplication.NSTerminateLater;
        }
        return NSApplication.NSTerminateNow;
    }

    /**
     * Quits the Rendezvous daemon and saves all preferences
     *
     * @param notification
     */
    public void applicationWillTerminate(NSNotification notification) {
        log.debug("applicationWillTerminate");

        this.invalidate();

        if(Preferences.instance().getBoolean("rendezvous.enable")) {
            //Terminating rendezvous discovery
            RendezvousFactory.instance().quit();
        }
        //Writing usage info
        Preferences.instance().setProperty("uses", Preferences.instance().getInteger("uses") + 1);
        Preferences.instance().save();
    }

    public void applicationWillRestartAfterUpdate(ID updater) {
        // Disable donation prompt after udpate install
        donationPrompt = false;
    }

    /**
     * Posted when the user has requested a logout or that the machine be powered off.
     *
     * @param notification
     */
    public void workspaceWillPowerOff(NSNotification notification) {
        log.debug("workspaceWillPowerOff");
    }

    /**
     * Posted before a user session is switched out. This allows an application to
     * disable some processing when its user session is switched out, and reenable when that
     * session gets switched back in, for example.
     *
     * @param notification
     */
    public void workspaceWillLogout(NSNotification notification) {
        log.debug("workspaceWillLogout");
    }

    public void workspaceWillSleep(NSNotification notification) {
        log.debug("workspaceWillSleep");
    }

    /**
     * Makes a unmounted browser window the key window and brings it to the front
     *
     * @return A reference to a browser window
     */
    public static BrowserController newDocument() {
        return MainController.newDocument(false);
    }

    /**
     *
     */
    private static List<BrowserController> browsers
            = new ArrayList<BrowserController>();

    /**
     * @return
     */
    public static List<BrowserController> getBrowsers() {
        return browsers;
    }


    /**
     * Makes a unmounted browser window the key window and brings it to the front
     *
     * @param force If true, open a new browser regardeless of any unused browser window
     * @return A reference to a browser window
     */
    public static BrowserController newDocument(boolean force) {
        log.debug("newDocument");
        final List<BrowserController> browsers = MainController.getBrowsers();
        if(!force) {
            for(BrowserController controller : browsers) {
                if(!controller.hasSession()) {
                    controller.window().makeKeyAndOrderFront(null);
                    return controller;
                }
            }
        }
        final BrowserController controller = new BrowserController();
        controller.addListener(new WindowListener() {
            public void windowWillClose() {
                browsers.remove(controller);
            }
        });
        if(!browsers.isEmpty()) {
            controller.cascade();
        }
        controller.window().makeKeyAndOrderFront(null);
        browsers.add(controller);
        return controller;
    }

    /**
     * Sent by Cocoa’s built-in scripting support during execution of get or set script commands to
     * find out if the delegate can handle operations on the specified key-value key.
     *
     * @param application
     * @param key
     * @return
     */
    @Applescript
    public boolean application_delegateHandlesKey(NSApplication application, String key) {
        return key.equals("orderedBrowsers");
    }

    @Applescript
    public NSArray orderedBrowsers() {
        NSMutableArray orderedDocs = NSMutableArray.array();
        for(BrowserController browser : MainController.getBrowsers()) {
            orderedDocs.addObject(browser.proxy());
        }
        return orderedDocs;
    }

    /**
     * We are not a Windows application. Long live the application wide menu bar.
     *
     * @param app
     * @return
     */
    public boolean applicationShouldTerminateAfterLastWindowClosed(NSApplication app) {
        return false;
    }

    /**
     * @return The available character sets available on this platform
     */
    public static String[] availableCharsets() {
        List<String> charsets = new Collection<String>();
        for(Charset charset : Charset.availableCharsets().values()) {
            final String name = charset.displayName();
            if(!(name.startsWith("IBM") || name.startsWith("x-"))) {
                charsets.add(name);
            }
        }
        return charsets.toArray(new String[charsets.size()]);
    }

    @Override
    protected String getBundleName() {
        return "Main";
    }
}