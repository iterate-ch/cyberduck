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

import com.apple.cocoa.application.NSApplication;
import com.apple.cocoa.application.NSButton;
import com.apple.cocoa.application.NSTextField;
import com.apple.cocoa.application.NSView;
import com.apple.cocoa.application.NSWindow;
import com.apple.cocoa.foundation.*;

import org.apache.log4j.Logger;

import java.io.IOException;
import java.net.SocketException;
import java.util.Enumeration;

/**
 * @version $Id$
 */
public class CDErrorController extends CDController {
    private static Logger log = Logger.getLogger(CDErrorController.class);

    /**
     *
     */
    private NSTextField errorField;

    public void setErrorField(NSTextField errorField) {
        this.errorField = errorField;
    }

    public NSView getView() {
        return view;
    }

    private NSButton alertIcon; // IBOutlet

    public void setAlertIcon(NSButton alertIcon) {
        this.alertIcon = alertIcon;
        this.alertIcon.setTarget(this);
        this.alertIcon.setAction(new NSSelector("launchNetworkAssistant", new Class[]{NSButton.class}));
    }

    public void launchNetworkAssistant(final NSButton sender) {
        this.host.diagnose();
    }

    /**
     *
     */
    private NSView view;

    public void setView(NSView view) {
        this.view = view;
    }

    /**
     * The parent view
     */
    private NSView container;

    /**
     * The neighbouring view the error view should be attached to
     */
    private NSView neighbour;

    private Host host;

    /**
     * @param container
     * @param neighbour
     * @param e
     * @param host
     */
    public CDErrorController(NSView container, NSView neighbour, Exception e, Host host) {
        this.container = container;
        this.neighbour = neighbour;
        if(!NSApplication.loadNibNamed("Error", this)) {
            log.fatal("Couldn't load Error.nib");
        }
        String alert = e.getMessage();
        String title = NSBundle.localizedString("Error", "");
        if(e instanceof FTPException) {
            title = "FTP " + NSBundle.localizedString("Error", "");
        }
        else if(e instanceof SshException) {
            title = "SSH " + NSBundle.localizedString("Error", "");
        }
        else if(e instanceof SocketException) {
            title = "Network " + NSBundle.localizedString("Error", "");
        }
        else if(e instanceof IOException) {
            title = "I/O " + NSBundle.localizedString("Error", "");
        }
        this.errorField.setAttributedStringValue(
                new NSAttributedString(title + ": " + alert, CDTableCell.BOLD_FONT_HIGHLIGHTED));
        this.host = host;
    }

    /**
     * @param sender
     */
    public void close(NSButton sender) {
        NSView subview = null;
        Enumeration iter = container.subviews().objectEnumerator();
        while(iter.hasMoreElements()) {
            subview = (NSView) iter.nextElement();
            if(subview.frame().origin().y() < view.frame().origin().y()) {
                subview.setFrameOrigin(
                        new NSPoint(subview.frame().origin().x(),
                                subview.frame().origin().y() + this.getView().frame().size().height())
                );
            }
        }
        if(subview != null) {
            // Resize the last component; usually the browser view to fit the window
            subview.setFrame(new NSRect(
                    subview.frame().origin().x(),
                    subview.frame().origin().y() - this.getView().frame().size().height(),
                    subview.frame().size().width(),
                    subview.frame().size().height() + this.getView().frame().size().height())
            );
        }
        view.removeFromSuperview();
        container.setNeedsDisplay(true);
        NSWindow window = this.window();
        window.setContentMinSize(
                new NSSize(window.contentMinSize().width(), window.contentMinSize().height() - view.frame().height()));
        this.invalidate();
    }

    /**
     *
     */
    public void display() {
        NSWindow window = this.window();
        if(neighbour.frame().height() < window.minSize().height()) {
            NSRect frame = new NSRect(window.frame().origin(),
                    new NSSize(window.frame().width(), window.frame().height() + view.frame().height()));
            window.setFrame(frame, true, true);
        }
        window.setContentMinSize(
                new NSSize(window.contentMinSize().width(), window.contentMinSize().height() + view.frame().height()));
        neighbour.setFrameSize(new NSSize(
                new NSSize(neighbour.frame().width(), neighbour.frame().height() - this.getView().frame().size().height()))
        );
        this.getView().setFrame(new NSRect(
                new NSPoint(neighbour.frame().origin().x(), neighbour.frame().size().height()),
                new NSSize(neighbour.frame().size().width(), this.getView().frame().size().height())
        ));
        container.addSubview(this.getView(), NSWindow.Below, neighbour);
        container.setNeedsDisplay(true);
    }

    /**
     * @return The parent window
     */
    private NSWindow window() {
        NSWindow window = null;
        NSView parent = container;
        while(null == (window = parent.window())) {
            parent = parent.superview();
        }
        return window;
    }
}
