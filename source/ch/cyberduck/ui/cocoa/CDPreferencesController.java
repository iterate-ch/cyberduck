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

import ch.cyberduck.core.Preferences;
import ch.cyberduck.core.Session;
import com.apple.cocoa.application.*;
import com.apple.cocoa.foundation.*;
import org.apache.log4j.Logger;

/**
* @version $Id$
 */
public class CDPreferencesController {
    private static Logger log = Logger.getLogger(CDPreferencesController.class);

    private static CDPreferencesController instance;

    private static final String CONNECTMODE_ACTIVE = NSBundle.localizedString("Active");
    private static final String CONNECTMODE_PASSIVE = NSBundle.localizedString("Passive");
    
    private static final String TRANSFERMODE_BINARY = NSBundle.localizedString("Binary");
    private static final String TRANSFERMODE_ASCII = NSBundle.localizedString("ASCII");

    private static final String PROTOCOL_FTP = NSBundle.localizedString("FTP");
    private static final String PROTOCOL_SFTP = NSBundle.localizedString("SFTP");

//    private static final String ASK_ME_WHAT_TO_DO = NSBundle.localizedString("Ask me what to do");
    private static final String OVERWRITE_EXISTING_FILE = NSBundle.localizedString("Overwrite existing file");
    private static final String TRY_TO_RESUME_TRANSFER = NSBundle.localizedString("Try to resume transfer");
    private static final String USE_A_SIMILAR_NAME = NSBundle.localizedString("Use similar name");
    
    // ----------------------------------------------------------
    // Outlets
    // ----------------------------------------------------------

    private NSButton downloadPathButton;
    public void setDownloadPathButton(NSButton downloadPathButton) {
	this.downloadPathButton = downloadPathButton;
	this.downloadPathButton.setTarget(this);
	this.downloadPathButton.setAction(new NSSelector("downloadPathButtonClicked", new Class[] {NSButton.class}));
    }

    public void downloadPathButtonClicked(NSButton sender) {
	NSOpenPanel panel = new NSOpenPanel();
	panel.setCanChooseFiles(false);
	panel.setCanChooseDirectories(true);
	panel.setAllowsMultipleSelection(false);
	panel.beginSheetForDirectory(System.getProperty("user.home"), null, null, this.window(), this, new NSSelector("openPanelDidEnd", new Class[]{NSOpenPanel.class, int.class, Object.class}), null);
    }

    private NSButton defaultBufferButton;
    public void setDefaultBufferButton(NSButton defaultBufferButton) {
	this.defaultBufferButton = defaultBufferButton;
	this.defaultBufferButton.setTarget(this);
	this.defaultBufferButton.setAction(new NSSelector("defaultBufferButtonClicked", new Class[] {NSButton.class}));
    }

    public void defaultBufferButtonClicked(NSButton sender) {
	Preferences.instance().setProperty("connection.buffer", Preferences.instance().getProperty("connection.buffer.default"));
    }

    private NSTextField bufferField;
    public void setBufferField(NSTextField bufferField) {
	this.bufferField = bufferField;
	this.bufferField.setStringValue(Preferences.instance().getProperty("connection.buffer"));
	NSNotificationCenter.defaultCenter().addObserver(
						  this,
						  new NSSelector("bufferFieldDidChange", new Class[]{NSNotification.class}),
						  NSControl.ControlTextDidChangeNotification,
						  this.bufferField);
    }

    public void bufferFieldDidChange(NSNotification sender) {
	try {
	    Integer.parseInt(this.bufferField.stringValue());
	    Preferences.instance().setProperty("connection.buffer", this.bufferField.stringValue());
	}
	catch(NumberFormatException e) {
	    log.error(e.getMessage());
	}
    }
    
    private NSTextField anonymousField;
    public void setAnonymousField(NSTextField anonymousField) {
	this.anonymousField = anonymousField;
	this.anonymousField.setStringValue(Preferences.instance().getProperty("ftp.anonymous.pass"));
	NSNotificationCenter.defaultCenter().addObserver(
						  this,
						  new NSSelector("anonymousFieldDidChange", new Class[]{NSNotification.class}),
						  NSControl.ControlTextDidChangeNotification,
						  this.anonymousField);
    }

