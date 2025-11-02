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

import ch.cyberduck.binding.application.NSButton;
import ch.cyberduck.binding.application.NSPopUpButton;
import ch.cyberduck.binding.application.NSSecureTextField;
import ch.cyberduck.binding.application.NSTextField;
import ch.cyberduck.core.Host;
import ch.cyberduck.core.LoginOptions;
import ch.cyberduck.core.Protocol;
import ch.cyberduck.core.preferences.HostPreferencesFactory;
import ch.cyberduck.ui.LoginInputValidator;

public abstract class DefaultBookmarkController extends BookmarkController {

    private final Host bookmark;
    private final LoginOptions options;

    public DefaultBookmarkController(final Host bookmark) {
        this(bookmark, new LoginOptions(bookmark.getProtocol()));
    }

    public DefaultBookmarkController(final Host bookmark, final LoginOptions options) {
        super(bookmark, new LoginInputValidator(bookmark, options), options);
        this.bookmark = bookmark;
        this.options = options;
    }

    public DefaultBookmarkController(final Host bookmark, final LoginInputValidator validator, final LoginOptions options) {
        super(bookmark, validator, options);
        this.bookmark = bookmark;
        this.options = options;
    }

    @Override
    public void setProtocolPopup(final NSPopUpButton button) {
        button.superview().setHidden(!HostPreferencesFactory.get(bookmark).getBoolean("bookmark.protocol.configurable"));
        super.setProtocolPopup(button);
    }

    @Override
    public void setPasswordField(final NSSecureTextField field) {
        this.addObserver(host -> field.superview().setHidden(!options.password));
        super.setPasswordField(field);
    }

    @Override
    public void setCertificatePopup(final NSPopUpButton button) {
        this.addObserver(host -> button.superview().setHidden(!options.certificate));
        super.setCertificatePopup(button);
    }

    @Override
    public void setTimezonePopup(final NSPopUpButton button) {
        this.addObserver(host -> button.superview().setHidden(host.getProtocol().isUTCTimezone()));
        super.setTimezonePopup(button);
    }

    @Override
    public void setEncodingPopup(final NSPopUpButton button) {
        this.addObserver(host -> button.superview().setHidden(!host.getProtocol().isEncodingConfigurable()));
        super.setEncodingPopup(button);
    }

    @Override
    public void setFtpModePopup(final NSPopUpButton button) {
        this.addObserver(host -> button.superview().setHidden(!(host.getProtocol().getType() == Protocol.Type.ftp)));
        super.setFtpModePopup(button);
    }

    @Override
    public void setHostField(final NSTextField field) {
        this.addObserver(host -> field.superview().superview().setHidden(!host.getProtocol().isHostnameConfigurable() && !host.getProtocol().isPortConfigurable()));
        super.setHostField(field);
    }

    @Override
    public void setPortField(final NSTextField field) {
        this.addObserver(host -> field.superview().superview().setHidden(!host.getProtocol().isHostnameConfigurable() && !host.getProtocol().isPortConfigurable()));
        super.setPortField(field);
    }

    @Override
    public void setPathField(final NSTextField field) {
        this.addObserver(host -> field.superview().setHidden(!host.getProtocol().isPathConfigurable()));
        super.setPathField(field);
    }

    @Override
    public void setUsernameField(final NSTextField field) {
        this.addObserver(host -> field.superview().setHidden(!host.getProtocol().isUsernameConfigurable()));
        super.setUsernameField(field);
    }

    @Override
    public void setAnonymousCheckbox(final NSButton button) {
        this.addObserver(host -> button.superview().setHidden(!host.getProtocol().isAnonymousConfigurable()));
        super.setAnonymousCheckbox(button);
    }

    @Override
    public void setPrivateKeyPopup(final NSPopUpButton button) {
        this.addObserver(host -> button.superview().setHidden(!host.getProtocol().isPrivateKeyConfigurable()));
        super.setPrivateKeyPopup(button);
    }
}
