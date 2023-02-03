package ch.cyberduck.core.box;

/*
 * Copyright (c) 2002-2020 iterate GmbH. All rights reserved.
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

import ch.cyberduck.core.box.io.swagger.client.model.File;
import ch.cyberduck.core.features.Write;
import ch.cyberduck.core.http.HttpUploadFeature;

import java.security.MessageDigest;

public class BoxSmallUploadService extends HttpUploadFeature<File, MessageDigest> {

    public BoxSmallUploadService(final BoxSession session, final BoxFileidProvider fileid, final Write<File> writer) {
        super(writer);
    }
}
