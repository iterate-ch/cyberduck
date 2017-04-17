package ch.cyberduck.core.onedrive;

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
import ch.cyberduck.core.Cache;
import ch.cyberduck.core.Filter;
import ch.cyberduck.core.ListProgressListener;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathAttributes;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.features.Search;

import org.apache.log4j.Logger;
import org.nuxeo.onedrive.client.OneDriveItem;
import org.nuxeo.onedrive.client.OneDriveRuntimeException;

import java.util.EnumSet;
import java.util.Iterator;

public class OneDriveSearchFeature implements Search {
    private static final Logger log = Logger.getLogger(OneDriveSearchFeature.class);

    private final OneDriveSession session;
    private final OneDriveAttributesFinderFeature attributes;

    public OneDriveSearchFeature(final OneDriveSession session) {
        this.session = session;
        this.attributes = new OneDriveAttributesFinderFeature(session);
    }

    @Override
    public AttributedList<Path> search(final Path workdir, final Filter<Path> regex, final ListProgressListener listener) throws BackgroundException {
        final AttributedList<Path> list = new AttributedList<>();
        // The query text used to search for items. Values may be matched across several fields including filename, metadata, and file content.
        final Iterator<OneDriveItem.Metadata> iterator = session.toFolder(workdir).search(regex.toPattern().pattern()).iterator();
        while(iterator.hasNext()) {
            final OneDriveItem.Metadata metadata;
            try {
                metadata = iterator.next();
            }
            catch(OneDriveRuntimeException e) {
                log.warn(e);
                continue;
            }
            final PathAttributes attributes = this.attributes.convert(metadata);
            list.add(new Path(metadata.getName(), metadata.isFolder() ? EnumSet.of(Path.Type.directory) : EnumSet.of(Path.Type.file), attributes));
        }
        return list;
    }

    @Override
    public boolean isRecursive() {
        return true;
    }

    @Override
    public Search withCache(final Cache<Path> cache) {
        return this;
    }
}
