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

import com.apple.cocoa.application.*;
import com.apple.cocoa.foundation.*;
import com.sshtools.j2ssh.transport.AbstractHostKeyVerification;
import com.sshtools.j2ssh.transport.InvalidHostFileException;
import org.apache.log4j.Logger;

/**
* @version $Id$
 * Concrete Coccoa implementation of a SSH HostKeyVerification
 */
public class CDHostKeyController extends AbstractHostKeyVerification {
    private static Logger log = Logger.getLogger(CDLoginController.class);

    private String host;
    private String fingerprint;
    private boolean done;
    private NSWindow mainWindow;

    public CDHostKeyController(NSWindow mainWindow) throws InvalidHostFileException {
	super();
	this.mainWindow = mainWindow;
    }

//    public CDHostKeyController(NSWindow mainWindow, String hostFile) throws InvalidHostFileException {
//	super(hostFile);
//	this.mainWindow = mainWindow;
  //  }
    

    public void onDeniedHost(String hostname) {
	log.debug("onDeniedHost");
	NSAlertPanel.beginCriticalAlertSheet(
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
		log.error(e.getMessage());
	    }
	}
    }

    public void onHostKeyMismatch(String host, String fingerprint, String actualHostKey) {
	log.debug("onHostKeyMismatch");
	this.host = host;
	this.fingerprint = fingerprint;
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
					   this, // context
					   "The host key supplied by " + host + " is: "
					   + actualHostKey +
					   "\nThe current allowed key for " + host + " is: "
					   + fingerprint +"\nDo you want to allow the host access?");
	while(!this.done) {
	    try {
		Thread.sleep(500); //milliseconds
	    }
	    catch(InterruptedException e) {
		log.error(e.getMessage());
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
		log.error(e.getMessage());
	    }
	}
    }


    public void deniedHostSheetDidEnd(NSWindow sheet, int returncode, NSWindow main) {
	log.debug("deniedHostSheetDidEnd");
	sheet.orderOut(null);
	done = true;
    }

    public void keyMismatchSheetDidEnd(NSWindow sheet, int returncode, NSWindow main) {
	log.debug("keyMismatchSheetDidEnd");
	sheet.orderOut(null);
	try {
	    if(returncode == NSAlertPanel.DefaultReturn)
		allowHost(host, fingerprint, false);
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
					    this, // context
					    "Cannot continue without a valid host key." // message
					    );
		log.info("Cannot continue without a valid host key");
	    }
	    if(returncode == NSAlertPanel.OtherReturn) {
		allowHost(host, fingerprint, true); // always allow host
	    }
	    done = true;
	}
	catch(InvalidHostFileException e) {
	    log.error(e.getMessage());
	}
    }

    public void unknownHostSheetDidEnd(NSWindow sheet, int returncode, NSWindow main) {
	log.debug("unknownHostSheetDidEnd");
	sheet.orderOut(null);
	try {
	    if(returncode == NSAlertPanel.DefaultReturn)
		allowHost(host, fingerprint, false); // allow host
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
	    log.error(e.getMessage());
	}
    }
}
