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
import org.apache.commons.lang3.StringUtils;

public class DriveFileidProvider implements IdProvider {

    private final DriveSession session;

    private Cache<Path> cache = PathCache.empty();

    public DriveFileidProvider(final DriveSession session) {
        this.session = session;
    }

    @Override
    public String getFileid(final Path file, final ListProgressListener listener) throws BackgroundException {
        if(StringUtils.isNotBlank(file.attributes().getVersionId())) {
            return file.attributes().getVersionId();
        }
        if(file.isRoot()
            || file.equals(DriveHomeFinderService.MYDRIVE_FOLDER)
            || file.equals(DriveHomeFinderService.SHARED_FOLDER_NAME)
            || file.equals(DriveHomeFinderService.TEAM_DRIVES_NAME)) {
            return DriveHomeFinderService.ROOT_FOLDER_ID;
        }
        if(cache.isCached(file.getParent())) {
            final AttributedList<Path> list = cache.get(file.getParent());
            final Path found = list.find(new IgnoreTrashedPathPredicate(file));
            if(null != found) {
                if(StringUtils.isNotBlank(found.attributes().getVersionId())) {
                    return this.set(file, found.attributes().getVersionId());
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
            return this.set(file, found.attributes().getVersionId());
        }
        final Path query;
        if(file.getType().contains(Path.Type.placeholder)) {
            query = new Path(file.getParent(), FilenameUtils.removeExtension(file.getName()), file.getType(), file.attributes());
        }
        else {
            query = file;
        }
        final AttributedList<Path> list = new FileidDriveListService(session, this, query).list(file.getParent(), new DisabledListProgressListener());
        final Path found = list.find(new IgnoreTrashedPathPredicate(file));
        if(null == found) {
            throw new NotfoundException(file.getAbsolute());
        }
        return this.set(file, found.attributes().getVersionId());
    }

    protected String set(final Path file, final String id) {
        file.attributes().setVersionId(id);
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
}
