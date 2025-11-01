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
import ch.cyberduck.binding.application.NSMenuItem;
import ch.cyberduck.binding.application.NSOpenPanel;
import ch.cyberduck.binding.application.NSPopUpButton;
import ch.cyberduck.binding.application.NSSecureTextField;
import ch.cyberduck.binding.application.NSTextField;
import ch.cyberduck.binding.application.NSView;
import ch.cyberduck.binding.application.SheetCallback;
import ch.cyberduck.binding.foundation.NSNotification;
import ch.cyberduck.binding.foundation.NSNotificationCenter;
import ch.cyberduck.binding.foundation.NSObject;
import ch.cyberduck.binding.foundation.NSURL;
import ch.cyberduck.core.Host;
import ch.cyberduck.core.Local;
import ch.cyberduck.core.LocalFactory;
import ch.cyberduck.core.LocaleFactory;
import ch.cyberduck.core.LoginOptions;
import ch.cyberduck.core.ProviderHelpServiceFactory;
import ch.cyberduck.core.StringAppender;
import ch.cyberduck.core.local.FilesystemBookmarkResolverFactory;
import ch.cyberduck.core.preferences.HostPreferencesFactory;
import ch.cyberduck.core.resources.IconCacheFactory;
import ch.cyberduck.core.sftp.openssh.OpenSSHPrivateKeyConfigurator;
import ch.cyberduck.ui.LoginInputValidator;

import org.apache.commons.lang3.StringUtils;
import org.rococoa.Foundation;
import org.rococoa.ID;
import org.rococoa.Rococoa;
import org.rococoa.cocoa.foundation.NSInteger;

public class LoginController extends AlertController {

    private final Host bookmark;
    private final String title;
    private final String reason;
    private final LoginOptions options;

    @Outlet
    private NSTextField usernameField;
    @Outlet
    private NSTextField passwordField;
    @Outlet
    private NSPopUpButton privateKeyPopup;
    @Outlet
    private NSOpenPanel privateKeyOpenPanel;

    public LoginController(final Host bookmark, final String title, final String reason, final LoginOptions options) {
        super(new LoginInputValidator(bookmark, options));
        this.bookmark = bookmark;
        this.title = title;
        this.reason = reason;
        this.options = options;
    }

    @Override
    public NSAlert loadAlert() {
        final NSAlert alert = NSAlert.alert();
        alert.setAlertStyle(NSAlert.NSWarningAlertStyle);
        alert.setIcon(IconCacheFactory.<NSImage>get().iconNamed(options.icon, 64));
        alert.setMessageText(LocaleFactory.localizedString(title, "Credentials"));
        alert.setInformativeText(new StringAppender().append(reason).toString());
        alert.addButtonWithTitle(LocaleFactory.localizedString("Login", "Login"));
        alert.addButtonWithTitle(LocaleFactory.localizedString("Cancel", "Login"));
        if(options.anonymous) {
            alert.addButtonWithTitle(LocaleFactory.localizedString("Skip", "Transfer"));
        }
        alert.setShowsSuppressionButton(options.keychain);
        if(options.keychain) {
            alert.suppressionButton().setTitle(LocaleFactory.localizedString("Save Password", "Keychain"));
            alert.suppressionButton().setState(bookmark.getCredentials().isSaved() ? NSCell.NSOnState : NSCell.NSOffState);
        }
        return alert;
    }

    @Override
    public NSView getAccessoryView(final NSAlert alert) {
        final NSView accessoryView = NSView.create();
        if(options.publickey) {
            privateKeyPopup = NSPopUpButton.buttonPullsDown(false);
            privateKeyPopup.setTarget(this.id());
            privateKeyPopup.setAction(Foundation.selector("privateKeyPopupClicked:"));
            privateKeyPopup.removeAllItems();
            privateKeyPopup.addItemWithTitle(LocaleFactory.localizedString("None"));
            privateKeyPopup.lastItem().setRepresentedObject(StringUtils.EMPTY);
            privateKeyPopup.menu().addItem(NSMenuItem.separatorItem());
            for(Local key : new OpenSSHPrivateKeyConfigurator().list()) {
                privateKeyPopup.addItemWithTitle(key.getAbbreviatedPath());
                privateKeyPopup.lastItem().setRepresentedObject(key.getAbsolute());
            }
            // Choose another folder
            privateKeyPopup.menu().addItem(NSMenuItem.separatorItem());
            privateKeyPopup.addItemWithTitle(String.format("%s…", LocaleFactory.localizedString("Choose")));
            if(bookmark.getCredentials().isPublicKeyAuthentication()) {
                final Local key = bookmark.getCredentials().getIdentity();
                privateKeyPopup.selectItemAtIndex(privateKeyPopup.indexOfItemWithRepresentedObject(key.getAbsolute()));
                if(-1 == privateKeyPopup.indexOfItemWithRepresentedObject(key.getAbsolute()).intValue()) {
                    final NSInteger index = new NSInteger(0);
                    privateKeyPopup.insertItemWithTitle_atIndex(key.getAbbreviatedPath(), index);
                    privateKeyPopup.itemAtIndex(index).setRepresentedObject(key.getAbsolute());
                }
            }
            else {
                privateKeyPopup.selectItemWithTitle(LocaleFactory.localizedString("None"));
            }
            this.addAccessorySubview(accessoryView, privateKeyPopup);
        }
        if(options.password) {
            passwordField = NSSecureTextField.textFieldWithString(StringUtils.EMPTY);
            this.updateField(passwordField, bookmark.getCredentials().getPassword());
            passwordField.cell().setPlaceholderString(options.getPasswordPlaceholder());
            NSNotificationCenter.defaultCenter().addObserver(this.id(),
                    Foundation.selector("passwordFieldTextDidChange:"),
                    NSControl.NSControlTextDidChangeNotification,
                    passwordField.id());
            this.addAccessorySubview(accessoryView, passwordField);
        }
        if(options.user) {
            usernameField = NSTextField.textFieldWithString(StringUtils.EMPTY);
            this.updateField(usernameField, bookmark.getCredentials().getUsername());
            usernameField.cell().setPlaceholderString(options.getUsernamePlaceholder());
            NSNotificationCenter.defaultCenter().addObserver(this.id(),
                    Foundation.selector("usernameFieldTextDidChange:"),
                    NSControl.NSControlTextDidChangeNotification,
                    usernameField.id());
            this.addAccessorySubview(accessoryView, usernameField);
        }
        return accessoryView;
    }

