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
public abstract class CDWindowController extends CDController
{
    protected static Logger log = Logger.getLogger(CDWindowController.class);

    public CDWindowController() {
        super();
    }

    /**
     * Called by the runtime after the NIB file has been loaded sucessfully
     */
    public abstract void awakeFromNib();

    private static NSMutableParagraphStyle lineBreakByTruncatingMiddleParagraph = new NSMutableParagraphStyle();

    static {
        lineBreakByTruncatingMiddleParagraph.setLineBreakMode(NSParagraphStyle.LineBreakByTruncatingMiddle);
    }

    protected static final NSDictionary TRUNCATE_MIDDLE_PARAGRAPH_DICTIONARY = new NSDictionary(
            new Object[]{lineBreakByTruncatingMiddleParagraph},
            new Object[]{NSAttributedString.ParagraphStyleAttributeName});

    /**
     * The window this controller is owner of
     */
    protected NSWindow window; // IBOutlet

    protected void post(NSTimer timer) {
        if(null == this.window) {
            //We override this because until the the timer fires in the event queue, the window may have become invalid
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

    /**
     * Override this method if the controller should not be invalidated after its window closes
     * @param notification
     */
    public void windowWillClose(NSNotification notification) {
        log.debug("windowWillClose:"+notification);
        //If the window is closed it is assumed the controller object is no longer used
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
            NSWindow window = (NSWindow) windows.objectAtIndex(count-1);
            NSPoint origin = window.frame().origin();
            origin = new NSPoint(origin.x(), origin.y() + window.frame().size().height());
            this.window.setFrameTopLeftPoint(this.window.cascadeTopLeftFromPoint(origin));
        }
    }

    public boolean hasSheet() {
        return this.window.attachedSheet() != null;
    }

    /**
     *
     * @param sheet
     */
    protected void alert(NSWindow sheet) {
        this.alert(sheet, new CDSheetCallback() {
            public void callback(int returncode) {
                ;
            }
        });
    }

    protected void alert(NSWindow sheet, final CDSheetCallback callback) {
        CDSheetController c = new CDSheetController(this, sheet) {
            public void callback(final int returncode) {
                callback.callback(returncode);
            }
        };
        c.beginSheet(false);
    }
}