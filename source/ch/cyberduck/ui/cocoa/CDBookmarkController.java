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
import ch.cyberduck.core.Session;

import com.apple.cocoa.application.*;
import com.apple.cocoa.foundation.*;

import org.apache.log4j.Logger;

/**
* @version $Id$
 */
public class CDBookmarkController {
    private static Logger log = Logger.getLogger(CDBookmarkController.class);
    
    private static final String FTP_STRING = NSBundle.localizedString("FTP (File Transfer)");
    private static final String SFTP_STRING = NSBundle.localizedString("SFTP (SSH Secure File Transfer)");
	
    private Host host;
    
    // ----------------------------------------------------------
    // Outlets
    // ----------------------------------------------------------
    
    private NSWindow window; // IBOutlet
    public void setWindow(NSWindow window) {
		this.window = window;
    }
    
    private NSPopUpButton protocolPopup; // IBOutlet
    public void setProtocolPopup(NSPopUpButton protocolPopup) {
		this.protocolPopup = protocolPopup;
		this.protocolPopup.setTarget(this);
		this.protocolPopup.setAction(new NSSelector("protocolSelectionChanged", new Class[] {Object.class}));
    }

	public void protocolSelectionChanged(Object sender) {
		log.debug("protocolSelectionChanged:"+sender);
		int tag = protocolPopup.selectedItem().tag();
		switch(tag) {
			case(Session.SSH_PORT):
				this.host.setProtocol(Session.SFTP);
				this.host.setPort(Session.SSH_PORT);
				break;
			case(Session.FTP_PORT):
				this.host.setProtocol(Session.FTP);
				this.host.setPort(Session.FTP_PORT);
				break;
		}
		this.updateFields();
    }
    	
    private NSTextField nicknameField; // IBOutlet
    public void setNicknameField(NSTextField nicknameField) {
		this.nicknameField = nicknameField;
    }
	
    private NSTextField hostField; // IBOutlet
    public void setHostField(NSTextField hostField) {
		this.hostField = hostField;
    }
	
    private NSTextField pathField; // IBOutlet
    public void setPathField(NSTextField pathField) {
		this.pathField = pathField;
    }
	
    private NSTextField urlField; // IBOutlet
    public void setUrlField(NSTextField urlField) {
		this.urlField = urlField;
    }
	
    private NSTextField usernameField; // IBOutlet
    public void setUsernameField(NSTextField usernameField) {
		this.usernameField = usernameField;
    }
	
    private static NSMutableArray allDocuments = new NSMutableArray();
    
    // ----------------------------------------------------------
    // Constructors
    // ----------------------------------------------------------
    
    public CDBookmarkController(Host bookmark) {
		log.debug("CDBookmarkController:"+bookmark);
		this.host = bookmark;
		allDocuments.addObject(this);
        if (false == NSApplication.loadNibNamed("Bookmark", this)) {
            log.fatal("Couldn't load Bookmark.nib");
            return;
        }
    }
    
    public void awakeFromNib() {
		log.debug("awakeFromNib");
		NSPoint origin = this.window.frame().origin();
		this.window.setFrameOrigin(new NSPoint(origin.x() + 16, origin.y() - 16));
		this.window.setTitle(this.host.getNickname());
		(NSNotificationCenter.defaultCenter()).addObserver(
													 this,
													 new NSSelector("hostInputDidEndEditing", new Class[]{NSNotification.class}),
													 NSControl.ControlTextDidChangeNotification,
													 hostField);
		(NSNotificationCenter.defaultCenter()).addObserver(
													 this,
													 new NSSelector("pathInputDidEndEditing", new Class[]{NSNotification.class}),
													 NSControl.ControlTextDidChangeNotification,
													 pathField);
		(NSNotificationCenter.defaultCenter()).addObserver(
													 this,
													 new NSSelector("nicknameInputDidEndEditing", new Class[]{NSNotification.class}),
													 NSControl.ControlTextDidChangeNotification,
													 nicknameField);
		(NSNotificationCenter.defaultCenter()).addObserver(
													 this,
													 new NSSelector("usernameInputDidEndEditing", new Class[]{NSNotification.class}),
													 NSControl.ControlTextDidChangeNotification,
													 usernameField);
		this.updateFields();
    }
    
    public void windowWillClose(NSNotification notification) {
		this.window().setDelegate(null);
		NSNotificationCenter.defaultCenter().removeObserver(this);
		allDocuments.removeObject(this);
    }
    
