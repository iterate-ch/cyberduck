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
import ch.cyberduck.core.exception.InteroperabilityException;
import ch.cyberduck.core.features.Headers;

import java.util.Map;

public class B2MetadataFeature implements Headers {

    private final PathContainerService containerService
            = new B2PathContainerService();

    private final B2Session session;

    public B2MetadataFeature(final B2Session session) {
        this.session = session;
    }

    @Override
    public Map<String, String> getMetadata(final Path file) throws BackgroundException {
        return new B2AttributesFeature(session).find(file).getMetadata();
    }

    @Override
    public void setMetadata(final Path file, final Map<String, String> metadata) throws BackgroundException {
        throw new InteroperabilityException(file.getAbsolute());
    }
}

