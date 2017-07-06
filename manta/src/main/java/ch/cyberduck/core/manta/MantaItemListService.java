package ch.cyberduck.core.manta;

/*
 * Copyright (c) 2002-2017 iterate GmbH. All rights reserved.
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
import ch.cyberduck.core.ListProgressListener;
import ch.cyberduck.core.ListService;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathAttributes;
import ch.cyberduck.core.PathContainerService;
import ch.cyberduck.core.exception.BackgroundException;

import org.apache.log4j.Logger;

import java.util.EnumSet;
import java.util.Iterator;

public class MantaItemListService implements ListService {
    private static final Logger log = Logger.getLogger(MantaItemListService.class);

    private final PathContainerService containerService
            = new PathContainerService();

    private final MantaSession session;
    private final MantaAttributesFinderFeature attributes;

    public MantaItemListService(final MantaSession session) {
        this.session = session;
        this.attributes = new MantaAttributesFinderFeature(session);
    }

    @Override
    public AttributedList<Path> list(final Path directory, final ListProgressListener listener) throws BackgroundException {
        final AttributedList<Path> children = new AttributedList<>();
//        try {
//            final MantaDrive drive = new OneDriveDrive(session.getClient(), containerService.getContainer(directory).getName());
//            final MantaFolder folder;
//            if(containerService.isContainer(directory)) {
//                folder = drive.getRoot();
//            }
//            else {
//                folder = session.toFolder(directory);
//            }
//            Iterator<MantaItem.Metadata> iterator = folder.iterator();
//            while(iterator.hasNext()) {
//                final MantaItem.Metadata metadata;
//                try {
//                    metadata = iterator.next();
//                }
//                catch(MantaRuntimeException e) {
//                    log.warn(e);
//                    continue;
//                }
//                final PathAttributes attributes = this.attributes.convert(metadata);
//                children.add(new Path(directory, metadata.getName(),
//                        metadata.isFolder() ? EnumSet.of(Path.Type.directory) : EnumSet.of(Path.Type.file), attributes));
//                listener.chunk(directory, children);
//            }
//        }
//        catch(MantaRuntimeException e) { // this catches iterator.hasNext() which in return should fail fast
//            throw new MantaExceptionMappingService().map("Listing directory {0} failed", e.getCause(), directory);
//        }
        return children;
    }
}
