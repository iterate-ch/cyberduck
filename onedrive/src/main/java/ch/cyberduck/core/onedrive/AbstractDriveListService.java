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

import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathAttributes;
import ch.cyberduck.core.onedrive.features.GraphFileIdProvider;

import org.apache.commons.lang3.StringUtils;
import org.nuxeo.onedrive.client.types.Drive;
import org.nuxeo.onedrive.client.types.Quota;

import java.util.EnumSet;

public abstract class AbstractDriveListService extends AbstractListService<Drive.Metadata> {

    public AbstractDriveListService(final GraphFileIdProvider fileid) {
        super(fileid);
    }

    @Override
    protected Path toPath(final Drive.Metadata metadata, final Path directory) {
        final PathAttributes attributes = new PathAttributes();
        attributes.setFileId(metadata.getId());
        final Quota quota = metadata.getQuota();
        if(quota != null) {
            final Long used = quota.getUsed();
            if(used != null) {
                attributes.setSize(used);
            }
        }
        String name = metadata.getName();
        if(StringUtils.isBlank(metadata.getName())) {
            name = metadata.getId();
        }
        return new Path(directory, name, EnumSet.of(Path.Type.directory, Path.Type.volume), attributes);
    }
}
