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
import com.sshtools.j2ssh.transport.AbstractKnownHostsKeyVerification;
import com.sshtools.j2ssh.transport.InvalidHostFileException;
import com.sshtools.j2ssh.transport.publickey.SshPublicKey;

import ch.cyberduck.core.Preferences;

/**
* @version $Id$
 *          Concrete Coccoa implementation of a SSH HostKeyVerification
 */
public class CDHostKeyController extends AbstractKnownHostsKeyVerification {
	private static Logger log = Logger.getLogger(CDLoginController.class);
	
	private static NSMutableArray instances = new NSMutableArray();
	
	private String host;
	private SshPublicKey publicKey;
	
	private CDController windowController;
	
	public void windowWillClose(NSNotification notification) {
		instances.removeObject(this);
	}
	
	public synchronized void alertSheetDidClose(Object sender) {
		this.notify();
	}
	
	public CDHostKeyController(CDController windowController) throws InvalidHostFileException {
		super(Preferences.instance().getProperty("ssh.knownhosts"));
		this.windowController = windowController;
		instances.addObject(this);
	}
	
	public synchronized void onHostKeyMismatch(final String host, final SshPublicKey allowedHostKey, final SshPublicKey actualHostKey) {
		log.debug("onHostKeyMismatch");
		while(this.windowController.window().attachedSheet() != null) {
			try {
				log.debug("Sleeping...");
				this.wait();
			}
			catch(InterruptedException e) {
				log.error(e.getMessage());
			}
		}
		this.host = host;
		this.publicKey = actualHostKey;
		this.windowController.window().makeKeyAndOrderFront(null);
		NSAlertPanel.beginCriticalAlertSheet(NSBundle.localizedString("Host key mismatch:", "")+" "+host, //title
											 NSBundle.localizedString("Allow", ""), // defaultbutton
											 NSBundle.localizedString("Deny", ""), //alternative button
											 isHostFileWriteable() ? NSBundle.localizedString("Always", "") : null, //other button
											 this.windowController.window(),
											 CDHostKeyController.this, //delegate
											 new NSSelector
											 ("keyMismatchSheetDidClose",
											  new Class[]
											  {
												  NSWindow.class, int.class, Object.class
											  }), // end selector
											 null, // dismiss selector
											 null, // context
											 NSBundle.localizedString("The host key supplied is", "")+": "
											 +actualHostKey.getFingerprint()+
											 "\n"+NSBundle.localizedString("The current allowed key for this host is", "")+" : "
											 +allowedHostKey.getFingerprint()+"\n"+NSBundle.localizedString("Do you want to allow the host access?", ""));
		this.windowController.window().makeKeyAndOrderFront(null);
		while(this.windowController.window().attachedSheet() != null) {
			try {
				log.debug("Sleeping...");
				this.wait();
			}
			catch(InterruptedException e) {
				log.error(e.getMessage());
			}
		}
	}
	
	public synchronized void keyMismatchSheetDidClose(NSWindow sheet, int returncode, Object contextInfo) {
		log.debug("keyMismatchSheetDidClose");
		sheet.orderOut(null);
		try {
			if(returncode == NSAlertPanel.DefaultReturn) {
				this.allowHost(host, publicKey, false);
			}
			if(returncode == NSAlertPanel.AlternateReturn) {
				while(this.windowController.window().attachedSheet() != null) {
					try {
						log.debug("Sleeping...");
						this.wait();
					}
					catch(InterruptedException e) {
						log.error(e.getMessage());
					}
				}
				this.windowController.window().makeKeyAndOrderFront(null);
				NSAlertPanel.beginCriticalAlertSheet(NSBundle.localizedString("Invalid host key", "Alert sheet title"), //title
													 NSBundle.localizedString("OK", ""), // defaultbutton
													 null, //alternative button
													 null, //other button
													 this.windowController.window(), //window
													 null, //delegate
													 new NSSelector("alertSheetDidClose", new Class[]{Object.class}), //didEndSelector
													 null, //dismiss selector
													 null, //context
													 NSBundle.localizedString("Cannot continue without a valid host key.", ""));
				log.info("Cannot continue without a valid host key");
			}
			if(returncode == NSAlertPanel.OtherReturn) {
				this.allowHost(host, publicKey, true); // always allow host
			}
		}
		catch(InvalidHostFileException e) {
			log.error(e.getMessage());
		}
		this.notify();
	}
	
	public synchronized void onUnknownHost(final String host,
	                                       final SshPublicKey publicKey) {
		log.debug("onUnknownHost");
		while(this.windowController.window().attachedSheet() != null) {
			try {
				log.debug("Sleeping...");
				this.wait();
			}
			catch(InterruptedException e) {
				log.error(e.getMessage());
			}
		}
		this.host = host;
		this.publicKey = publicKey;
		this.windowController.window().makeKeyAndOrderFront(null);
		NSAlertPanel.beginInformationalAlertSheet(NSBundle.localizedString("Unknown host key for", "")+" "+host, //title
												  NSBundle.localizedString("Allow", ""), // defaultbutton
												  NSBundle.localizedString("Deny", ""), //alternative button
												  isHostFileWriteable() ? NSBundle.localizedString("Always", "") : null, //other button
												  this.windowController.window(), //window
												  CDHostKeyController.this, //delegate
												  new NSSelector
												  ("unknownHostSheetDidClose",
												   new Class[]
												   {
													   NSWindow.class, int.class, Object.class
												   }), // end selector
												  null, // dismiss selector
												  null, // context
												  NSBundle.localizedString("The host is currently unknown to the system. The host key fingerprint is", "")+": "+publicKey.getFingerprint()+".");
		this.windowController.window().makeKeyAndOrderFront(null);
		while(this.windowController.window().attachedSheet() != null) {
			try {
				log.debug("Sleeping...");
				this.wait();
			}
			catch(InterruptedException e) {
				log.error(e.getMessage());
			}
		}
		this.notify();
	}
	
	public synchronized void unknownHostSheetDidClose(NSWindow sheet, int returncode, Object contextInfo) {
		log.debug("unknownHostSheetDidClose");
		sheet.orderOut(null);
		try {
			if(returncode == NSAlertPanel.DefaultReturn) {
				this.allowHost(host, publicKey, false); // allow host
			}
			if(returncode == NSAlertPanel.AlternateReturn) {
				while(this.windowController.window().attachedSheet() != null) {
					try {
						log.debug("Sleeping...");
						this.wait();
					}
					catch(InterruptedException e) {
						log.error(e.getMessage());
					}
				}
				this.windowController.window().makeKeyAndOrderFront(null);
				NSAlertPanel.beginCriticalAlertSheet(NSBundle.localizedString("Invalid host key", ""), //title
													 NSBundle.localizedString("OK", ""), // defaultbutton
													 null, //alternative button
													 null, //other button
													 this.windowController.window(),
													 this, //delegate
													 new NSSelector("alertSheetDidClose", new Class[]{Object.class}), //didEndSelector
													 null, // dismiss selector
													 null, // context
													 NSBundle.localizedString("Cannot continue without a valid host key.", "") // message
													 );
				log.info("Cannot continue without a valid host key");
			}
			if(returncode == NSAlertPanel.OtherReturn) {
				this.allowHost(host, publicKey, true); // always allow host
			}
		}
		catch(InvalidHostFileException e) {
			log.error(e.getMessage());
		}
		this.notify();
	}
}