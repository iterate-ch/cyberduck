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
import ch.cyberduck.core.threading.WorkerBackgroundAction;
import ch.cyberduck.core.worker.CreateSymlinkWorker;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;

import java.text.MessageFormat;
import java.util.Collections;
import java.util.EnumSet;

public class CreateSymlinkController extends FileController {

    public CreateSymlinkController(final BrowserController parent, final Cache<Path> cache) {
        super(parent, cache, NSAlert.alert(
                LocaleFactory.localizedString("Create new symbolic link", "File"),
                StringUtils.EMPTY,
                LocaleFactory.localizedString("Create", "File"),
                null,
                LocaleFactory.localizedString("Cancel", "File")
        ));
        alert.setIcon(IconCacheFactory.<NSImage>get().aliasIcon(null, 64));
        final Path selected = this.getSelected();
        inputField.setStringValue(FilenameUtils.getBaseName(selected.getName()));
        this.setMessage(MessageFormat.format(LocaleFactory.localizedString("Enter the name for the new symbolic link for {0}", "File"),
                selected.getName()));
    }

    @Override
    public void callback(final int returncode) {
        if(returncode == DEFAULT_OPTION) {
            final Path selected = this.getSelected();
            this.run(selected, new Path(this.getWorkdir(), inputField.stringValue(), EnumSet.of(Path.Type.file)));
        }
    }

    protected void run(final Path selected, final Path link) {
        parent.background(new WorkerBackgroundAction<Path>(parent, parent.getSession(), new CreateSymlinkWorker(link, selected) {
            @Override
            public void cleanup(final Path symlink) {
                if(symlink.getName().startsWith(".")) {
                    parent.setShowHiddenFiles(true);
                }
                parent.reload(parent.workdir(), Collections.singletonList(link), Collections.singletonList(link));
            }
        }));
    }
}