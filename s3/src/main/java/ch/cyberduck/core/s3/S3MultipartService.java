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

import org.jets3t.service.model.MultipartPart;
import org.jets3t.service.model.MultipartUpload;

import java.util.List;

public interface S3MultipartService {
    List<MultipartUpload> find(Path file) throws BackgroundException;

    List<MultipartPart> list(MultipartUpload multipart) throws BackgroundException;

    void delete(MultipartUpload upload) throws BackgroundException;
}
