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
import ch.cyberduck.ui.cocoa.delegate.HistoryMenuDelegate;
import ch.cyberduck.ui.cocoa.growl.Growl;

import com.apple.cocoa.application.*;
import com.apple.cocoa.foundation.*;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * @version $Id$
 */
public class CDMainController extends CDController {
    private static Logger log = Logger.getLogger(CDMainController.class);

    static {
        BasicConfigurator.configure();
    }

    public void awakeFromNib() {
        NSNotificationCenter.defaultCenter().addObserver(this,
                new NSSelector("applicationShouldSleep", new Class[]{Object.class}),
                NSWorkspace.WorkspaceWillSleepNotification,
                null);

        NSNotificationCenter.defaultCenter().addObserver(this,
                new NSSelector("applicationShouldWake", new Class[]{Object.class}),
                NSWorkspace.WorkspaceDidWakeNotification,
                null);
        Logger.getRootLogger().setLevel(Level.toLevel(
                Preferences.instance().getProperty("logging")));
    }

    // ----------------------------------------------------------
    // Outlets
    // ----------------------------------------------------------

    private NSMenu encodingMenu;

    public void setEncodingMenu(NSMenu encodingMenu) {
        this.encodingMenu = encodingMenu;
        String[] charsets = ((CDMainController) NSApplication.sharedApplication().delegate()).availableCharsets();
        for(int i = 0; i < charsets.length; i++) {
            NSMenuItem item = new NSMenuItem(charsets[i],
                    new NSSelector("encodingMenuClicked", new Class[]{Object.class}),
                    "");
            this.encodingMenu.addItem(item);
        }
    }

    private NSMenu columnMenu;

    public void setColumnMenu(NSMenu columnMenu) {
        this.columnMenu = columnMenu;
        Map columns = new HashMap();
        columns.put("browser.columnKind", NSBundle.localizedString("Kind", ""));
        columns.put("browser.columnSize", NSBundle.localizedString("Size", ""));
        columns.put("browser.columnModification", NSBundle.localizedString("Modified", ""));
        columns.put("browser.columnOwner", NSBundle.localizedString("Owner", ""));
        columns.put("browser.columnPermissions", NSBundle.localizedString("Permissions", ""));
        Iterator identifiers = columns.keySet().iterator();
        int i = 0;
        for(Iterator iter = columns.values().iterator(); iter.hasNext(); i++) {
            NSMenuItem item = new NSMenuItem((String)iter.next(),
                    new NSSelector("columnMenuClicked", new Class[]{Object.class}),
                    "");
            final String identifier = (String)identifiers.next();
            item.setState(Preferences.instance().getBoolean(identifier) ? NSCell.OnState : NSCell.OffState);
            item.setRepresentedObject(identifier);
            this.columnMenu.insertItemAtIndex(item, i);
        }
    }

    public void columnMenuClicked(final NSMenuItem sender) {
        final String identifier = (String)sender.representedObject();
        final boolean enabled = !Preferences.instance().getBoolean(identifier);
        sender.setState(enabled ? NSCell.OnState : NSCell.OffState);
        Preferences.instance().setProperty(identifier, enabled);
        CDBrowserController.updateBrowserTableColumns();
    }

    public void historyMenuClicked(NSMenuItem sender) {
        NSWorkspace.sharedWorkspace().openFile(HistoryMenuDelegate.HISTORY_FOLDER.getAbsolutePath());
    }

    public void bugreportMenuClicked(final Object sender) {
        try {
            NSWorkspace.sharedWorkspace().openURL(
                    new java.net.URL(Preferences.instance().getProperty("website.bug")));
        }
        catch(java.net.MalformedURLException e) {
            log.error(e.getMessage());
        }
    }

    public void helpMenuClicked(final Object sender) {
        try {
            String locale = "en";
            NSArray preferredLocalizations = NSBundle.preferredLocalizations(
                    NSBundle.mainBundle().localizations());
            if(preferredLocalizations.count() > 0) {
                locale = (String) preferredLocalizations.objectAtIndex(0);
            }
            NSWorkspace.sharedWorkspace().openURL(
                    new java.net.URL(Preferences.instance().getProperty("website.help") + locale + "/"));
        }
        catch(java.net.MalformedURLException e) {
            log.error(e.getMessage());
        }
    }

    public void faqMenuClicked(final Object sender) {
        NSWorkspace.sharedWorkspace().openFile(
                new File(NSBundle.mainBundle().pathForResource("Cyberduck FAQ", "rtfd")).toString());
    }

