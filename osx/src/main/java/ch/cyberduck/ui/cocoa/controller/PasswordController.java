package ch.cyberduck.ui.cocoa.controller;

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

import ch.cyberduck.binding.Action;
import ch.cyberduck.binding.AlertController;
import ch.cyberduck.binding.Outlet;
import ch.cyberduck.binding.application.NSAlert;
import ch.cyberduck.binding.application.NSButton;
import ch.cyberduck.binding.application.NSCell;
import ch.cyberduck.binding.application.NSControl;
import ch.cyberduck.binding.application.NSImage;
import ch.cyberduck.binding.application.NSSecureTextField;
import ch.cyberduck.binding.application.NSView;
import ch.cyberduck.binding.foundation.NSNotification;
import ch.cyberduck.binding.foundation.NSNotificationCenter;
import ch.cyberduck.core.Credentials;
import ch.cyberduck.core.DefaultProviderHelpService;
import ch.cyberduck.core.LocaleFactory;
import ch.cyberduck.core.LoginOptions;
import ch.cyberduck.core.resources.IconCacheFactory;

import org.apache.commons.lang3.StringUtils;
import org.rococoa.Foundation;
import org.rococoa.cocoa.foundation.NSPoint;
import org.rococoa.cocoa.foundation.NSRect;

public class PasswordController extends AlertController {

    @Outlet
    private NSSecureTextField inputField;
    @Outlet
    private NSButton keychainCheckbox;
    @Outlet
    private NSButton suppressionCheckbox;

    private final Credentials credentials;
    private final String title;
    private final String reason;
    private final LoginOptions options;

    public PasswordController(final Credentials credentials, final String title, final String reason, final LoginOptions options) {
        this.credentials = credentials;
        this.title = title;
        this.reason = reason;
        this.options = options;
    }

    @Override
    public void loadBundle() {
        final NSAlert alert = NSAlert.alert();
        alert.setAlertStyle(NSAlert.NSInformationalAlertStyle);
        alert.setIcon(IconCacheFactory.<NSImage>get().iconNamed(options.icon, 64));
        alert.setMessageText(title);
        alert.setInformativeText(reason);
        alert.addButtonWithTitle(LocaleFactory.localizedString("Unlock Vault", "Cryptomator"));
        alert.addButtonWithTitle(LocaleFactory.localizedString("Cancel", "Alert"));
        this.loadBundle(alert);
    }

    @Action
    public void keychainCheckboxClicked(final NSButton sender) {
        credentials.setSaved(sender.state() == NSCell.NSOnState);
    }

    @Action
    public void passwordFieldTextDidChange(NSNotification notification) {
        credentials.setPassword(inputField.stringValue());
    }

    @Override
    public NSView getAccessoryView(final NSAlert alert) {
        NSView view = NSView.create(new NSRect(alert.window().frame().size.width.doubleValue(), 0));
        suppressionCheckbox = NSButton.buttonWithFrame(new NSRect(alert.window().frame().size.width.doubleValue(), 18));
        suppressionCheckbox.setTitle(LocaleFactory.localizedString("Always"));
        suppressionCheckbox.setAction(Foundation.selector("suppressionButtonClicked:"));
        suppressionCheckbox.setTarget(this.id());
        suppressionCheckbox.setButtonType(NSButton.NSSwitchButton);
        suppressionCheckbox.setState(NSCell.NSOffState);
        suppressionCheckbox.sizeToFit();
        // Override accessory view with location menu added
        suppressionCheckbox.setFrameOrigin(new NSPoint(0, 0));
        view.addSubview(suppressionCheckbox);
        if(options.keychain) {
            keychainCheckbox = NSButton.buttonWithFrame(new NSRect(alert.window().frame().size.width.doubleValue(), 18));
            keychainCheckbox.setTitle(LocaleFactory.localizedString("Add to Keychain", "Login"));
            keychainCheckbox.setAction(Foundation.selector("keychainCheckboxClicked:"));
            keychainCheckbox.setTarget(this.id());
            keychainCheckbox.setButtonType(NSButton.NSSwitchButton);
            keychainCheckbox.setState(NSCell.NSOffState);
            keychainCheckbox.sizeToFit();
            // Override accessory view with location menu added
            keychainCheckbox.setFrameOrigin(new NSPoint(0, this.getFrame(alert, view).size.height.doubleValue()));
            view.addSubview(keychainCheckbox);
        }
        inputField = NSSecureTextField.textfieldWithFrame(new NSRect(alert.window().frame().size.width.doubleValue(), 22));
        inputField.cell().setPlaceholderString(credentials.getPasswordPlaceholder());
        inputField.setFrameOrigin(new NSPoint(0, this.getFrame(alert, view).size.height.doubleValue() + view.subviews().count().doubleValue() * SUBVIEWS_VERTICAL_SPACE));
        view.addSubview(inputField);
        return view;
    }

    @Override
    protected void focus(final NSAlert alert) {
        super.focus(alert);
        inputField.selectText(null);
        NSNotificationCenter.defaultCenter().addObserver(this.id(),
                Foundation.selector("passwordFieldTextDidChange:"),
                NSControl.NSControlTextDidChangeNotification,
                inputField);
    }

    @Override
    public boolean validate() {
        return StringUtils.isNotBlank(inputField.stringValue());
    }

    @Override
    protected String help() {
        return new DefaultProviderHelpService().help("/howto/cryptomator");
    }
}
