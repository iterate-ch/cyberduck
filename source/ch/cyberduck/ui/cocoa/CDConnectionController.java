/*
 *  ch.cyberduck.ui.cocoa.CDConnectionController.java
 *  Cyberduck
 *
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

package ch.cyberduck.ui.cocoa;

import com.apple.cocoa.foundation.*;
import com.apple.cocoa.application.*;

import java.io.IOException;

import java.util.Observer;
import java.util.Observable;

import ch.cyberduck.core.*;
import ch.cyberduck.core.http.*;
import ch.cyberduck.core.sftp.*;
import ch.cyberduck.core.ftp.*;

import ch.cyberduck.ui.cocoa.CDPathPopUpButton;
import ch.cyberduck.ui.cocoa.CDStatusLabel;

import com.sshtools.j2ssh.session.SessionChannelClient;
import com.sshtools.j2ssh.transport.InvalidHostFileException;
import com.sshtools.j2ssh.transport.HostKeyVerification;
import com.sshtools.j2ssh.authentication.PasswordAuthentication;
import com.sshtools.j2ssh.authentication.AuthenticationProtocolState;
import com.sshtools.j2ssh.sftp.*;
import com.sshtools.j2ssh.*;

import org.apache.log4j.Appender;
import org.apache.log4j.Logger;

public class CDConnectionController extends NSObject implements Observer {

    private static Logger log = Logger.getLogger(CDConnectionController.class);

    public NSWindow mainWindow; /* IBOutlet */
    public NSWindow connectionSheet; /* IBOutlet */
    public NSWindow loginSheet; /* IBOutlet */

    public NSPopUpButton pathPopUpButton;
    public NSTextField statusLabel;
    public NSTextField pathField; /* IBOutlet */
    public NSTextField portField; /* IBOutlet */
    public NSPopUpButton protocolPopup; /* IBOutlet */
    public NSTextField hostField; /* IBOutlet */
    public NSTextField usernameField; /* IBOutlet */
    public NSSecureTextField passwordField; /* IBOutlet */
    public NSView logView; /* IBOutlet */
    public NSProgressIndicator progressIndicator; /* IBOutlet */
    
    public NSTableView browserTable;  /* IBOutlet */

    //public NSView connectedListView; /* IBOutlet */

    private CDConnectionController controller = this;
    
    public CDConnectionController() {
	super();
	log.debug("CDConnectionController");
    }

    public void awakeFromNib() {
	//
    }

    public void connect(NSObject sender) {
	log.debug("connect");
	try {
	    Host host = null;
	    String protocol = null;
	    int tag = protocolPopup.selectedItem().tag();
	    switch(tag) {
		case(Session.SSH_PORT):
		    protocol = Session.SFTP;
		    break;
		case(Session.FTP_PORT):
		    protocol = Session.FTP;
		    break;
		case(Session.HTTP_PORT):
		    protocol = Session.HTTP;
		    break;
//		case(Session.HTTPS_PORT):
//		    protocol = Session.HTTPS;
//		    break;
	    }

	    //@todo new connection via menu item recent connection
     //	if(sender instanceof NSMenu
     //NSMenuItem item = menu.getSelectedItem()
     //host = item.

//	    log.debug(protocol+","+hostField.stringValue()+","+usernameField.stringValue()+","+passwordField.stringValue());
	    if(sender instanceof NSTextField) {
		host = new Host(protocol, ((NSControl)sender).stringValue(), 22, null);
	    }
	    if(sender instanceof NSButton) {
		NSApplication.sharedApplication().endSheet(connectionSheet, NSAlertPanel.AlternateReturn);
		Login login = new CDLogin(usernameField.stringValue(), passwordField.stringValue());
		host = new Host(protocol, hostField.stringValue(), 22, login);
	    }
	    host.status.fireActiveEvent();

	    mainWindow.setTitle(host.getName());
	    
	    host.addObserver((CDBrowserView)browserTable);
	    host.addObserver((CDPathPopUpButton)pathPopUpButton);
	    host.status.addObserver((CDStatusLabel)statusLabel);
	    host.status.addObserver((CDLogView)logView);
	    host.status.addObserver((CDProgressWheel)progressIndicator);
	    host.status.addObserver(this);
	    
	    Session session = host.getSession();
	    //@todo only when sftp
	    if(protocol.equals(Session.SFTP))
		host.setHostKeyVerification(new CDHostKeyVerification());
	    session.start();
	}
	catch(IOException e) {
	    log.error(e.toString());
	}
	//	connectedListView.addSubview(new CDConnectedItemView(host));
//	connectedListView.setNeedsDisplay(true);

//	Thread session = new Session(host);
//	session.start();	

 //                // Now try to write to a file without creating it!
 //SftpFile file = sftp.openFile("shinning.txt",
 //                            SftpSubsystemClient.OPEN_CREATE
 //                          | SftpSubsystemClient.OPEN_WRITE);


    }

    public void disconnect(NSObject sender) {
	log.debug("disconnect");
    }

    public void update(Observable o, Object arg) {
//	log.debug("update:"+arg);
	if(o instanceof Status) {
	    Message msg = (Message)arg;
	    if(msg.getTitle().equals(Message.ERROR)) {
		//public static void beginAlertSheet( String title, String defaultButton, String alternateButton, String otherButton, NSWindow docWindow, Object modalDelegate, NSSelector didEndSelector, NSSelector didDismissSelector, Object contextInfo, String message)
		NSAlertPanel.beginAlertSheet(
			       "Error", //title
			       "OK",// defaultbutton
			       null,//alternative button
			       null,//other button
			       mainWindow, //docWindow
			       null, //modalDelegate
			       null, //didEndSelector
			       null, // dismiss selector
			       null, // context
			       msg.getDescription() // message
					    );
	    }
	}
    }

    private class CDLogin extends Login {
	private boolean done;
	private boolean tryAgain;

	public CDLogin(String u, String p) {
	    super(u, p);
	}

	public void loginSheetDidEnd(NSWindow sheet, int returncode, NSWindow main) {
	    CDLoginSheet loginSheet = (CDLoginSheet)sheet;
	    switch(returncode) {
		case(NSAlertPanel.DefaultReturn):
		    tryAgain = true;
		    this.setUsername(loginSheet.getUser());///@todo
		    this.setPassword(loginSheet.getPass());
		case(NSAlertPanel.AlternateReturn):
		    tryAgain = false;
	    }
	    done = true;
	    sheet.close();
	}

	public boolean loginFailure() {
	    log.info("Authentication failed.");
	    mainWindow.makeFirstResponder(loginSheet);
	    //NSApplication.beginSheet( NSWindow sheet, NSWindow docWindow, Object modalDelegate, NSSelector didEndSelector, Object contextInfo)
	    NSApplication.sharedApplication().beginSheet(
						  loginSheet, //sheet
						  mainWindow, //docWindow
						  this, //modalDelegate
						  new NSSelector(
		       "loginSheetDidEnd",
		       new Class[] { NSWindow.class, int.class, NSWindow.class }
		       ),// did end selector
						  null); //contextInfo
	    while(!done) {
		try {
		    Thread.sleep(500); //milliseconds
		}
		catch(InterruptedException e) {
		    e.printStackTrace();
		}
	    }
	    return tryAgain;
	}	
    }
    
    private class CDHostKeyVerification extends HostKeyVerification {
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
				  mainWindow,
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
				  mainWindow,
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
				  mainWindow,//window
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
	    sheet.close();
	    done = true;
	}

	public void keyMismatchSheetDidEnd(NSWindow sheet, int returncode, NSWindow main) {
	    log.debug("keyMismatchSheetDidEnd");
	    try {
		if(returncode == NSAlertPanel.DefaultReturn)
		    allowHost(host, fingerprint, false);
		if(returncode == NSAlertPanel.AlternateReturn) {
		    sheet.close();
		    NSAlertPanel.beginInformationalAlertSheet(
						"Invalid host key", //title
						"OK",// defaultbutton
						null,//alternative button
						null,//other button
						mainWindow,
						this, //delegate
						null,// end selector
						null, // dismiss selector
						this, // context
						"Cannot continue without a valid host key." // message
						);
		    log.info("Cannot continue without a valid host key");
		}
		if(returncode == NSAlertPanel.OtherReturn)
		    sheet.close();
		done = true;
	    }
	    catch(InvalidHostFileException e) {
		e.printStackTrace();
	    }
	}

	public void unknownHostSheetDidEnd(NSWindow sheet, int returncode, NSWindow main) {
	    log.debug("unknownHostSheetDidEnd");
	    try {
		if(returncode == NSAlertPanel.DefaultReturn)
		    allowHost(host, fingerprint, false); // allow host
		if(returncode == NSAlertPanel.AlternateReturn) {
		    sheet.close();
		    NSAlertPanel.beginInformationalAlertSheet(
					    "Invalid host key", //title
					    "OK",// defaultbutton
					    null,//alternative button
					    null,//other button
					    mainWindow,
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
