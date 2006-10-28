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

import ch.cyberduck.ui.cocoa.threading.BackgroundAction;
import ch.cyberduck.ui.cocoa.threading.BackgroundActionImpl;

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

    protected static final NSDictionary TRUNCATE_MIDDLE_ATTRIBUTES = new NSDictionary(
            new Object[]{lineBreakByTruncatingMiddleParagraph},
            new Object[]{NSAttributedString.ParagraphStyleAttributeName});

    protected static final NSDictionary TRUNCATE_MIDDLE_BOLD_RED_FONT_ATTRIBUTES = new NSDictionary(
            new Object[]{lineBreakByTruncatingMiddleParagraph, NSFont.boldSystemFontOfSize(10.0f), NSColor.redColor()},
            new Object[]{NSAttributedString.ParagraphStyleAttributeName, NSAttributedString.FontAttributeName, NSAttributedString.ForegroundColorAttributeName}
    );

    protected static final NSDictionary FIXED_WITH_FONT_ATTRIBUTES = new NSDictionary(
            new Object[]{NSFont.userFixedPitchFontOfSize(9.0f)},
            new Object[]{NSAttributedString.FontAttributeName}
    );

    protected static final NSDictionary BOLD_RED_FONT_ATTRIBUTES = new NSDictionary(
            new Object[]{NSFont.boldSystemFontOfSize(10.0f), NSColor.redColor()},
            new Object[]{NSAttributedString.FontAttributeName, NSAttributedString.ForegroundColorAttributeName}
    );

    /**
     * Run the runnable in the background using a new thread
     * @param runnable
     */
    public void background(final BackgroundActionImpl runnable, final Object lock) {
        log.debug("background:"+runnable);
        new Thread() {
            public void run() {
                // Synchronize all background threads to this lock so actions run
                // sequentially as they were initiated from the main interface thread
                synchronized(lock) {
                    log.debug("Acquired lock for background runnable:"+runnable);
                    try {
                        // Execute the action of the runnable
                        runnable.run();
                    }
                    catch(NullPointerException e) {
                        // We might get a null pointer if the session has been interrupted
                        // during the action in progress and closing the underlying socket
                        // asynchronously. See Session#interrupt
                        log.info("Due to closing the underlying socket asynchronously, the " +
                                "action was interrupted while still pending completion");
                    }
                    finally {
                        // Invoke the cleanup on the main thread to let the action
                        // synchronize the user interface
                        CDWindowController.this.invoke(new Runnable() {
                            public void run() {
                                runnable.cleanup();
                                // If there was any failure, display the summary now
                                if(runnable.hasFailed()) {
                                    runnable.alert(lock);
                                }
                            }
                        });
                    }
                    log.debug("Releasing lock for background runnable:"+runnable);
                }
            }
        }.start();
        log.debug("Started background runnable for:"+runnable);
    }

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
        if(null == this.window) {
            return false;
        }
        return this.window.attachedSheet() != null;
    }

    /**
     *
     * @param sheet
     */
    protected void alert(final NSWindow sheet) {
        this.alert(sheet, new CDSheetCallback() {
            public void callback(final int returncode) {
                ;
            }
        });
    }

    protected void alert(final NSWindow sheet, final CDSheetCallback callback) {
        this.alert(sheet, callback, false);
    }

    protected void alert(final NSWindow sheet, final CDSheetCallback callback, final boolean blocking) {
        CDSheetController c = new CDSheetController(this, sheet) {
            public void callback(final int returncode) {
                callback.callback(returncode);
            }
        };
        c.beginSheet(blocking);
    }
}