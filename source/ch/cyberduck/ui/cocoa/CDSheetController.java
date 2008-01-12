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

import ch.cyberduck.ui.cocoa.threading.WindowMainAction;

import org.apache.log4j.Logger;

/**
 * @version $Id$
 */
public abstract class CDSheetController extends CDWindowController implements CDSheetCallback {
    private static Logger log = Logger.getLogger(CDSheetController.class);

    /**
     * The controller of the parent window
     */
    protected final CDWindowController parent;

    /**
     * The sheet window must be provided later with #setWindow (usually called when loading the NIB file)
     *
     * @param parent The controller of the parent window
     */
    public CDSheetController(final CDWindowController parent) {
        this.parent = parent;
    }

    /**
     * Use this if no custom sheet is given (and no NIB file loaded)
     *
     * @param parent The controller of the parent window
     * @param sheet  The window to attach as the sheet
     */
    public CDSheetController(final CDWindowController parent, NSWindow sheet) {
        this.parent = parent;
        this.window = sheet;
    }

    /**
     * @return Null by default, a sheet with no custom NIB
     */
    protected String getBundleName() {
        return null;
    }

    public void awakeFromNib() {
        ;
    }

    /**
     * This must be the target action for any button in the sheet dialog. Will validate the input
     * and close the sheet; #sheetDidClose will be called afterwards
     *
     * @param sender A button in the sheet dialog
     */
    public void closeSheet(final NSButton sender) {
        log.debug("closeSheet:" + sender);
        if(sender.tag() == DEFAULT_OPTION
                || sender.tag() == ALTERNATE_OPTION) {
            if(!this.validateInput()) {
                NSApplication.beep();
                return;
            }
        }
        NSApplication.sharedApplication().endSheet(this.window(), sender.tag());
    }

    private int returncode;

    /**
     * @return The tag of the button this sheet was dismissed with
     */
    public int returnCode() {
        return this.returncode;
    }

    /**
     * Check input fields for any errors
     *
     * @return true if a valid input has been given
     */
    protected boolean validateInput() {
        return true;
    }

    /**
     * Called after the sheet has been dismissed by the user. The return codes are defined in
     * <code>ch.cyberduck.ui.cooca.CDSheetCallback</code>
     *
     * @param returncode
     */
    public abstract void callback(final int returncode);

    public void beginSheet() {
        if(!CDMainApplication.isMainThread()) {
            synchronized(parent) {
                CDMainApplication.invoke(new WindowMainAction(parent) {
                    public void run() {
                        //Invoke again on main thread
                        CDSheetController.this.beginSheet();
                        synchronized(parent.window()) {
                            parent.window().notify();
                        }
                    }
                });
                synchronized(parent.window()) {
                    while(!parent.hasSheet()) {
                        try {
                            log.debug("Sleeping:waitSheetDisplayLock...");
                            parent.window().wait();
                            log.debug("Awakened:waitSheetDisplayLock");
                        }
                        catch(InterruptedException e) {
                            log.error(e.getMessage());
                        }
                    }
                }
                synchronized(parent.window()) {
                    while(parent.hasSheet()) {
                        try {
                            log.debug("Sleeping:waitForSheetDismiss...");
                            parent.window().wait();
                            log.debug("Awakened:waitForSheetDismiss");
                        }
                        catch(InterruptedException e) {
                            log.error(e.getMessage());
                        }
                    }
                }
                return;
            }
        }
        this.loadBundle();
        final NSApplication app = NSApplication.sharedApplication();
        app.beginSheet(this.window(), //window
                parent.window(), // modalForWindow
                this, // modalDelegate
                new NSSelector("sheetDidClose",
                        new Class[]{NSPanel.class, int.class, Object.class}), // did end selector
                null); //context
    }

    /**
     * Called by the runtime after a sheet has been dismissed. Ends any modal session and
     * sends the returncode to the callback implementation. Also invalidates this controller to be
     * garbage collected and notifies the lock object
     *
     * @param sheet
     * @param returncode Identifier for the button clicked by the user
     * @param context    Not used
     */
    public void sheetDidClose(NSPanel sheet, final int returncode, Object context) {
        log.debug("sheetDidClose:" + sheet);
        sheet.orderOut(null);
        this.returncode = returncode;
        this.callback(returncode);
        if(!this.isSingleton()) {
            this.invalidate();
        }
        synchronized(parent.window()) {
            parent.window().notify();
        }
    }

    /**
     * @return True if the class is a singleton and the object should
     *         not be invlidated upon the sheet is closed
     * @see #sheetDidClose(com.apple.cocoa.application.NSPanel, int, Object)
     */
    public boolean isSingleton() {
        return false;
    }
}
