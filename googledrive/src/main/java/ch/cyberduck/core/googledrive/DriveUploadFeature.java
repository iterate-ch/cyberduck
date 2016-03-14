package ch.cyberduck.core.googledrive;

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
import ch.cyberduck.core.http.HttpUploadFeature;
import ch.cyberduck.core.io.Checksum;
import ch.cyberduck.core.preferences.PreferencesFactory;

import org.apache.log4j.Logger;

import java.io.InputStream;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class DriveUploadFeature extends HttpUploadFeature<String, MessageDigest> {
    private static final Logger log = Logger.getLogger(DriveUploadFeature.class);

    public DriveUploadFeature(final DriveSession session) {
        super(new DriveWriteFeature(session));
    }

    @Override
    protected InputStream decorate(final InputStream in, final MessageDigest digest) {
        if(null == digest) {
            log.warn("MD5 calculation disabled");
            return in;
        }
        else {
            return new DigestInputStream(in, digest);
        }
    }

    @Override
    protected MessageDigest digest() {
        MessageDigest digest = null;
        if(PreferencesFactory.get().getBoolean("google.drive.upload.checksum")) {
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
    protected void post(final Path file, final MessageDigest digest, final String etag) throws BackgroundException {
        this.verify(file, digest, Checksum.parse(etag));
    }
}
