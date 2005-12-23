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

import com.apple.cocoa.application.*;
import com.apple.cocoa.foundation.*;

import org.apache.log4j.Logger;

/**
 * @version $Id$
 */
public abstract class CDWindowController extends CDController {
    protected static Logger log = Logger.getLogger(CDWindowController.class);

    private static NSMutableParagraphStyle lineBreakByTruncatingMiddleParagraph = new NSMutableParagraphStyle();

    static {
        lineBreakByTruncatingMiddleParagraph.setLineBreakMode(NSParagraphStyle.LineBreakByTruncatingMiddle);
    }

    protected static final NSDictionary TRUNCATE_MIDDLE_PARAGRAPH_DICTIONARY = new NSDictionary(
            new Object[]{lineBreakByTruncatingMiddleParagraph},
            new Object[]{NSAttributedString.ParagraphStyleAttributeName});

    protected NSWindow window; // IBOutlet

    protected void post(NSTimer timer) {
        if(null == this.window) {
            return;
        }
        super.post(timer);
    }

    public void setWindow(NSWindow window) {
        this.window = window;
        (NSNotificationCenter.defaultCenter()).addObserver(this,
                new NSSelector("windowWillClose", new Class[]{NSNotification.class}),
                NSWindow.WindowWillCloseNotification,
                this.window);
        this.window.setReleasedWhenClosed(true);
    }

    public NSWindow window() {
        return this.window;
    }

    public boolean windowShouldClose(NSWindow sender) {
        return true;
    }

    public void windowWillClose(NSNotification notification) {
        log.debug("windowWillClose:"+notification);
        this.invalidate();
    }

    protected void invalidate() {
        this.window = null;
        super.invalidate();
    }

    public void cascade() {
        NSArray windows = NSApplication.sharedApplication().windows();
        int count = windows.count();
        if (count != 0) {
            while (0 != count--) {
                NSWindow window = (NSWindow) windows.objectAtIndex(count);
                NSPoint origin = window.frame().origin();
                origin = new NSPoint(origin.x(), origin.y() + window.frame().size().height());
                this.window.setFrameTopLeftPoint(this.window.cascadeTopLeftFromPoint(origin));
                break;
            }
        }
    }

    public boolean hasSheet() {
        return this.window.attachedSheet() != null;
    }
}