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
import ch.cyberduck.core.DisabledListProgressListener;
import ch.cyberduck.core.ListProgressListener;
import ch.cyberduck.core.ListService;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathAttributes;
import ch.cyberduck.core.PathNormalizer;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.preferences.PreferencesFactory;
import ch.cyberduck.core.storegate.io.swagger.client.ApiException;
import ch.cyberduck.core.storegate.io.swagger.client.api.FilesApi;
import ch.cyberduck.core.storegate.io.swagger.client.model.File;
import ch.cyberduck.core.storegate.io.swagger.client.model.FileContents;
import ch.cyberduck.core.storegate.io.swagger.client.model.RootFolder;

import java.util.EnumSet;

public class StoregateListService implements ListService {

    private final StoregateSession session;
    private final StoregateIdProvider fileid;

    private final int chunksize = PreferencesFactory.get().getInteger("storegate.listing.chunksize");

    public StoregateListService(final StoregateSession session, final StoregateIdProvider fileid) {
        this.session = session;
        this.fileid = fileid;
    }

    @Override
    public AttributedList<Path> list(final Path directory, final ListProgressListener listener) throws BackgroundException {
        if(directory.isRoot()) {
            final AttributedList<Path> list = new AttributedList<>();
            for(RootFolder root : session.roots()) {
                switch(root.getRootFolderType()) {
                    case NUMBER_0: // My Files
                    case NUMBER_1: // Common
                        list.add(new Path(PathNormalizer.normalize(root.getName()), EnumSet.of(Path.Type.directory, Path.Type.volume), new PathAttributes().withVersionId(root.getId())));
                        break;
                }
            }
            listener.chunk(directory, list);
            return list;
        }
        else {
            try {
                final AttributedList<Path> children = new AttributedList<>();
                final StoregateAttributesFinderFeature attributes = new StoregateAttributesFinderFeature(session, fileid);
                int pageIndex = 0;
                FileContents files;
                do {
                    files = new FilesApi(this.session.getClient()).filesGetById(
                        fileid.getFileid(directory, new DisabledListProgressListener()),
                        pageIndex,
                        chunksize,
                        "Name asc",
                        0, // All
                        true,
                        false
                    );
                    for(File f : files.getFiles()) {
                        final PathAttributes attrs = attributes.toAttributes(f);
                        final EnumSet<Path.Type> type = (f.getFlags() & File.FlagsEnum.Folder.getValue()) == 1 ?
                            EnumSet.of(Path.Type.directory) :
                            EnumSet.of(Path.Type.file);
                        final Path p = new Path(directory, f.getName(), type, attrs);
                        children.add(p);
                        listener.chunk(directory, children);
                    }
                    pageIndex++;
                }
                while(children.size() < files.getTotalRowCount());
                return children;
            }
            catch(ApiException e) {
                throw new StoregateExceptionMappingService().map("Listing directory {0} failed", e, directory);
            }
        }
    }

    @Override
    public ListService withCache(final Cache<Path> cache) {
        fileid.withCache(cache);
        return this;
    }
}
