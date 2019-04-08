package ch.cyberduck.core.storegate;

/*
 * Copyright (c) 2002-2019 iterate GmbH. All rights reserved.
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
import ch.cyberduck.core.Cache;
import ch.cyberduck.core.ListProgressListener;
import ch.cyberduck.core.ListService;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathAttributes;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.storegate.io.swagger.client.ApiException;
import ch.cyberduck.core.storegate.io.swagger.client.api.FilesApi;
import ch.cyberduck.core.storegate.io.swagger.client.model.File;
import ch.cyberduck.core.storegate.io.swagger.client.model.FileContents;

import java.util.EnumSet;

public class StoregateListService implements ListService {

    private final StoregateSession session;

    private final Integer PAGE_SIZE = 1000;

    public StoregateListService(final StoregateSession session) {
        this.session = session;
    }

    @Override
    public AttributedList<Path> list(final Path directory, final ListProgressListener listener) throws BackgroundException {
        try {
            final AttributedList<Path> children = new AttributedList<>();
            int pageIndex = 0;
            int rows = 0;
            do {
                final FileContents files = new FilesApi(this.session.getClient()).filesGet(
                    directory.getAbsolute(),
                    pageIndex,
                    PAGE_SIZE,
                    "Name asc",
                    0, // All
                    true,
                    false
                );
                for(File f : files.getFiles()) {
                    final PathAttributes attrs = new PathAttributes();
                    final Path p = new Path(directory, f.getName(), (f.getFlags().getValue() & 1) == 1 ? EnumSet.of(Path.Type.directory) :
                        EnumSet.of(Path.Type.file), attrs);
                    attrs.setModificationDate(f.getModified().getMillis());
                    attrs.setCreationDate(f.getCreated().getMillis());
                    attrs.setSize(f.getSize());
                    children.add(p);
                }
                rows = files.getTotalRowCount();
                pageIndex++;
            }
            while(rows == PAGE_SIZE);
            return children;
        }
        catch(ApiException e) {
            //TODO Exception handling
        }
        return AttributedList.emptyList();
    }

    @Override
    public ListService withCache(final Cache<Path> cache) {
        return this;
    }
}
