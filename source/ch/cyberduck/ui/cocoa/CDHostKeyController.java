package ch.cyberduck.ui.cocoa;

/*
 *  Copyright (c) 2004 David Kocher. All rights reserved.
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

import com.sshtools.j2ssh.transport.AbstractKnownHostsKeyVerification;
import com.sshtools.j2ssh.transport.InvalidHostFileException;
import com.sshtools.j2ssh.transport.publickey.SshPublicKey;

import com.apple.cocoa.application.NSAlertPanel;
import com.apple.cocoa.application.NSWindow;
import com.apple.cocoa.foundation.NSBundle;
import com.apple.cocoa.foundation.NSMutableArray;
import com.apple.cocoa.foundation.NSNotification;
import com.apple.cocoa.foundation.NSSelector;

import org.apache.log4j.Logger;

import ch.cyberduck.core.Preferences;

/**
 * @version $Id$
 * Concrete Coccoa implementation of a SSH HostKeyVerification
 */
public class CDHostKeyController extends AbstractKnownHostsKeyVerification {
	private static Logger log = Logger.getLogger(CDHostKeyController.class);

	private static NSMutableArray instances = new NSMutableArray();

	private String host;
	private SshPublicKey publicKey;

	private CDWindowController windowController;

	public void windowWillClose(NSNotification notification) {
		instances.removeObject(this);
	}

	public CDHostKeyController(CDWindowController windowController) {
		this.windowController = windowController;
		try {
			this.setKnownHostFile(Preferences.instance().getProperty("ssh.knownhosts"));
		}
		catch(com.sshtools.j2ssh.transport.InvalidHostFileException e) {
			//This exception is thrown whenever an exception occurs open or reading from the host file.
			this.windowController.beginSheet(NSAlertPanel.criticalAlertPanel(NSBundle.localizedString("Error", ""), //title
			    NSBundle.localizedString("Could not open or read the host file", "")+": "+e.getMessage(), // message
			    NSBundle.localizedString("OK", ""), // defaultbutton
			    null, //alternative button
			    null //other button
			));
		}
		instances.addObject(this);
	}

	public void onHostKeyMismatch(final String host, final SshPublicKey allowedHostKey, final SshPublicKey actualHostKey) {
		log.debug("onHostKeyMismatch");
		this.host = host;
		this.publicKey = actualHostKey;
		NSWindow sheet = NSAlertPanel.criticalAlertPanel(NSBundle.localizedString("Host key mismatch:", "")+" "+host, //title
		    NSBundle.localizedString("The host key supplied is", "")+": "
		    +actualHostKey.getFingerprint()+
		    "\n"+NSBundle.localizedString("The current allowed key for this host is", "")+" : "
		    +allowedHostKey.getFingerprint()+"\n"+NSBundle.localizedString("Do you want to allow the host access?", ""),
		    NSBundle.localizedString("Allow", ""), // defaultbutton
		    NSBundle.localizedString("Deny", ""), //alternative button
		    isHostFileWriteable() ? NSBundle.localizedString("Always", "") : null //other button
		);
		this.windowController.beginSheet(sheet,
		    this, //delegate
		    new NSSelector
		        ("keyMismatchSheetDidEnd",
		            new Class[]
		            {
			            NSWindow.class, int.class, Object.class
		            }), // end selector
		    null);
		this.windowController.waitForSheetEnd();
	}

	public void keyMismatchSheetDidEnd(NSWindow sheet, int returncode, Object contextInfo) {
		log.debug("keyMismatchSheetDidEnd");
        sheet.orderOut(null);
		try {
			if(returncode == NSAlertPanel.DefaultReturn) {
				this.allowHost(host, publicKey, false);
			}
			if(returncode == NSAlertPanel.AlternateReturn) {
                log.info("Cannot continue without a valid host key");
			}
			if(returncode == NSAlertPanel.OtherReturn) {
				this.allowHost(host, publicKey, true); // always allow host
			}
		}
		catch(InvalidHostFileException e) {
			log.error(e.getMessage());
		}
        this.windowController.endSheet(sheet, returncode);
	}

	public void onUnknownHost(final String host,
	                          final SshPublicKey publicKey) {
		log.debug("onUnknownHost");
		this.host = host;
		this.publicKey = publicKey;
		NSWindow sheet = NSAlertPanel.criticalAlertPanel(NSBundle.localizedString("Unknown host key for", "")+" "+host, //title
		    NSBundle.localizedString("The host is currently unknown to the system. The host key fingerprint is", "")+": "+publicKey.getFingerprint()+".",
		    NSBundle.localizedString("Allow", ""), // defaultbutton
		    NSBundle.localizedString("Deny", ""), //alternative button
		    isHostFileWriteable() ? NSBundle.localizedString("Always", "") : null //other button
		);
		this.windowController.beginSheet(sheet,
		    this, //delegate
		    new NSSelector
		        ("unknownHostSheetDidEnd",
		            new Class[]
		            {
			            NSWindow.class, int.class, Object.class
		            }), // end selector
		    null);
		this.windowController.waitForSheetEnd();
	}

	public void unknownHostSheetDidEnd(NSWindow sheet, int returncode, Object contextInfo) {
		log.debug("unknownHostSheetDidEnd");
        sheet.orderOut(null);
		try {
			if(returncode == NSAlertPanel.DefaultReturn) {
				this.allowHost(host, publicKey, false); // allow host
			}
			if(returncode == NSAlertPanel.AlternateReturn) {
				log.info("Cannot continue without a valid host key");
			}
			if(returncode == NSAlertPanel.OtherReturn) {
				this.allowHost(host, publicKey, true); // always allow host
			}
		}
		catch(InvalidHostFileException e) {
			log.error(e.getMessage());
		}
        this.windowController.endSheet(sheet, returncode);
	}
}