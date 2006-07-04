package ch.cyberduck.ui.cocoa;

/*
 *  Copyright (c) 2005 David Kocher. All rights reserved.
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

import com.enterprisedt.net.ftp.FTPException;
import com.sshtools.j2ssh.SshException;

import ch.cyberduck.core.Host;

import com.apple.cocoa.application.*;
import com.apple.cocoa.foundation.*;

import org.apache.log4j.Logger;

import java.io.IOException;
import java.net.SocketException;
import java.util.Enumeration;

/**
 * @version $Id$
 */
public abstract class CDErrorController extends CDController {
    protected static Logger log = Logger.getLogger(CDErrorController.class);

    /**
     * The parent view
     */
    protected NSView container;

    protected Host host;

    protected Exception failure;

    /**
     * @param container
     * @param failure
     * @param host
     */
    public CDErrorController(NSView container, Exception failure, Host host) {
        this.container = container;
        this.host = host;
        this.failure = failure;
    }

    protected String getErrorText() {
        String alert = failure.getMessage();
        String title = NSBundle.localizedString("Error", "");
        if(failure instanceof FTPException) {
            title = "FTP " + NSBundle.localizedString("Error", "");
        }
        else if(failure instanceof SshException) {
            title = "SSH " + NSBundle.localizedString("Error", "");
        }
        else if(failure instanceof SocketException) {
            title = "Network " + NSBundle.localizedString("Error", "");
        }
        else if(failure instanceof IOException) {
            title = "I/O " + NSBundle.localizedString("Error", "");
        }
        return title + ": " + alert;
    }

    public void setHighlighted(boolean highlighted) {
        this.errorField.setTextColor(highlighted ? NSColor.whiteColor() : NSColor.blackColor());
    }

    /**
     * Called before the view is removed from the parent view
     */
    protected abstract void viewWillClose();

    private void close() {
        if(null == view.superview()) {
            return; //as the view has already been removed from its superview
        }
        NSView subview = null;
        Enumeration iter = container.subviews().objectEnumerator();
        while(iter.hasMoreElements()) {
            subview = (NSView) iter.nextElement();
            if(subview.frame().origin().y() < view.frame().origin().y()) {
                subview.setFrameOrigin(
                        new NSPoint(subview.frame().origin().x(),
                                subview.frame().origin().y() + view.frame().size().height())
                );
            }
        }
        this.viewWillClose();
        view.removeFromSuperview();
        this.viewDidClose();
        container.setNeedsDisplay(true);
        this.invalidate();
    }

    /**
     * Called after the view has been removed from the parent view
     */
    protected abstract void viewDidClose();

    /**
     * @param sender
     */
    public void close(NSButton sender) {
        this.close();
    }

    public abstract void display();

    /**
     * @return The parent window
     */
    protected NSWindow window() {
        NSWindow window = null;
        NSView parent = container;
        while(null == (window = parent.window())) {
            parent = parent.superview();
        }
        return window;
    }

    /**
     *
     */
    protected NSTextField errorField; //IBOutlet

    public void setErrorField(NSTextField errorField) {
        this.errorField = errorField;
    }

    protected NSButton alertIcon; //IBOutlet

    public void setAlertIcon(NSButton alertIcon) {
        this.alertIcon = alertIcon;
        this.alertIcon.setTarget(this);
        this.alertIcon.setAction(new NSSelector("launchNetworkAssistant", new Class[]{NSButton.class}));
    }

    /**
     * Run the network diagnostics assistant
     * @param sender
     */
    public void launchNetworkAssistant(final NSButton sender) {
        this.host.diagnose();
    }

    /**
     *
     */
    protected NSView view; //IBOutlet

    public void setView(NSView view) {
        this.view = view;
    }
}
