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
import ch.cyberduck.binding.application.NSButton;
import ch.cyberduck.binding.application.NSCell;
import ch.cyberduck.binding.application.NSSecureTextField;
import ch.cyberduck.binding.foundation.NSNotification;
import ch.cyberduck.core.Host;
import ch.cyberduck.core.LoginOptions;

import org.apache.commons.lang3.StringUtils;
import org.rococoa.Foundation;

public class ConnectionController extends BookmarkContainerController {

    private final Host bookmark;
    private final LoginOptions options;

    @Outlet
    private NSButton keychainCheckbox;

    @Outlet
    private NSSecureTextField passwordField;

    public ConnectionController(final Host bookmark) {
        this(bookmark, new LoginOptions(bookmark.getProtocol()));
    }

    public ConnectionController(final Host bookmark, final LoginOptions options) {
        super(bookmark, options);
        this.bookmark = bookmark;
        this.options = options;
    }

    @Override
    protected String getBundleName() {
        return "Connection";
    }

    @Override
    public void callback(final int returncode) {
        if(CANCEL_OPTION == returncode) {
            bookmark.getCredentials().setPassword(null);
        }
    }

    @Outlet
    public void setKeychainCheckbox(NSButton keychainCheckbox) {
        this.keychainCheckbox = keychainCheckbox;
        this.keychainCheckbox.setTarget(this.id());
        this.keychainCheckbox.setAction(Foundation.selector("keychainCheckboxClicked:"));
        this.keychainCheckbox.setEnabled(options.keychain);
        this.keychainCheckbox.setState(bookmark.getCredentials().isSaved() ? NSCell.NSOnState : NSCell.NSOffState);
    }

    @Action
    public void keychainCheckboxClicked(final NSButton sender) {
        bookmark.getCredentials().setSaved(sender.state() == NSCell.NSOnState);
    }

    @Override
    public void setPasswordField(final NSSecureTextField field) {
        this.passwordField = field;
        super.setPasswordField(field);
    }

    @Override
    public void passwordFieldTextDidEndEditing(final NSNotification notification) {
        // Use password for this connection only without saving in keychain
        bookmark.getCredentials().setPassword(StringUtils.strip(passwordField.stringValue()));
    }

    @Override
    protected void setPasswordFromKeychain(final String password) {
        if(StringUtils.isNotBlank(password)) {
            super.setPasswordFromKeychain(password);
            bookmark.getCredentials().setPassword(password);
        }
        // Otherwise keep password entered for this connection
    }

    @Override
    protected boolean isPasswordEditable() {
        // Allow to enter password regardless of keychain preference
        return options.password;
    }
}
