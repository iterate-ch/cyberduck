package ch.cyberduck.ui.cocoa;

/*
 *  Copyright (c) 2002 David Kocher. All rights reserved.
 *  http://icu.unizh.ch/~dkocher/
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

import com.apple.cocoa.foundation.*;
import com.apple.cocoa.application.*;
import com.sshtools.j2ssh.transport.InvalidHostFileException;
import com.sshtools.j2ssh.transport.AbstractHostKeyVerification;
import ch.cyberduck.core.Session;
import ch.cyberduck.core.Host;
import ch.cyberduck.core.Preferences;
import org.apache.log4j.Logger;

/**
* @version $Id$
 */
public class CDConnectionSheet {//extends NSPanel {
    private static Logger log = Logger.getLogger(CDConnectionSheet.class);

    // ----------------------------------------------------------
    // Constructors
    // ----------------------------------------------------------

    public CDConnectionSheet() {
	super();
	log.debug("CDConnectionSheet");
	NSApplication.loadNibNamed("Connection", this);
    }
    
    // ----------------------------------------------------------
    // Outlets
    // ----------------------------------------------------------

    private NSWindow sheet;
    public void setSheet(NSWindow sheet) {
	this.sheet = sheet;
    }
    
    private NSPopUpButton protocolPopup;    
    public void setProtocolPopup(NSPopUpButton protocolPopup) {
	this.protocolPopup = protocolPopup;
    }

    private NSTextField hostField;
    public void setHostField(NSTextField hostField) {
	this.hostField = hostField;
    }
    
    private NSTextField pathField;
    public void setPathField(NSTextField pathField) {
	this.pathField = pathField;
    }
    
    private NSTextField portField;
    public void setPortField(NSTextField portField) {
	this.portField = portField;
    }

    private NSTextField usernameField;
    public void setUsernameField(NSTextField usernameField) {
	this.usernameField = usernameField;
    }

    private NSTextField passField;
    public void setPassField(NSTextField passField) {
	this.passField = passField;
    }
    
    private NSTextField urlLabel;
    public void setUrlLabel(NSTextField urlLabel) {
	this.urlLabel = urlLabel;
    }


    
    public NSWindow window() {
	return this.sheet;
    }
        
    public void awakeFromNib() {
	log.debug("awakeFromNib");
	// Notify the textInputDidChange() method if the user types.
	(NSNotificationCenter.defaultCenter()).addObserver(
						    this,
						    new NSSelector("textInputDidChange", new Class[]{NSNotification.class}),
						    NSControl.ControlTextDidChangeNotification,
						    hostField);
	(NSNotificationCenter.defaultCenter()).addObserver(
						    this,
						    new NSSelector("textInputDidChange", new Class[]{NSNotification.class}),
						    NSControl.ControlTextDidChangeNotification,
						    pathField);
	(NSNotificationCenter.defaultCenter()).addObserver(
						    this,
						    new NSSelector("textInputDidChange", new Class[]{NSNotification.class}),
						    NSControl.ControlTextDidChangeNotification,
						    portField);
	(NSNotificationCenter.defaultCenter()).addObserver(
						    this,
						    new NSSelector("textInputDidChange", new Class[]{NSNotification.class}),
						    NSControl.ControlTextDidChangeNotification,
						    usernameField);
        //@todo this.usernameField.setStringValue(Preferences.instance().getProperty("connection.login.name"));
	//@todo this.pathField.setStringValue(Preferences.instance().getProperty("connection.path.default"));
//	this.textInputDidChange(null);
	this.portField.setIntValue(protocolPopup.selectedItem().tag());
	this.pathField.setStringValue("~");
    }

    
    public void protocolSelectionChanged(NSObject sender) {
	log.debug("protocolSelectionChanged");
	NSMenuItem selectedItem = protocolPopup.selectedItem();
	if(selectedItem.tag() == Session.SSH_PORT)
	    portField.setIntValue(Session.SSH_PORT);
	if(selectedItem.tag() == Session.FTP_PORT)
	    portField.setIntValue(Session.FTP_PORT);
	if(selectedItem.tag() == Session.HTTP_PORT)
	    portField.setIntValue(Session.HTTP_PORT);
	//@todo HTTPS
    }

