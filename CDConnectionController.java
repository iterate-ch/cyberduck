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

import com.apple.cocoa.foundation.*;
import com.apple.cocoa.application.*;

import com.sshtools.j2ssh.session.SessionChannelClient;
import com.sshtools.j2ssh.transport.InvalidHostFileException;
import com.sshtools.j2ssh.transport.HostKeyVerification;
import com.sshtools.j2ssh.authentication.PasswordAuthentication;
import com.sshtools.j2ssh.authentication.AuthenticationProtocolState;
import com.sshtools.j2ssh.sftp.*;
import com.sshtools.j2ssh.*;

import ch.cyberduck.connection.Bookmark;
//import ch.cyberduck.connection.Path;
//import ch.cyberduck.connection.Status;
//import ch.cyberduck.connection.Message;
//import ch.cyberduck.connection.Session;

import org.apache.log4j.Logger;

public class CDConnectionController extends NSObject {

    private static Logger log = Logger.getLogger(CDConnectionController.class);

    public NSWindow mainWindow; /* IBOutlet */
    public NSWindow connectionSheet; /* IBOutlet */
    public NSWindow loginSheet; /* IBOutlet */

    public NSSecureTextField passwordField; /* IBOutlet */
    public NSTextField pathField; /* IBOutlet */
    public NSTextField portField; /* IBOutlet */
    public NSPopUpButton protocolPopup; /* IBOutlet */
    public NSTextField hostField; /* IBOutlet */
    public NSTextField usernameField; /* IBOutlet */
    public NSTextView logTextView; /* IBOutlet */
    public NSProgressIndicator progressIndicator; /* IBOutlet */
    public NSTextField statusLabel; /* IBOutlet */
    public NSTableView browserTable;  /* IBOutlet */
    public NSView connectedListView; /* IBOutlet */

    private CDConnectionController controller = this;
    
    public CDConnectionController() {
	super();
	log.debug("CDConnectionController");
    }

    public void awakeFromNib() {
	org.apache.log4j.BasicConfigurator.configure();

//	log.addAppender(statusLabel);
    }


    public void connect(NSObject sender) {
	log.debug("CDConnectionController:connect");
	Host host = null;
    //	Path b = new Path();
	//	if(sender instanceof NSMenu
	//NSMenuItem item = menu.getSelectedItem()
	//host = item.
	if(sender instanceof NSTextField) {
	    host = new Host(Host.SFTP, ((NSControl)sender).stringValue(), 22, null, null);
	}
	if(sender instanceof NSButton) {
	    NSApplication.sharedApplication().endSheet(connectionSheet, NSAlertPanel.AlternateReturn);
	    host = new Host(Host.SFTP, hostField.stringValue(), 22, usernameField.stringValue(), passwordField.stringValue());
	}
	mainWindow.setTitle(host.getHostname());
	connectedListView.addSubview(new CDConnectedItemView(host));
	connectedListView.setNeedsDisplay(true);
	Thread session = new Session(host);
	session.start();
	

 //                // Now try to write to a file without creating it!
 //SftpFile file = sftp.openFile("shinning.txt",
 //                            SftpSubsystemClient.OPEN_CREATE
 //                          | SftpSubsystemClient.OPEN_WRITE);


	/*
	Bookmark b = new Bookmark();
	b.setProtocol(Session.FTP);
	b.setTransferType(com.enterprisedt.net.ftp.FTPTransferType.ASCII);
	 b.setPort(Session.FTP_PORT);
	b.setHost(hostField.stringValue());
	b.setServerPath(pathField.stringValue());
	b.transfer();
 */
    }

    public void disconnect(NSObject sender) {
	log.debug("CDConnectionController:disconnect");
    }

    private class Session extends Thread {
	private Host host;
	
