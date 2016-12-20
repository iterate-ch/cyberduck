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

import ch.cyberduck.binding.application.AppKitFunctionsLibrary;
import ch.cyberduck.binding.application.NSApplication;
import ch.cyberduck.binding.application.NSButton;
import ch.cyberduck.binding.application.PanelReturnCodeMapper;
import ch.cyberduck.binding.application.SheetCallback;
import ch.cyberduck.ui.InputValidator;

import org.apache.log4j.Logger;

public abstract class SheetController extends WindowController implements SheetCallback, InputValidator {
    private static final Logger log = Logger.getLogger(SheetController.class);

    private final NSApplication application = NSApplication.sharedApplication();

    private SheetCallback callback;
    private InputValidator validator;

    public SheetController() {
        this.callback = this;
        this.validator = this;
    }

    public SheetController(final InputValidator callback) {
        this.callback = this;
        this.validator = callback;
    }

    public void setValidator(final InputValidator validator) {
        this.validator = validator;
    }

    public void setCallback(final SheetCallback callback) {
        this.callback = callback;
    }

    @Override
    public void callback(final int returncode) {
        //
    }

    @Override
    public boolean validate() {
        return true;
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
        if(option == SheetCallback.DEFAULT_OPTION || option == SheetCallback.ALTERNATE_OPTION) {
            window.endEditingFor(null);
            if(!validator.validate()) {
                AppKitFunctionsLibrary.beep();
                return;
            }
        }
        callback.callback(option);
        application.endSheet(window, option);
    }
}
