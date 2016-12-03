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
import ch.cyberduck.core.PasswordCallbackFactory;
import ch.cyberduck.core.PasswordStoreFactory;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.features.Location;
import ch.cyberduck.core.resources.IconCacheFactory;
import ch.cyberduck.core.threading.WorkerBackgroundAction;
import ch.cyberduck.core.worker.CreateVaultWorker;
import ch.cyberduck.ui.browser.UploadTargetFinder;

import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;

public class VaultController extends FolderController {
    private final BrowserController parent;

    public VaultController(final BrowserController parent, final Cache<Path> cache, final Set<Location.Name> regions) {
        super(parent, cache, regions, NSAlert.alert(
                LocaleFactory.localizedString("Create Vault", "Cryptomator"),
                LocaleFactory.localizedString("Enter the name for the new folder:", "Folder"),
                LocaleFactory.localizedString("Create Vault", "Cryptomator"),
                null,
                LocaleFactory.localizedString("Cancel", "Folder")
        ));
        this.alert.setIcon(IconCacheFactory.<NSImage>get().iconNamed("cryptomator.tiff", 64));
        this.parent = parent;
    }

    @Override
    public void callback(final int returncode) {
        if(returncode == DEFAULT_OPTION) {
            final String filename = inputField.stringValue();
            final Path folder = new Path(new UploadTargetFinder(this.getWorkdir()).find(this.getSelected()),
                    filename, EnumSet.of(Path.Type.directory));
            parent.background(new WorkerBackgroundAction<Boolean>(parent, parent.getSession(),
                    new CreateVaultWorker(folder, this.getLocation(), PasswordStoreFactory.get(), PasswordCallbackFactory.get(parent)) {
                        @Override
                        public void cleanup(final Boolean done) {
                            parent.reload(parent.workdir(), Collections.singletonList(folder), Collections.singletonList(folder));
                        }
                    })
            );
        }
    }
}