    public void textInputDidChange(NSNotification sender) {
	log.debug("textInputDidChange");
	urlLabel.setStringValue(usernameField.stringValue()+"@"+hostField.stringValue()+":"+portField.stringValue()+"/"+pathField.stringValue());
    }


    public void closeSheet(NSObject sender) {
	log.debug("closeSheet");
	// Ends a document modal session by specifying the sheet window, sheet. Also passes along a returnCode to the delegate.
	NSApplication.sharedApplication().endSheet(hostField.window(), ((NSButton)sender).tag());
    }

    public void connectionSheetDidEnd(NSWindow sheet, int returncode, NSWindow main) {
	log.debug("connectionSheetDidEnd");
	sheet.orderOut(this);
	switch(returncode) {
	    case(NSAlertPanel.DefaultReturn):
		int tag = protocolPopup.selectedItem().tag();
		String protocol = null;
		int port = -1;
		switch(tag) {
		    case(Session.SSH_PORT):
			protocol = Session.SFTP;
			port = Session.SSH_PORT;
			break;
		    case(Session.FTP_PORT):
			protocol = Session.FTP;
			port = Session.FTP_PORT;
			break;
		    case(Session.HTTP_PORT):
			protocol = Session.HTTP;
			port = Session.HTTP_PORT;
			break;
		    //		case(Session.HTTPS_PORT):
      //		    protocol = Session.HTTPS;
      //                 port = Session.HTTPS_PORT;
      //		    break;
		}

		Host host = new Host(protocol, hostField.stringValue(), port, pathField.stringValue(), new CDLoginController(usernameField.stringValue(), passField.stringValue()));

		if(host.getProtocol().equals(Session.SFTP)) {
		    try {
			host.setHostKeyVerification(new CDHostKeyVerification());
		    }
		    catch(InvalidHostFileException e) {
		//This exception is thrown whenever an exception occurs open or reading from the host file.
			NSAlertPanel.beginAlertSheet(
				"Error", //title
				"OK",// defaultbutton
				null,//alternative button
				null,//other button
				null,//@todo mainWindow, //docWindow
				null, //modalDelegate
				null, //didEndSelector
				null, // dismiss selector
				null, // context
				"Could not open or read the host file: "+e.getMessage() // message
				);
		//@todo run alert sheet?
			log.error(e.getMessage());
		    }
		}
		    
//@todo		host.addObserver(this);
		CDConnectionController controller = new CDConnectionController(host);
		controller.connect();

	case(NSAlertPanel.AlternateReturn):
		//
	}
    }




        // ----------------------------------------------------------
    // CDHostKeyVerification
    // ----------------------------------------------------------

    /**
	* Concrete Coccoa implementation of a SSH HostKeyVerification
     */
    private class CDHostKeyVerification extends AbstractHostKeyVerification {
	private String host;
	private String fingerprint;

	private boolean done;

	public CDHostKeyVerification() throws InvalidHostFileException {
	    super();
	    log.debug("CDHostKeyVerification");
	}

	public CDHostKeyVerification(String hostFile) throws InvalidHostFileException {
	    super(hostFile);
	}

	public void onDeniedHost(String hostname) {
	    log.debug("onDeniedHost");
	    NSAlertPanel.beginInformationalAlertSheet(
					       "Access denied", //title
					       "OK",// defaultbutton
					       null,//alternative button
					       null,//other button
					       null,//@todomainWindow,
					       this, //delegate
					       new NSSelector
					       (
	     "deniedHostSheetDidEnd",
	     new Class[]
	     {
		 NSWindow.class, int.class, NSWindow.class
	     }
	     ),// end selector
					       null, // dismiss selector
					       this, // context
					       "Access to the host " + hostname + " is denied from this system" // message
					       );
	    while(!this.done) {
		try {
		    Thread.sleep(500); //milliseconds
		}
		catch(InterruptedException e) {
		    e.printStackTrace();
		}
	    }
	}

