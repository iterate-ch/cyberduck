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

import ch.cyberduck.core.ConnectionCallback;
import ch.cyberduck.core.Local;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.exception.InteroperabilityException;
import ch.cyberduck.core.features.Upload;
import ch.cyberduck.core.features.Write;
import ch.cyberduck.core.http.HttpUploadFeature;
import ch.cyberduck.core.io.BandwidthThrottle;
import ch.cyberduck.core.io.Checksum;
import ch.cyberduck.core.io.StreamListener;
import ch.cyberduck.core.preferences.PreferencesFactory;
import ch.cyberduck.core.transfer.TransferStatus;

import org.apache.log4j.Logger;
import org.jets3t.service.model.StorageObject;

import java.io.IOException;
import java.io.InputStream;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class S3SingleUploadService extends HttpUploadFeature<StorageObject, MessageDigest> {
    private static final Logger log = Logger.getLogger(S3SingleUploadService.class);

    private final S3Session session;
    private Write<StorageObject> writer;

    public S3SingleUploadService(final S3Session session, final Write<StorageObject> writer) {
        super(writer);
        this.session = session;
        this.writer = writer;
    }

    @Override
    public StorageObject upload(final Path file, final Local local, final BandwidthThrottle throttle,
                                final StreamListener listener, final TransferStatus status, final ConnectionCallback callback) throws BackgroundException {
        final S3Protocol.AuthenticationHeaderSignatureVersion signatureVersion = session.getSignatureVersion();
        switch(signatureVersion) {
            case AWS4HMACSHA256:
                if(Checksum.NONE == status.getChecksum()) {
                    status.setChecksum(writer.checksum(file).compute(local.getInputStream(), status));
                }
                break;
        }
        try {
            return super.upload(file, local, throttle, listener, status, callback);
        }
        catch(InteroperabilityException e) {
            if(!session.getSignatureVersion().equals(signatureVersion)) {
                // Retry if upload fails with Header "x-amz-content-sha256" set to the hex-encoded SHA256 hash of the
                // request payload is required for AWS Version 4 request signing
                return this.upload(file, local, throttle, listener, status, callback);
            }
            throw e;
        }
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
        MessageDigest digest = null;
        if(PreferencesFactory.get().getBoolean("queue.upload.checksum.calculate")) {
            try {
                digest = MessageDigest.getInstance("MD5");
            }
            catch(NoSuchAlgorithmException e) {
                throw new IOException(e.getMessage(), e);
            }
        }
        return digest;
    }

    @Override
    protected void post(final Path file, final MessageDigest digest, final StorageObject part) throws BackgroundException {
        if(null != part.getServerSideEncryptionAlgorithm()) {
            log.warn(String.format("Skip checksum verification for %s with server side encryption enabled", file));
            return;
        }
        this.verify(file, digest, Checksum.parse(part.getETag()));
    }

    @Override
    public Upload<StorageObject> withWriter(final Write<StorageObject> writer) {
        this.writer = writer;
        return super.withWriter(writer);
    }
}
