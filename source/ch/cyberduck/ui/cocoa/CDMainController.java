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

import ch.cyberduck.core.Host;
import ch.cyberduck.core.Preferences;

import com.apple.cocoa.application.*;
import com.apple.cocoa.foundation.*;

import java.io.File;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

public class CDMainController {
	private static Logger log = Logger.getLogger(CDMainController.class);

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
		try {
			//	    NSBundle bundle = NSBundle.bundleForClass(this.getClass());
			String currentVersionNumber = (String) NSBundle.mainBundle().objectForInfoDictionaryKey("CFBundleVersion");
			log.info("Current version:" + currentVersionNumber);

			NSData data = new NSData(new java.net.URL(Preferences.instance().getProperty("website.update.xml")));
			if (null == data) {
				NSAlertPanel.runCriticalAlert(
				    NSBundle.localizedString("Error", "Alert sheet title"), //title
				    NSBundle.localizedString("There was a problem checking for an update. Please try again later.", "Alert sheet text"),
				    NSBundle.localizedString("OK", "Alert sheet default button"), // defaultbutton
				    null, //alternative button
				    null//other button
				);
				return;
			}
			log.debug(data.length() + " bytes.");
			//			NSDictionary entries = (NSDictionary)NSPropertyListSerialization.propertyListFromXMLData(data);
			String[] errorString = new String[]{null};
			Object propertyListFromXMLData =
			    NSPropertyListSerialization.propertyListFromData(data,
			        NSPropertyListSerialization.PropertyListImmutable,
			        new int[]{NSPropertyListSerialization.PropertyListXMLFormat},
			        errorString);
			if (errorString[0] != null || null == propertyListFromXMLData) {
				log.error("Version info could not be retrieved: " + errorString[0]);
				NSAlertPanel.runCriticalAlert(
				    NSBundle.localizedString("Error", "Alert sheet title"), //title
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
					NSAlertPanel.runInformationalAlert(
					    NSBundle.localizedString("No update", "Alert sheet title"), //title
					    NSBundle.localizedString("No newer version available.", "Alert sheet text") + " Cyberduck " + currentVersionNumber + " " + NSBundle.localizedString("is up to date.", "Alert sheet text"),
					    "OK", // defaultbutton
					    null, //alternative button
					    null//other button
					);
				}
				else {
					if (false == NSApplication.loadNibNamed("Update", this)) {
						log.fatal("Couldn't load Update.nib");
						return;
					}
					this.updateLabel.setStringValue("Cyberduck " + currentVersionNumber + " " + NSBundle.localizedString("is out of date. The current version is", "Alert sheet text") + " " + latestVersionNumber + ".");
					this.updateText.replaceCharactersInRange(new NSRange(updateText.textStorage().length(), 0), comment);
					this.updateSheet.setTitle(filename);
					this.updateSheet.makeKeyAndOrderFront(null);
				}
			}
		}
		catch (Exception e) {
			log.error(e.getMessage());
		}
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
		NSApplication.sharedApplication().replyToApplicationShouldTerminate(true);
	}

	public void preferencesMenuClicked(Object sender) {
		CDPreferencesController controller = CDPreferencesController.instance();
		controller.window().makeKeyAndOrderFront(null);
	}

	public CDDownloadController newDownloadMenuClicked(Object sender) {
		CDDownloadController controller = new CDDownloadController();
		controller.window().makeKeyAndOrderFront(null);
		return controller;
	}

	public CDBrowserController newBrowserMenuClicked(Object sender) {
		CDBrowserController controller = new CDBrowserController();
		controller.window().makeKeyAndOrderFront(null);
		return controller;
	}

	public void showTransferQueueClicked(Object sender) {
		CDQueueController.instance().window().makeKeyAndOrderFront(null);
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
				CDBrowserController controller = newBrowserMenuClicked(null);
				controller.mount(host);
				return true;
			}
		}
		return false;
	}

	public boolean applicationShouldHandleReopen(NSApplication app, boolean visibleWindowsFound) {
		if (visibleWindowsFound)
			return true;
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
			this.newBrowserMenuClicked(null);
		}
		if (Preferences.instance().getProperty("queue.openByDefault").equals("true")) {
			this.showTransferQueueClicked(null);
		}
	}

	public int applicationShouldTerminate(NSApplication app) {
		log.debug("applicationShouldTerminate");
//		NSArray windows = NSApplication.sharedApplication().windows();
//		if(windows.count() > 0) {
//			log.debug("Open windows:"+windows);
//			java.util.Enumeration i = windows.objectEnumerator();
//			while(i.hasMoreElements()) {
//				NSWindow window = (NSWindow)i.nextElement();
//				if(window.isVisible())
//					window.performClose(null);
//			}
//			return NSApplication.TerminateLater;
//		}
		Preferences.instance().setProperty("uses", Integer.parseInt(Preferences.instance().getProperty("uses")) + 1);
//		Preferences.instance().save();
		CDBookmarksImpl.instance().save();
//		CDHistoryImpl.instance().save();
//		CDQueuesImpl.instance().save();

		if (Integer.parseInt(Preferences.instance().getProperty("uses")) > 5 && Preferences.instance().getProperty("donate").equals("true")) {
			if (false == NSApplication.loadNibNamed("Donate", this)) {
				log.fatal("Couldn't load Donate.nib");
				return NSApplication.TerminateNow;
			}
			this.donationSheet.center();
			this.donationSheet.makeKeyAndOrderFront(null);
			return NSApplication.TerminateLater;
		}
		return NSApplication.TerminateNow;
	}

	public boolean applicationShouldTerminateAfterLastWindowClosed(NSApplication app) {
		return false;
	}

	//    public abstract NSMenu applicationDockMenu(NSApplication sender)
}
