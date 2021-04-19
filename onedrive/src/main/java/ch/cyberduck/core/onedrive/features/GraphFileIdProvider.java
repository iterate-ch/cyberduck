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
import ch.cyberduck.core.ListProgressListener;
import ch.cyberduck.core.ListService;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.exception.NotfoundException;
import ch.cyberduck.core.features.FileIdProvider;
import ch.cyberduck.core.onedrive.GraphSession;

import org.apache.commons.lang3.StringUtils;

public class GraphFileIdProvider implements FileIdProvider {

    private final GraphSession session;

    public GraphFileIdProvider(final GraphSession session) {
        this.session = session;
    }

    @Override
    public String getFileId(final Path file, final ListProgressListener listener) throws BackgroundException {
        if(StringUtils.isNotBlank(file.attributes().getFileId())) {
            return file.attributes().getFileId();
        }
        final AttributedList<Path> list = session._getFeature(ListService.class).list(file.getParent(), listener);
        final Path found = list.find(path -> file.getAbsolute().equals(path.getAbsolute()));
        if(null == found) {
            throw new NotfoundException(file.getAbsolute());
        }
        return this.set(file, found.attributes().getFileId());
    }

    protected String set(final Path file, final String id) {
        file.attributes().setFileId(id);
        return id;
    }
}
