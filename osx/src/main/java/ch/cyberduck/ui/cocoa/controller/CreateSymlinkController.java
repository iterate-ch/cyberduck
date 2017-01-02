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

import ch.cyberduck.binding.application.NSAlert;
import ch.cyberduck.binding.application.NSImage;
import ch.cyberduck.binding.application.NSView;
import ch.cyberduck.core.Cache;
import ch.cyberduck.core.LocaleFactory;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.resources.IconCacheFactory;

import org.apache.commons.io.FilenameUtils;

import java.text.MessageFormat;

public class CreateSymlinkController extends FileController {

    private final Path selected;
    private final Callback callback;

    public CreateSymlinkController(final Path workdir, final Path selected, final Cache<Path> cache, final Callback callback) {
        super(workdir, selected, cache);
        this.selected = selected;
        this.callback = callback;
    }

    @Override
    public void loadBundle() {
        final NSAlert alert = NSAlert.alert();
        alert.setMessageText(LocaleFactory.localizedString("Create new symbolic link", "File"));
        alert.setInformativeText(MessageFormat.format(LocaleFactory.localizedString("Enter the name for the new symbolic link for {0}", "File"), selected.getName()));
        alert.addButtonWithTitle(LocaleFactory.localizedString("Create", "File"));
        alert.addButtonWithTitle(LocaleFactory.localizedString("Cancel", "File"));
        alert.setIcon(IconCacheFactory.<NSImage>get().aliasIcon(null, 64));
        super.loadBundle(alert);
    }

    @Override
    public NSView getAccessoryView(final NSAlert alert) {
        final NSView view = super.getAccessoryView(alert);
        inputField.setStringValue(FilenameUtils.getBaseName(selected.getName()));
        return view;
    }

    @Override
    public void callback(final int returncode, final Path file) {
        callback.callback(selected, file);
    }

    public interface Callback {
        void callback(final Path selected, final Path link);
    }
}