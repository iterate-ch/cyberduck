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
import ch.cyberduck.binding.WindowController;
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
import ch.cyberduck.core.LocaleFactory;
import ch.cyberduck.core.LoginOptions;
import ch.cyberduck.core.local.BrowserLauncherFactory;
import ch.cyberduck.core.preferences.PreferencesFactory;
import ch.cyberduck.core.resources.IconCacheFactory;

import org.apache.commons.lang3.StringUtils;
import org.rococoa.Foundation;
import org.rococoa.cocoa.foundation.NSRect;

public class PasswordController extends AlertController {

    private final NSNotificationCenter notificationCenter = NSNotificationCenter.defaultCenter();

    @Outlet
    protected final NSSecureTextField inputField
            = NSSecureTextField.textfieldWithFrame(new NSRect(0, 22));

    private final Credentials credentials;

    public PasswordController(final WindowController parent, final Credentials credentials,
                              final String title, final String reason, final LoginOptions options) {
        super(parent, NSAlert.alert(
                title,
                reason,
                LocaleFactory.localizedString("Unlock Vault", "Cryptomator"),
                null,
                LocaleFactory.localizedString("Cancel", "Alert")
        ), NSAlert.NSInformationalAlertStyle);
        this.credentials = credentials;
        alert.setIcon(IconCacheFactory.<NSImage>get().iconNamed(options.icon, 64));
        alert.setShowsSuppressionButton(options.keychain);
        alert.suppressionButton().setTitle(LocaleFactory.localizedString("Add to Keychain", "Login"));
        alert.setShowsHelp(true);
        inputField.cell().setPlaceholderString(credentials.getPasswordPlaceholder());
        notificationCenter.addObserver(this.id(),
                Foundation.selector("passwordFieldTextDidChange:"),
                NSControl.NSControlTextDidChangeNotification,
                this.inputField);
        alert.suppressionButton().setAction(Foundation.selector("keychainCheckboxClicked:"));
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
    public NSView getAccessoryView() {
        return inputField;
    }

    @Override
    protected void focus() {
        super.focus();
        inputField.selectText(null);
    }

    @Override
    public void callback(final int returncode) {
        //
    }

    @Override
    public boolean validate() {
        return StringUtils.isNotBlank(inputField.stringValue());
    }

    @Override
    protected void help() {
        final StringBuilder site = new StringBuilder(PreferencesFactory.get().getProperty("website.help"));
        site.append("/howto/cryptomator");
        BrowserLauncherFactory.get().open(site.toString());
    }
}
