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

import com.apple.cocoa.application.NSAlertPanel;
import com.apple.cocoa.application.NSWindow;
import com.apple.cocoa.foundation.NSBundle;
import com.apple.cocoa.foundation.NSMutableArray;
import com.apple.cocoa.foundation.NSNotification;
import com.apple.cocoa.foundation.NSSelector;

import org.apache.log4j.Logger;

import ch.cyberduck.core.Preferences;
import com.sshtools.j2ssh.transport.AbstractKnownHostsKeyVerification;
import com.sshtools.j2ssh.transport.InvalidHostFileException;
import com.sshtools.j2ssh.transport.publickey.SshPublicKey;

/**
 * @version $Id$
 *          Concrete Coccoa implementation of a SSH HostKeyVerification
 */
public class CDHostKeyController extends AbstractKnownHostsKeyVerification {
    private static Logger log = Logger.getLogger(CDLoginController.class);

    private static NSMutableArray instances = new NSMutableArray();

    private String host;
    private SshPublicKey publicKey;

	private Controller windowController;
	
    public void windowWillClose(NSNotification notification) {
        instances.removeObject(this);
    }

    public CDHostKeyController(Controller windowController) throws InvalidHostFileException {
        super(Preferences.instance().getProperty("ssh.knownhosts"));
        this.windowController = windowController;
        instances.addObject(this);
    }

    public void onHostKeyMismatch(final String host, 
								  final SshPublicKey allowedHostKey, 
								  final SshPublicKey actualHostKey) {
        log.debug("onHostKeyMismatch");
		if(null == windowController.window() || null == windowController.window().delegate()) {
			log.error("Parent window or its delegate is null; cannot begin sheet!");
			try {
				if(NSAlertPanel.DefaultReturn == NSAlertPanel.runInformationalAlert(NSBundle.localizedString("Host key mismatch", "Alert sheet title"), //title
												   NSBundle.localizedString("The host key supplied is", "") + ": "
												   + actualHostKey.getFingerprint() +
												   "\n" + NSBundle.localizedString("The current allowed key for this host is", "") + " : "
												   + allowedHostKey.getFingerprint() + "\n" + NSBundle.localizedString("Do you want to allow the host access?", ""),
												   NSBundle.localizedString("Allow"), // defaultbutton
												   NSBundle.localizedString("Deny"), //alternative button
												   null//other button
																				   )) {
					this.allowHost(host, publicKey, false);
				}
			}
			catch (InvalidHostFileException e) {
				log.error(e.getMessage());
			}
			return;
		}
        while (windowController.window().attachedSheet() != null) {
            try {
                log.debug("----------  Waiting for attached sheet to be closed first...");
                Thread.sleep(1000); //milliseconds
            }
            catch (InterruptedException e) {
                log.error(e.getMessage());
            }
        }
        this.host = host;
        this.publicKey = actualHostKey;
		NSAlertPanel.beginCriticalAlertSheet(NSBundle.localizedString("Host key mismatch:", "") + " " + host, //title
											 NSBundle.localizedString("Allow", ""), // defaultbutton
											 NSBundle.localizedString("Deny", ""), //alternative button
											 isHostFileWriteable() ? NSBundle.localizedString("Always", "") : null, //other button
											 windowController.window(),
											 CDHostKeyController.this, //delegate
											 new NSSelector
											 ("keyMismatchSheetDidEnd",
											  new Class[]
											  {
												  NSWindow.class, int.class, Object.class
											  }), // end selector
											 null, // dismiss selector
											 null, // context
											 NSBundle.localizedString("The host key supplied is", "") + ": "
											 + actualHostKey.getFingerprint() +
											 "\n" + NSBundle.localizedString("The current allowed key for this host is", "") + " : "
											 + allowedHostKey.getFingerprint() + "\n" + NSBundle.localizedString("Do you want to allow the host access?", ""));
        while (windowController.window().attachedSheet() != null) {
            try {
                log.debug("Sleeping...");
                Thread.sleep(1000); //milliseconds
            }
            catch (InterruptedException e) {
                log.error(e.getMessage());
            }
        }
    }

