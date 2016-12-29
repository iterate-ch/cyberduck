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
import ch.cyberduck.ui.browser.UploadTargetFinder;

import java.util.EnumSet;

public class CreateFileController extends FileController {

    private final Callback callback;

    public CreateFileController(final Path workdir, final Path selected, final Cache<Path> cache, final Callback callback) {
        super(workdir, selected, cache, NSAlert.alert(
                LocaleFactory.localizedString("Create new file", "File"),
                LocaleFactory.localizedString("Enter the name for the new file", "File"),
                LocaleFactory.localizedString("Create", "File"),
                EditorFactory.instance().getDefaultEditor() != Application.notfound ? LocaleFactory.localizedString("Edit", "File") : null,
                LocaleFactory.localizedString("Cancel", "File")
        ));
        this.callback = callback;
        alert.setIcon(IconCacheFactory.<NSImage>get().documentIcon(null, 64));
    }

    @Override
    public void callback(final int returncode) {
        final Path parent = new UploadTargetFinder(this.getWorkdir()).find(this.getSelected());
        switch(returncode) {
            case DEFAULT_OPTION:
                this.run(parent, inputField.stringValue(), false);
                break;
            case ALTERNATE_OPTION:
                this.run(parent, inputField.stringValue(), true);
                break;
        }
    }

    private void run(final Path directory, final String filename, final boolean edit) {
        final Path file = new Path(directory, filename, EnumSet.of(Path.Type.file));
        callback.callback(edit, file);
    }

    public interface Callback {
        public void callback(final boolean edit, final Path file);
    }
}