    @Override
    protected void focus(final NSAlert alert) {
        super.focus(alert);
        if(options.user) {
            usernameField.selectText(null);
        }
        if(options.password && !StringUtils.isBlank(bookmark.getCredentials().getUsername())) {
            passwordField.selectText(null);
        }
    }

    @Override
    public void callback(final int returncode) {
        switch(returncode) {
            case ALTERNATE_OPTION:
                bookmark.getCredentials().setUsername(HostPreferencesFactory.get(bookmark).getProperty("connection.login.anon.name"));
                bookmark.getCredentials().setPassword(HostPreferencesFactory.get(bookmark).getProperty("connection.login.anon.pass"));
                break;
            case CANCEL_OPTION:
                bookmark.getCredentials().setPassword(null);
                break;
        }
    }

    @Override
    public boolean validate(final int option) {
        switch(option) {
            case DEFAULT_OPTION:
                return super.validate(option);
        }
        return true;
    }

    @Override
    public void suppressionButtonClicked(final NSButton sender) {
        super.suppressionButtonClicked(sender);
        bookmark.getCredentials().setSaved(sender.state() == NSCell.NSOnState);
    }

    @Action
    public void usernameFieldTextDidChange(final NSNotification notification) {
        bookmark.getCredentials().setUsername(StringUtils.trim(usernameField.stringValue()));
    }

    @Action
    public void passwordFieldTextDidChange(final NSNotification notification) {
        bookmark.getCredentials().setPassword(StringUtils.trim(passwordField.stringValue()));
    }

    @Action
    public void privateKeyPopupClicked(final NSPopUpButton sender) {
        final String selected = sender.selectedItem().representedObject();
        if(null == selected) {
            privateKeyOpenPanel = NSOpenPanel.openPanel();
            privateKeyOpenPanel.setCanChooseDirectories(false);
            privateKeyOpenPanel.setCanChooseFiles(true);
            privateKeyOpenPanel.setAllowsMultipleSelection(false);
            privateKeyOpenPanel.setMessage(LocaleFactory.localizedString("Select the private key in PEM or PuTTY format", "Credentials"));
            privateKeyOpenPanel.setPrompt(LocaleFactory.localizedString("Choose"));
            privateKeyOpenPanel.beginSheetForDirectory(LocalFactory.get("~/.ssh").getAbsolute(), null, this.window(), this.id(),
                    Foundation.selector("privateKeyPanelDidEnd:returnCode:contextInfo:"), null);
        }
        else {
            bookmark.getCredentials().setIdentity(StringUtils.isBlank(selected) ? null : LocalFactory.get(selected));
        }
    }

    @Action
    public void privateKeyPanelDidEnprivateKeyPanelDidEnd_returnCode_contextInfo(NSOpenPanel sheet, final int returncode, ID contextInfo) {
        switch(returncode) {
            case SheetCallback.DEFAULT_OPTION:
                final NSObject url = privateKeyOpenPanel.URLs().lastObject();
                if(url != null) {
                    final Local selected = LocalFactory.get(Rococoa.cast(url, NSURL.class).path());
                    selected.setBookmark(FilesystemBookmarkResolverFactory.get().create(selected));
                    bookmark.getCredentials().setIdentity(selected);
                }
                break;
            case SheetCallback.ALTERNATE_OPTION:
                bookmark.getCredentials().setIdentity(null);
                break;
        }
    }

    @Override
    protected String help() {
        return ProviderHelpServiceFactory.get().help(bookmark.getProtocol());
    }
}