    public void anonymousFieldDidChange(NSNotification sender) {
	Preferences.instance().setProperty("ftp.anonymous.pass", this.anonymousField.stringValue());
    }

    private NSTextField downloadPathField;
    public void setDownloadPathField(NSTextField downloadPathField) {
	this.downloadPathField = downloadPathField;
	this.downloadPathField.setStringValue(Preferences.instance().getProperty("connection.download.folder"));
	NSNotificationCenter.defaultCenter().addObserver(
						  this,
						  new NSSelector("downloadPathFieldDidChange", new Class[]{NSNotification.class}),
						  NSControl.ControlTextDidChangeNotification,
						  this.downloadPathField);
    }

    public void downloadPathFieldDidChange(NSNotification sender) {
	Preferences.instance().setProperty("connection.download.folder", this.downloadPathField.stringValue());
    }

    private NSTextField loginField;
    public void setLoginField(NSTextField loginField) {
	this.loginField = loginField;
	this.loginField.setStringValue(Preferences.instance().getProperty("connection.login.name"));
	NSNotificationCenter.defaultCenter().addObserver(
						  this,
						  new NSSelector("loginFieldDidChange", new Class[]{NSNotification.class}),
						  NSControl.ControlTextDidChangeNotification,
						  this.loginField);
    }

    public void loginFieldDidChange(NSNotification sender) {
	Preferences.instance().setProperty("connection.login.name", this.loginField.stringValue());
    }

    private NSButton showHiddenCheckbox;
    public void setShowHiddenCheckbox(NSButton showHiddenCheckbox) {
	this.showHiddenCheckbox = showHiddenCheckbox;
	this.showHiddenCheckbox.setTarget(this);
	this.showHiddenCheckbox.setAction(new NSSelector("showHiddenCheckboxClicked", new Class[] {NSButton.class}));
	showHiddenCheckbox.setState(Preferences.instance().getProperty("browser.showHidden").equals("true") ? NSCell.OnState : NSCell.OffState);
    }

    public void showHiddenCheckboxClicked(NSButton sender) {
	switch(sender.state()) {
	    case NSCell.OnState:
		Preferences.instance().setProperty("browser.showHidden", "true");
		break;
	    case NSCell.OffState:
		Preferences.instance().setProperty("browser.showHidden", "false");
		break;
	}
    }

    private NSButton newBrowserCheckbox;
    public void setNewBrowserCheckbox(NSButton newBrowserCheckbox) {
	this.newBrowserCheckbox = newBrowserCheckbox;
	this.newBrowserCheckbox.setTarget(this);
	this.newBrowserCheckbox.setAction(new NSSelector("newBrowserCheckboxClicked", new Class[] {NSButton.class}));
	newBrowserCheckbox.setState(Preferences.instance().getProperty("browser.opendefault").equals("true") ? NSCell.OnState : NSCell.OffState);
    }

    public void newBrowserCheckboxClicked(NSButton sender) {
	switch(sender.state()) {
	    case NSCell.OnState:
		Preferences.instance().setProperty("browser.opendefault", "true");
		break;
	    case NSCell.OffState:
		Preferences.instance().setProperty("browser.opendefault", "false");
		break;
	}
    }

    private NSButton closeTransferCheckbox;
    public void setCloseTransferCheckbox(NSButton closeTransferCheckbox) {
	this.closeTransferCheckbox = closeTransferCheckbox;
	this.closeTransferCheckbox.setTarget(this);
	this.closeTransferCheckbox.setAction(new NSSelector("closeTransferCheckboxClicked", new Class[] {NSButton.class}));
	closeTransferCheckbox.setState(Preferences.instance().getProperty("transfer.close").equals("true") ? NSCell.OnState : NSCell.OffState);
    }

