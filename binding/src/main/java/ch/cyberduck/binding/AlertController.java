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
import ch.cyberduck.binding.application.NSWindow;
import ch.cyberduck.binding.foundation.NSEnumerator;
import ch.cyberduck.binding.foundation.NSObject;
import ch.cyberduck.core.ProviderHelpServiceFactory;
import ch.cyberduck.core.local.BrowserLauncherFactory;
import ch.cyberduck.ui.InputValidator;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.rococoa.Foundation;
import org.rococoa.Rococoa;
import org.rococoa.cocoa.CGFloat;
import org.rococoa.cocoa.foundation.NSPoint;
import org.rococoa.cocoa.foundation.NSRect;

public abstract class AlertController extends SheetController implements InputValidator {
    private static final Logger log = LogManager.getLogger(AlertController.class);

    protected static final int SUBVIEWS_VERTICAL_SPACE = 4;

    private boolean suppressed = false;

    @Outlet
    private NSAlert alert;

    public AlertController() {
        super();
    }

    public AlertController(final InputValidator callback) {
        super(callback);
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

    public abstract NSAlert loadAlert();

    public NSAlert alert() {
        return alert;
    }

    @Override
    public void loadBundle() {
        alert = this.loadAlert();
        log.debug("Display alert {}", alert);
        alert.setShowsHelp(true);
        alert.setDelegate(this.id());
        if(alert.showsSuppressionButton()) {
            alert.suppressionButton().setTarget(this.id());
            alert.suppressionButton().setAction(Foundation.selector("suppressionButtonClicked:"));
        }
        final NSWindow window = alert.window();
        log.debug("Use window {}", window);
        this.setWindow(window);
        // Layout alert view on main thread
        this.focus(alert);
    }

    @Override
    public void setWindow(final NSWindow window) {
        super.setWindow(window);
        window.setReleasedWhenClosed(false);
    }

    protected void focus(final NSAlert alert) {
        log.debug("Focus alert {}", alert);
        final NSEnumerator buttons = alert.buttons().objectEnumerator();
        NSObject button;
        while((button = buttons.nextObject()) != null) {
            final NSButton b = Rococoa.cast(button, NSButton.class);
            b.setTarget(this.id());
            b.setAction(SheetController.BUTTON_CLOSE_SELECTOR);
        }
        final NSView accessory = this.getAccessoryView(alert);
        if(accessory != null) {
            final NSRect frame = this.getFrame(accessory);
            accessory.setFrameSize(frame.size);
            alert.setAccessoryView(accessory);
            window.makeFirstResponder(accessory);
        }
        // First call layout and then do any special positioning and sizing of the accessory view prior to running the alert
        alert.layout();
        window.recalculateKeyViewLoop();
    }

    protected NSRect getFrame(final NSView accessory) {
        final NSRect frame = new NSRect(window.frame().size.width.doubleValue(), accessory.frame().size.height.doubleValue());
        final NSEnumerator enumerator = accessory.subviews().objectEnumerator();
        NSObject next;
        while(null != (next = enumerator.nextObject())) {
            final NSView subview = Rococoa.cast(next, NSView.class);
            frame.size.height = new CGFloat(frame.size.height.doubleValue() + subview.frame().size.height.doubleValue() + SUBVIEWS_VERTICAL_SPACE * 2);
        }
        log.debug("Calculated frame {} for alert {} with accessory {}", frame, this, accessory);
        return frame;
    }

    /**
     * @return Help page.
     */
    protected String help() {
        return ProviderHelpServiceFactory.get().help();
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
        log.debug("Suppression state set to {}", suppressed);
    }

    public boolean isSuppressed() {
        return suppressed;
    }

    protected void addAccessorySubview(final NSView accessoryView, final NSView view) {
        view.setFrameSize(this.getFrame(view).size);
        view.setFrameOrigin(new NSPoint(0, this.getFrame(accessoryView).size.height.doubleValue()
                + accessoryView.subviews().count().doubleValue() * SUBVIEWS_VERTICAL_SPACE));
        accessoryView.addSubview(view);
    }
}
