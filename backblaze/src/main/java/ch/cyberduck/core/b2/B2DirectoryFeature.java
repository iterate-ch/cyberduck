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

import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathContainerService;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.features.Directory;
import ch.cyberduck.core.preferences.PreferencesFactory;

import org.apache.http.entity.ByteArrayEntity;

import java.util.Collections;

import synapticloop.b2.BucketType;
import synapticloop.b2.exception.B2ApiException;

public class B2DirectoryFeature implements Directory {

    private final PathContainerService containerService
            = new B2PathContainerService();

    private final B2Session session;

    public B2DirectoryFeature(final B2Session session) {
        this.session = session;
    }

    @Override
    public void mkdir(final Path file) throws BackgroundException {
        this.mkdir(file, null);
    }

    @Override
    public void mkdir(final Path file, final String region) throws BackgroundException {
        try {
            if(containerService.isContainer(file)) {
                session.getClient().createBucket(containerService.getContainer(file).getName(),
                        BucketType.valueOf(PreferencesFactory.get().getProperty("b2.bucket.acl.default")));
            }
            else {
                session.getClient().uploadFile(
                        new B2FileidProvider(session).getFileid(containerService.getContainer(file)),
                        String.format("%s/.bzEmpty", containerService.getKey(file)),
                        new ByteArrayEntity(new byte[0]), "da39a3ee5e6b4b0d3255bfef95601890afd80709",
                        "application/octet-stream", Collections.emptyMap());
            }
        }
        catch(B2ApiException e) {
            throw new B2ExceptionMappingService().map("Cannot create folder {0}", e, file);
        }
    }
}