    public void licenseMenuClicked(final Object sender) {
        NSWorkspace.sharedWorkspace().openFile(
                new File(NSBundle.mainBundle().pathForResource("License", "txt")).toString());
    }

    public void websiteMenuClicked(final Object sender) {
        try {
            NSWorkspace.sharedWorkspace().openURL(new java.net.URL(Preferences.instance().getProperty("website.home")));
        }
        catch(java.net.MalformedURLException e) {
            log.error(e.getMessage());
        }
    }

    public void forumMenuClicked(final Object sender) {
        try {
            NSWorkspace.sharedWorkspace().openURL(new java.net.URL(Preferences.instance().getProperty("website.forum")));
        }
        catch(java.net.MalformedURLException e) {
            log.error(e.getMessage());
        }
    }

    public void donateMenuClicked(final Object sender) {
        try {
            NSWorkspace.sharedWorkspace().openURL(new java.net.URL(Preferences.instance().getProperty("website.donate")));
        }
        catch(java.net.MalformedURLException e) {
            log.error(e.getMessage());
        }
    }

    public void aboutMenuClicked(final Object sender) {
        NSDictionary dict = new NSDictionary(
                new String[]{(String) NSBundle.mainBundle().objectForInfoDictionaryKey("CFBundleShortVersionString"), ""},
                new String[]{"ApplicationVersion", "Version"}
        );
        NSApplication.sharedApplication().orderFrontStandardAboutPanelWithOptions(dict);
    }

    public void feedbackMenuClicked(final Object sender) {
        try {
            String versionString = (String) NSBundle.mainBundle().objectForInfoDictionaryKey("CFBundleVersion");
            NSWorkspace.sharedWorkspace().openURL(new java.net.URL(Preferences.instance().getProperty("mail.feedback")
                    + "?subject=Cyberduck-" + versionString));
        }
        catch(java.net.MalformedURLException e) {
            log.error(e.getMessage());
        }
    }

    public void preferencesMenuClicked(final Object sender) {
        CDPreferencesController controller = CDPreferencesController.instance();
        controller.window().makeKeyAndOrderFront(null);
    }

    public void newDownloadMenuClicked(final Object sender) {
        CDSheetController c = new CDDownloadController(CDQueueController.instance());
        c.beginSheet(false);
    }

    public void newBrowserMenuClicked(final Object sender) {
        this.openDefaultBookmark(this.newDocument(true));
    }

    public void showTransferQueueClicked(final Object sender) {
        CDQueueController c = CDQueueController.instance();
        c.window().makeKeyAndOrderFront(null);
    }

    public void downloadBookmarksFromDotMacClicked(final Object sender) {
        CDDotMacController controller = new CDDotMacController();
        controller.downloadBookmarks();
        controller.invalidate();
    }

    public void uploadBookmarksToDotMacClicked(final Object sender) {
        CDDotMacController c = new CDDotMacController();
        c.uploadBookmarks();
        c.invalidate();
    }

    // ----------------------------------------------------------
    // Application delegate methods
    // ----------------------------------------------------------

    /**
     * @param app
     * @param filename
     * @return
     */
    public boolean applicationOpenFile(NSApplication app, String filename) {
        log.debug("applicationOpenFile:" + filename);
        File f = new File(filename);
        if(f.exists()) {
            if(f.getAbsolutePath().endsWith(".duck")) {
                Host host = this.importBookmark(f);
                if(host != null) {
                    this.newDocument().mount(host);
                    return true;
                }
            }
            else {
                NSArray windows = NSApplication.sharedApplication().windows();
                int count = windows.count();
                while(0 != count--) {
                    NSWindow window = (NSWindow) windows.objectAtIndex(count);
                    final CDBrowserController controller = CDBrowserController.controllerForWindow(window);
                    if(null != controller) {
                        if(controller.isMounted()) {
                            final Path workdir = controller.workdir();
                            final Transfer q = new UploadTransfer();
                            final Session session = controller.getTransferSession();
                            q.addRoot(PathFactory.createPath(session, workdir.getAbsolute(),
                                    new Local(f.getAbsolutePath())));
                            controller.transfer(q, workdir);
                            break;
                        }
                    }
                }
            }
        }
        return false;
    }

