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

import java.util.Observer;
import java.util.Observable;
import java.util.Iterator;
import java.util.List;
import org.apache.log4j.Logger;

import com.sshtools.j2ssh.session.SessionChannelClient;
import com.sshtools.j2ssh.transport.InvalidHostFileException;
import com.sshtools.j2ssh.transport.AbstractHostKeyVerification;
import com.sshtools.j2ssh.authentication.PasswordAuthenticationClient;
import com.sshtools.j2ssh.authentication.AuthenticationProtocolState;
import com.sshtools.j2ssh.sftp.*;
import com.sshtools.j2ssh.*;

import ch.cyberduck.core.*;
import ch.cyberduck.ui.ObserverList;
import ch.cyberduck.ui.cocoa.CDPathComboBox;

/**
* @version $Id$
*/
public class CDConnectionController extends NSObject implements Observer {
    private static Logger log = Logger.getLogger(CDConnectionController.class);


    // ----------------------------------------------------------
    // Outlets from CDMainWindow
    // ----------------------------------------------------------
    
    private CDMainWindow mainWindow; // IBOutlet
    public void setMainWindow(CDMainWindow mainWindow) {
	this.mainWindow = mainWindow;
    }

    private CDConnectionSheet connectionSheet; // IBOutlet
    public void setConnectionSheet(CDConnectionSheet connectionSheet) {
	this.connectionSheet = connectionSheet;
    }

    private CDLoginSheet loginSheet; // IBOutlet
    public void setLoginSheet(CDLoginSheet loginSheet) {
	this.loginSheet = loginSheet;
    }
    
    private NSProgressIndicator progressIndicator; // IBOutlet
    public void setProgressIndicator(NSProgressIndicator progressIndicator) {
	this.progressIndicator = progressIndicator;
	this.progressIndicator.setIndeterminate(true);
    }

    private NSTextField statusLabel; // IBOutlet
    public void setStatusLabel(NSTextField statusLabel) {
	this.statusLabel = statusLabel;
    }

    private NSMenu recentConnectionsMenu;
    private NSMenuItem recentConnectionsMenuItem;
    public void setRecentConnectionsMenuItem(NSMenuItem recentConnectionsMenuItem) {
	this.recentConnectionsMenuItem = recentConnectionsMenuItem;
    }


    // ----------------------------------------------------------
    // Outlets from CDConnectionSheet
    // ----------------------------------------------------------
    
    public CDPathComboBox pathComboBox; // IBOutlet
    public NSTextField pathField; // IBOutlet
    public NSTextField portField; // IBOutlet
    public NSPopUpButton protocolPopup; // IBOutlet
    public NSTextField hostField; // IBOutlet
    //@todo remove
    public NSTextField usernameField; // IBOutlet
    public NSTextField passwordField; // IBOutlet
    public NSTextView logView; // IBOutlet
    
//    public CDBrowserView browserView; // IBOutlet
//    public CDTransferView transferView; // IBOutlet
    public CDHostView hostView; // IBOutlet

    public void awakeFromNib() {
	ObserverList.instance().registerObserver(this);
	recentConnectionsMenuItem.setSubmenu(recentConnectionsMenu = new NSMenu());
	List hosts = History.instance();
	Iterator i = hosts.iterator();
	while(i.hasNext()) {
	    Host h = (Host)i.next();
	    // Adds a new item with title aString, action aSelector, and key equivalent keyEquiv to the end of the receiver. Returns the new menu item. If you do not want the menu item to have a key equivalent, keyEquiv should be an empty string and not null.
	    recentConnectionsMenu.addItem(h.getName(), new NSSelector("connect", new Class[] {null}), "");
	}
    }    
  
    public CDConnectionController() {
	super();
	log.debug("CDConnectionController");
    }

    public void recycle(NSObject sender) {
	log.debug("recycle");
	Host host = (Host)((CDHostTableDataSource)hostView.dataSource()).getEntry(hostView.selectedRow());
	host.recycle();
    }

    public void disconnect(NSObject sender) {
	log.debug("disconnect");
	Host host = (Host)((CDHostTableDataSource)hostView.dataSource()).getEntry(hostView.selectedRow());
	host.closeSession();
	host.deleteObservers();
    }    

