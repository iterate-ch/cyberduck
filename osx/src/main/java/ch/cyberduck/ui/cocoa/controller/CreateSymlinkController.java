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
import ch.cyberduck.core.resources.IconCacheFactory;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;

import java.text.MessageFormat;
import java.util.EnumSet;

public class CreateSymlinkController extends FileController {

    private final Callback callback;

    public CreateSymlinkController(final Path workdir, final Path selected, final Cache<Path> cache, final Callback callback) {
        super(workdir, selected, cache, NSAlert.alert(
                LocaleFactory.localizedString("Create new symbolic link", "File"),
                StringUtils.EMPTY,
                LocaleFactory.localizedString("Create", "File"),
                null,
                LocaleFactory.localizedString("Cancel", "File")
        ));
        this.callback = callback;
        alert.setIcon(IconCacheFactory.<NSImage>get().aliasIcon(null, 64));
        inputField.setStringValue(FilenameUtils.getBaseName(selected.getName()));
        this.setMessage(MessageFormat.format(LocaleFactory.localizedString("Enter the name for the new symbolic link for {0}", "File"),
                selected.getName()));
    }

    @Override
    public void callback(final int returncode) {
        if(returncode == DEFAULT_OPTION) {
            final Path selected = this.getSelected();
            callback.callback(selected, new Path(this.getWorkdir(), inputField.stringValue(), EnumSet.of(Path.Type.file)));
        }
    }

    public interface Callback {
        void callback(final Path selected, final Path link);
    }
}