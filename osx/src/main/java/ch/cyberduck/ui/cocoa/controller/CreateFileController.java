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
import ch.cyberduck.core.threading.WorkerBackgroundAction;
import ch.cyberduck.core.worker.TouchWorker;
import ch.cyberduck.ui.browser.UploadTargetFinder;

import java.util.Collections;
import java.util.EnumSet;

public class CreateFileController extends FileController {

    public CreateFileController(final BrowserController parent, final Cache<Path> cache) {
        super(parent, cache, NSAlert.alert(
                LocaleFactory.localizedString("Create new file", "File"),
                LocaleFactory.localizedString("Enter the name for the new file:", "File"),
                LocaleFactory.localizedString("Create", "File"),
                EditorFactory.instance().getDefaultEditor() != Application.notfound ? LocaleFactory.localizedString("Edit", "File") : null,
                LocaleFactory.localizedString("Cancel", "File")
        ));
        alert.setIcon(IconCacheFactory.<NSImage>get().documentIcon(null, 64));
    }

    @Override
    public void callback(final int returncode) {
        final Path parent = new UploadTargetFinder(this.getWorkdir()).find(this.getSelected());
        if(returncode == DEFAULT_OPTION) {
            this.run(parent, inputField.stringValue(), false);
        }
        else if(returncode == ALTERNATE_OPTION) {
            this.run(parent, inputField.stringValue(), true);
        }
    }

    private void run(final Path directory, final String filename, final boolean edit) {
        final Path file = new Path(directory, filename, EnumSet.of(Path.Type.file));
        parent.background(new WorkerBackgroundAction<Boolean>(parent, parent.getSession(),
                new TouchWorker(file) {
                    @Override
                    public void cleanup(final Boolean done) {
                        if(filename.charAt(0) == '.') {
                            parent.setShowHiddenFiles(true);
                        }
                        parent.reload(parent.workdir(), Collections.singletonList(file), Collections.singletonList(file));
                        if(edit) {
                            file.attributes().setSize(0L);
                            parent.edit(file);
                        }
                    }
                }));
    }
}