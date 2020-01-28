package ch.cyberduck.core;

/*
 * Copyright (c) 2002-2020 iterate GmbH. All rights reserved.
 * https://cyberduck.io/
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 */

import ch.cyberduck.core.exception.AccessDeniedException;
import ch.cyberduck.core.exception.LocalAccessDeniedException;
import ch.cyberduck.core.io.watchservice.WatchServiceFactory;
import ch.cyberduck.core.local.FileWatcher;
import ch.cyberduck.core.preferences.Preferences;
import ch.cyberduck.core.preferences.PreferencesFactory;

import org.apache.commons.io.FilenameUtils;
import org.apache.log4j.Logger;

import java.io.IOException;

public abstract class MonitorFolderHostCollection extends AbstractFolderHostCollection {
    private static final Logger log = Logger.getLogger(MonitorFolderHostCollection.class);

    private final Preferences preferences = PreferencesFactory.get();
    private final FileWatcher monitor = new FileWatcher(WatchServiceFactory.get());

    public MonitorFolderHostCollection(final Local f) {
        super(f);
    }

    @Override
    public void load() throws AccessDeniedException {
        super.load();
        if(preferences.getBoolean("bookmarks.folder.monitor")) {
            try {
                monitor.register(folder, FILE_FILTER, this);
            }
            catch(IOException e) {
                throw new LocalAccessDeniedException(String.format("Failure monitoring directory %s", folder.getName()), e);
            }
        }
    }

    @Override
    public void fileWritten(final Local file) {
        if(this.isLocked()) {
            log.debug(String.format("Skip reading bookmark from %s", file));
        }
        else {
            try {
                // Read from disk and re-insert to collection
                final Host bookmark = HostReaderFactory.get().read(file);
                final int index = this.indexOf(bookmark);
                if(index != -1) {
                    if(log.isDebugEnabled()) {
                        log.debug(String.format("Replace bookmark %s at index %d", bookmark, index));
                    }
                    this.replace(index, bookmark);
                }
            }
            catch(AccessDeniedException e) {
                log.warn(String.format("Failure reading file %s", file));
            }
        }
    }

    @Override
    public void fileDeleted(final Local file) {
        if(this.isLocked()) {
            log.debug(String.format("Skip reading bookmark from %s", file));
        }
        else {
            final Host bookmark = this.lookup(FilenameUtils.getBaseName(file.getName()));
            if(bookmark != null) {
                log.warn(String.format("Delete bookmark %s", bookmark));
                this.remove(bookmark);
            }
        }
    }

    @Override
    public void fileCreated(final Local file) {
        if(this.isLocked()) {
            log.debug(String.format("Skip reading bookmark from %s", file));
        }
        else {
            try {
                final Host bookmark = HostReaderFactory.get().read(file);
                log.warn(String.format("Add bookmark %s", bookmark));
                this.add(bookmark);
            }
            catch(AccessDeniedException e) {
                log.warn(String.format("Failure reading file %s", file));
            }
        }
    }
}
