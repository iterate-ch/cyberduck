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
import com.apple.cocoa.foundation.NSSelector;

/**
 * @version $Id$
 */
public abstract class CDSheetController extends CDWindowController implements CDSheetCallback
{

    /**
     * The controller of the parent window
     */
    protected CDWindowController parent;

    /**
     * The sheet window must be provided later with #setWindow (usually called when loading the NIB)
     * @param parent
     */
    public CDSheetController(CDWindowController parent) {
        this.parent = parent;
    }

    /**
     *
     * @param parent The controller of the parent window
     * @param sheet The window to attach as the sheet
     */
    public CDSheetController(CDWindowController parent, NSWindow sheet) {
        this.parent = parent;
        this.window = sheet;
    }

    /**
     * The synchronisation lock to check that only one sheet is displayed at a time
     */
    private final Object lock = new Object();

    /**
     *
     */
    private NSModalSession modalSession = null;

    /**
     *
     * @param sender A button in the sheet dialog
     */
    public void closeSheet(NSButton sender) {
        log.debug("closeSheet:"+sender);
        if(sender.tag() == DEFAULT_OPTION
                || sender.tag() == ALTERNATE_OPTION) {
            if(!this.validateInput()) {
                return;
            }
        }
        if (modalSession != null) {
            NSApplication.sharedApplication().endModalSession(modalSession);
            modalSession = null;
        }
        NSApplication.sharedApplication().endSheet(this.window(), sender.tag());
    }

    /**
     *
     * @return true if a valid input has been given
     */
    protected boolean validateInput() {
        return true;
    }

    /**
     * Wait in the current thread until the sheet
     * attached to this window has been dismissed by the user
     */
    protected void waitForSheetEnd() {
        log.debug("waitForSheetEnd");
        synchronized (lock) {
            while (parent.hasSheet()) {
                try {
                    if (Thread.currentThread().getName().equals("main")
                            || Thread.currentThread().getName().equals("AWT-AppKit")) {
                        log.warn("Waiting on main thread; will run modal!");
                        NSApplication app = NSApplication.sharedApplication();
                        modalSession = NSApplication.sharedApplication().beginModalSessionForWindow(
                                this.parent.window().attachedSheet());
                        while (parent.hasSheet()) {
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

    /**
     *
     * @param returncode
     */
    public abstract void callback(int returncode);

    /**
     * Will attach the sheet to the parent window
     */
    protected void beginSheet(boolean blocking) {
        log.debug("beginSheet:" + this.window());
        synchronized (lock) {
            if (!this.parent.window().isKeyWindow()) {
                this.parent.window().makeKeyAndOrderFront(null);
            }
            this.waitForSheetEnd();
            NSApplication app = NSApplication.sharedApplication();
            app.beginSheet(this.window(), //window
                    this.parent.window(),
                    this, //modalDelegate
                    new NSSelector("sheetDidClose",
                            new Class[]{NSPanel.class, int.class, Object.class}), // did end selector
                    null); //contextInfo
            this.window().makeKeyAndOrderFront(null);
            if(blocking) {
                this.waitForSheetEnd();
            }
        }
    }

    /**
     *
     * @param sheet
     * @param returncode
     * @param context
     */
    public void sheetDidClose(NSPanel sheet, int returncode, Object context) {
        sheet.orderOut(null);
        this.callback(returncode);
        this.invalidate();
        synchronized (lock) {
            lock.notifyAll();
        }
    }
}
