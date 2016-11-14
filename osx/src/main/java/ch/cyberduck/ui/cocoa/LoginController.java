package ch.cyberduck.ui.cocoa;

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

import ch.cyberduck.binding.HyperlinkAttributedStringFactory;
import ch.cyberduck.binding.Outlet;
import ch.cyberduck.binding.application.NSCell;
import ch.cyberduck.binding.application.NSImage;
import ch.cyberduck.binding.application.NSImageView;
import ch.cyberduck.binding.application.NSTextField;
import ch.cyberduck.core.Credentials;
import ch.cyberduck.core.Host;
import ch.cyberduck.core.LocaleFactory;
import ch.cyberduck.core.LoginOptions;
import ch.cyberduck.core.Scheme;
import ch.cyberduck.core.StringAppender;
import ch.cyberduck.core.resources.IconCacheFactory;
import ch.cyberduck.ui.LoginInputValidator;

public class LoginController extends ConnectionController {

    private final String title;
    private final String reason;

    @Outlet
    private NSImageView iconView;
    @Outlet
    private NSTextField titleField;
    @Outlet
    private NSTextField textField;

    public LoginController(final String title, final String reason,
                           final Host bookmark, final Credentials credentials, final LoginOptions options) {
        super(bookmark, credentials, new LoginInputValidator(credentials, bookmark, options));
        this.title = title;
        this.reason = reason;
        this.addObserver(new BookmarkObserver() {
            @Override
            public void change(final Host bookmark) {
                if(!options.user) {
                    usernameField.setEnabled(false);
                }
                if(!options.password) {
                    passwordField.setEnabled(false);
                }
                if(!options.keychain) {
                    keychainCheckbox.setEnabled(false);
                    keychainCheckbox.setState(NSCell.NSOffState);
                }
                if(!options.anonymous) {
                    anonymousCheckbox.setEnabled(false);
                    anonymousCheckbox.setState(NSCell.NSOffState);
                }
                if(!options.publickey) {
                    privateKeyPopup.setEnabled(false);
                    privateKeyPopup.selectItemWithTitle(LocaleFactory.localizedString("None"));
                }
            }
        });
    }

    @Override
    protected String getBundleName() {
        return "Login";
    }

    @Override
    public void awakeFromNib() {
        super.awakeFromNib();
        window.makeFirstResponder(usernameField);
    }

    public void setIconView(NSImageView iconView) {
        this.iconView = iconView;
        this.iconView.setImage(IconCacheFactory.<NSImage>get().iconNamed(bookmark.getProtocol().disk()));
    }

    public void setTitleField(NSTextField titleField) {
        this.titleField = titleField;
        this.updateField(this.titleField, LocaleFactory.localizedString(title, "Credentials"));
    }

    public void setTextField(NSTextField textField) {
        this.textField = textField;
        this.textField.setSelectable(true);
        if(reason.startsWith(Scheme.http.name())) {
            // For OAuth2
            this.textField.setAttributedStringValue(HyperlinkAttributedStringFactory.create(reason));
            this.textField.setAllowsEditingTextAttributes(true);
            this.textField.setSelectable(true);
        }
        else {
            this.updateField(this.textField, new StringAppender().append(reason).toString());
        }
    }

}