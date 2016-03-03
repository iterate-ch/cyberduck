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

import org.apache.http.entity.ByteArrayEntity;

import java.util.Collections;

import synapticloop.b2.exception.B2Exception;

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
            session.getClient().uploadFile(
                    new B2FileidProvider(session).getFileid(containerService.getContainer(file)),
                    String.format("%s/.bzEmpty", containerService.getKey(file)),
                    new ByteArrayEntity(new byte[0]), "adc83b19e793491b1c6ea0fd8b46cd9f32e592fc",
                    null, Collections.emptyMap());
        }
        catch(B2Exception e) {
            throw new B2ExceptionMappingService().map("Cannot create folder {0}", e, file);
        }
    }
}

