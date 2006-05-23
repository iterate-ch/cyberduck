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

import com.apple.cocoa.application.NSApplication;
import com.apple.cocoa.application.NSButton;
import com.apple.cocoa.application.NSTextField;
import com.apple.cocoa.application.NSView;
import com.apple.cocoa.application.NSWindow;
import com.apple.cocoa.foundation.NSAttributedString;
import com.apple.cocoa.foundation.NSPoint;
import com.apple.cocoa.foundation.NSRect;
import com.apple.cocoa.foundation.NSSize;

import org.apache.log4j.Logger;

/**
 *
 */
public class CDErrorController extends CDWindowController {
    private static Logger log = Logger.getLogger(CDErrorController.class);

    public void awakeFromNib() {
        ;
    }

    public void setErrorField(NSTextField errorField) {
        this.errorField = errorField;
    }

    public void setView(NSView view) {
        this.view = view;
    }

    /**
     *
     */
    private NSTextField errorField;

    public NSView getView() {
        return view;
    }

    /**
     *
     */
    private NSView view;

    /**
     *
     */
    private CDBrowserController parent;

    /**
     * @param parent
     * @param message
     */
    public CDErrorController(CDBrowserController parent, String message) {
        this.parent = parent;
        if(!NSApplication.loadNibNamed("Error", this)) {
            log.fatal("Couldn't load Error.nib");
        }
        this.errorField.setAttributedStringValue(
                new NSAttributedString(message, CDTableCell.BOLD_FONT_HIGHLIGHTED));
    }

    /**
     * @param sender
     */
    public void close(NSButton sender) {
        // nstableview > nsclipview > nsscrollview > nsview
        NSView superview = parent.getSelectedBrowserView().superview().superview().superview();
        NSView subview = null;
        for(int i = 0; i < superview.subviews().count(); i++) {
            subview = (NSView) superview.subviews().objectAtIndex(i);
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
        superview.superview().setNeedsDisplay(true);
    }

    public void display() {
        NSView browser = parent.getSelectedBrowserView().superview().superview();
        browser.setFrameSize(new NSSize(
                new NSSize(browser.frame().width(), browser.frame().height() - this.getView().frame().size().height()))
        );
        this.getView().setFrame(new NSRect(
                new NSPoint(browser.frame().origin().x(), browser.frame().size().height()),
                new NSSize(browser.frame().size().width(), this.getView().frame().size().height())
        ));
        browser.superview().addSubview(this.getView(), NSWindow.Below, browser);
        browser.superview().superview().setNeedsDisplay(true);
    }
}
