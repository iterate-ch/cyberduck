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

import ch.cyberduck.core.Local;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.exception.ChecksumException;
import ch.cyberduck.core.http.AbstractHttpWriteFeature;
import ch.cyberduck.core.http.HttpUploadFeature;
import ch.cyberduck.core.io.BandwidthThrottle;
import ch.cyberduck.core.io.SHA256ChecksumCompute;
import ch.cyberduck.core.io.StreamCancelation;
import ch.cyberduck.core.io.StreamListener;
import ch.cyberduck.core.io.StreamProgress;
import ch.cyberduck.core.transfer.TransferStatus;

import org.apache.commons.codec.binary.Hex;
import org.apache.log4j.Logger;
import org.jets3t.service.model.StorageObject;

import java.io.IOException;
import java.io.InputStream;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.MessageFormat;

/**
 * @version $Id$
 */
public class S3SingleUploadService extends HttpUploadFeature<StorageObject, MessageDigest> {
    private static final Logger log = Logger.getLogger(S3SingleUploadService.class);

    public S3SingleUploadService(final S3Session session) {
        super(new S3WriteFeature(session));
    }

    public S3SingleUploadService(final AbstractHttpWriteFeature<StorageObject> writer) {
        super(writer);
    }

    @Override
    public StorageObject upload(final Path file, final Local local, final BandwidthThrottle throttle,
                                final StreamListener listener, final TransferStatus status,
                                final StreamCancelation cancel, final StreamProgress progress) throws BackgroundException {
        status.setChecksum(new TransferStatus.Checksum("SHA-256",
                        new SHA256ChecksumCompute().compute(local.getInputStream()))
        );
        return super.upload(file, local, throttle, listener, status, cancel, progress);
    }

    @Override
    protected InputStream decorate(final InputStream in, final MessageDigest digest) throws IOException {
        if(null == digest) {
            log.warn("MD5 calculation disabled");
            return super.decorate(in, null);
        }
        else {
            return new DigestInputStream(super.decorate(in, digest), digest);
        }
    }

    @Override
    protected MessageDigest digest() throws IOException {
        // Content-MD5 not set. Need to verify ourselves instad of S3
        try {
            return MessageDigest.getInstance("MD5");
        }
        catch(NoSuchAlgorithmException e) {
            throw new IOException(e.getMessage(), e);
        }
    }

    @Override
    protected void post(final MessageDigest digest, final StorageObject part) throws BackgroundException {
        if(null != digest) {
            // Obtain locally-calculated MD5 hash.
            final String expected = Hex.encodeHexString(digest.digest());
            if(!expected.equals(part.getETag())) {
                throw new ChecksumException("Upload {0} failed",
                        MessageFormat.format("Mismatch between MD5 hash {0} of uploaded data and ETag {1} returned by the server",
                                expected, part.getETag()));
            }
        }
    }
}