	public void onHostKeyMismatch(String host, String fingerprint, String actualHostKey) {
	    log.debug("onHostKeyMismatch");
	    this.host = host;
	    this.fingerprint = fingerprint;
	    NSAlertPanel.beginInformationalAlertSheet(
					       "Host key mismatch", //title
					       "Allow",// defaultbutton
					       "Deny",//alternative button
					       isHostFileWriteable() ? "Always" : null,//other button
					       null,//@todo mainWindow,
					       this, //delegate
					       new NSSelector
					       (
	     "keyMismatchSheetDidEnd",
	     new Class[]
	     {
		 NSWindow.class, int.class, NSWindow.class
	     }
	     ),// end selector
					       null, // dismiss selector
					       this, // context
					       "The host key supplied by " + host + " is: "
					       + actualHostKey +
					       "The current allowed key for " + host + " is: "
					       + fingerprint +"\nDo you want to allow the host access?");
	    while(!this.done) {
		try {
		    Thread.sleep(500); //milliseconds
		}
		catch(InterruptedException e) {
		    e.printStackTrace();
		}
	    }
	}


	public void onUnknownHost(String host, String fingerprint) {
	    log.debug("onUnknownHost");
	    this.host = host;
	    this.fingerprint = fingerprint;
	    NSAlertPanel.beginInformationalAlertSheet(
					       "Unknown host", //title
					       "Allow",// defaultbutton
					       "Deny",//alternative button
					       isHostFileWriteable() ? "Always" : null,//other button
					       null,//@todo mainWindow,//window
					       this, //delegate
					       new NSSelector
					       (
	     "unknownHostSheetDidEnd",
	     new Class[]
	     {
		 NSWindow.class, int.class, NSWindow.class
	     }
	     ),// end selector
					       null, // dismiss selector
					       this, // context
					       "The host " + host
					       + " is currently unknown to the system. The host key fingerprint is: " + fingerprint+".");
	    while(!this.done) {
		try {
		    Thread.sleep(500); //milliseconds
		}
		catch(InterruptedException e) {
		    e.printStackTrace();
		}
	    }
	}


	public void deniedHostSheetDidEnd(NSWindow sheet, int returncode, NSWindow main) {
	    log.debug("deniedHostSheetDidEnd");
	    sheet.orderOut(this);
	    done = true;
	}

	public void keyMismatchSheetDidEnd(NSWindow sheet, int returncode, NSWindow main) {
	    log.debug("keyMismatchSheetDidEnd");
	    sheet.orderOut(this);
	    try {
		if(returncode == NSAlertPanel.DefaultReturn)
		    allowHost(host, fingerprint, false);
		if(returncode == NSAlertPanel.AlternateReturn) {
		    NSAlertPanel.beginInformationalAlertSheet(
						"Invalid host key", //title
						"OK",// defaultbutton
						null,//alternative button
						null,//other button
						null,//@todo mainWindow,
						this, //delegate
						null,// end selector
						null, // dismiss selector
						this, // context
						"Cannot continue without a valid host key." // message
						);
		    log.info("Cannot continue without a valid host key");
		}
		if(returncode == NSAlertPanel.OtherReturn) {
		    //
		}
		done = true;
	    }
	    catch(InvalidHostFileException e) {
		e.printStackTrace();
	    }
	}

	public void unknownHostSheetDidEnd(NSWindow sheet, int returncode, NSWindow main) {
	    log.debug("unknownHostSheetDidEnd");
	    sheet.orderOut(this);
	    try {
		if(returncode == NSAlertPanel.DefaultReturn)
		    allowHost(host, fingerprint, false); // allow host
		if(returncode == NSAlertPanel.AlternateReturn) {
		    NSAlertPanel.beginInformationalAlertSheet(
						"Invalid host key", //title
						"OK",// defaultbutton
						null,//alternative button
						null,//other button
						null,//@todo mainWindow,
						this, //delegate
						null,// end selector
						null, // dismiss selector
						this, // context
						"Cannot continue without a valid host key." // message
						);
		    log.info("Cannot continue without a valid host key");
		}
		if(returncode == NSAlertPanel.OtherReturn)
		    allowHost(host, fingerprint, true); // always allow host
		done = true;
	    }
	    catch(InvalidHostFileException e) {
		e.printStackTrace();
	    }
	}
    }

}
