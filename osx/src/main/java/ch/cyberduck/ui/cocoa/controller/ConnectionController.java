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
import ch.cyberduck.core.Host;
import ch.cyberduck.core.LoginOptions;

import org.rococoa.Foundation;

public class ConnectionController extends BookmarkContentViewController {

    private final Host bookmark;
    private final LoginOptions options;

    @Outlet
    private NSButton keychainCheckbox;

    public ConnectionController(final Host bookmark) {
        this(bookmark, new LoginOptions(bookmark.getProtocol()));
    }

    public ConnectionController(final Host bookmark, final LoginOptions options) {
        super(bookmark, new DefaultBookmarkController(bookmark, options));
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
}
