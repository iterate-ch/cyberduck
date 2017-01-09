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
import ch.cyberduck.binding.application.NSCell;
import ch.cyberduck.binding.application.NSView;
import ch.cyberduck.binding.application.SheetCallback;
import ch.cyberduck.binding.foundation.NSEnumerator;
import ch.cyberduck.binding.foundation.NSObject;
import ch.cyberduck.core.DefaultProviderHelpService;
import ch.cyberduck.core.local.BrowserLauncherFactory;
import ch.cyberduck.ui.InputValidator;

import org.apache.log4j.Logger;
import org.rococoa.Foundation;
import org.rococoa.Rococoa;
import org.rococoa.cocoa.CGFloat;
import org.rococoa.cocoa.foundation.NSRect;

public abstract class AlertController extends SheetController implements SheetCallback, InputValidator {
    private static final Logger log = Logger.getLogger(AlertController.class);

    protected static final int SUBVIEWS_VERTICAL_SPACE = 4;

    private boolean suppressed = false;

    @Outlet
    private NSAlert alert;

    public AlertController(final NSAlert alert) {
        this.loadBundle(alert);
    }

    public AlertController() {
        this.setValidator(this);
    }

    /**
     * @return Null by default, a sheet with no custom NIB
     */
    @Override
    protected String getBundleName() {
        return null;
    }

    public NSView getAccessoryView(final NSAlert alert) {
        return null;
    }

    public int beginSheet(final WindowController parent) {
        return new SheetInvoker(this, parent, this).beginSheet();
    }

    @Override
    public void callback(final int returncode) {
        log.warn(String.format("Ignore return code %d", returncode));
    }

    @Override
    public void loadBundle() {
        //
    }

    protected void loadBundle(final NSAlert alert) {
        this.alert = alert;
        alert.setShowsHelp(true);
        alert.setDelegate(this.id());
        if(alert.showsSuppressionButton()) {
            alert.suppressionButton().setTarget(this.id());
            alert.suppressionButton().setAction(Foundation.selector("suppressionButtonClicked:"));
        }
        // Layout alert view on main thread
        this.focus(alert);
        this.setWindow(alert.window());
    }

    protected void focus(final NSAlert alert) {
        NSEnumerator buttons = alert.buttons().objectEnumerator();
        NSObject button;
        while(((button = buttons.nextObject()) != null)) {
            final NSButton b = Rococoa.cast(button, NSButton.class);
            b.setTarget(this.id());
            b.setAction(Foundation.selector("closeSheet:"));
        }
        final NSView accessory = this.getAccessoryView(alert);
        if(accessory != null) {
            final NSRect frame = this.getFrame(alert, accessory);
            accessory.setFrameSize(frame.size);
            alert.setAccessoryView(accessory);
            alert.window().makeFirstResponder(accessory);
        }
        // First call layout and then do any special positioning and sizing of the accessory view prior to running the alert
        alert.layout();
        alert.window().recalculateKeyViewLoop();
    }

    protected NSRect getFrame(final NSAlert alert, final NSView accessory) {
        final NSRect frame = new NSRect(alert.window().frame().size.width.doubleValue(), accessory.frame().size.height.doubleValue());
        final NSEnumerator enumerator = accessory.subviews().objectEnumerator();
        NSObject next;
        while(null != (next = enumerator.nextObject())) {
            final NSView subview = Rococoa.cast(next, NSView.class);
            frame.size.height = new CGFloat(frame.size.height.doubleValue() + subview.frame().size.height.doubleValue() + SUBVIEWS_VERTICAL_SPACE * 2);
        }
        return frame;
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

    @Action
    public void suppressionButtonClicked(final NSButton sender) {
        suppressed = sender.state() == NSCell.NSOnState;
    }

    public boolean isSuppressed() {
        return suppressed;
    }
}