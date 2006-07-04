package ch.cyberduck.ui.cocoa;

/*
 *  Copyright (c) 2006 David Kocher. All rights reserved.
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

import ch.cyberduck.core.Host;

import com.apple.cocoa.application.NSView;
import com.apple.cocoa.application.NSWindow;
import com.apple.cocoa.application.NSApplication;
import com.apple.cocoa.foundation.NSRect;
import com.apple.cocoa.foundation.NSSize;
import com.apple.cocoa.foundation.NSPoint;
import com.apple.cocoa.foundation.NSAttributedString;

/**
 * @version $Id$
 */
public class CDBrowserErrorController extends CDErrorController {

    /**
     * The neighbouring view the error view should be attached to
     */
    private NSView neighbour;


    /**
     * @param container
     * @param neighbour
     * @param failure
     * @param host
     */
    public CDBrowserErrorController(NSView container, NSView neighbour, Exception failure, Host host) {
        super(container, failure, host);
        this.neighbour = neighbour;
        if(!NSApplication.loadNibNamed("BrowserError", this)) {
            log.fatal("Couldn't load BrowserError.nib");
        }
    }

    protected void viewWillClose() {
        // Resize the last component; usually the browser view to fit the window
        neighbour.setFrame(new NSRect(
                neighbour.frame().origin().x(),
                neighbour.frame().origin().y() - view.frame().size().height(),
                neighbour.frame().size().width(),
                neighbour.frame().size().height() + view.frame().size().height())
        );
    }

    public void display() {
        this.errorField.setAttributedStringValue(new NSAttributedString(this.getErrorText()));
        NSWindow window = this.window();
        if(neighbour.frame().height() < window.minSize().height()) {
            NSRect frame = new NSRect(window.frame().origin(),
                    new NSSize(window.frame().width(), window.frame().height() + view.frame().height()));
            window.setFrame(frame, true, true);
        }
        window.setContentMinSize(
                new NSSize(window.contentMinSize().width(), window.contentMinSize().height() + view.frame().height()));
        neighbour.setFrameSize(
                new NSSize(neighbour.frame().width(), neighbour.frame().height() - view.frame().size().height())
        );
        view.setFrame(new NSRect(
                new NSPoint(neighbour.frame().origin().x(), neighbour.frame().size().height()),
                new NSSize(neighbour.frame().size().width(), view.frame().size().height())
        ));
        container.addSubview(view, NSWindow.Below, neighbour);
        container.setNeedsDisplay(true);
    }

    protected void viewDidClose() {
        NSWindow window = this.window();
        window.setContentMinSize(
                new NSSize(window.contentMinSize().width(), window.contentMinSize().height() - view.frame().height()));
    }
}
