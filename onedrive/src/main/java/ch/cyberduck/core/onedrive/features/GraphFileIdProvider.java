package ch.cyberduck.core.onedrive.features;

/*
 * Copyright (c) 2002-2018 iterate GmbH. All rights reserved.
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

import ch.cyberduck.core.AttributedList;
import ch.cyberduck.core.CachingFileIdProvider;
import ch.cyberduck.core.DisabledListProgressListener;
import ch.cyberduck.core.ListService;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.SimplePathPredicate;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.exception.NotfoundException;
import ch.cyberduck.core.features.FileIdProvider;
import ch.cyberduck.core.onedrive.GraphSession;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class GraphFileIdProvider extends CachingFileIdProvider implements FileIdProvider {
    private static final Logger log = LogManager.getLogger(GraphFileIdProvider.class);

    private final GraphSession session;

    public GraphFileIdProvider(final GraphSession session) {
        super(session.getCaseSensitivity());
        this.session = session;
    }

    @Override
    public String getFileId(final Path file) throws BackgroundException {
        if(StringUtils.isNotBlank(file.attributes().getFileId())) {
            return file.attributes().getFileId();
        }
        final String cached = super.getFileId(file);
        if(cached != null) {
            log.debug("Return cached fileid {} for file {}", cached, file);
            return cached;
        }
        final AttributedList<Path> list = session._getFeature(ListService.class).list(file.getParent(),
                new DisabledListProgressListener());
        final Path found = list.find(new SymlinkUnawarePathPredicate(file));
        if(null == found) {
            throw new NotfoundException(file.getAbsolute());
        }
        return this.cache(file, found.attributes().getFileId());
    }

    private final static class SymlinkUnawarePathPredicate extends SimplePathPredicate {
        public SymlinkUnawarePathPredicate(final Path file) {
            super(file.isFile() ? Path.Type.file : Path.Type.directory, file.getAbsolute());
        }

        @Override
        public boolean test(final Path test) {
            return this.equals(new SymlinkUnawarePathPredicate(test));
        }
    }
}