	private NSTextField pkLabel;
    public void setPkLabel(NSTextField pkLabel) {
		this.pkLabel = pkLabel;
		this.pkLabel.setStringValue(NSBundle.localizedString("No Private Key selected"));
    }
	
	private NSButton pkCheckbox;
	public void setPkCheckbox(NSButton pkCheckbox) {
		this.pkCheckbox = pkCheckbox;
		this.pkCheckbox.setTarget(this);
		this.pkCheckbox.setAction(new NSSelector("pkCheckboxSelectionChanged", new Class[] {Object.class}));
	}
	
	public void pkCheckboxSelectionChanged(Object sender) {
		log.debug("pkCheckboxSelectionChanged");
		if(this.pkLabel.stringValue().equals(NSBundle.localizedString("No Private Key selected"))) {
			NSOpenPanel panel = new NSOpenPanel();
			panel.setCanChooseDirectories(false);
			panel.setCanChooseFiles(true);
			panel.setAllowsMultipleSelection(false);
			panel.beginSheetForDirectory(System.getProperty("user.home")+"/.ssh", null, null, this.window(), this, new NSSelector("pkSelectionPanelDidEnd", new Class[]{NSOpenPanel.class, int.class, Object.class}), null);
		}
		else {
			this.pkCheckbox.setState(NSCell.OffState);
			this.pkLabel.setStringValue(NSBundle.localizedString("No Private Key selected"));
		}
	}
	
    public void pkSelectionPanelDidEnd(NSOpenPanel sheet, int returnCode, Object contextInfo) {
		log.debug("pkSelectionPanelDidEnd");
		sheet.orderOut(null);
		switch(returnCode) {
			case(NSPanel.OKButton): {
				NSArray selected = sheet.filenames();
				java.util.Enumeration enumerator = selected.objectEnumerator();
				while (enumerator.hasMoreElements()) {
					String pk = (String)enumerator.nextElement();
					this.host.getLogin().setPrivateKeyFile(pk);
					this.pkLabel.setStringValue(pk);
				}
				break;
			}
			case(NSPanel.CancelButton): {
				this.host.getLogin().setPrivateKeyFile(null);
				this.pkCheckbox.setState(NSCell.OffState);
				this.pkLabel.setStringValue(NSBundle.localizedString("No Private Key selected"));
				break;
			}
		}
    }
	
    public void hostInputDidEndEditing(NSNotification sender) {
		log.debug("hostInputDidEndEditing");
		this.host.setHostname(hostField.stringValue());
		this.updateFields();
    }

	public void pathInputDidEndEditing(NSNotification sender) {
		log.debug("pathInputDidEndEditing");
		this.host.setDefaultPath(pathField.stringValue());
		this.updateFields();
    }
	
    public void nicknameInputDidEndEditing(NSNotification sender) {
		log.debug("nicknameInputDidEndEditing");
		this.host.setNickname(nicknameField.stringValue());
		this.updateFields();
    }
	
    public void usernameInputDidEndEditing(NSNotification sender) {
		log.debug("usernameInputDidEndEditing");
		this.host.getLogin().setUsername(usernameField.stringValue());
		this.updateFields();
    }
    
    private void updateFields() {
		this.window.setTitle(this.host.getNickname());
		this.urlField.setStringValue(this.host.getURL());
		this.hostField.setStringValue(this.host.getHostname());
		this.nicknameField.setStringValue(this.host.getNickname());
		this.pathField.setStringValue(this.host.getDefaultPath());
		this.usernameField.setStringValue(this.host.getLogin().getUsername());
		this.protocolPopup.selectItemWithTitle(this.host.getProtocol().equals(Session.FTP) ? FTP_STRING : SFTP_STRING);
		this.pkCheckbox.setEnabled(this.host.getProtocol().equals(Session.SFTP));
		if(this.host.getLogin().usesPublicKeyAuthentication()) {
			this.pkCheckbox.setState(NSCell.OnState);
			this.pkLabel.setStringValue(this.host.getLogin().getPrivateKeyFile());
		}
		else {
			this.pkCheckbox.setState(NSCell.OffState);
			this.pkLabel.setStringValue(NSBundle.localizedString("No Private Key selected"));
		}
    }
    
    public NSWindow window() {
		return this.window;
    }
}
