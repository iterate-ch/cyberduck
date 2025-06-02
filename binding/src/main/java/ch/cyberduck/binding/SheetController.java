package ch.cyberduck.binding;

/*
 * Copyright (c) 2002-2016 iterate GmbH. All rights reserved.
 * https://cyberduck.io/
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 */

import ch.cyberduck.binding.application.AlertSheetReturnCodeMapper;
import ch.cyberduck.binding.application.AppKitFunctionsLibrary;
import ch.cyberduck.binding.application.NSApplication;
import ch.cyberduck.binding.application.NSButton;
import ch.cyberduck.binding.application.NSWindow;
import ch.cyberduck.binding.application.SheetCallback;
import ch.cyberduck.ui.InputValidator;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.rococoa.ID;

public abstract class SheetController extends WindowController implements InputValidator, SheetCallback {
    private static final Logger log = LogManager.getLogger(SheetController.class);

    private static final NSApplication app = NSApplication.sharedApplication();

    private final InputValidator validator;

    public SheetController() {
        this(disabled);
    }

    public SheetController(final InputValidator callback) {
        this.validator = callback;
    }

    @Override
    public boolean validate(final int option) {
        return validator.validate(option);
    }

    /**
     * This must be the target action for any button in the sheet dialog. Will validate the input
     * and close the sheet
     *
     * @param sender A button in the sheet dialog
     * @see SheetCallback#DEFAULT_OPTION
     * @see SheetCallback#CANCEL_OPTION
     * @see SheetCallback#ALTERNATE_OPTION
     * @see SheetDidCloseReturnCodeDelegate#sheetDidClose_returnCode_contextInfo(NSWindow, int, ID)
     */
    @Action
    public void closeSheet(final NSButton sender) {
        log.debug("Close sheet with button {}", sender.title());
        this.closeSheetWithOption(new AlertSheetReturnCodeMapper().getOption(sender));
    }

    /**
     * @param option Tag set on button
     * @see SheetDidCloseReturnCodeDelegate#sheetDidClose_returnCode_contextInfo(NSWindow, int, ID)
     */
    public void closeSheetWithOption(int option) {
        log.debug("Close sheet with option {}", option);
        window.endEditingFor(null);
        if(option == SheetCallback.DEFAULT_OPTION || option == SheetCallback.ALTERNATE_OPTION) {
            if(!this.validate(option)) {
                log.warn("Failed validation with option {}", option);
                AppKitFunctionsLibrary.beep();
                return;
            }
        }
        if(window.isSheet()) {
            // Ends a document modal session by specifying the sheet window.
            app.endSheet(window, option);
        }
        else {
            // The result code you want returned from the runModalForWindow:
            app.stopModalWithCode(option);
        }
        window.orderOut(null);
    }

    @Override
    public void callback(final int returncode) {
        log.warn("Return code {} not handled", returncode);
    }

    @Delegate
    // Handle keyboard esc event when not running as sheet
    public void cancel(ID sender) {
        this.closeSheetWithOption(SheetCallback.CANCEL_OPTION);
    }

    public static class NoBundleSheetController extends SheetController {
        public NoBundleSheetController(final NSWindow window) {
            this(window, InputValidator.disabled);
        }

        public NoBundleSheetController(final NSWindow window, final InputValidator callback) {
            super(callback);
            this.setWindow(window);
        }

        @Override
        protected String getBundleName() {
            return null;
        }
    }
}
