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

import ch.cyberduck.core.Path;
import ch.cyberduck.core.shared.DefaultTouchFeature;

import com.google.api.services.storage.model.StorageObject;

public class GoogleStorageTouchFeature extends DefaultTouchFeature<StorageObject> {

    public GoogleStorageTouchFeature(final GoogleStorageSession session) {
        super(new GoogleStorageWriteFeature(session));
    }

    @Override
    public boolean isSupported(final Path workdir, final String filename) {
        // Creating files is only possible inside a bucket.
        return !workdir.isRoot();
    }
}
