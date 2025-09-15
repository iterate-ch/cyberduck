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
import ch.cyberduck.binding.application.NSTextField;
import ch.cyberduck.binding.application.NSView;
import ch.cyberduck.binding.application.NSWindow;
import ch.cyberduck.binding.application.SheetCallback;
import ch.cyberduck.binding.foundation.NSNotification;
import ch.cyberduck.binding.foundation.NSNotificationCenter;
import ch.cyberduck.core.BookmarkNameProvider;
import ch.cyberduck.core.Credentials;
import ch.cyberduck.core.Host;
import ch.cyberduck.core.LocaleFactory;
import ch.cyberduck.core.LoginOptions;
import ch.cyberduck.core.ProviderHelpServiceFactory;
import ch.cyberduck.core.StringAppender;
import ch.cyberduck.core.resources.IconCacheFactory;

import org.apache.commons.lang3.StringUtils;
import org.rococoa.Foundation;
import org.rococoa.Rococoa;

public class PasswordController extends AlertController {

    @Outlet
    private NSTextField inputField;

    private final Host bookmark;
    private final Credentials credentials;
    private final String title;
    private final String reason;
    private final LoginOptions options;

    public PasswordController(final Host bookmark, final Credentials credentials, final String title, final String reason, final LoginOptions options) {
        this.bookmark = bookmark;
        this.credentials = credentials;
        this.title = title;
        this.reason = reason;
        this.options = options;
    }

    @Override
    public NSAlert loadAlert() {
        final NSAlert alert = NSAlert.alert();
        alert.setAlertStyle(NSAlert.NSInformationalAlertStyle);
        alert.setIcon(IconCacheFactory.<NSImage>get().iconNamed(options.icon, 64));
        alert.setMessageText(title);
        alert.setInformativeText(new StringAppender().append(reason).toString());
        alert.addButtonWithTitle(LocaleFactory.localizedString("Continue", "Credentials"));
        alert.addButtonWithTitle(LocaleFactory.localizedString("Cancel", "Alert"));
        if(options.anonymous) {
            alert.addButtonWithTitle(LocaleFactory.localizedString("Skip", "Transfer"));
        }
        alert.setShowsSuppressionButton(options.keychain);
        if(options.keychain) {
            alert.suppressionButton().setTitle(LocaleFactory.localizedString("Save Password", "Keychain"));
            alert.suppressionButton().setState(credentials.isSaved() ? NSCell.NSOnState : NSCell.NSOffState);
        }
        return alert;
    }

    @Override
    public void setWindow(final NSWindow window) {
        window.setTitle(BookmarkNameProvider.toString(bookmark));
        super.setWindow(window);
    }

    @Action
    public void suppressionButtonClicked(final NSButton sender) {
        super.suppressionButtonClicked(sender);
        credentials.setSaved(sender.state() == NSCell.NSOnState);
    }

    @Action
    public void passwordFieldTextDidChange(final NSNotification notification) {
        credentials.setPassword(StringUtils.trim(inputField.stringValue()));
    }

    @Override
    public NSView getAccessoryView(final NSAlert alert) {
        if(options.password) {
            inputField = NSSecureTextField.textFieldWithString(StringUtils.EMPTY);
        }
        else {
            inputField = NSTextField.textFieldWithString(StringUtils.EMPTY);
        }
        final NSView accessoryView = NSView.create();
        inputField.cell().setPlaceholderString(options.getPasswordPlaceholder());
        NSNotificationCenter.defaultCenter().addObserver(this.id(),
                Foundation.selector("passwordFieldTextDidChange:"),
                NSControl.NSControlTextDidChangeNotification,
                inputField.id());
        this.addAccessorySubview(accessoryView, inputField);
        return accessoryView;
    }

    public void setPasswordFieldText(final String input) {
        credentials.setPassword(input);
    }

    @Override
    protected void focus(final NSAlert alert) {
        super.focus(alert);
        inputField.selectText(null);
    }

    @Override
    public boolean validate(final int option) {
        if(option == SheetCallback.ALTERNATE_OPTION) {
            if(options.anonymous) {
                return true;
            }
        }
        return StringUtils.isNotBlank(inputField.stringValue());
    }

    @Override
    protected String help() {
        return ProviderHelpServiceFactory.get().help(bookmark.getProtocol());
    }
}
