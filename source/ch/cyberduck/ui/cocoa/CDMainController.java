package ch.cyberduck.ui.cocoa;

/*
 *  Copyright (c) 2003 David Kocher. All rights reserved.
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

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import ch.cyberduck.core.Host;
import ch.cyberduck.core.Preferences;

public class CDMainController extends NSObject {
    private static Logger log = Logger.getLogger(CDMainController.class);

    private static final File VERSION_FILE = new File(NSPathUtilities.stringByExpandingTildeInPath("~/Library/Application Support/Cyberduck/Version.plist"));

    static {
        org.apache.log4j.BasicConfigurator.configure();
        Logger log = Logger.getRootLogger();
        log.setLevel(Level.toLevel(Preferences.instance().getProperty("logging")));
        //	log.setLevel(Level.OFF);
        //	log.setLevel(Level.DEBUG);
        //	log.setLevel(Level.INFO);
        //	log.setLevel(Level.WARN);
        //	log.setLevel(Level.ERROR);
        //	log.setLevel(Level.FATAL);
    }

    // ----------------------------------------------------------
    // Outlets
    // ----------------------------------------------------------

    private NSWindow donationSheet; // IBOutlet

    public void setDonationSheet(NSWindow donationSheet) {
        this.donationSheet = donationSheet;
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
	
	public void setBookmarkMenu(NSMenu bookmarkMenu) {
		this.bookmarkMenu = bookmarkMenu;
//		this.bookmarkMenu.setDelegate(new BookmarkMenuDelegate());
//		for(int i = 0; i < CDBookmarksImpl.instance().size(); i++) {
//			Host h = CDBookmarksImpl.instance().getItem(i);
//			NSMenuItem item = new NSMenuItem(h.getNickname(),
//											 new NSSelector("bookmarkMenuClicked", new Class[]{Object.class}),
//											 "");
//			this.bookmarkMenu.addItem(item);
//		}
	}
	
	private class BookmarkMenuDelegate {
		public int numberOfItemsInMenu(NSMenu menu) {
			return CDBookmarksImpl.instance().size();
		}

		/**
*		Called when a menu is about to be displayed at the start of a tracking session so the delegate
		* can modify the menu. You can change the menu by adding, removing or modifying menu items. 
		 * Be sure to set the proper enable state for any new menu items. If populating the menu will
		 * take a long time, implement numberOfItemsInMenu and menuUpdateItemAtIndex instead.
*/
		public void menuNeedsUpdate(NSMenu menu) {
			log.debug("menuNeedsUpdate:"+menu);
		}

		/**
		* Called to let you update a menu item before it is displayed. If your numberOfItemsInMenu delegate method returns a positive value, then your menuUpdateItemAtIndex method is called for each item in the menu. You can then update the menu title, image, and so forth for the menu item. Return true to continue the process. If you return false, your menuUpdateItemAtIndex is not called again. In that case, it is your responsibility to trim any extra items from the menu.
		 */
		public boolean menuUpdateItemAtIndex(NSMenu menu, NSMenuItem item, int index, boolean shouldCancel) {
			log.debug("menuUpdateItemAtIndex"+item);
			return true;
		}
	
	}
	
	public void bookmarkMenuClicked(Object sender) {
		log.debug("bookmarkMenuClicked:"+sender);
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
		new Thread() {
			public void run() {
				try {
					String currentVersionNumber = (String) NSBundle.mainBundle().objectForInfoDictionaryKey("CFBundleVersion");
					log.info("Current version:" + currentVersionNumber);
					
					NSData data = new NSData(new java.net.URL(Preferences.instance().getProperty("website.update.xml")));
					if (null == data) {
						NSAlertPanel.runCriticalAlert(NSBundle.localizedString("Error", "Alert sheet title"), //title
													  NSBundle.localizedString("There was a problem checking for an update. Please try again later.", "Alert sheet text"),
													  NSBundle.localizedString("OK", "Alert sheet default button"), // defaultbutton
													  null, //alternative button
													  null//other button
													  );
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
						NSAlertPanel.runCriticalAlert(NSBundle.localizedString("Error", "Alert sheet title"), //title
													  NSBundle.localizedString("Update check failed. Version info could not be retrieved", "Alert sheet text") + ": " + errorString[0],
													  "OK", // defaultbutton
													  null, //alternative button
													  null//other button
													  );
					}
					else {
						log.info(propertyListFromXMLData.toString());
						NSDictionary entries = (NSDictionary) propertyListFromXMLData;
						String latestVersionNumber = (String) entries.objectForKey("version");
						log.info("Latest version:" + latestVersionNumber);
						String filename = (String) entries.objectForKey("file");
						String comment = (String) entries.objectForKey("comment");
						
						if (currentVersionNumber.equals(latestVersionNumber)) {
							NSAlertPanel.runInformationalAlert(NSBundle.localizedString("No update", "Alert sheet title"), //title
															   NSBundle.localizedString("No newer version available.", "Alert sheet text") + " Cyberduck " + currentVersionNumber + " " + NSBundle.localizedString("is up to date.", "Alert sheet text"),
															   "OK", // defaultbutton
															   null, //alternative button
															   null//other button
															   );
						}
						else {
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
			}
		}.start();
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
            String currentVersionNumber = (String) NSBundle.bundleForClass(this.getClass()).objectForInfoDictionaryKey("CFBundleVersion");
            NSWorkspace.sharedWorkspace().openURL(new java.net.URL(Preferences.instance().getProperty("mail") + "?subject=Cyberduck-" + currentVersionNumber));
        }
        catch (java.net.MalformedURLException e) {
            log.error(e.getMessage());
        }
    }

    public void neverShowDonationSheetAgain(NSButton sender) {
        switch (sender.state()) {
            case NSCell.OnState:
                Preferences.instance().setProperty("donate", "false");
                break;
            case NSCell.OffState:
                Preferences.instance().setProperty("donate", "true");
                break;
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
        CDBrowserController controller = new CDBrowserController();
		controller.window().makeKeyAndOrderFront(null);
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
            Host host = CDBookmarksImpl.instance().importBookmark(f);
            if (host != null) {
				CDBrowserController controller = new CDBrowserController();
				controller.window().makeKeyAndOrderFront(null);
                controller.mount(host);
                return true;
            }
        }
        return false;
    }

    public boolean applicationShouldHandleReopen(NSApplication app, boolean visibleWindowsFound) {
        log.info("applicationShouldHandleReopen:" + visibleWindowsFound);
        NSArray windows = NSApplication.sharedApplication().windows();
        if (windows.count() > 0) {
            log.debug("Open windows:" + windows);
        }
        if (visibleWindowsFound) {
            return true;
        }
        if (Preferences.instance().getProperty("browser.openByDefault").equals("true")) {
            this.newBrowserMenuClicked(null);
            return false;
        }
        return true;
    }

    public void applicationDidFinishLaunching(NSNotification notification) {
        // To get service requests to go to the controller...
        //        NSApplication.sharedApplication().setServicesProvider(this);
        log.info("Available localizations:" + NSBundle.mainBundle().localizations());
        if (Preferences.instance().getProperty("browser.openByDefault").equals("true")) {
			if (!Preferences.instance().getProperty("connection.host.default").equals(NSBundle.localizedString("Empty Browser", ""))) {
				
			}
            this.newBrowserMenuClicked(null);
        }
        if (Preferences.instance().getProperty("queue.openByDefault").equals("true")) {
            this.showTransferQueueClicked(null);
        }
		int uses = Integer.parseInt(Preferences.instance().getProperty("uses"));
        if (uses > 5 && Preferences.instance().getProperty("donate").equals("true")) {
            if (false == NSApplication.loadNibNamed("Donate", this)) {
                log.fatal("Couldn't load Donate.nib");
            }
			else {
				this.donationSheet.setTitle(this.donationSheet.title()+" ("+uses+")");
				this.donationSheet.center();
				this.donationSheet.makeKeyAndOrderFront(null);
			}
        }
    }

    public boolean applicationShouldTerminate(NSApplication app) {
        log.debug("applicationShouldTerminate");
		//Writing version info
		this.saveVersionInfo();
		//Writing usage info
        Preferences.instance().setProperty("uses", Integer.parseInt(Preferences.instance().getProperty("uses")) + 1);
//		return this.checkForMountedBrowsers(app);
		return true;
    }
	
	private boolean checkForMountedBrowsers(NSApplication app) {
		NSArray windows = app.windows();
        int count = windows.count();
        boolean needsConfirm = false;
		
        // Determine if there are any open connections
        while (!needsConfirm && (0 != count--)) {
            NSWindow window = (NSWindow) windows.objectAtIndex(count);
            CDBrowserController controller = CDBrowserController.controllerForWindow(window);
            if (null != controller) {
                if (controller.isConnected())
                    needsConfirm = true;
            }
        }
		
        if(needsConfirm) {
            int choice = NSAlertPanel.runAlert(
											   NSBundle.localizedString("Quit", ""),
											   NSBundle.localizedString("You are connected to at least one remote site. Do you want to review open browsers?", ""),
											   NSBundle.localizedString("Review...", ""), //default
											   NSBundle.localizedString("Quit Anyway", 
																		""), //alternate
											   NSBundle.localizedString("Cancel", 
																		"Cancel")); //other
			
            if (choice == NSAlertPanel.OtherReturn) {
                // Cancel
                return false;
            } 
			else if (choice != NSAlertPanel.AlternateReturn) {
                // Review unsaved; Quit Anyway falls through
                count = windows.count();
                while (0 != count--) {
                    NSWindow window = (NSWindow) windows.objectAtIndex(count);
                    CDBrowserController controller = CDBrowserController.controllerForWindow(window);
                    if (null != controller) {
                        window.makeKeyAndOrderFront(null);
                        if (false == controller.windowShouldClose(window))
                            return false;
                    }
                }
            }
        }
		return true;
	}
	
	private void saveVersionInfo() {
        try {
            NSMutableArray list = new NSMutableArray();
            NSMutableDictionary dict = new NSMutableDictionary();
            dict.setObjectForKey((String)NSBundle.mainBundle().objectForInfoDictionaryKey("CFBundleVersion"), "Version");
            list.addObject(dict);
            NSMutableData collection = new NSMutableData();
            String[] errorString = new String[]{null};
            collection.appendData(NSPropertyListSerialization.dataFromPropertyList(
                                                                                   list,
                                                                                   NSPropertyListSerialization.PropertyListXMLFormat,
                                                                                   errorString)
                                  );
            if (errorString[0] != null)
                log.error("Problem writing version file: " + errorString[0]);
			
            if (collection.writeToURL(VERSION_FILE.toURL(), true))
                log.info("Version file sucessfully saved to :" + VERSION_FILE.toString());
            else
                log.error("Error saving version file to :" + VERSION_FILE.toString());
        }
        catch (java.net.MalformedURLException e) {
            log.error(e.getMessage());
        }
	}

    public boolean applicationShouldTerminateAfterLastWindowClosed(NSApplication app) {
        return false;
    }

    //    public abstract NSMenu applicationDockMenu(NSApplication sender)
}
