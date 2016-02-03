/*
 * Copyright (c) 2015-2016 Spectra Logic Corporation. All rights reserved.
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

package ch.cyberduck.core.spectra;

import ch.cyberduck.core.DisabledLoginCallback;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.features.Attributes;
import ch.cyberduck.core.features.Delete;
import ch.cyberduck.core.features.Find;
import ch.cyberduck.core.http.ResponseOutputStream;
import ch.cyberduck.core.s3.S3DefaultDeleteFeature;
import ch.cyberduck.core.s3.S3MultipartService;
import ch.cyberduck.core.s3.S3WriteFeature;
import ch.cyberduck.core.transfer.TransferStatus;

import org.jets3t.service.model.StorageObject;

import java.util.Collections;

public class SpectraWriteFeature extends S3WriteFeature {

    private final Delete delete;

    public SpectraWriteFeature(final SpectraSession session) {
        this(session, new S3DefaultDeleteFeature(session));
    }

    public SpectraWriteFeature(final SpectraSession session, final S3MultipartService multipartService,
                               final Find finder, final Attributes attributes) {
        this(session, multipartService, finder, attributes, new S3DefaultDeleteFeature(session));
    }

    public SpectraWriteFeature(final SpectraSession session, final Delete delete) {
        super(session);
        this.delete = delete;
    }

    public SpectraWriteFeature(final SpectraSession session, final S3MultipartService multipartService,
                               final Find finder, final Attributes attributes, final S3DefaultDeleteFeature delete) {
        super(session, multipartService, finder, attributes);
        this.delete = delete;
    }

    @Override
    public ResponseOutputStream<StorageObject> write(final Path file, final TransferStatus status) throws BackgroundException {
        if(status.isExists()) {
            delete.delete(Collections.singletonList(file), new DisabledLoginCallback(), new Delete.Callback() {
                @Override
                public void delete(final Path file) {
                    //
                }
            });
        }
        return super.write(file, status);
    }
}
