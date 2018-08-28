package ch.cyberduck.core.onedrive;

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
import ch.cyberduck.core.Cache;
import ch.cyberduck.core.ListProgressListener;
import ch.cyberduck.core.ListService;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathAttributes;
import ch.cyberduck.core.exception.BackgroundException;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.nuxeo.onedrive.client.OneDriveDrive;
import org.nuxeo.onedrive.client.OneDriveRuntimeException;
import org.nuxeo.onedrive.client.resources.GroupItem;

import java.util.EnumSet;
import java.util.Iterator;

public abstract class AbstractDriveListService implements ListService {
    private static final Logger log = Logger.getLogger(AbstractDriveListService.class);

    public AbstractDriveListService() {
    }

    protected final void run(final Iterator<OneDriveDrive.Metadata> iterator, final Path directory, final AttributedList<Path> children, final ListProgressListener listener) throws BackgroundException {
        while(iterator.hasNext()) {
            final OneDriveDrive.Metadata metadata;
            try {
                metadata = iterator.next();
            }
            catch(OneDriveRuntimeException e) {
                log.warn(e.getMessage());
                continue;
            }

            final PathAttributes attributes = new PathAttributes();
            attributes.setVersionId(metadata.getId());
            attributes.setSize(metadata.getTotal());

            String name = metadata.getName();
            if (StringUtils.isBlank(metadata.getName())) {
                name = metadata.getId();
            }

            children.add(new Path(directory, name, EnumSet.of(Path.Type.directory, Path.Type.volume), attributes));
            listener.chunk(directory, children);
        }
    }

    @Override
    public ListService withCache(final Cache<Path> cache) {
        return this;
    }
}
