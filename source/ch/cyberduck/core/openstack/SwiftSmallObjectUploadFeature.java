package ch.cyberduck.core.openstack;

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

import org.apache.commons.codec.binary.Hex;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import ch.iterate.openstack.swift.model.StorageObject;

/**
 * @author Joel Wright <joel.wright@sohonet.com>
 * @version $Id$
 */
public class SwiftSmallObjectUploadFeature extends HttpUploadFeature<StorageObject, MessageDigest> {
    private static final Logger log = Logger.getLogger(SwiftSmallObjectUploadFeature.class);

    public SwiftSmallObjectUploadFeature(final SwiftSession session) {
        super(new SwiftWriteFeature(session));
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
        if(Preferences.instance().getBoolean("openstack.upload.md5")) {
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
    protected void post(final MessageDigest digest, final StorageObject response) throws BackgroundException {
        if(null != digest) {
            // Obtain locally-calculated MD5 hash.
            final String expected = Hex.encodeHexString(digest.digest());
            // Compare our locally-calculated hash with the ETag returned by S3.
            if(!expected.equals(response.getMd5sum())) {
                throw new ChecksumException("Upload failed",
                        String.format("Mismatch between MD5 hash of uploaded data (%s) and ETag returned by the server (%s)",
                                expected, response.getMd5sum()));
            }
            if(log.isDebugEnabled()) {
                log.debug(String.format("Verified checksum for %s", response));
            }
        }
    }
}