	public Session(Host host) {
	    this.host = host;
	}
	public void run() {
	    try {
		SshClient SSH = new SshClient();
		SSH.connect(hostField.stringValue(), new CDHostKeyVerification());
		PasswordAuthentication auth = new PasswordAuthentication();
		auth.setUsername(usernameField.stringValue());
		auth.setPassword(passwordField.stringValue());
		int result = SSH.authenticate(auth);
		if (result == AuthenticationProtocolState.COMPLETE) {
		    SessionChannelClient session = SSH.openSessionChannel();
		    SftpSubsystemClient SFTP = new SftpSubsystemClient();
		    session.startSubsystem(SFTP);
		    SftpFile workingDirectory = SFTP.openDirectory(".");
		    java.util.List children = new java.util.ArrayList();
		    CDBrowserTableDataSource browserTableDataSource = (CDBrowserTableDataSource)browserTable.dataSource();
		    int read = 1;
		    while(read > 0) {
			read = SFTP.listChildren(workingDirectory, children);
		    }
		    java.util.Iterator i = children.iterator();
		    browserTableDataSource.clear();
		    while(i.hasNext()) {
			browserTableDataSource.addEntry(i.next());
			browserTable.reloadData();
		    }

		}
		else {
		    NSApplication.sharedApplication().beginSheet(loginSheet, mainWindow, null,
						   new NSSelector(
			"loginSheetDidEnd",
			new Class[] { NSWindow.class, int.class, NSWindow.class }
			),// end selector
						   null);
		    log.info("Authentication failed.");
		}
	    }
	    catch(InvalidHostFileException e) {
		e.printStackTrace();
	    }
	    catch(java.io.IOException e) {
		e.printStackTrace();
	    }	    
	}

	public void loginSheetDidEnd(NSWindow sheet, int returncode, NSWindow main) {
	    sheet.close();
	}	
    }

    public void closeLoginSheet(NSObject sender) {
	// Ends a document modal session by specifying the sheet window, sheet. Also passes along a returnCode to the delegate.
	NSApplication.sharedApplication().endSheet(loginSheet, NSAlertPanel.AlternateReturn);
    }
        
    private class CDHostKeyVerification extends HostKeyVerification {
	private String host;
	private String fingerprint;
	
	private boolean done;

	public boolean isDone() {
	    return this.done;
	}

	public CDHostKeyVerification() throws InvalidHostFileException {
	    super();
	    log.debug("CDHostKeyVerification()");
	}

	public CDHostKeyVerification(String hostFile) throws InvalidHostFileException {
	    super(hostFile);
	}

	public void onDeniedHost(String hostname) {
	    log.debug("CDHostKeyVerification():onDeniedHost");
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
	    while(!this.isDone()) {
		try {
		    Thread.sleep(500); //milliseconds
		}
		catch(InterruptedException e) {
		    e.printStackTrace();
		}
	    }
	}
	
	public void onHostKeyMismatch(String host, String fingerprint, String actualHostKey) {
	    log.debug("CDHostKeyVerification():onHostKeyMismatch");
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
	    while(!this.isDone()) {
		try {
		    Thread.sleep(500); //milliseconds
		}
		catch(InterruptedException e) {
		    e.printStackTrace();
		}
	    }
	}


	public void onUnknownHost(String host, String fingerprint) {
	    log.debug("CDHostKeyVerification():onUnknownHost");
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
	    while(!this.isDone()) {
		try {
		    Thread.sleep(500); //milliseconds
		}
		catch(InterruptedException e) {
		    e.printStackTrace();
		}
	    }
	}


	public void deniedHostSheetDidEnd(NSWindow sheet, int returncode, NSWindow main) {
	    log.debug("CDHostKeyVerification():deniedHostSheetDidEnd");
	    sheet.close();
	    done = true;
	}

	public void keyMismatchSheetDidEnd(NSWindow sheet, int returncode, NSWindow main) {
	    log.debug("CDHostKeyVerification():keyMismatchSheetDidEnd");
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
	    log.debug("CDHostKeyVerification():unknownHostSheetDidEnd");
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
