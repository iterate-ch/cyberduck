package ch.cyberduck.core.s3;

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
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.exception.NotfoundException;

import org.jets3t.service.model.MultipartPart;
import org.jets3t.service.model.MultipartUpload;

import java.util.Collections;
import java.util.List;

public class S3DisabledMultipartService implements S3MultipartService {

    @Override
    public List<MultipartUpload> find(final Path file) throws BackgroundException {
        return Collections.emptyList();
    }

    @Override
    public List<MultipartPart> list(final MultipartUpload multipart) throws BackgroundException {
        return Collections.emptyList();
    }

    @Override
    public void delete(final MultipartUpload upload) throws BackgroundException {
        throw new NotfoundException(upload.getUploadId());
    }
}

