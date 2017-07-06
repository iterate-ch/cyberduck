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
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathAttributes;
import ch.cyberduck.core.RootListService;
import ch.cyberduck.core.exception.BackgroundException;


import java.util.EnumSet;

/**
 * List the available drives for a user (Manta) or SharePoint site (document libraries).
 */
public class MantaContainerListService implements RootListService {

    private final MantaSession session;

    public MantaContainerListService(final MantaSession session) {
        this.session = session;
    }

    @Override
    public AttributedList<Path> list(final Path directory, final ListProgressListener listener) throws BackgroundException {
        final AttributedList<Path> children = new AttributedList<>();
//        try {
//            // In most cases, Manta and OneDrive for Business users will only have a single
//            // drive available, the default drive. When using Manta API with a SharePoint team site,
//            // this API returns the collection of document libraries created in the site.
//            final MantaDrivesIterator iter = new OneDriveDrivesIterator(session.getClient());
//            while(iter.hasNext()) {
//                try {
//                    final MantaDrive.Metadata metadata = iter.next();
//                    final PathAttributes attributes = new PathAttributes();
//                    attributes.setSize(metadata.getTotal());
//                    children.add(new Path(directory, metadata.getId(), EnumSet.of(Path.Type.directory, Path.Type.volume), attributes));
//                    listener.chunk(directory, children);
//                }
//                catch(MantaRuntimeException e) {
//                    throw new MantaExceptionMappingService().map(e.getCause());
//                }
//            }
//        }
//        catch(MantaRuntimeException e) { // this catches iterator.hasNext() which in return should fail fast
//            throw new MantaExceptionMappingService().map("Listing directory {0} failed", e.getCause(), directory);
//        }
        return children;
    }
}
