package ch.cyberduck.core.b2;

/*
 * Copyright (c) 2002-2016 iterate GmbH. All rights reserved.
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

import ch.cyberduck.core.DefaultIOExceptionMappingService;
import ch.cyberduck.core.MappingMimeTypeService;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathContainerService;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.features.Touch;

import org.apache.http.entity.ByteArrayEntity;

import java.io.IOException;
import java.util.Collections;

import synapticloop.b2.exception.B2ApiException;

import static ch.cyberduck.core.b2.B2MetadataFeature.X_BZ_INFO_SRC_LAST_MODIFIED_MILLIS;

public class B2TouchFeature implements Touch {

    private PathContainerService containerService
            = new B2PathContainerService();

    private final B2Session session;

    public B2TouchFeature(final B2Session session) {
        this.session = session;
    }

    @Override
    public void touch(final Path file) throws BackgroundException {
        try {
            session.getClient().uploadFile(
                    new B2FileidProvider(session).getFileid(containerService.getContainer(file)),
                    containerService.getKey(file),
                    new ByteArrayEntity(new byte[0]), "da39a3ee5e6b4b0d3255bfef95601890afd80709",
                    new MappingMimeTypeService().getMime(file.getName()),
                    Collections.singletonMap(
                            X_BZ_INFO_SRC_LAST_MODIFIED_MILLIS, String.valueOf(System.currentTimeMillis())
                    )
            );
        }
        catch(B2ApiException e) {
            throw new B2ExceptionMappingService(session).map("Cannot create folder {0}", e, file);
        }
        catch(IOException e) {
            throw new DefaultIOExceptionMappingService().map(e);
        }
    }

    @Override
    public boolean isSupported(final Path workdir) {
        // Creating files is only possible inside a bucket.
        return !workdir.isRoot();
    }
}
