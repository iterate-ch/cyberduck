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
import ch.cyberduck.binding.Outlet;
import ch.cyberduck.binding.application.NSAlert;
import ch.cyberduck.binding.application.NSControl;
import ch.cyberduck.binding.application.NSImage;
import ch.cyberduck.binding.application.NSLevelIndicator;
import ch.cyberduck.binding.application.NSSecureTextField;
import ch.cyberduck.binding.application.NSView;
import ch.cyberduck.binding.foundation.NSNotification;
import ch.cyberduck.binding.foundation.NSNotificationCenter;
import ch.cyberduck.core.Cache;
import ch.cyberduck.core.LocaleFactory;
import ch.cyberduck.core.PasswordStrengthValidator;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.StringAppender;
import ch.cyberduck.core.features.Location;
import ch.cyberduck.core.resources.IconCacheFactory;
import ch.cyberduck.core.vault.VaultCredentials;

import org.apache.commons.lang3.StringUtils;
import org.rococoa.Foundation;
import org.rococoa.cocoa.foundation.NSRect;

import java.util.EnumSet;
import java.util.Set;

public class VaultController extends FolderController {

    private final Callback callback;

    @Outlet
    private NSSecureTextField passwordField;
    @Outlet
    private NSSecureTextField confirmField;
    @Outlet
    private NSLevelIndicator strengthIndicator;

    private final NSNotificationCenter notificationCenter
            = NSNotificationCenter.defaultCenter();

    private final PasswordStrengthValidator passwordStrengthValidator = new PasswordStrengthValidator();

    public VaultController(final Path workdir, final Path selected, final Cache<Path> cache, final Set<Location.Name> regions, final Location.Name defaultRegion, final Callback callback) {
        super(workdir, selected, cache, regions, defaultRegion, new FolderController.Callback() {
            @Override
            public void callback(final Path folder, final String region) {
                //
            }
        });
        this.callback = callback;
    }

    @Override
    public NSAlert loadAlert() {
        final NSAlert alert = NSAlert.alert();
        alert.setAlertStyle(NSAlert.NSInformationalAlertStyle);
        alert.setMessageText(LocaleFactory.localizedString("Create Vault", "Cryptomator"));
        final String message = LocaleFactory.localizedString("Enter the name for the new folder", "Folder");
        alert.setInformativeText(new StringAppender().append(message).toString());
        alert.addButtonWithTitle(LocaleFactory.localizedString("Create Vault", "Cryptomator"));
        alert.addButtonWithTitle(LocaleFactory.localizedString("Cancel", "Folder"));
        alert.setIcon(IconCacheFactory.<NSImage>get().iconNamed("cryptomator.tiff", 64));
        return alert;
    }

    @Action
    public void passwordFieldTextDidChange(final NSNotification notification) {
        strengthIndicator.setIntValue(passwordStrengthValidator.getScore(passwordField.stringValue()).getScore());
    }

    public NSView getAccessoryView(final NSAlert alert) {
        final NSView accessoryView = NSView.create();
        confirmField = NSSecureTextField.textFieldWithString(StringUtils.EMPTY);
        confirmField.cell().setPlaceholderString(LocaleFactory.localizedString("Confirm Passphrase", "Cryptomator"));
        this.addAccessorySubview(accessoryView, confirmField);

        strengthIndicator = NSLevelIndicator.levelIndicatorWithFrame(new NSRect(0, 18));
        strengthIndicator.setTickMarkPosition(1);
        if(strengthIndicator.respondsToSelector(Foundation.selector("setLevelIndicatorStyle:"))) {
            strengthIndicator.setLevelIndicatorStyle(NSLevelIndicator.NSDiscreteCapacityLevelIndicatorStyle);
        }
        this.addAccessorySubview(accessoryView, strengthIndicator);
        passwordField = NSSecureTextField.textFieldWithString(StringUtils.EMPTY);
        passwordField.cell().setPlaceholderString(LocaleFactory.localizedString("Passphrase", "Cryptomator"));
        notificationCenter.addObserver(this.id(),
                Foundation.selector("passwordFieldTextDidChange:"),
                NSControl.NSControlTextDidChangeNotification,
                passwordField.id());
        this.addAccessorySubview(accessoryView, passwordField);

        this.addAccessorySubview(accessoryView, super.getAccessoryView(alert));
        return accessoryView;
    }

    @Override
    public boolean validate(final int option) {
        if(super.validate(option)) {
            if(StringUtils.isBlank(passwordField.stringValue())) {
                return false;
            }
            if(StringUtils.isBlank(confirmField.stringValue())) {
                return false;
            }
            if(!StringUtils.equals(passwordField.stringValue(), confirmField.stringValue())) {
                return false;
            }
            return true;
        }
        return false;
    }

    @Override
    public void callback(final int returncode, final Path file) {
        file.setType(EnumSet.of(Path.Type.directory));
        final VaultCredentials credentials = new VaultCredentials(passwordField.stringValue()).withSaved(this.isSuppressed());
        callback.callback(file, this.getLocation(), credentials);
    }

    public interface Callback {
        void callback(final Path folder, final String region, final VaultCredentials passphrase);
    }
}
