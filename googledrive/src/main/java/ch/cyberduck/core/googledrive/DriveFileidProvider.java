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
import ch.cyberduck.core.Cache;
import ch.cyberduck.core.DisabledListProgressListener;
import ch.cyberduck.core.ListProgressListener;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathCache;
import ch.cyberduck.core.SimplePathPredicate;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.exception.NotfoundException;
import ch.cyberduck.core.features.IdProvider;

import org.apache.commons.io.FilenameUtils;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

public class DriveFileidProvider implements IdProvider {

    public static final String KEY_FILE_ID = "file_id";

    private final DriveSession session;

    private Cache<Path> cache = PathCache.empty();

    public DriveFileidProvider(final DriveSession session) {
        this.session = session;
    }

    @Override
    public String getFileid(final Path file, final ListProgressListener listener) throws BackgroundException {
        if(file.attributes().getCustom().containsKey(KEY_FILE_ID)) {
            return file.attributes().getCustom().get(KEY_FILE_ID);
        }
        if(file.isRoot()
            || file.equals(DriveHomeFinderService.MYDRIVE_FOLDER)
            || file.equals(DriveHomeFinderService.SHARED_FOLDER_NAME)
            || file.equals(DriveHomeFinderService.TEAM_DRIVES_NAME)) {
            return DriveHomeFinderService.ROOT_FOLDER_ID;
        }
        if(cache.isCached(file.getParent())) {
            final AttributedList<Path> list = cache.get(file.getParent());
            final Path found = list.filter(new IgnoreTrashedComparator()).find(new SimplePathPredicate(file));
            if(null != found) {
                if(found.attributes().getCustom().containsKey(KEY_FILE_ID)) {
                    return this.set(file, found.attributes().getCustom().get(KEY_FILE_ID));
                }
            }
        }
        if(DriveHomeFinderService.TEAM_DRIVES_NAME.equals(file.getParent())) {
            final Path found = new DriveTeamDrivesListService(session).withCache(cache).list(file.getParent(), listener).find(
                new SimplePathPredicate(file)
            );
            if(null == found) {
                throw new NotfoundException(file.getAbsolute());
            }
            return this.set(file, found.attributes().getCustom().get(KEY_FILE_ID));
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
        return this.set(file, found.attributes().getCustom().get(KEY_FILE_ID));
    }

    protected String set(final Path file, final String id) {
        final Map<String, String> custom = new HashMap<>(file.attributes().getCustom());
        custom.put(KEY_FILE_ID, id);
        file.attributes().setCustom(custom);
        return id;
    }

    @Override
    public DriveFileidProvider withCache(final Cache<Path> cache) {
        this.cache = cache;
        return this;
    }

    public static final class IgnoreTrashedPathPredicate extends SimplePathPredicate {
        public IgnoreTrashedPathPredicate(final Path file) {
            super(file);
        }

        @Override
        public boolean test(final Path test) {
            if(test.attributes().isDuplicate()) {
                // Ignore trashed files
                return false;
            }
            return super.test(test);
        }
    }

    private static final class IgnoreTrashedComparator implements Comparator<Path> {
        @Override
        public int compare(final Path o1, final Path o2) {
            return Boolean.compare(o1.attributes().isDuplicate(), o2.attributes().isDuplicate());
        }
    }
}
