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
import ch.cyberduck.core.Credentials;
import ch.cyberduck.core.LocaleFactory;
import ch.cyberduck.core.LoginOptions;
import ch.cyberduck.core.PasswordCallback;
import ch.cyberduck.core.PasswordStoreFactory;
import ch.cyberduck.core.PasswordStrengthValidator;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.exception.LoginCanceledException;
import ch.cyberduck.core.features.Location;
import ch.cyberduck.core.local.BrowserLauncherFactory;
import ch.cyberduck.core.preferences.PreferencesFactory;
import ch.cyberduck.core.resources.IconCacheFactory;
import ch.cyberduck.core.threading.WorkerBackgroundAction;
import ch.cyberduck.core.worker.CreateVaultWorker;
import ch.cyberduck.ui.browser.UploadTargetFinder;

import org.apache.commons.lang3.StringUtils;
import org.rococoa.Foundation;
import org.rococoa.cocoa.foundation.NSPoint;
import org.rococoa.cocoa.foundation.NSRect;

import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;

public class VaultController extends FolderController {

    private final NSNotificationCenter notificationCenter = NSNotificationCenter.defaultCenter();

    private final BrowserController parent;

    @Outlet
    private final NSSecureTextField passwordField;
    @Outlet
    private final NSSecureTextField confirmField;
    @Outlet
    private final NSLevelIndicator strengthIndicator;
    @Outlet
    private final NSView view;

    private final PasswordStrengthValidator passwordStrengthValidator = new PasswordStrengthValidator();

    public VaultController(final BrowserController parent, final Cache<Path> cache, final Set<Location.Name> regions) {
        super(parent, cache, regions, NSAlert.alert(
                LocaleFactory.localizedString("Create Vault", "Cryptomator"),
                LocaleFactory.localizedString("Enter the name for the new folder", "Folder"),
                LocaleFactory.localizedString("Create Vault", "Cryptomator"),
                null,
                LocaleFactory.localizedString("Cancel", "Folder")
        ));
        this.alert.setIcon(IconCacheFactory.<NSImage>get().iconNamed("cryptomator.tiff", 64));
        this.parent = parent;
        this.view = NSView.create();
        this.passwordField = NSSecureTextField.textfieldWithFrame(new NSRect(window.frame().size.width.doubleValue(), 22));
        this.passwordField.cell().setPlaceholderString(LocaleFactory.localizedString("Passphrase", "Cryptomator"));
        this.confirmField = NSSecureTextField.textfieldWithFrame(new NSRect(window.frame().size.width.doubleValue(), 22));
        this.confirmField.cell().setPlaceholderString(LocaleFactory.localizedString("Confirm Passphrase", "Cryptomator"));
        this.strengthIndicator = NSLevelIndicator.levelIndicatorWithFrame(new NSRect(window.frame().size.width.doubleValue(), 22));
        this.strengthIndicator.setLevelIndicatorStyle(NSLevelIndicator.NSDiscreteCapacityLevelIndicatorStyle);
        // this.strengthIndicator.setMinValue(PasswordStrengthValidator.Strength.veryweak.getScore());
        // this.strengthIndicator.setMaxValue(PasswordStrengthValidator.Strength.verystrong.getScore());
        this.notificationCenter.addObserver(this.id(),
                Foundation.selector("passwordFieldTextDidChange:"),
                NSControl.NSControlTextDidChangeNotification,
                this.passwordField);
    }

    @Action
    public void passwordFieldTextDidChange(NSNotification notification) {
        strengthIndicator.setIntValue(passwordStrengthValidator.getScore(passwordField.stringValue()).getScore());
    }

    public NSView getAccessoryView() {
        confirmField.setFrameOrigin(new NSPoint(0, 0));
        view.addSubview(confirmField);
        strengthIndicator.setFrameOrigin(new NSPoint(0, this.getFrame(view).size.height.doubleValue() + view.subviews().count().doubleValue() * SUBVIEWS_VERTICAL_SPACE));
        view.addSubview(strengthIndicator);
        passwordField.setFrameOrigin(new NSPoint(0, this.getFrame(view).size.height.doubleValue() + view.subviews().count().doubleValue() * SUBVIEWS_VERTICAL_SPACE));
        view.addSubview(passwordField);
        final NSView accessory = super.getAccessoryView();
        accessory.setFrame(this.getFrame(accessory));
        accessory.setFrameOrigin(new NSPoint(0, this.getFrame(view).size.height.doubleValue() + view.subviews().count().doubleValue() * SUBVIEWS_VERTICAL_SPACE));
        view.addSubview(accessory);
        return view;
    }

    @Override
    public void callback(final int returncode) {
        if(returncode == DEFAULT_OPTION) {
            final String filename = inputField.stringValue();
            final Path folder = new Path(new UploadTargetFinder(this.getWorkdir()).find(this.getSelected()),
                    filename, EnumSet.of(Path.Type.directory));
            final String passphrase = passwordField.stringValue();
            parent.background(new WorkerBackgroundAction<Boolean>(parent, parent.getSession(),
                    new CreateVaultWorker(folder, this.getLocation(), PasswordStoreFactory.get(), new PasswordCallback() {
                        @Override
                        public void prompt(final Credentials credentials, final String title, final String reason, final LoginOptions options) throws LoginCanceledException {
                            credentials.setPassword(passphrase);
                        }
                    }) {
                        @Override
                        public void cleanup(final Boolean done) {
                            parent.reload(parent.workdir(), Collections.singletonList(folder), Collections.singletonList(folder));
                        }
                    })
            );
        }
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
    protected void help() {
        final StringBuilder site = new StringBuilder(PreferencesFactory.get().getProperty("website.help"));
        site.append("/howto/cryptomator");
        BrowserLauncherFactory.get().open(site.toString());
    }
}
