package ch.cyberduck.core.s3;

/*
 * Copyright (c) 2002-2013 David Kocher. All rights reserved.
 * http://cyberduck.ch/
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
 *
 * Bug fixes, suggestions and comments should be sent to feedback@cyberduck.ch
 */

import ch.cyberduck.core.Path;
import ch.cyberduck.core.Preferences;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.features.Attributes;
import ch.cyberduck.core.features.Write;
import ch.cyberduck.core.http.AbstractHttpWriteFeature;
import ch.cyberduck.core.http.ResponseOutputStream;
import ch.cyberduck.core.transfer.TransferStatus;

import org.jets3t.service.model.MultipartPart;
import org.jets3t.service.model.MultipartUpload;
import org.jets3t.service.model.StorageObject;

import java.util.Collections;

/**
 * @version $Id$
 */
public class S3WriteFeature extends AbstractHttpWriteFeature<StorageObject> implements Write {

    private S3Session session;

    public S3WriteFeature(final S3Session session) {
        this.session = session;
    }

    @Override
    public ResponseOutputStream<StorageObject> write(final Path file, final TransferStatus status) throws BackgroundException {
        final S3SingleUploadService service = new S3SingleUploadService(session);
        return service.write(file, service.createObjectDetails(file), status.getLength(),
                Collections.<String, String>emptyMap());
    }

    /**
     * @return No Content-Range support
     */
    @Override
    public Append append(final Path file, final Attributes feature) throws BackgroundException {
        if(file.getLocal().attributes().getSize() > Preferences.instance().getLong("s3.upload.multipart.threshold")) {
            final S3MultipartUploadService multipart = new S3MultipartUploadService(session);
            final MultipartUpload upload = multipart.find(file);
            if(upload != null) {
                Long size = 0L;
                for(MultipartPart completed : multipart.list(upload)) {
                    size += completed.getSize();
                }
                return new Append(size);
            }
        }
        return new Append();
    }
}
