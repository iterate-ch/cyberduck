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

import ch.cyberduck.binding.Action;
import ch.cyberduck.binding.application.AppKitFunctionsLibrary;
import ch.cyberduck.binding.application.NSApplication;
import ch.cyberduck.binding.application.NSButton;
import ch.cyberduck.binding.application.NSWindow;
import ch.cyberduck.binding.application.PanelReturnCodeMapper;
import ch.cyberduck.binding.application.SheetCallback;
import ch.cyberduck.binding.foundation.NSThread;
import ch.cyberduck.core.threading.ControllerMainAction;

import org.apache.log4j.Logger;
import org.rococoa.Foundation;
import org.rococoa.ID;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CountDownLatch;

public abstract class SheetController extends WindowController implements SheetCallback {
    private static Logger log = Logger.getLogger(SheetController.class);

    /**
     * Keep a reference to the sheet to protect it from being
     * deallocated as a weak reference before the callback from the runtime
     */
    protected static final Set<SheetController> sheetRegistry
            = new HashSet<SheetController>();

    /**
     * The controller of the parent window
     */
    protected final WindowController parent;

    /**
     * Dismiss button clicked
     */
    private int returncode;

    private CountDownLatch signal
            = new CountDownLatch(1);

    /**
     * The sheet window must be provided later with #setWindow (usually called when loading the NIB file)
     *
     * @param parent The controller of the parent window
     */
    public SheetController(final WindowController parent) {
        this.parent = parent;
        sheetRegistry.add(this);
    }

    /**
     * @return Null by default, a sheet with no custom NIB
     */
    @Override
    protected String getBundleName() {
        return null;
    }

    /**
     * This must be the target action for any button in the sheet dialog. Will validate the input
     * and close the sheet; #sheetDidClose will be called afterwards
     *
     * @param sender A button in the sheet dialog
     */
    @Action
    public void closeSheet(final NSButton sender) {
        if(log.isDebugEnabled()) {
            log.debug(String.format("Close sheet with button %s", sender.title()));
        }
        final int option = new PanelReturnCodeMapper().getOption(sender);
        if(option == DEFAULT_OPTION || option == ALTERNATE_OPTION) {
            if(!this.validateInput()) {
                AppKitFunctionsLibrary.beep();
                return;
            }
        }
        NSApplication.sharedApplication().endSheet(this.window(), option);
    }

    /**
     * @return The tag of the button this sheet was dismissed with
     */
    public int returnCode() {
        return returncode;
    }

    /**
     * Check input fields for any errors
     *
     * @return true if a valid input has been given
     */
    protected boolean validateInput() {
        return true;
    }

    public void beginSheet() {
        synchronized(parent.window()) {
            if(NSThread.isMainThread()) {
                this.loadBundle();
                // No need to call invoke on main thread
                this.beginSheet(this.window());
            }
            else {
                final SheetController controller = this;
                invoke(new ControllerMainAction(this) {
                    @Override
                    public void run() {
                        controller.loadBundle();
                        //Invoke again on main thread
                        controller.beginSheet(controller.window());
                    }
                }, true);
                if(log.isDebugEnabled()) {
                    log.debug("Await sheet dismiss");
                }
                // Synchronize on parent controller. Only display one sheet at once.
                try {
                    signal.await();
                }
                catch(InterruptedException e) {
                    log.error("Error waiting for sheet dismiss", e);
                    this.callback(CANCEL_OPTION);
                }
            }
        }
    }

    protected void beginSheet(final NSWindow window) {
        parent.window().makeKeyAndOrderFront(null);
        NSApplication.sharedApplication().beginSheet(window, //sheet
                parent.window(), // modalForWindow
                this.id(), // modalDelegate
                Foundation.selector("sheetDidClose:returnCode:contextInfo:"),
                null); //context
    }

    /**
     * Called by the runtime after a sheet has been dismissed. Ends any modal session and
     * sends the returncode to the callback implementation. Also invalidates this controller to be
     * garbage collected and notifies the lock object
     *
     * @param sheet       Sheet window
     * @param returncode  Identifier for the button clicked by the user
     * @param contextInfo Not used
     */
    public void sheetDidClose_returnCode_contextInfo(final NSWindow sheet, final int returncode, ID contextInfo) {
        sheet.orderOut(null);
        this.returncode = returncode;
        this.callback(returncode);
        signal.countDown();
        if(!this.isSingleton()) {
            this.invalidate();
        }
    }

    @Override
    public void invalidate() {
        sheetRegistry.remove(this);
        super.invalidate();
    }

    /**
     * @return True if the class is a singleton and the object should
     * not be invlidated upon the sheet is closed
     * @see #sheetDidClose_returnCode_contextInfo(ch.cyberduck.binding.application.NSWindow, int, org.rococoa.ID)
     */
    @Override
    public boolean isSingleton() {
        return false;
    }
}