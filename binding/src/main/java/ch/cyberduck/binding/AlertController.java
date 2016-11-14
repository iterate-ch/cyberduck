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

import ch.cyberduck.binding.application.NSAlert;
import ch.cyberduck.binding.application.NSButton;
import ch.cyberduck.binding.application.NSView;
import ch.cyberduck.binding.application.SheetCallback;
import ch.cyberduck.binding.foundation.NSEnumerator;
import ch.cyberduck.binding.foundation.NSObject;
import ch.cyberduck.core.DefaultProviderHelpService;
import ch.cyberduck.ui.InputValidator;

import org.rococoa.Foundation;
import org.rococoa.Rococoa;
import org.rococoa.cocoa.foundation.NSRect;

public abstract class AlertController extends SheetController implements SheetCallback, InputValidator {

    private final WindowController parent;

    /**
     * If using alert and no custom window
     */
    protected final NSAlert alert;

    public final SheetInvoker sheet;

    public AlertController(final WindowController parent, final NSAlert alert) {
        this(parent, alert, NSAlert.NSWarningAlertStyle);
    }

    public AlertController(final WindowController parent, final NSAlert alert, final int style) {
        this.parent = parent;
        this.alert = alert;
        this.alert.setAlertStyle(style);
        this.alert.setDelegate(this.id());
        this.sheet = new SheetInvoker(this, parent, alert.window());
        this.setValidator(this);
        this.setWindow(alert.window());
    }

    /**
     * @return Null by default, a sheet with no custom NIB
     */
    @Override
    protected String getBundleName() {
        return null;
    }

    public NSView getAccessoryView() {
        return null;
    }

    public int beginSheet() {
        this.focus();
        return sheet.beginSheet();
    }

    protected void focus() {
        NSEnumerator buttons = alert.buttons().objectEnumerator();
        NSObject button;
        while(((button = buttons.nextObject()) != null)) {
            final NSButton b = Rococoa.cast(button, NSButton.class);
            b.setTarget(this.id());
            b.setAction(Foundation.selector("closeSheet:"));
        }
        final NSView accessory = this.getAccessoryView();
        if(accessory != null) {
            accessory.setFrame(new NSRect(alert.window().contentView().frame().size.width.floatValue(),
                    accessory.frame().size.height.floatValue()));
            alert.setAccessoryView(accessory);
            alert.window().makeFirstResponder(accessory);
        }
        alert.layout();
    }

    protected void setTitle(final String title) {
        alert.setMessageText(title);
    }

    protected void setMessage(final String message) {
        alert.setInformativeText(message);
    }

    /**
     * Open help page.
     */
    protected void help() {
        new DefaultProviderHelpService().help();
    }

    @Override
    public boolean validate() {
        return true;
    }

    /**
     * When the help button is pressed, the alert delegate (delegate) is first sent a alertShowHelp: message.
     *
     * @param alert Alert window
     * @return True if help request was handled.
     */
    @Action
    public boolean alertShowHelp(final NSAlert alert) {
        this.help();
        return true;
    }
}