    public void closeTransferCheckboxClicked(NSButton sender) {
	switch(sender.state()) {
	    case NSCell.OnState:
		Preferences.instance().setProperty("transfer.close", "true");
		break;
	    case NSCell.OffState:
		Preferences.instance().setProperty("transfer.close", "false");
		break;
	}
    }

    private NSButton processCheckbox;
    public void setProcessCheckbox(NSButton processCheckbox) {
	this.processCheckbox = processCheckbox;
	this.processCheckbox.setTarget(this);
	this.processCheckbox.setAction(new NSSelector("processCheckboxClicked", new Class[] {NSButton.class}));
    }

    public void processCheckboxClicked(NSButton sender) {
	switch(sender.state()) {
	    case NSCell.OnState:
		Preferences.instance().setProperty("connection.download.postprocess", "true");
		break;
	    case NSCell.OffState:
		Preferences.instance().setProperty("connection.download.postprocess", "false");
		break;
	}
    }

    private NSPopUpButton duplicateCombo;
    public void setDuplicateCombo(NSPopUpButton duplicateCombo) {
	this.duplicateCombo = duplicateCombo;
	this.duplicateCombo.setTarget(this);
	this.duplicateCombo.setAction(new NSSelector("duplicateComboClicked", new Class[] {NSPopUpButton.class}));
	this.duplicateCombo.removeAllItems();	
	this.duplicateCombo.addItemsWithTitles(new NSArray(new String[]{OVERWRITE_EXISTING_FILE, TRY_TO_RESUME_TRANSFER, USE_A_SIMILAR_NAME}));
//	if(Preferences.instance().getProperty("download.duplicate").equals("ask"))
//	    this.duplicateCombo.setTitle(ASK_ME_WHAT_TO_DO);
	if(Preferences.instance().getProperty("download.duplicate").equals("overwrite"))
	    this.duplicateCombo.setTitle(OVERWRITE_EXISTING_FILE);
	else if(Preferences.instance().getProperty("download.duplicate").equals("resume"))
	    this.duplicateCombo.setTitle(TRY_TO_RESUME_TRANSFER);
	else if(Preferences.instance().getProperty("download.duplicate").equals("similar"))
	    this.duplicateCombo.setTitle(USE_A_SIMILAR_NAME);
    }

    public void duplicateComboClicked(NSPopUpButton sender) {
//	if(sender.selectedItem().title().equals(ASK_ME_WHAT_TO_DO))
//	    Preferences.instance().setProperty("download.duplicate", "ask");
	if(sender.selectedItem().title().equals(OVERWRITE_EXISTING_FILE))
	    Preferences.instance().setProperty("download.duplicate", "overwrite");
	else if(sender.selectedItem().title().equals(TRY_TO_RESUME_TRANSFER))
	    Preferences.instance().setProperty("download.duplicate", "resume");
	else if(sender.selectedItem().title().equals(USE_A_SIMILAR_NAME))
	    Preferences.instance().setProperty("download.duplicate", "similar");
    }

    private NSPopUpButton transfermodeCombo;
    public void setTransfermodeCombo(NSPopUpButton transfermodeCombo) {
	this.transfermodeCombo = transfermodeCombo;
	this.transfermodeCombo.setTarget(this);
	this.transfermodeCombo.setAction(new NSSelector("transfermodeComboClicked", new Class[] {NSPopUpButton.class}));
	this.transfermodeCombo.removeAllItems();
	this.transfermodeCombo.addItemsWithTitles(new NSArray(new String[]{TRANSFERMODE_BINARY, TRANSFERMODE_ASCII}));
	if(Preferences.instance().getProperty("ftp.transfermode").equals("binary"))
	    this.transfermodeCombo.setTitle(TRANSFERMODE_BINARY);
	else
	    this.transfermodeCombo.setTitle(TRANSFERMODE_ASCII);
    }

