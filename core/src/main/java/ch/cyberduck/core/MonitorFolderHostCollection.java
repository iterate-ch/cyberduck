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
import ch.cyberduck.core.local.FileWatcherListener;
import ch.cyberduck.core.preferences.Preferences;
import ch.cyberduck.core.preferences.PreferencesFactory;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.util.Comparator;

public class MonitorFolderHostCollection extends AbstractFolderHostCollection implements FileWatcherListener {
    private static final Logger log = LogManager.getLogger(MonitorFolderHostCollection.class);

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
            log.debug("Skip reading bookmark from {}", file);
        }
        else {
            try {
                // Read from disk and re-insert to collection
                final Host bookmark = HostReaderFactory.get().read(file);
                final int index = this.indexOf(bookmark);
                if(index != -1) {
                    // Found bookmark with matching UUID
                    if(new HostEditComparator().compare(bookmark, this.get(index)) != 0) {
                        if(log.isDebugEnabled()) {
                            log.debug("Replace bookmark {} at index {}", bookmark, index);
                        }
                        this.replace(index, bookmark);
                    }
                }
            }
            catch(AccessDeniedException e) {
                log.warn("Failure reading file {}", file);
            }
        }
    }

    private static final class HostEditComparator implements Comparator<Host> {
        @Override
        public int compare(final Host o1, final Host o2) {
            final int i = o1.compareTo(o2);
            if(i == 0) {
                // Additionally to default fields check for nickname
                if(!StringUtils.equals(o1.getNickname(), o2.getNickname())) {
                    return StringUtils.compare(o1.getNickname(), o2.getNickname());
                }
                if(!StringUtils.equals(o1.getDefaultPath(), o2.getDefaultPath())) {
                    return StringUtils.compare(o1.getDefaultPath(), o2.getDefaultPath());
                }
                // Additionally to default fields check for changed labels
                if(!o1.getLabels().equals(o2.getLabels())) {
                    return -1;
                }
                return 0;
            }
            return i;
        }
    }

    @Override
    public void fileDeleted(final Local file) {
        if(this.isLocked()) {
            log.debug("Skip reading bookmark from {}", file);
        }
        else {
            final Host bookmark = this.lookup(FilenameUtils.getBaseName(file.getName()));
            if(bookmark != null) {
                log.warn("Delete bookmark {}", bookmark);
                this.remove(bookmark);
            }
        }
    }

    @Override
    public void fileCreated(final Local file) {
        if(this.isLocked()) {
            log.debug("Skip reading bookmark from {}", file);
        }
        else {
            try {
                final Host bookmark = HostReaderFactory.get().read(file);
                log.warn("Add bookmark {}", bookmark);
                this.add(bookmark);
            }
            catch(AccessDeniedException e) {
                log.warn("Failure reading file {}", file);
            }
        }
    }
}
