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
import ch.cyberduck.core.Cache;
import ch.cyberduck.core.LocaleFactory;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.editor.EditorFactory;
import ch.cyberduck.core.local.Application;
import ch.cyberduck.core.resources.IconCacheFactory;

public class CreateFileController extends FileController {

    private final Callback callback;

    public CreateFileController(final Path workdir, final Path selected, final Cache<Path> cache, final Callback callback) {
        super(workdir, selected, cache);
        this.callback = callback;
    }

    @Override
    public void loadBundle() {
        final NSAlert alert = NSAlert.alert();
        alert.setAlertStyle(NSAlert.NSInformationalAlertStyle);
        alert.setMessageText(LocaleFactory.localizedString("Create new file", "File"));
        alert.setInformativeText(LocaleFactory.localizedString("Enter the name for the new file", "File"));
        alert.addButtonWithTitle(LocaleFactory.localizedString("Create", "File"));
        alert.addButtonWithTitle(LocaleFactory.localizedString("Cancel", "File"));
        if(EditorFactory.instance().getDefaultEditor() != Application.notfound) {
            alert.addButtonWithTitle(LocaleFactory.localizedString("Edit", "File"));
        }
        alert.setIcon(IconCacheFactory.<NSImage>get().documentIcon(null, 64));
        super.loadBundle(alert);
    }

    @Override
    public void callback(final int returncode, final Path file) {
        switch(returncode) {
            case DEFAULT_OPTION:
                callback.callback(false, file);
                break;
            case ALTERNATE_OPTION:
                callback.callback(true, file);
                break;
        }
    }

    public interface Callback {
        void callback(final boolean edit, final Path file);
    }
}