    public void transfermodeComboClicked(NSPopUpButton sender) {
	if(sender.selectedItem().title().equals(TRANSFERMODE_ASCII))
	    Preferences.instance().setProperty("ftp.transfermode", "ascii");
	else
	    Preferences.instance().setProperty("ftp.transfermode", "binary");
    }

    private NSPopUpButton connectmodeCombo;
    public void setConnectmodeCombo(NSPopUpButton connectmodeCombo) {
	this.connectmodeCombo = connectmodeCombo;
	this.connectmodeCombo.setTarget(this);
	this.connectmodeCombo.setAction(new NSSelector("connectmodeComboClicked", new Class[] {NSPopUpButton.class}));
	this.connectmodeCombo.removeAllItems();
	this.connectmodeCombo.addItemsWithTitles(new NSArray(new String[]{CONNECTMODE_ACTIVE, CONNECTMODE_PASSIVE}));
	if(Preferences.instance().getProperty("ftp.connectmode").equals("passive"))
	    connectmodeCombo.setTitle(CONNECTMODE_PASSIVE);
	else
	    connectmodeCombo.setTitle(CONNECTMODE_ACTIVE);
    }

    public void connectmodeComboClicked(NSPopUpButton sender) {
	if(sender.selectedItem().title().equals(CONNECTMODE_ACTIVE))
	    Preferences.instance().setProperty("ftp.connectmode", "active");
	else
	    Preferences.instance().setProperty("ftp.connectmode", "passive");
    }

    private NSPopUpButton protocolCombo;
    public void setProtocolCombo(NSPopUpButton protocolCombo) {
	this.protocolCombo = protocolCombo;
	this.protocolCombo.setTarget(this);
	this.protocolCombo.setAction(new NSSelector("protocolComboClicked", new Class[] {NSPopUpButton.class}));
	this.protocolCombo.removeAllItems();
	this.protocolCombo.addItemsWithTitles(new NSArray(new String[]{PROTOCOL_FTP, PROTOCOL_SFTP}));
	if(Preferences.instance().getProperty("connection.protocol.default").equals("ftp"))
	    protocolCombo.setTitle(PROTOCOL_FTP);
	else
	    protocolCombo.setTitle(PROTOCOL_SFTP);
    }

    public void protocolComboClicked(NSPopUpButton sender) {
	if(sender.selectedItem().title().equals(PROTOCOL_FTP)) {
	    Preferences.instance().setProperty("connection.protocol.default", Session.FTP);
	    Preferences.instance().setProperty("connection.port.default", Session.FTP_PORT);
	}
	else {
	    Preferences.instance().setProperty("connection.protocol.default", Session.SFTP);
	    Preferences.instance().setProperty("connection.port.default", Session.SSH_PORT);
	}
    }


    private NSWindow window;
    public void setWindow(NSWindow window) {
	this.window = window;
    }


    private static NSMutableArray allDocuments = new NSMutableArray();

    public static CDPreferencesController instance() {
	if(null == instance) {
	    instance = new CDPreferencesController();
	    allDocuments.addObject(instance);
	}
        if (false == NSApplication.loadNibNamed("Preferences", instance)) {
            log.fatal("Couldn't load Preferences.nib");
        }
	return instance;
    }

    private CDPreferencesController() {
	allDocuments.addObject(this);
    }

    public NSWindow window() {
	return this.window;
    }

    public void windowWillClose(NSNotification notification) {
	this.window().setDelegate(null);
	NSNotificationCenter.defaultCenter().removeObserver(this);
	allDocuments.removeObject(this);
    }

    public void openPanelDidEnd(NSOpenPanel sheet, int returnCode, Object contextInfo) {
	switch(returnCode) {
	    case(NSPanel.OKButton): {
		NSArray selected = sheet.filenames();
		String filename;
		if((filename = (String)selected.lastObject()) != null) {
		    Preferences.instance().setProperty("connection.download.folder", filename);
		    this.downloadPathField.setStringValue(Preferences.instance().getProperty("connection.download.folder"));
		}
		break;
	    }
	    case(NSPanel.CancelButton): {
		break;
	    }
	}
    }
}
