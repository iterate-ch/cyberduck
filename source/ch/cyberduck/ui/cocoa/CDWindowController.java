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
import com.apple.cocoa.application.NSModalSession;
import com.apple.cocoa.application.NSMutableParagraphStyle;
import com.apple.cocoa.application.NSParagraphStyle;
import com.apple.cocoa.application.NSWindow;
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

    private final Object lock = new Object();

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

    public void endSheet(NSWindow sheet, int tag) {
        log.debug("endSheet");
        if (modalSession != null) {
            NSApplication.sharedApplication().endModalSession(modalSession);
            modalSession = null;
        }
        NSApplication.sharedApplication().endSheet(sheet, tag);
        synchronized (lock) {
            lock.notifyAll();
        }
    }

    public void waitForSheetEnd() {
        log.debug("waitForSheetEnd");
        synchronized (lock) {
            while (this.hasSheet()) {
                try {
                    if (Thread.currentThread().getName().equals("main")
                            || Thread.currentThread().getName().equals("AWT-AppKit")) {
                        log.warn("Waiting on main thread; will run modal!");
                        NSApplication app = NSApplication.sharedApplication();
                        modalSession = NSApplication.sharedApplication().beginModalSessionForWindow(
                                this.window.attachedSheet());
                        while (this.hasSheet()) {
                            app.runModalSession(modalSession);
                        }
                        return;
                    }
                    log.debug("Sleeping:waitForSheetEnd...");
                    lock.wait();
                    log.debug("Awakened:waitForSheetEnd");
                }
                catch (InterruptedException e) {
                    log.error(e.getMessage());
                }
            }
        }
    }

    public void sheetWithoutTargetDidEnd(NSWindow sheet, int returncode, Object contextInfo) {
        this.endSheet(sheet, returncode);
        sheet.orderOut(null);
    }

    public void beginSheet(NSWindow sheet) {
        this.beginSheet(sheet, this, new NSSelector
                ("sheetWithoutTargetDidEnd",
                        new Class[]
                                {
                                        NSWindow.class, int.class, Object.class
                                }), null); // end selector);
    }

    public void beginSheet(final NSWindow sheet, final Object delegate,
                           final NSSelector endSelector, final Object contextInfo) {
        log.debug("beginSheet:" + sheet);
        synchronized (lock) {
            if (!this.window.isKeyWindow()) {
                this.window.makeKeyAndOrderFront(null);
            }
            this.waitForSheetEnd();
            NSApplication app = NSApplication.sharedApplication();
            app.beginSheet(sheet, //window
                    this.window,
                    delegate, //modalDelegate
                    endSelector, // did end selector
                    contextInfo); //contextInfo
            sheet.makeKeyAndOrderFront(null);
            lock.notifyAll();
        }
    }

    private NSModalSession modalSession = null;

    public boolean hasSheet() {
        return this.window.attachedSheet() != null;
    }
}