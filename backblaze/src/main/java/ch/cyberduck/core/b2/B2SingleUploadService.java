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

import ch.cyberduck.core.Local;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.http.HttpUploadFeature;
import ch.cyberduck.core.io.BandwidthThrottle;
import ch.cyberduck.core.io.ChecksumCompute;
import ch.cyberduck.core.io.ChecksumComputeFactory;
import ch.cyberduck.core.io.HashAlgorithm;
import ch.cyberduck.core.io.StreamCancelation;
import ch.cyberduck.core.io.StreamListener;
import ch.cyberduck.core.io.StreamProgress;
import ch.cyberduck.core.transfer.TransferStatus;

import java.security.MessageDigest;

import synapticloop.b2.response.B2FileResponse;

public class B2SingleUploadService extends HttpUploadFeature<B2FileResponse, MessageDigest> {

    private final ChecksumCompute checksum
            = ChecksumComputeFactory.get(HashAlgorithm.sha1);

    public B2SingleUploadService(final B2Session session) {
        this(session, new B2WriteFeature(session));
    }

    public B2SingleUploadService(final B2Session session, final B2WriteFeature writer) {
        super(writer);
    }

    @Override
    public B2FileResponse upload(final Path file, final Local local, final BandwidthThrottle throttle,
                                 final StreamListener listener, final TransferStatus status,
                                 final StreamCancelation cancel, final StreamProgress progress) throws BackgroundException {
        status.setChecksum(checksum.compute(local.getInputStream()));
        return super.upload(file, local, throttle, listener, status, cancel, progress);
    }
}
