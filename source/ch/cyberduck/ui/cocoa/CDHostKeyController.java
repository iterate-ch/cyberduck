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
//import com.sshtools.j2ssh.transport.AbstractHostKeyVerification;
import com.sshtools.j2ssh.transport.AbstractKnownHostsKeyVerification;
import com.sshtools.j2ssh.transport.InvalidHostFileException;
import com.sshtools.j2ssh.transport.publickey.SshPublicKey;
import org.apache.log4j.Logger;

import ch.cyberduck.core.Preferences;

/**
* @version $Id$
 * Concrete Coccoa implementation of a SSH HostKeyVerification
 */
public class CDHostKeyController extends AbstractKnownHostsKeyVerification {
    private static Logger log = Logger.getLogger(CDLoginController.class);

    private String host;
    private SshPublicKey publicKey;
    private boolean done;
    private NSWindow mainWindow;

    public CDHostKeyController(NSWindow mainWindow) throws InvalidHostFileException {
	super(CDPreferencesImpl.instance().getProperty("sshtools.home"));
	this.mainWindow = mainWindow;
    }

//    public CDHostKeyController(NSWindow mainWindow, String hostFile) throws InvalidHostFileException {
//	super(hostFile);
//	this.mainWindow = mainWindow;
  //  }
    

//    public void onDeniedHost(String hostname) {
//	log.info("onDeniedHost");
//	NSAlertPanel.beginCriticalAlertSheet(
//					   "Access denied", //title
//					   "OK",// defaultbutton
//					   null,//alternative button
//					   null,//other button
//					   mainWindow,
//					   this, //delegate
//					   new NSSelector
//					   (
//	 "deniedHostSheetDidEnd",
//	 new Class[]
//	 {
//	     NSWindow.class, int.class, NSWindow.class
//	 }
//	 ),// end selector
//					   null, // dismiss selector
//					   null, // context
//					   "Access to the host " + hostname + " is denied from this system" // message
//					   );
//	while(!this.done) {
//	    try {
//		Thread.sleep(500); //milliseconds
//	    }
//	    catch(InterruptedException e) {
//		log.error(e.getMessage());
//	    }
//	}
//  }

    public void onHostKeyMismatch(String host, SshPublicKey allowedHostKey, SshPublicKey actualHostKey) {
	log.info("onHostKeyMismatch");
	this.host = host;
	this.publicKey = actualHostKey;
	NSAlertPanel.beginCriticalAlertSheet(
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
					   null, // context
					   "The host key supplied by " + host + " is: "
					   + actualHostKey.getFingerprint() +
					   "\nThe current allowed key for is: "
					   + allowedHostKey.getFingerprint() +"\nDo you want to allow the host access?");
	while(!this.done) {
	    try {
		Thread.sleep(500); //milliseconds
	    }
	    catch(InterruptedException e) {
		log.error(e.getMessage());
	    }
	}
    }


    public void onUnknownHost(String host, SshPublicKey publicKey) {
	log.info("onUnknownHost");
	this.host = host;
	this.publicKey = publicKey;
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
					   null, // context
					   "The host " + host
					   + " is currently unknown to the system. The host key fingerprint is: " + publicKey.getFingerprint()+".");
	while(!this.done) {
	    try {
		Thread.sleep(500); //milliseconds
	    }
	    catch(InterruptedException e) {
		log.error(e.getMessage());
	    }
	}
    }


//    public void deniedHostSheetDidEnd(NSWindow sheet, int returncode, NSWindow main) {
//	log.info("deniedHostSheetDidEnd");
//	sheet.orderOut(null);
//	done = true;
//  }

    public void keyMismatchSheetDidEnd(NSWindow sheet, int returncode, NSWindow main) {
	log.info("keyMismatchSheetDidEnd");
	sheet.orderOut(null);
	try {
	    if(returncode == NSAlertPanel.DefaultReturn)
		allowHost(host, publicKey, false);
	    if(returncode == NSAlertPanel.AlternateReturn) {
		NSAlertPanel.beginCriticalAlertSheet(
					    "Invalid host key", //title
					    "OK",// defaultbutton
					    null,//alternative button
					    null,//other button
					    mainWindow,
					    this, //delegate
					    null,// end selector
					    null, // dismiss selector
					    null, // context
					    "Cannot continue without a valid host key." // message
					    );
		log.info("Cannot continue without a valid host key");
	    }
	    if(returncode == NSAlertPanel.OtherReturn) {
		allowHost(host, publicKey, true); // always allow host
	    }
	    done = true;
	}
	catch(InvalidHostFileException e) {
	    log.error(e.getMessage());
	}
    }

    public void unknownHostSheetDidEnd(NSWindow sheet, int returncode, NSWindow main) {
	log.info("unknownHostSheetDidEnd");
	sheet.orderOut(null);
	try {
	    if(returncode == NSAlertPanel.DefaultReturn)
		allowHost(host, publicKey, false); // allow host
	    if(returncode == NSAlertPanel.AlternateReturn) {
		NSAlertPanel.beginCriticalAlertSheet(
					    "Invalid host key", //title
					    "OK",// defaultbutton
					    null,//alternative button
					    null,//other button
					    mainWindow,
					    this, //delegate
					    null,// end selector
					    null, // dismiss selector
					    null, // context
					    "Cannot continue without a valid host key." // message
					    );
		log.info("Cannot continue without a valid host key");
	    }
	    if(returncode == NSAlertPanel.OtherReturn)
		allowHost(host, publicKey, true); // always allow host
	    done = true;
	}
	catch(InvalidHostFileException e) {
	    log.error(e.getMessage());
	}
    }
}