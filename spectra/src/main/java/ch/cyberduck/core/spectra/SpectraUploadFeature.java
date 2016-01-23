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
import ch.cyberduck.core.Local;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.features.Delete;
import ch.cyberduck.core.io.BandwidthThrottle;
import ch.cyberduck.core.io.StreamCancelation;
import ch.cyberduck.core.io.StreamListener;
import ch.cyberduck.core.io.StreamProgress;
import ch.cyberduck.core.s3.S3DefaultDeleteFeature;
import ch.cyberduck.core.s3.S3SingleUploadService;
import ch.cyberduck.core.transfer.TransferStatus;

import org.jets3t.service.model.StorageObject;

import java.util.Collections;

public class SpectraUploadFeature extends S3SingleUploadService {

    private final S3DefaultDeleteFeature delete;

    public SpectraUploadFeature(final SpectraSession session) {
        this(session, new S3DefaultDeleteFeature(session));
    }

    public SpectraUploadFeature(final SpectraSession session, final S3DefaultDeleteFeature delete) {
        super(session);
        this.delete = delete;
    }

    @Override
    public StorageObject upload(final Path file, final Local local,
                                final BandwidthThrottle throttle, final StreamListener listener,
                                final TransferStatus status, final StreamCancelation cancel,
                                final StreamProgress progress) throws BackgroundException {
        if(status.isExists()) {
            delete.delete(Collections.singletonList(file), new DisabledLoginCallback(), new Delete.Callback() {
                @Override
                public void delete(final Path file) {
                    //
                }
            });
        }
        return super.upload(file, local, throttle, listener, status, cancel, progress);
    }
}