    public void connect(NSObject sender) {
	log.debug("connect");

	// All we need to connect - default values set in Host.class
	String server = null;
	String path = null;
	String protocol = Preferences.instance().getProperty("connection.protocol.default");
	int port = -1;
	Login login = new CDLogin(); //anonymous

	//connection initiated from menu item "recent connections"
	if(sender instanceof NSMenuItem) {
	    log.debug("New connection from \"Recent Connections\"");
	    NSMenuItem item = (NSMenuItem)sender;
	    Host h = History.instance().getHost(item.title());
	    h.openSession();
	    return;
	}
	if(sender instanceof NSTextField) { //connection initiated from toolbar text field
	    log.debug("New connection from \"Quick Connect\"");
	    server = ((NSControl)sender).stringValue();
	}
	if(sender instanceof NSButton) { //connection initiated from connection sheet
	    log.debug("New connection from \"Connection Sheet\"");
	    NSApplication.sharedApplication().endSheet(connectionSheet, NSAlertPanel.AlternateReturn);
	    server = hostField.stringValue();
	    path = pathField.stringValue();
	    int tag = protocolPopup.selectedItem().tag();
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
	    login = new CDLogin(usernameField.stringValue(), passwordField.stringValue());
	}
	log.debug(protocol+","+hostField.stringValue()+","+usernameField.stringValue()+","+passwordField.stringValue());

	Host host = new Host(protocol, server, port, path, login);

	if(protocol.equals(Session.SFTP)) {
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
			       mainWindow, //docWindow
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
	host.openSession();
    }

/*
    public void download(Path download) {
	log.debug("download:"+download);
	((CDTransferTableDataSource)transferView.dataSource()).addEntry(download);
	download.addObserver(transferView);
	download.download();
    }

    public void upload(Path upload) {
	log.debug("upload:"+upload);
	((CDTransferTableDataSource)transferView.dataSource()).addEntry(upload);
	upload.addObserver(transferView);
	upload.upload();
    }
 */

    public void update(Observable o, Object arg) {
	//	log.debug("update:"+arg);
	if(o instanceof Host) {
	    if(arg instanceof Message) {
		Host host = (Host)o;
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
		// update status label
		if(msg.getTitle().equals(Message.PROGRESS)) {
		    statusLabel.setStringValue(msg.getDescription());
		}
		if(msg.getTitle().equals(Message.OPEN)) {
		    progressIndicator.startAnimation(this);
		    History.instance().add(host);
		}
		if(msg.getTitle().equals(Message.CONNECTED)) {
		    progressIndicator.stopAnimation(this);
		}
		if(msg.getTitle().equals(Message.SELECTION)) {
		    mainWindow.setTitle(host.getName());
		}
	    }
	}
    }


    // ----------------------------------------------------------
    // CDLogin
    // ----------------------------------------------------------
    
    private class CDLogin extends Login {
	private boolean done;
	private boolean tryAgain;

        public CDLogin() {
            super();
        }
        
	public CDLogin(String u, String p) {
	    super(u, p);
	}

	/**
	* Selector method from 
	* @see loginFailure
	*/
	public void loginSheetDidEnd(NSWindow sheet, int returncode, NSWindow main) {
	    log.info("loginSheetDidEnd");
	    switch(returncode) {
		case(NSAlertPanel.DefaultReturn):
		    tryAgain = true;
		    this.setUsername(loginSheet.user());
		    this.setPassword(loginSheet.pass());
                    break;
		case(NSAlertPanel.AlternateReturn):
		    tryAgain = false;
                    break;
	    }
	    done = true;
	    loginSheet.close();
	}

	public boolean loginFailure(String message) {
	    log.info("Authentication failed");
	    if(loginSheet == null)
		NSApplication.loadNibNamed("Login", CDConnectionController.this);
//	    loginSheet.makeKeyAndOrderFront(this);//@todo
//@todo	    loginSheet.setUser(this.getUsername());
	    loginSheet.setExplanation(message);
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
