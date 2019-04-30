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

import ch.cyberduck.core.AbstractPath;
import ch.cyberduck.core.AttributedList;
import ch.cyberduck.core.Cache;
import ch.cyberduck.core.ListProgressListener;
import ch.cyberduck.core.ListService;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathAttributes;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.preferences.PreferencesFactory;
import ch.cyberduck.core.storegate.io.swagger.client.ApiException;
import ch.cyberduck.core.storegate.io.swagger.client.api.FilesApi;
import ch.cyberduck.core.storegate.io.swagger.client.model.File;
import ch.cyberduck.core.storegate.io.swagger.client.model.FileContents;

import java.util.EnumSet;

public class StoregateListService implements ListService {

    private final StoregateSession session;

    public StoregateListService(final StoregateSession session) {
        this.session = session;
    }

    @Override
    public AttributedList<Path> list(final Path directory, final ListProgressListener listener) throws BackgroundException {
        return this.list(directory, listener, PreferencesFactory.get().getInteger("storegate.listing.chunksize"));
    }

    public AttributedList<Path> list(final Path directory, final ListProgressListener listener, final int chunksize) throws BackgroundException {
        try {
            final AttributedList<Path> children = new AttributedList<>();
            final StoregateAttributesFinderFeature feature = new StoregateAttributesFinderFeature(session);
            int pageIndex = 0;
            FileContents files;
            do {
                files = new FilesApi(this.session.getClient()).filesGet(
                    String.format("/Home/%s%s", session.username(), directory.getAbsolute()), //TODO handle team folder
                    pageIndex,
                    chunksize,
                    "Name asc",
                    0, // All
                    true,
                    false
                );
                for(File f : files.getFiles()) {
                    final PathAttributes attrs = feature.toAttributes(f);
                    final EnumSet<AbstractPath.Type> type = (f.getFlags() & File.FlagsEnum.Folder.getValue()) == 1 ?
                        EnumSet.of(Path.Type.directory) :
                        EnumSet.of(Path.Type.file);
                    final Path p = new Path(directory, f.getName(), type, attrs);
                    children.add(p);
                    listener.chunk(directory, children);
                }
                pageIndex++;
            }
            while(files.getTotalRowCount() == chunksize);
            return children;
        }
        catch(ApiException e) {
            throw new StoregateExceptionMappingService().map(e);
        }
    }

    @Override
    public ListService withCache(final Cache<Path> cache) {
        return this;
    }
}
