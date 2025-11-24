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
import ch.cyberduck.binding.application.NSButton;
import ch.cyberduck.binding.application.NSWindow;
import ch.cyberduck.binding.application.SheetCallback;
import ch.cyberduck.ui.InputValidator;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.rococoa.Foundation;
import org.rococoa.ID;
import org.rococoa.Selector;

import java.util.HashSet;
import java.util.Set;

public abstract class SheetController extends WindowController implements InputValidator, SheetCallback {
    private static final Logger log = LogManager.getLogger(SheetController.class);

    public static final Selector BUTTON_CLOSE_SELECTOR = Foundation.selector("closeSheet:");

    private final InputValidator validator;
    private final Set<AlertRunner.CloseHandler> handlers = new HashSet<>();

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
        handlers.forEach(h -> h.closed(window, option));
    }

    @Override
    public void invalidate() {
        super.invalidate();
        handlers.clear();
    }

    public void addHandler(final AlertRunner.CloseHandler handler) {
        handlers.add(handler);
    }

    @Override
    public void callback(final int returncode) {
        log.warn("Return code {} not handled", returncode);
    }

    /**
     * Implementation with no bundle loaded but window reference only
     */
    public static class NoBundleSheetController extends SheetController {
        public NoBundleSheetController() {
            this(InputValidator.disabled);
        }

        public NoBundleSheetController(final InputValidator callback) {
            super(callback);
        }

        @Override
        protected String getBundleName() {
            return null;
        }
    }
}
