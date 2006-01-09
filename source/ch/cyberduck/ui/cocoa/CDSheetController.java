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
     * The sheet window must be provided later with #setWindow (usually called when loading the NIB file)
     * @param parent The controller of the parent window
     */
    public CDSheetController(CDWindowController parent) {
        this.parent = parent;
    }

    /**
     * Use this if no custom sheet is given (and no NIB file loaded)
     * @param parent The controller of the parent window
     * @param sheet The window to attach as the sheet
     */
    public CDSheetController(CDWindowController parent, NSWindow sheet) {
        this.parent = parent;
        this.window = sheet;
    }

    public void awakeFromNib() {
        ;
    }

    /**
     * The synchronisation lock to check that only one sheet is displayed at a time
     */
    private final Object lock = new Object();

    /**
     * This must be the target action for any button in the sheet dialog. Will validate the input
     * and close the sheet; #sheetDidClose will be called afterwards
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
        NSApplication.sharedApplication().endSheet(this.window(), sender.tag());
    }

    /**
     * Check input fields for any errors
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
        synchronized (lock) {
            while (parent.hasSheet()) {
                try {
                    if (Thread.currentThread().getName().equals("main")
                            || Thread.currentThread().getName().equals("AWT-AppKit")) {
                        log.warn("Waiting on main thread; will run modal!");
                        NSApplication app = NSApplication.sharedApplication();
                        NSModalSession modalSession = app.beginModalSessionForWindow(
                                this.parent.window().attachedSheet());
                        while (parent.hasSheet()) {
                            app.runModalSession(modalSession);
                        }
                        app.endModalSession(modalSession);
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
     * Called after the sheet has been dismissed by the user. The returncodes are defined in
     * <code>ch.cyberduck.ui.cooca.CDSheetCallback</code>
     * @param returncode
     */
    public abstract void callback(int returncode);

    /**
     * Will attach the sheet to the parent window
     * @param blocking will wait for the sheet to end if true
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
                    null); //context
            this.window().makeKeyAndOrderFront(null);
            if(blocking) {
                this.waitForSheetEnd();
            }
        }
    }

    /**
     * Called by the runtime after a sheet has been dismissed. Ends any modal session and
     * sends the returncode to the callback implementation. Also invalidates this controller to be
     * garbage collected and notifies the lock object
     * @param sheet
     * @param returncode Identifier for the button clicked by the user
     * @param context Not used
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
