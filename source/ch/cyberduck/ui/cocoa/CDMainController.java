package ch.cyberduck.ui.cocoa;

/*
 *  Copyright (c) 2004 David Kocher. All rights reserved.
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

import com.apple.cocoa.application.*;
import com.apple.cocoa.foundation.*;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Observable;
import java.util.Observer;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.BasicConfigurator;

import ch.cyberduck.core.*;
import ch.cyberduck.ui.cocoa.growl.Growl;

public class CDMainController extends NSObject {
    private static Logger log = Logger.getLogger(CDMainController.class);

    private static final File VERSION_FILE = new File(NSPathUtilities.stringByExpandingTildeInPath("~/Library/Application Support/Cyberduck/Version.plist"));

    public void awakeFromNib() {
        NSNotificationCenter.defaultCenter().addObserver(this,
                new NSSelector("applicationShouldSleep", new Class[]{Object.class}),
                NSWorkspace.WorkspaceWillSleepNotification,
                null);

        NSNotificationCenter.defaultCenter().addObserver(this,
                new NSSelector("applicationShouldWake", new Class[]{Object.class}),
                NSWorkspace.WorkspaceDidWakeNotification,
                null);
		
        this.threadWorkerTimer = new NSTimer(0.2, this, new NSSelector("handleThreadWorkerTimerEvent", new Class[]{NSTimer.class}), null, true);
        NSRunLoop.currentRunLoop().addTimerForMode(this.threadWorkerTimer, NSRunLoop.DefaultRunLoopMode);
    }

    // If we want the equivalent to SwingUtilities.invokeLater() for Cocoa, we have to fend for ourselves, it seems.
    private NSTimer threadWorkerTimer;

    /**
     * Called very frequently, every 0.1 seconds
     */
    private void handleThreadWorkerTimerEvent(NSTimer t) {
        //log.debug("handleThreadWorkerTimerEvent");
        Runnable item;
        while ((item = ThreadUtilities.instance().next()) != null) {
            item.run();
        }
    }

    static {
        BasicConfigurator.configure();
        Logger log = Logger.getRootLogger();
        log.setLevel(Level.toLevel(Preferences.instance().getProperty("logging")));
    }

    // ----------------------------------------------------------
    // Outlets
    // ----------------------------------------------------------

    private NSWindow donationSheet; // IBOutlet

    public void setDonationSheet(NSWindow donationSheet) {
		log.debug("setDonationSheet");
        this.donationSheet = donationSheet;
    }

    private NSButton neverShowDonationCheckbox;

    public void setNeverShowDonationCheckbox(NSButton neverShowDonationCheckbox) {
        this.neverShowDonationCheckbox = neverShowDonationCheckbox;
        this.neverShowDonationCheckbox.setTarget(this);
        this.neverShowDonationCheckbox.setState(Preferences.instance().getProperty("donate").equals("false") ? NSCell.OnState : NSCell.OffState);
    }

    private NSButton autoUpdateCheckbox;

    public void setAutoUpdateCheckbox(NSButton autoUpdateCheckbox) {
        this.autoUpdateCheckbox = autoUpdateCheckbox;
        this.autoUpdateCheckbox.setTarget(this);
        this.autoUpdateCheckbox.setAction(new NSSelector("autoUpdateCheckboxClicked", new Class[]{NSButton.class}));
        this.autoUpdateCheckbox.setState(Preferences.instance().getProperty("update.check").equals("true") ? NSCell.OnState : NSCell.OffState);
    }

    public void autoUpdateCheckboxClicked(NSButton sender) {
        switch (sender.state()) {
            case NSCell.OnState:
                Preferences.instance().setProperty("update.check", true);
                break;
            case NSCell.OffState:
                Preferences.instance().setProperty("update.check", false);
                break;
        }
    }

    public NSWindow updateSheet; // IBOutlet

    public void setUpdateSheet(NSWindow updateSheet) {
        this.updateSheet = updateSheet;
    }

    private NSTextField updateLabel; // IBOutlet

    public void setUpdateLabel(NSTextField updateLabel) {
        this.updateLabel = updateLabel;
    }

    private NSTextView updateText; // IBOutlet

    public void setUpdateText(NSTextView updateText) {
        this.updateText = updateText;
    }

    private NSMenu bookmarkMenu;
    private NSMenu rendezvousMenu;
    private NSObject bookmarkMenuDelegate;
    private NSObject rendezvousMenuDelegate;
    private Rendezvous rendezvous;

    public void setBookmarkMenu(NSMenu bookmarkMenu) {
		log.debug("setBookmarkMenu");
        this.bookmarkMenu = bookmarkMenu;
        this.rendezvousMenu = new NSMenu();
        this.rendezvousMenu.setAutoenablesItems(false);
        NSSelector setDelegateSelector =
                new NSSelector("setDelegate", new Class[]{Object.class});
        if (setDelegateSelector.implementedByClass(NSMenu.class)) {
            this.bookmarkMenu.setDelegate(this.bookmarkMenuDelegate = new BookmarkMenuDelegate());
            this.rendezvousMenu.setDelegate(this.rendezvousMenuDelegate =
                    new RendezvousMenuDelegate(this.rendezvous = new Rendezvous()));
        }
        this.bookmarkMenu.setSubmenuForItem(rendezvousMenu, this.bookmarkMenu.itemWithTitle("Rendezvous"));
    }

    private class BookmarkMenuDelegate extends NSObject {
        private Map items = new HashMap();

        public BookmarkMenuDelegate() {
            super();
        }

        public int numberOfItemsInMenu(NSMenu menu) {
            return CDBookmarkTableDataSource.instance().size() + 6; //index 0-3 are static menu items, 4 is sepeartor, 5 is Rendezvous with submenu, 6 is sepearator
        }

        /**
         * Called to let you update a menu item before it is displayed. If your
         * numberOfItemsInMenu delegate method returns a positive value,
         * then your menuUpdateItemAtIndex method is called for each item in the menu.
         * You can then update the menu title, image, and so forth for the menu item.
         * Return true to continue the process. If you return false, your menuUpdateItemAtIndex
         * is not called again. In that case, it is your responsibility to trim any extra items from the menu.
         */
        public boolean menuUpdateItemAtIndex(NSMenu menu, NSMenuItem item, int index, boolean shouldCancel) {
//			log.debug("menuUpdateItemAtIndex"+index);
            if (index == 4) {
                item.setEnabled(true);
                item.setImage(NSImage.imageNamed("rendezvous16.tiff"));
            }
            if (index > 5) {
                Host h = CDBookmarkTableDataSource.instance().getItem(index - 6);
                item.setTitle(h.getNickname());
                item.setTarget(this);
//				item.setImage(documentIcon);
                item.setAction(new NSSelector("bookmarkMenuClicked", new Class[]{Object.class}));
                items.put(item, h);
            }
            return true;
        }

        public void bookmarkMenuClicked(Object sender) {
            log.debug("bookmarkMenuClicked:" + sender);
            CDBrowserController controller = CDMainController.this.newDocument();
            controller.mount((Host)items.get(sender));
        }
    }

    private class RendezvousMenuDelegate extends NSObject implements Observer {
        private Map items = new HashMap();

        public RendezvousMenuDelegate(Rendezvous rendezvous) {
			log.debug("RendezvousMenuDelegate");
            rendezvous.addObserver(this);
        }

        public void update(final Observable o, final Object arg) {
            log.debug("update:" + o + "," + arg);
            ThreadUtilities.instance().invokeLater(new Runnable() {
                public void run() {
                    if (o instanceof Rendezvous) {
                        if (arg instanceof Message) {
                            Message msg = (Message)arg;
                            Host host = rendezvous.getService((String)msg.getContent());
                            if (msg.getTitle().equals(Message.RENDEZVOUS_ADD)) {
								Growl.instance().notifyWithImage("Rendezvous", (String)msg.getContent(), "rendezvous");
                                items.put((String)msg.getContent(),
                                        host);
                            }
                            if (msg.getTitle().equals(Message.RENDEZVOUS_REMOVE)) {
                                items.remove((String)msg.getContent());
                            }
                        }
                    }
                }
            });
        }

        public int numberOfItemsInMenu(NSMenu menu) {
            //log.debug("numberOfItemsInMenu"+menu);
            if (items.size() > 0) {
                return items.size();
            }
            return 1;
        }

        /**
         * Called to let you update a menu item before it is displayed. If your
         * numberOfItemsInMenu delegate method returns a positive value,
         * then your menuUpdateItemAtIndex method is called for each item in the menu.
         * You can then update the menu title, image, and so forth for the menu item.
         * Return true to continue the process. If you return false, your menuUpdateItemAtIndex
         * is not called again. In that case, it is your responsibility to trim any extra items from the menu.
         */
        public boolean menuUpdateItemAtIndex(NSMenu menu, NSMenuItem sender, int index, boolean shouldCancel) {
            //log.debug("menuUpdateItemAtIndex:"+index);
            if (items.size() == 0) {
                sender.setTitle(NSBundle.localizedString("No Rendezvous services available", ""));
                sender.setEnabled(false);
                return true;
            }
            else {
                Host h = (Host)items.values().toArray()[index];
                sender.setTitle(h.getNickname());
                sender.setTarget(this);
                sender.setEnabled(true);
                sender.setAction(new NSSelector("rendezvousMenuClicked", new Class[]{NSMenuItem.class}));
                return true;
            }
        }

        public void rendezvousMenuClicked(NSMenuItem sender) {
			//log.debug("rendezvousMenuClicked:" + sender);
            CDBrowserController controller = CDMainController.this.newDocument();
            controller.mount((Host)items.get(sender.title()));
        }
    }

    public void helpMenuClicked(Object sender) {
        NSWorkspace.sharedWorkspace().openFile(new File(NSBundle.mainBundle().pathForResource("Help", "rtfd")).toString());
    }

    public void faqMenuClicked(Object sender) {
        NSWorkspace.sharedWorkspace().openFile(new File(NSBundle.mainBundle().pathForResource("Cyberduck FAQ", "rtfd")).toString());
    }

    public void licenseMenuClicked(Object sender) {
        NSWorkspace.sharedWorkspace().openFile(new File(NSBundle.mainBundle().pathForResource("License", "txt")).toString());
    }

    public void updateMenuClicked(Object sender) {
        this.checkForUpdate(true);
    }

    public void checkForUpdate(final boolean verbose) {
        ThreadUtilities.instance().invokeLater(new Runnable() {
            public void run() {
                // An autorelease pool is used to manage Foundation’s autorelease mechanism for
                // Objective-C objects. NSAutoreleasePool provides Java applications access to
                // autorelease pools. Typically it is not necessary for Java applications to
                // use NSAutoreleasePools since Java manages garbage collection. However, some
                // situations require an autorelease pool; for instance, if you start off a thread
                // that calls Cocoa, there won’t be a top-level pool.
                int mypool = NSAutoreleasePool.push();
                try {
                    String currentVersionNumber = (String)NSBundle.mainBundle().objectForInfoDictionaryKey("CFBundleVersion");
                    log.info("Current version:" + currentVersionNumber);

                    NSData data = new NSData(new java.net.URL(Preferences.instance().getProperty("website.update.xml")));
                    if (null == data) {
                        if (verbose) {
                            NSAlertPanel.runCriticalAlert(NSBundle.localizedString("Error", "Alert sheet title"), //title
                                    NSBundle.localizedString("There was a problem checking for an update. Please try again later.", "Alert sheet text"),
                                    NSBundle.localizedString("OK", "Alert sheet default button"), // defaultbutton
                                    null, //alternative button
                                    null//other button
                            );
                        }
                        return;
                    }
                    String[] errorString = new String[]{null};
                    Object propertyListFromXMLData =
                            NSPropertyListSerialization.propertyListFromData(data,
                                    NSPropertyListSerialization.PropertyListImmutable,
                                    new int[]{NSPropertyListSerialization.PropertyListXMLFormat},
                                    errorString);
                    if (errorString[0] != null || null == propertyListFromXMLData) {
                        log.error("Version info could not be retrieved: " + errorString[0]);
                        if (verbose) {
                            NSAlertPanel.runCriticalAlert(NSBundle.localizedString("Error", "Alert sheet title"), //title
                                    NSBundle.localizedString("There was a problem checking for an update. Please try again later.", "Alert sheet text") + " (" + errorString[0] + ")",
                                    NSBundle.localizedString("OK", "Alert sheet default button"), // defaultbutton
                                    null, //alternative button
                                    null//other button
                            );
                        }
                    }
                    else {
                        log.info(propertyListFromXMLData.toString());
                        NSDictionary entries = (NSDictionary)propertyListFromXMLData;
                        String latestVersionNumber = (String)entries.objectForKey("version");
                        log.info("Latest version:" + latestVersionNumber);
                        String filename = (String)entries.objectForKey("file");
                        String comment = (String)entries.objectForKey("comment");

                        if (currentVersionNumber.equals(latestVersionNumber)) {
                            if (verbose) {
                                NSAlertPanel.runInformationalAlert(NSBundle.localizedString("No update", "Alert sheet title"), //title
                                        NSBundle.localizedString("No newer version available.", "Alert sheet text") + " Cyberduck " + currentVersionNumber + " " + NSBundle.localizedString("is up to date.", "Alert sheet text"),
                                        "OK", // defaultbutton
                                        null, //alternative button
                                        null//other button
                                );
                            }
                        }
                        else {
                            // Update available, show update dialog
                            if (false == NSApplication.loadNibNamed("Update", CDMainController.this)) {
                                log.fatal("Couldn't load Update.nib");
                                return;
                            }
                            updateLabel.setStringValue("Cyberduck " + currentVersionNumber + " " + NSBundle.localizedString("is out of date. The current version is", "Alert sheet text") + " " + latestVersionNumber + ".");
                            updateText.replaceCharactersInRange(new NSRange(updateText.textStorage().length(), 0), comment);
                            updateSheet.setTitle(filename);
                            updateSheet.makeKeyAndOrderFront(null);
                        }
                    }
                }
                catch (Exception e) {
                    log.error(e.getMessage());
                }
                finally {
                    NSAutoreleasePool.pop(mypool);
                }
            }
        });
    }

    public void websiteMenuClicked(Object sender) {
        try {
            NSWorkspace.sharedWorkspace().openURL(new java.net.URL(Preferences.instance().getProperty("website.home")));
        }
        catch (java.net.MalformedURLException e) {
            log.error(e.getMessage());
        }
    }

    public void donateMenuClicked(Object sender) {
        try {
            NSWorkspace.sharedWorkspace().openURL(new java.net.URL(Preferences.instance().getProperty("website.donate")));
        }
        catch (java.net.MalformedURLException e) {
            log.error(e.getMessage());
        }
    }

    public void feedbackMenuClicked(Object sender) {
        try {
            String currentVersionNumber = (String)NSBundle.bundleForClass(this.getClass()).objectForInfoDictionaryKey("CFBundleVersion");
            NSWorkspace.sharedWorkspace().openURL(new java.net.URL(Preferences.instance().getProperty("mail") + "?subject=Cyberduck-" + currentVersionNumber));
        }
        catch (java.net.MalformedURLException e) {
            log.error(e.getMessage());
        }
    }

    public void closeUpdateSheet(NSButton sender) {
        log.debug("closeUpdateSheet");
        updateSheet.close();
        switch (sender.tag()) {
            case (NSAlertPanel.DefaultReturn):
                try {
                    NSWorkspace.sharedWorkspace().openURL(new java.net.URL(Preferences.instance().getProperty("website.update") + updateSheet.title()));
                }
                catch (java.net.MalformedURLException e) {
                    log.error(e.getMessage());
                }
                break;
            case (NSAlertPanel.AlternateReturn):
                break;
        }
    }

    public void closeDonationSheet(NSButton sender) {
        log.debug("closeDonationSheet");
        donationSheet.close();
        switch (sender.tag()) {
            case NSAlertPanel.DefaultReturn:
                try {
                    NSWorkspace.sharedWorkspace().openURL(new java.net.URL(Preferences.instance().getProperty("website.donate")));
                }
                catch (java.net.MalformedURLException e) {
                    log.error(e.getMessage());
                }
                break;
            case NSAlertPanel.AlternateReturn:
                break;
        }
        switch (neverShowDonationCheckbox.state()) {
            case NSCell.OnState:
                Preferences.instance().setProperty("donate", "false");
                break;
            case NSCell.OffState:
                Preferences.instance().setProperty("donate", "true");
                break;
        }
    }

    public void preferencesMenuClicked(Object sender) {
        CDPreferencesController controller = CDPreferencesController.instance();
        controller.window().makeKeyAndOrderFront(null);
    }

    public void newDownloadMenuClicked(Object sender) {
        CDDownloadController controller = new CDDownloadController();
        controller.window().makeKeyAndOrderFront(null);
    }

	public void newBrowserMenuClicked(Object sender) {
        this.newDocument();
    }
	
    public void showTransferQueueClicked(Object sender) {
        CDQueueController controller = CDQueueController.instance();
        controller.window().makeKeyAndOrderFront(null);
    }

    // ----------------------------------------------------------
    // Application delegate methods
    // ----------------------------------------------------------

    public boolean applicationOpenFile(NSApplication app, String filename) {
        log.debug("applicationOpenFile:" + filename);
        File f = new File(filename);
        if (f.exists()) {
            log.info("Found file: " + f.toString());
            Host host = CDBookmarkTableDataSource.instance().importBookmark(f);
            if (host != null) {
                this.newDocument().mount(host);
                return true;
            }
        }
        return false;
    }

    public boolean applicationOpenTempFile(NSApplication app, String filename) {
        log.debug("applicationOpenTempFile:" + filename);
        return this.applicationOpenFile(app, filename);
    }

	/**
		* @return true if the file was successfully opened, false otherwise.
	 */
    public boolean applicationOpenUntitledFile(NSApplication app) {
        log.debug("applicationOpenUntitledFile");
        if (Preferences.instance().getProperty("browser.openByDefault").equals("true")) {
			return this.newDocument() != null;
		}
		return false;
    }

	public boolean applicationShouldHandleReopen(NSApplication app, boolean visibleWindowsFound) {
        log.info("applicationShouldHandleReopen");
		return true;
    }
	
	public void applicationWillFinishLaunching(NSNotification notification) {
        this.rendezvous.init();
	}
	
    public void applicationDidFinishLaunching(NSNotification notification) {
        log.info("Available localizations:" + NSBundle.mainBundle().localizations());
        if (Preferences.instance().getProperty("queue.openByDefault").equals("true")) {
            this.showTransferQueueClicked(null);
        }
        int uses = Integer.parseInt(Preferences.instance().getProperty("uses"));
        if (Preferences.instance().getProperty("donate").equals("true")) {
            if (false == NSApplication.loadNibNamed("Donate", this)) {
                log.fatal("Couldn't load Donate.nib");
            }
            else {
                this.donationSheet.setTitle(this.donationSheet.title() + " (" + uses + ")");
                this.donationSheet.center();
                this.donationSheet.makeKeyAndOrderFront(null);
            }
        }
        if (Preferences.instance().getProperty("update.check").equals("true")) {
            this.checkForUpdate(false);
        }
    }

    public void applicationShouldSleep(Object o) {
        log.debug("applicationShouldSleep");
        this.rendezvous.quit();
    }

    public void applicationShouldWake(Object o) {
        log.debug("applicationShouldWake");
        this.rendezvous.init();
    }

    public int applicationShouldTerminate(NSApplication app) {
        log.debug("applicationShouldTerminate");
        return this.checkForMountedBrowsers(app);
    }

    public void applicationWillTerminate(NSNotification notification) {
        log.debug("applicationWillTerminate");
        NSNotificationCenter.defaultCenter().removeObserver(this);
        //Terminating rendezvous discovery
        this.rendezvous.quit();
		//Writing version info
        this.saveVersionInfo();
		//Writing usage info
        Preferences.instance().setProperty("uses", Integer.parseInt(Preferences.instance().getProperty("uses")) + 1);
        Preferences.instance().save();
    }

    // ----------------------------------------------------------
    // Applescriptability
    // ----------------------------------------------------------
	
	public boolean applicationDelegateHandlesKey(NSApplication application, String key) {
		log.debug("applicationDelegateHandlesKey:"+key);
		if (key.equals("orderedDocuments"))
			return true;
		return false;
	}
	
	private static NSPoint cascadedWindowPoint;
	
	public CDBrowserController newDocument() {
        CDBrowserController controller = new CDBrowserController();
		NSPoint origin = controller.window().frame().origin();
		if(null == cascadedWindowPoint) {
			cascadedWindowPoint = new NSPoint(origin.x(), origin.y());
		}
		controller.window().setFrameTopLeftPoint(cascadedWindowPoint);
//			controller.window().setFrameOrigin(cascadedWindowPoint);
		// move point for next window
		cascadedWindowPoint = controller.window().cascadeTopLeftFromPoint(cascadedWindowPoint);
		//cascadedWindowPoint = controller.window().cascadeTopLeftFromPoint(new NSPoint(origin.x(), origin.y()));
        controller.window().makeKeyAndOrderFront(null);
		return controller;
	}
	
	public NSArray orderedDocuments() {
        log.debug("orderedDocuments");
        NSApplication app = NSApplication.sharedApplication();
        NSArray orderedWindows = (NSArray)NSKeyValue.valueForKey(app, "orderedWindows");
        int i, c = orderedWindows.count();
        NSMutableArray orderedDocs = new NSMutableArray();
        Object curDelegate;
        for (i = 0; i < c; i++) {
            curDelegate = ((NSWindow)orderedWindows.objectAtIndex(i)).delegate();
            if ((curDelegate != null) && (curDelegate instanceof CDBrowserController)) {
                orderedDocs.addObject(curDelegate);
            }
        }
        return orderedDocs;
    }
	
    public void insertInOrderedDocumentsAtIndex(CDBrowserController doc, int index) {
        log.debug("insertInOrderedDocumentsAtIndex" + doc);
        doc.window().makeKeyAndOrderFront(null);
    }
	
    // ----------------------------------------------------------

    private int checkForMountedBrowsers(NSApplication app) {
        NSArray windows = app.windows();
        int count = windows.count();
        // Determine if there are any open connections
        while (0 != count--) {
            NSWindow window = (NSWindow)windows.objectAtIndex(count);
            CDBrowserController controller = CDBrowserController.controllerForWindow(window);
            if (null != controller) {
                if (controller.isConnected()) {
                    int choice = NSAlertPanel.runAlert(NSBundle.localizedString("Quit", ""),
                            NSBundle.localizedString("You are connected to at least one remote site. Do you want to review open browsers?", ""),
                            NSBundle.localizedString("Review...", ""), //default
                            NSBundle.localizedString("Quit Anyway", ""), //alternate
                            NSBundle.localizedString("Cancel", "")); //other
                    if (choice == NSAlertPanel.AlternateReturn) {
                        // Quit
                        return NSApplication.TerminateNow;
                    }
                    if (choice == NSAlertPanel.OtherReturn) {
                        // Cancel
                        return NSApplication.TerminateCancel;
                    }
                    if (choice == NSAlertPanel.DefaultReturn) {
                        // Review
                        // if at least one window reqested to terminate later, we shall wait
                        CDBrowserController.reviewMountedBrowsers(true);
                        return NSApplication.TerminateLater;
                    }
                }
            }
        }
//		return CDQueueController.instance().checkForRunningTransfers();
        return NSApplication.TerminateNow;
    }

    private String readVersionInfo() {
        if (VERSION_FILE.exists()) {
            NSData plistData = new NSData(VERSION_FILE);
            String[] errorString = new String[]{null};
            Object propertyListFromXMLData =
                    NSPropertyListSerialization.propertyListFromData(plistData,
                            NSPropertyListSerialization.PropertyListImmutable,
                            new int[]{NSPropertyListSerialization.PropertyListXMLFormat},
                            errorString);
            if (errorString[0] != null) {
                log.error("Problem reading version file: " + errorString[0]);
            }
            else {
                log.debug("Successfully read version info: " + propertyListFromXMLData);
            }
            if (propertyListFromXMLData instanceof NSDictionary) {
                NSDictionary dict = (NSDictionary)propertyListFromXMLData;
                return (String)dict.objectForKey("Version");
            }
        }
        return null;
    }

    private void saveVersionInfo() {
        try {
            NSMutableDictionary dict = new NSMutableDictionary();
            dict.setObjectForKey((String)NSBundle.mainBundle().objectForInfoDictionaryKey("CFBundleVersion"), "Version");
            NSMutableData collection = new NSMutableData();
            String[] errorString = new String[]{null};
            collection.appendData(NSPropertyListSerialization.dataFromPropertyList(dict,
                    NSPropertyListSerialization.PropertyListXMLFormat,
                    errorString));
            if (errorString[0] != null) {
                log.error("Problem writing version file: " + errorString[0]);
            }
            if (collection.writeToURL(VERSION_FILE.toURL(), true)) {
                log.info("Version file sucessfully saved to :" + VERSION_FILE.toString());
            }
            else {
                log.error("Error saving version file to :" + VERSION_FILE.toString());
            }
        }
        catch (java.net.MalformedURLException e) {
            log.error(e.getMessage());
        }
    }

    public boolean applicationShouldTerminateAfterLastWindowClosed(NSApplication app) {
        return false;
    }
}
