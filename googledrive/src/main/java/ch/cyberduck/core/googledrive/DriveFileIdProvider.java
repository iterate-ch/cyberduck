package ch.cyberduck.core.googledrive;

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

import ch.cyberduck.core.AttributedList;
import ch.cyberduck.core.DisabledListProgressListener;
import ch.cyberduck.core.ListProgressListener;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.SimplePathPredicate;
import ch.cyberduck.core.cache.LRUCache;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.exception.NotfoundException;
import ch.cyberduck.core.features.FileIdProvider;
import ch.cyberduck.core.preferences.PreferencesFactory;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import java.util.Comparator;

public class DriveFileIdProvider implements FileIdProvider {
    private static final Logger log = Logger.getLogger(DriveFileIdProvider.class);

    private final DriveSession session;
    private final LRUCache<SimplePathPredicate, String> cache = LRUCache.build(PreferencesFactory.get().getLong("fileid.cache.size"));

    public DriveFileIdProvider(final DriveSession session) {
        this.session = session;
    }

    @Override
    public String getFileId(final Path file, final ListProgressListener listener) throws BackgroundException {
        if(StringUtils.isNotBlank(file.attributes().getFileId())) {
            return file.attributes().getFileId();
        }
        if(file.isRoot()
                || new SimplePathPredicate(file).test(DriveHomeFinderService.MYDRIVE_FOLDER)
                || new SimplePathPredicate(file).test(DriveHomeFinderService.SHARED_FOLDER_NAME)
                || new SimplePathPredicate(file).test(DriveHomeFinderService.SHARED_DRIVES_NAME)) {
            return DriveHomeFinderService.ROOT_FOLDER_ID;
        }
        if(cache.contains(new SimplePathPredicate(file))) {
            final String cached = cache.get(new SimplePathPredicate(file));
            if(log.isDebugEnabled()) {
                log.debug(String.format("Return cached fileid %s for file %s", cached, file));
            }
            return cached;
        }
        if(DriveHomeFinderService.SHARED_DRIVES_NAME.equals(file.getParent())) {
            final Path found = new DriveTeamDrivesListService(session, this).list(file.getParent(), listener).find(
                    new SimplePathPredicate(file)
            );
            if(null == found) {
                throw new NotfoundException(file.getAbsolute());
            }
            return this.cache(file, found.attributes().getFileId());
        }
        final Path query;
        if(file.getType().contains(Path.Type.placeholder)) {
            query = new Path(file.getParent(), FilenameUtils.removeExtension(file.getName()), file.getType(), file.attributes());
        }
        else {
            query = file;
        }
        final AttributedList<Path> list = new FileidDriveListService(session, this, query).list(file.getParent(), new DisabledListProgressListener());
        final Path found = list.filter(new IgnoreTrashedComparator()).find(new SimplePathPredicate(file));
        if(null == found) {
            throw new NotfoundException(file.getAbsolute());
        }
        return this.cache(file, found.attributes().getFileId());
    }

    public String cache(final Path file, final String id) {
        if(log.isDebugEnabled()) {
            log.debug(String.format("Cache %s for file %s", id, file));
        }
        if(null == id) {
            cache.remove(new SimplePathPredicate(file));
            file.attributes().setFileId(null);
        }
        else {
            cache.put(new SimplePathPredicate(file), id);
            file.attributes().setFileId(id);
        }
        return id;
    }

    @Override
    public void clear() {
        cache.clear();
    }

    private static final class IgnoreTrashedComparator implements Comparator<Path> {
        @Override
        public int compare(final Path o1, final Path o2) {
            return Boolean.compare(o1.attributes().isDuplicate(), o2.attributes().isDuplicate());
        }
    }
}