    /**
     * Sent directly by theApplication to the delegate. The method should attempt to open the file filename,
     * returning true if the file is successfully opened, and false otherwise. By design, a
     * file opened through this method is assumed to be temporary—it’s the application’s
     * responsibility to remove the file at the appropriate time.
     *
     * @param app
     * @param filename
     * @return
     */
    public boolean applicationOpenTempFile(NSApplication app, String filename) {
        log.debug("applicationOpenTempFile:" + filename);
        return this.applicationOpenFile(app, filename);
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
     * @param controller
     */
    private void openDefaultBookmark(CDBrowserController controller) {
        String defaultBookmark = Preferences.instance().getProperty("browser.defaultBookmark");
        if(null == defaultBookmark) {
            return; //No default bookmark given
        }
        for(Iterator iter = HostCollection.instance().iterator(); iter.hasNext();) {
            Host host = (Host) iter.next();
            if(host.getNickname().equals(defaultBookmark)) {
                controller.mount(host);
                return;
            }
        }
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
    public boolean applicationShouldHandleReopen(NSApplication app, boolean visibleWindowsFound) {
        log.debug("applicationShouldHandleReopen");
        if(this.orderedBrowsers().count() == 0 && this.orderedTransfers().count() == 0) {
            this.openDefaultBookmark(this.newDocument());
        }
        return false;
    }

    private static final File SESSIONS_FOLDER
            = new File(Preferences.instance().getProperty("application.support.path"), "Sessions");

    static {
        SESSIONS_FOLDER.mkdir();
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
        log.info("Running Java " + System.getProperty("java.version"));
        Growl.instance().register();
        if(log.isInfoEnabled())
            log.info("Available localizations:" + NSBundle.mainBundle().localizations());
        if(Preferences.instance().getBoolean("queue.openByDefault")) {
            this.showTransferQueueClicked(null);
        }
        Rendezvous.instance().addListener(new RendezvousListener() {
            public void serviceResolved(String identifier) {
                Growl.instance().notifyWithImage("Bonjour", Rendezvous.instance().getDisplayedName(identifier),
                        "rendezvous.icns");
            }

            public void serviceLost(String servicename) {
                ;
            }
        });
        if(Preferences.instance().getBoolean("rendezvous.enable")) {
            Rendezvous.instance().init();
        }
        if(Preferences.instance().getBoolean("browser.serialize")) {
            File[] files = SESSIONS_FOLDER.listFiles(new java.io.FilenameFilter() {
                public boolean accept(File dir, String name) {
                    return name.endsWith(".duck");
                }
            });
            for(int i = 0; i < files.length; i++) {
                this.newDocument(true).mount(this.importBookmark(files[i]));
                files[i].delete();
            }
            if(files.length == 0) {
                // Open empty browser if no saved sessions
                if(Preferences.instance().getBoolean("browser.openUntitled")) {
                    this.openDefaultBookmark(this.newDocument());
                }
            }
        }
        else if(Preferences.instance().getBoolean("browser.openUntitled")) {
            this.openDefaultBookmark(this.newDocument());
        }

    }

    /**
     * Posted before the machine goes to sleep. An observer of this message
     * can delay sleep for up to 30 seconds while handling this notification.
     *
     * @param o
     */
    public void applicationShouldSleep(Object o) {
        log.debug("applicationShouldSleep");
        //Stopping rendezvous service discovery
        if(Preferences.instance().getBoolean("rendezvous.enable")) {
            Rendezvous.instance().quit();
        }
        //halt all transfers
        CDQueueController.instance().stopAllButtonClicked(null);
        //close all browsing connections
        NSArray windows = NSApplication.sharedApplication().windows();
        int count = windows.count();
        // Determine if there are any open connections
        while(0 != count--) {
            NSWindow window = (NSWindow) windows.objectAtIndex(count);
            CDBrowserController controller = CDBrowserController.controllerForWindow(window);
            if(null != controller) {
                if(controller.isBusy()) {
                    controller.interrupt();
                }
                controller.unmount(false);
            }
        }
    }

    /**
     *
     */
    private boolean donationBoxDisplayed = false;

    /**
     * Invoked from within the terminate method immediately before the
     * application terminates. sender is the NSApplication to be terminated.
     * If this method returns false, the application is not terminated,
     * and control returns to the main event loop.
     *
     * @param app
     * @return Return true to allow the application to terminate.
     */
    public int applicationShouldTerminate(NSApplication app) {
        log.debug("applicationShouldTerminate");
        if(!donationBoxDisplayed && Preferences.instance().getBoolean("donate.reminder")) {
            final int uses = Preferences.instance().getInteger("uses");
            CDWindowController c = new CDWindowController() {
                private NSButton neverShowDonationCheckbox;

                public void setNeverShowDonationCheckbox(NSButton neverShowDonationCheckbox) {
                    this.neverShowDonationCheckbox = neverShowDonationCheckbox;
                    this.neverShowDonationCheckbox.setTarget(this);
                    this.neverShowDonationCheckbox.setState(NSCell.OffState);
                }

                public void awakeFromNib() {
                    this.window().setTitle(this.window().title() + " (" + uses + ")");
                    this.window().center();
                    this.window().makeKeyAndOrderFront(null);
                }

                public void closeDonationSheet(final NSButton sender) {
                    this.window().close();
                    Preferences.instance().setProperty("donate.reminder", neverShowDonationCheckbox.state() == NSCell.OffState);
                    if(sender.tag() == CDSheetCallback.DEFAULT_OPTION) {
                        try {
                            NSWorkspace.sharedWorkspace().openURL(
                                    new java.net.URL(Preferences.instance().getProperty("website.donate")));
                        }
                        catch(java.net.MalformedURLException e) {
                            log.error(e.getMessage());
                        }
                    }
                    NSApplication.sharedApplication().terminate(null);
                }
            };
            synchronized(NSApplication.sharedApplication()) {
                if(!NSApplication.loadNibNamed("Donate", c)) {
                    log.fatal("Couldn't load Donate.nib");
                }
            }
            donationBoxDisplayed = true;
            return NSApplication.TerminateCancel;
        }
        NSArray windows = app.windows();
        int count = windows.count();
        // Determine if there are any open connections
        while(0 != count--) {
            NSWindow window = (NSWindow) windows.objectAtIndex(count);
            CDBrowserController controller = CDBrowserController.controllerForWindow(window);
            if(null != controller) {
                if(controller.isMounted()) {
                    if(Preferences.instance().getBoolean("browser.serialize")) {
                        Host bookmark = (Host) controller.getSession().getHost().clone();
                        bookmark.setDefaultPath(controller.workdir().getAbsolute());
                        this.exportBookmark(bookmark,
                                new File(SESSIONS_FOLDER, bookmark.getNickname() + ".duck"));
                    }
                }
                if(controller.isConnected()) {
                    if(Preferences.instance().getBoolean("browser.confirmDisconnect")) {
                        int choice = NSAlertPanel.runAlert(NSBundle.localizedString("Quit", ""),
                                NSBundle.localizedString("You are connected to at least one remote site. Do you want to review open browsers?", ""),
                                NSBundle.localizedString("Quit Anyway", ""), //default
                                NSBundle.localizedString("Cancel", ""), //other
                                NSBundle.localizedString("Review...", "")); //alternate
                        if(choice == CDSheetCallback.ALTERNATE_OPTION) {
                            // Review if at least one window reqested to terminate later, we shall wait
                            return CDBrowserController.applicationShouldTerminate(app);
                        }
                        if(choice == CDSheetCallback.CANCEL_OPTION) {
                            // Cancel
                            return NSApplication.TerminateCancel;
                        }
                        if(choice == CDSheetCallback.DEFAULT_OPTION) {
                            // Quit
                            return CDQueueController.applicationShouldTerminate(app);
                        }
                    }
                    else {
                        if(controller.isBusy()) {
                            controller.interrupt();
                        }
                        controller.unmount(true);
                    }
                }
            }
        }
        return CDQueueController.applicationShouldTerminate(app);
    }

    public void applicationWillTerminate(NSNotification notification) {
        log.debug("applicationWillTerminate");
        NSNotificationCenter.defaultCenter().removeObserver(this);
        //Terminating rendezvous discovery
        Rendezvous.instance().quit();
        //Writing usage info
        Preferences.instance().setProperty("uses", Preferences.instance().getInteger("uses") + 1);
        Preferences.instance().save();
    }

    public CDBrowserController newDocument() {
        return this.newDocument(false);
    }

    public CDBrowserController newDocument(boolean force) {
        log.debug("newDocument");
        NSArray browsers = this.orderedBrowsers();
        if(!force) {
            java.util.Enumeration enumerator = browsers.objectEnumerator();
            while(enumerator.hasMoreElements()) {
                CDBrowserController controller = (CDBrowserController) enumerator.nextElement();
                if(!controller.hasSession()) {
                    controller.window().makeKeyAndOrderFront(null);
                    return controller;
                }
            }
        }
        CDBrowserController controller = new CDBrowserController();
        if(browsers.count() > 0) {
            controller.cascade();
        }
        controller.window().makeKeyAndOrderFront(null);
        return controller;
    }

    // ----------------------------------------------------------
    // Applescriptability
    // ----------------------------------------------------------

    public boolean applicationDelegateHandlesKey(NSApplication application, String key) {
        return key.equals("orderedBrowsers");
    }

    public NSArray orderedTransfers() {
        NSApplication app = NSApplication.sharedApplication();
        NSArray orderedWindows = (NSArray) NSKeyValue.valueForKey(app, "orderedWindows");
        int c = orderedWindows.count();
        NSMutableArray orderedDocs = new NSMutableArray();
        for(int i = 0; i < c; i++) {
            if(((NSWindow) orderedWindows.objectAtIndex(i)).isVisible()) {
                Object delegate = ((NSWindow) orderedWindows.objectAtIndex(i)).delegate();
                if((delegate != null) && (delegate instanceof CDQueueController)) {
                    orderedDocs.addObject(delegate);
                    return orderedDocs;
                }
            }
        }
        log.debug("orderedTransfers:" + orderedDocs);
        return orderedDocs;
    }

    public NSArray orderedBrowsers() {
        NSApplication app = NSApplication.sharedApplication();
        NSArray orderedWindows = (NSArray) NSKeyValue.valueForKey(app, "orderedWindows");
        int c = orderedWindows.count();
        NSMutableArray orderedDocs = new NSMutableArray();
        for(int i = 0; i < c; i++) {
            if(((NSWindow) orderedWindows.objectAtIndex(i)).isVisible()) {
                Object delegate = ((NSWindow) orderedWindows.objectAtIndex(i)).delegate();
                if((delegate != null) && (delegate instanceof CDBrowserController)) {
                    orderedDocs.addObject(delegate);
                }
            }
        }
        return orderedDocs;
    }

    public boolean applicationShouldTerminateAfterLastWindowClosed(NSApplication app) {
        return false;
    }


    protected String[] availableCharsets() {
        List charsets = new ArrayList();
        for(Iterator iter = java.nio.charset.Charset.availableCharsets().values().iterator(); iter.hasNext();) {
            String name = ((java.nio.charset.Charset) iter.next()).displayName();
            if(!(name.startsWith("IBM") || name.startsWith("x-"))) {
                charsets.add(name);
            }
        }
        return (String[]) charsets.toArray(new String[0]);
    }

    /**
     * @param file
     * @return The imported bookmark deserialized as a #Host
     */
    public Host importBookmark(final File file) {
        NSData plistData = new NSData(file);
        String[] errorString = new String[]{null};
        Object propertyListFromXMLData =
                NSPropertyListSerialization.propertyListFromData(plistData,
                        NSPropertyListSerialization.PropertyListImmutable,
                        new int[]{NSPropertyListSerialization.PropertyListXMLFormat},
                        errorString);
        if(errorString[0] != null) {
            log.error("Problem reading bookmark file: " + errorString[0]);
            return null;
        }
        if(propertyListFromXMLData instanceof NSDictionary) {
            return new Host((NSDictionary) propertyListFromXMLData);
        }
        log.error("Invalid file format:" + file);
        return null;
    }

    /**
     * @param bookmark
     * @param file
     */
    public void exportBookmark(final Host bookmark, File file) {
        try {
            file = new File(file.getParentFile(), file.getName().replace('/', ':'));
            log.info("Exporting bookmark " + bookmark + " to " + file);
            NSMutableData collection = new NSMutableData();
            String[] errorString = new String[]{null};
            collection.appendData(NSPropertyListSerialization.dataFromPropertyList(bookmark.getAsDictionary(),
                    NSPropertyListSerialization.PropertyListXMLFormat,
                    errorString));
            if(errorString[0] != null) {
                log.error("Problem writing bookmark file: " + errorString[0]);
            }
            if(collection.writeToURL(file.toURL(), true)) {
                log.info("Bookmarks sucessfully saved in :" + file.toString());
                NSWorkspace.sharedWorkspace().noteFileSystemChangedAtPath(file.getAbsolutePath());
            }
            else {
                log.error("Error saving Bookmarks in :" + file.toString());
            }
        }
        catch(java.net.MalformedURLException e) {
            log.error(e.getMessage());
        }
    }
}
