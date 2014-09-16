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

import ch.cyberduck.core.Preferences;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.exception.ChecksumException;
import ch.cyberduck.core.http.HttpUploadFeature;

import org.apache.log4j.Logger;
import org.jets3t.service.ServiceException;
import org.jets3t.service.model.StorageObject;
import org.jets3t.service.utils.ServiceUtils;

import java.io.IOException;
import java.io.InputStream;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * @version $Id$
 */
public class S3SingleUploadService extends HttpUploadFeature<StorageObject, MessageDigest> {
    private static final Logger log = Logger.getLogger(S3SingleUploadService.class);

    private S3Session session;

    public S3SingleUploadService(final S3Session session) {
        this(session, new S3WriteFeature(session));
    }

    public S3SingleUploadService(final S3Session session, final S3WriteFeature writer) {
        super(writer);
        this.session = session;
    }

    @Override
    protected InputStream decorate(final InputStream in, final MessageDigest digest) throws IOException {
        if(null == digest) {
            log.warn("MD5 calculation disabled");
            return in;
        }
        else {
            return new DigestInputStream(super.decorate(in, digest), digest);
        }
    }

    @Override
    protected MessageDigest digest() {
        MessageDigest digest = null;
        if(!Preferences.instance().getBoolean("s3.upload.metadata.md5")) {
            // Content-MD5 not set. Need to verify ourselves instad of S3
            try {
                digest = MessageDigest.getInstance("MD5");
            }
            catch(NoSuchAlgorithmException e) {
                log.error(e.getMessage());
            }
        }
        return digest;
    }

    @Override
    protected void post(final MessageDigest digest, final StorageObject part) throws BackgroundException {
        if(null != digest) {
            // Obtain locally-calculated MD5 hash.
            final String hexMD5 = ServiceUtils.toHex(digest.digest());
            try {
                session.getClient().verifyExpectedAndActualETagValues(hexMD5, part);
            }
            catch(ServiceException e) {
                throw new ChecksumException("Upload failed", e.getMessage(), e);
            }
        }
    }
}
