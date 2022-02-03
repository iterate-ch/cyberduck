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

import ch.cyberduck.core.Path;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.features.Write;
import ch.cyberduck.core.http.HttpUploadFeature;
import ch.cyberduck.core.io.Checksum;
import ch.cyberduck.core.preferences.HostPreferences;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import ch.iterate.openstack.swift.model.StorageObject;

public class SwiftSmallObjectUploadFeature extends HttpUploadFeature<StorageObject, MessageDigest> {
    private static final Logger log = LogManager.getLogger(SwiftSmallObjectUploadFeature.class);

    private final SwiftSession session;

    public SwiftSmallObjectUploadFeature(final SwiftSession session, final Write<StorageObject> writer) {
        super(writer);
        this.session = session;
    }

    @Override
    protected InputStream decorate(final InputStream in, final MessageDigest digest) throws IOException {
        if(null == digest) {
            log.warn("MD5 calculation disabled");
            return super.decorate(in, null);
        }
        else {
            return new DigestInputStream(in, digest);
        }
    }

    @Override
    protected MessageDigest digest() throws IOException {
        MessageDigest digest = null;
        if(new HostPreferences(session.getHost()).getBoolean("queue.upload.checksum.calculate")) {
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
    protected void post(final Path file, final MessageDigest digest, final StorageObject response) throws BackgroundException {
        this.verify(file, digest, Checksum.parse(response.getMd5sum()));
    }
}
