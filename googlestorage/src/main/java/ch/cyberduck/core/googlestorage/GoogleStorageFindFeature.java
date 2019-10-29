package ch.cyberduck.core.googlestorage;

/*
 * Copyright (c) 2002-2019 iterate GmbH. All rights reserved.
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

import ch.cyberduck.core.Cache;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.exception.AccessDeniedException;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.exception.NotfoundException;
import ch.cyberduck.core.features.Find;

public class GoogleStorageFindFeature implements Find {

    private final GoogleStorageAttributesFinderFeature attributes;

    public GoogleStorageFindFeature(final GoogleStorageSession session) {
        this.attributes = new GoogleStorageAttributesFinderFeature(session);
    }

    @Override
    public boolean find(final Path file) throws BackgroundException {
        if(file.isRoot()) {
            return true;
        }
        try {
            attributes.find(file);
            return true;
        }
        catch(NotfoundException e) {
            return false;
        }
        catch(AccessDeniedException e) {
            // Object is inaccessible to current user, but does exist.
            return true;
        }
    }

    @Override
    public Find withCache(final Cache<Path> cache) {
        attributes.withCache(cache);
        return this;
    }
}
