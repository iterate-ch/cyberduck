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
import ch.cyberduck.core.DefaultProviderHelpService;
import ch.cyberduck.core.LocaleFactory;
import ch.cyberduck.core.PasswordStrengthValidator;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.features.Location;
import ch.cyberduck.core.resources.IconCacheFactory;
import ch.cyberduck.core.vault.VaultCredentials;

import org.apache.commons.lang3.StringUtils;
import org.rococoa.Foundation;
import org.rococoa.cocoa.foundation.NSPoint;
import org.rococoa.cocoa.foundation.NSRect;

import java.util.EnumSet;
import java.util.Set;

public class VaultController extends FolderController {

    private final Callback callback;

    @Outlet
    private NSView view;
    @Outlet
    private NSSecureTextField passwordField;
    @Outlet
    private NSSecureTextField confirmField;
    @Outlet
    private NSLevelIndicator strengthIndicator;

    private final NSNotificationCenter notificationCenter
            = NSNotificationCenter.defaultCenter();

    private final PasswordStrengthValidator passwordStrengthValidator = new PasswordStrengthValidator();

    public VaultController(final Path workdir, final Path selected, final Cache<Path> cache, final Set<Location.Name> regions, final Callback callback) {
        super(workdir, selected, cache, regions, new FolderController.Callback() {
            @Override
            public void callback(final Path folder, final String region) {
                //
            }
        });
        this.callback = callback;
    }

    @Override
    public void loadBundle() {
        final NSAlert alert = NSAlert.alert();
        alert.setAlertStyle(NSAlert.NSInformationalAlertStyle);
        alert.setMessageText(LocaleFactory.localizedString("Create Vault", "Cryptomator"));
        alert.setInformativeText(LocaleFactory.localizedString("Enter the name for the new folder", "Folder"));
        alert.addButtonWithTitle(LocaleFactory.localizedString("Create Vault", "Cryptomator"));
        alert.addButtonWithTitle(LocaleFactory.localizedString("Cancel", "Folder"));
        alert.setIcon(IconCacheFactory.<NSImage>get().iconNamed("cryptomator.tiff", 64));
        super.loadBundle(alert);
    }

    @Action
    public void passwordFieldTextDidChange(NSNotification notification) {
        strengthIndicator.setIntValue(passwordStrengthValidator.getScore(passwordField.stringValue()).getScore());
    }

    public NSView getAccessoryView(final NSAlert alert) {
        view = NSView.create(new NSRect(alert.window().frame().size.width.doubleValue(), 0));
        confirmField = NSSecureTextField.textfieldWithFrame(new NSRect(alert.window().frame().size.width.doubleValue(), 22));
        confirmField.cell().setPlaceholderString(LocaleFactory.localizedString("Confirm Passphrase", "Cryptomator"));
        confirmField.setFrameOrigin(new NSPoint(0, 0));
        view.addSubview(confirmField);

        strengthIndicator = NSLevelIndicator.levelIndicatorWithFrame(new NSRect(alert.window().frame().size.width.doubleValue(), 18));
        strengthIndicator.setLevelIndicatorStyle(NSLevelIndicator.NSDiscreteCapacityLevelIndicatorStyle);
        strengthIndicator.setFrameOrigin(new NSPoint(0, this.getFrame(alert, view).size.height.doubleValue() + view.subviews().count().doubleValue() * SUBVIEWS_VERTICAL_SPACE));
        view.addSubview(strengthIndicator);

        passwordField = NSSecureTextField.textfieldWithFrame(new NSRect(alert.window().frame().size.width.doubleValue(), 22));
        passwordField.cell().setPlaceholderString(LocaleFactory.localizedString("Passphrase", "Cryptomator"));
        notificationCenter.addObserver(this.id(),
                Foundation.selector("passwordFieldTextDidChange:"),
                NSControl.NSControlTextDidChangeNotification,
                passwordField);
        passwordField.setFrameOrigin(new NSPoint(0, this.getFrame(alert, view).size.height.doubleValue() + view.subviews().count().doubleValue() * SUBVIEWS_VERTICAL_SPACE));
        view.addSubview(passwordField);

        final NSView accessory = super.getAccessoryView(alert);
        accessory.setFrameSize(this.getFrame(alert, accessory).size);
        accessory.setFrameOrigin(new NSPoint(0, this.getFrame(alert, view).size.height.doubleValue() + view.subviews().count().doubleValue() * SUBVIEWS_VERTICAL_SPACE));
        view.addSubview(accessory);
        return view;
    }

    @Override
    public boolean validate() {
        if(super.validate()) {
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
    protected String help() {
        return new DefaultProviderHelpService().help("/howto/cryptomator");
    }

    @Override
    public void callback(final int returncode, final Path file) {
        file.setType(EnumSet.of(Path.Type.directory));
        final VaultCredentials credentials = new VaultCredentials(passwordField.stringValue());
        credentials.setSaved(this.isSuppressed());
        callback.callback(file, this.getLocation(), credentials);
    }

    public interface Callback {
        void callback(final Path folder, final String region, final VaultCredentials passphrase);
    }
}
