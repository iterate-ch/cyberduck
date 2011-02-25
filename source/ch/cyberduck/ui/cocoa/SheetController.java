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

import ch.cyberduck.core.threading.ControllerMainAction;
import ch.cyberduck.ui.cocoa.application.AppKitFunctionsLibrary;
import ch.cyberduck.ui.cocoa.application.NSApplication;
import ch.cyberduck.ui.cocoa.application.NSButton;
import ch.cyberduck.ui.cocoa.application.NSWindow;

import org.rococoa.Foundation;
import org.rococoa.ID;

import org.apache.log4j.Logger;

import java.util.HashSet;
import java.util.Set;

/**
 * @version $Id$
 */
public abstract class SheetController extends WindowController implements SheetCallback {
    private static Logger log = Logger.getLogger(SheetController.class);

    /**
     * The controller of the parent window
     */
    protected final WindowController parent;

    /**
     * The sheet window must be provided later with #setWindow (usually called when loading the NIB file)
     *
     * @param parent The controller of the parent window
     */
    public SheetController(final WindowController parent) {
        this.parent = parent;
    }

    /**
     * Use this if no custom sheet is given (and no NIB file loaded)
     *
     * @param parent The controller of the parent window
     * @param sheet  The window to attach as the sheet
     */
    public SheetController(final WindowController parent, NSWindow sheet) {
        this.parent = parent;
        this.window = sheet;
    }

    /**
     * @return Null by default, a sheet with no custom NIB
     */
    @Override
    protected String getBundleName() {
        return null;
    }

    /**
     * @return The controller of this sheet parent window
     */
    protected WindowController getParentController() {
        return parent;
    }

    /**
     * This must be the target action for any button in the sheet dialog. Will validate the input
     * and close the sheet; #sheetDidClose will be called afterwards
     *
     * @param sender A button in the sheet dialog
     */
    @Action
    public void closeSheet(final NSButton sender) {
        log.debug("closeSheet:" + sender);
        if(sender.tag() == DEFAULT_OPTION || sender.tag() == OTHER_OPTION) {
            if(!this.validateInput()) {
                AppKitFunctionsLibrary.beep();
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
     * @param returncode
     * @param context
     */
    protected void callback(final int returncode, ID context) {
        this.returncode = returncode;
        this.callback(returncode);
        synchronized(parent.window()) {
            parent.window().notify();
        }
        if(!this.isSingleton()) {
            this.invalidate();
        }
    }

    /**
     *
     */
    public void beginSheet() {
        // Synchronize on parent controller. Only display one sheet at once.
        synchronized(parent) {
            if(isMainThread()) {
                // No need to call invoke on main thread
                this.beginSheetImpl();
                return;
            }
            invoke(new ControllerMainAction(this) {
                public void run() {
                    //Invoke again on main thread
                    beginSheetImpl();
                }
            }, true);
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
        }
    }

    /**
     * Keep a reference to the sheet to protect it from being
     * deallocated as a weak reference before the callback from the runtime
     */
    protected static final Set<SheetController> sheetRegistry
            = new HashSet<SheetController>();

    protected void beginSheetImpl() {
        this.loadBundle();
        parent.window().makeKeyAndOrderFront(null);
        NSApplication.sharedApplication().beginSheet(this.window(), //window
                parent.window(), // modalForWindow
                this.id(), // modalDelegate
                Foundation.selector("sheetDidClose:returnCode:contextInfo:"),
                null); //context
        sheetRegistry.add(this);
    }

    /**
     * Called by the runtime after a sheet has been dismissed. Ends any modal session and
     * sends the returncode to the callback implementation. Also invalidates this controller to be
     * garbage collected and notifies the lock object
     *
     * @param sheet
     * @param returncode  Identifier for the button clicked by the user
     * @param contextInfo Not used
     */
    public void sheetDidClose_returnCode_contextInfo(final NSWindow sheet, final int returncode, ID contextInfo) {
        sheet.orderOut(null);
        this.callback(returncode, contextInfo);
        sheetRegistry.remove(this);
    }

    /**
     * @return True if the class is a singleton and the object should
     *         not be invlidated upon the sheet is closed
     * @see #sheetDidClose_returnCode_contextInfo(ch.cyberduck.ui.cocoa.application.NSWindow, int, org.rococoa.ID)
     */
    @Override
    public boolean isSingleton() {
        return false;
    }
}