    public void keyMismatchSheetDidEnd(NSWindow sheet, int returncode, Object contextInfo) {
        log.debug("keyMismatchSheetDidEnd");
        sheet.orderOut(null);
        try {
            if (returncode == NSAlertPanel.DefaultReturn) {
                this.allowHost(host, publicKey, false);
            }
            if (returncode == NSAlertPanel.AlternateReturn) {
                NSAlertPanel.beginCriticalAlertSheet(NSBundle.localizedString("Invalid host key", ""), //title
													 "OK", // defaultbutton
													 null, //alternative button
													 null, //other button
													 windowController.window(),
													 this, //delegate
													 null, // end selector
													 null, // dismiss selector
													 null, // context
													 NSBundle.localizedString("Cannot continue without a valid host key.", "") // message
													 );
                log.info("Cannot continue without a valid host key");
            }
            if (returncode == NSAlertPanel.OtherReturn) {
                this.allowHost(host, publicKey, true); // always allow host
            }
        }
        catch (InvalidHostFileException e) {
            log.error(e.getMessage());
        }
    }

	public void onUnknownHost(final String host, 
							  final SshPublicKey publicKey ) {
        log.debug("onUnknownHost");
        this.host = host;
        this.publicKey = publicKey;
		if(null == windowController.window() || null == windowController.window().delegate()) {
			log.error("Parent window or its delegate is null; cannot begin sheet!");
			try {
				if(NSAlertPanel.DefaultReturn == NSAlertPanel.runInformationalAlert(NSBundle.localizedString("Host key mismatch", "Alert sheet title"), //title
																				   NSBundle.localizedString("The host is currently unknown to the system. The host key fingerprint is", "") + ": " + publicKey.getFingerprint() + ".",
																				   NSBundle.localizedString("Allow"), // defaultbutton
																				   NSBundle.localizedString("Deny"), //alternative button
																				   null//other button
																				   )) {
					this.allowHost(host, publicKey, false);
				}
			}
			catch (InvalidHostFileException e) {
				log.error(e.getMessage());
			}
			return;
		}
        while (windowController.window().attachedSheet() != null) {
            try {
                log.debug("----------  Waiting for attached sheet to be closed first...");
                Thread.sleep(1000); //milliseconds
            }
            catch (InterruptedException e) {
                log.error(e.getMessage());
            }
        }
		NSAlertPanel.beginInformationalAlertSheet(NSBundle.localizedString("Unknown host key for", "") + " " + host, //title
												  NSBundle.localizedString("Allow", ""), // defaultbutton
												  NSBundle.localizedString("Deny", ""), //alternative button
												  isHostFileWriteable() ? NSBundle.localizedString("Always", "") : null, //other button
												  windowController.window(), //window
												  CDHostKeyController.this, //delegate
												  new NSSelector
												  ("unknownHostSheetDidEnd",
												   new Class[]
												   {
													   NSWindow.class, int.class, Object.class
												   }), // end selector
												  null, // dismiss selector
												  null, // context
												  NSBundle.localizedString("The host is currently unknown to the system. The host key fingerprint is", "") + ": " + publicKey.getFingerprint() + ".");
        while (windowController.window().attachedSheet() != null) {
            try {
                log.debug("Sleeping...");
                Thread.sleep(1000); //milliseconds
            }
            catch (InterruptedException e) {
                log.error(e.getMessage());
            }
        }
    }

    public void unknownHostSheetDidEnd(NSWindow sheet, int returncode, Object contextInfo) {
        log.debug("unknownHostSheetDidEnd");
        sheet.orderOut(null);
        try {
            if (returncode == NSAlertPanel.DefaultReturn) {
                this.allowHost(host, publicKey, false); // allow host
            }
            if (returncode == NSAlertPanel.AlternateReturn) {
                NSAlertPanel.beginCriticalAlertSheet(NSBundle.localizedString("Invalid host key", ""), //title
                        NSBundle.localizedString("OK", ""), // defaultbutton
                        null, //alternative button
                        null, //other button
                        windowController.window(),
                        CDHostKeyController.this, //delegate
                        null, // end selector
                        null, // dismiss selector
                        null, // context
                        NSBundle.localizedString("Cannot continue without a valid host key.", "") // message
                );
                log.info("Cannot continue without a valid host key");
            }
            if (returncode == NSAlertPanel.OtherReturn) {
                this.allowHost(host, publicKey, true); // always allow host
            }
//            done = true;
        }
        catch (InvalidHostFileException e) {
            log.error(e.getMessage());
        }
    }
}