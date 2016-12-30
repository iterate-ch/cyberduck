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
import ch.cyberduck.binding.application.NSWindow;
import ch.cyberduck.binding.application.SheetCallback;
import ch.cyberduck.binding.foundation.NSEnumerator;
import ch.cyberduck.binding.foundation.NSObject;
import ch.cyberduck.core.DefaultProviderHelpService;
import ch.cyberduck.core.local.BrowserLauncherFactory;
import ch.cyberduck.ui.InputValidator;

import org.rococoa.Foundation;
import org.rococoa.Rococoa;
import org.rococoa.cocoa.CGFloat;
import org.rococoa.cocoa.foundation.NSRect;

public abstract class AlertController extends SheetController implements SheetCallback, InputValidator {

    protected static final int SUBVIEWS_VERTICAL_SPACE = 4;

    /**
     * If using alert and no custom window
     */
    protected final NSAlert alert;

    public AlertController(final NSAlert alert) {
        this(alert, NSAlert.NSWarningAlertStyle);
    }

    public AlertController(final NSAlert alert, final int style) {
        this.alert = alert;
        this.alert.setAlertStyle(style);
        this.alert.setDelegate(this.id());
        this.setValidator(this);
        this.setCallback(this);
        final NSWindow window = this.alert.window();
        this.setWindow(window);
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

    public int beginSheet(final WindowController parent) {
        return new SheetInvoker(this, parent, this).beginSheet();
    }

    @Override
    public NSWindow window() {
        this.focus();
        return alert.window();
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
            final NSRect frame = this.getFrame(accessory);
            accessory.setFrameSize(frame.size);
            alert.setAccessoryView(accessory);
            alert.window().makeFirstResponder(accessory);
        }
        // First call layout and then do any special positioning and sizing of the accessory view prior to running the alert
        alert.layout();
        alert.window().recalculateKeyViewLoop();
    }

    protected NSRect getFrame(final NSView accessory) {
        final NSRect frame = new NSRect(window.frame().size.width.doubleValue(), accessory.frame().size.height.doubleValue());
        final NSEnumerator enumerator = accessory.subviews().objectEnumerator();
        NSObject next;
        while(null != (next = enumerator.nextObject())) {
            final NSView subview = Rococoa.cast(next, NSView.class);
            frame.size.height = new CGFloat(frame.size.height.doubleValue() + subview.frame().size.height.doubleValue() + SUBVIEWS_VERTICAL_SPACE * 2);
        }
        return frame;
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
    protected String help() {
        return new DefaultProviderHelpService().help();
    }

    /**
     * When the help button is pressed, the alert delegate (delegate) is first sent a alertShowHelp: message.
     *
     * @param alert Alert window
     */
    @Action
    public void alertShowHelp(final NSAlert alert) {
        BrowserLauncherFactory.get().open(this.help());
    }
}