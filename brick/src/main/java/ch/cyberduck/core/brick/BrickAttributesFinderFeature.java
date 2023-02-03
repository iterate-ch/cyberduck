package ch.cyberduck.core.brick;

/*
 * Copyright (c) 2002-2021 iterate GmbH. All rights reserved.
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

import ch.cyberduck.core.ListProgressListener;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathAttributes;
import ch.cyberduck.core.brick.io.swagger.client.ApiException;
import ch.cyberduck.core.brick.io.swagger.client.api.FilesApi;
import ch.cyberduck.core.brick.io.swagger.client.model.FileEntity;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.exception.NotfoundException;
import ch.cyberduck.core.features.AttributesAdapter;
import ch.cyberduck.core.features.AttributesFinder;
import ch.cyberduck.core.io.Checksum;

import org.apache.commons.lang3.StringUtils;

public class BrickAttributesFinderFeature implements AttributesFinder, AttributesAdapter<FileEntity> {

    private final BrickSession session;

    public BrickAttributesFinderFeature(final BrickSession session) {
        this.session = session;
    }

    @Override
    public PathAttributes find(final Path file, final ListProgressListener listener) throws BackgroundException {
        try {
            final FileEntity entity = new FilesApi(new BrickApiClient(session))
                .download(StringUtils.removeStart(file.getAbsolute(), String.valueOf(Path.DELIMITER)),
                    "stat", null, false, false);
            switch(entity.getType()) {
                case "file":
                    if(file.isDirectory()) {
                        throw new NotfoundException(file.getAbsolute());
                    }
                    break;
                case "directory":
                    if(file.isFile()) {
                        throw new NotfoundException(file.getAbsolute());
                    }
            }
            return this.toAttributes(entity);
        }
        catch(ApiException e) {
            throw new BrickExceptionMappingService().map("Failure to read attributes of {0}", e, file);
        }
    }

    @Override
    public PathAttributes toAttributes(final FileEntity entity) {
        final PathAttributes attr = new PathAttributes();
        attr.setChecksum(Checksum.parse(entity.getMd5()));
        attr.setRegion(entity.getRegion());
        if(entity.getSize() != null) {
            attr.setSize(entity.getSize());
        }
        if(entity.getProvidedMtime() != null) {
            attr.setModificationDate(entity.getProvidedMtime().getMillis());
        }
        else if(entity.getMtime() != null) {
            attr.setModificationDate(entity.getMtime().getMillis());
        }
        return attr;
    }
}
