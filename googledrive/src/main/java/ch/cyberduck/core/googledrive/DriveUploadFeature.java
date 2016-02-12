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
import ch.cyberduck.core.exception.ChecksumException;
import ch.cyberduck.core.http.HttpUploadFeature;
import ch.cyberduck.core.preferences.PreferencesFactory;

import org.apache.commons.codec.binary.Hex;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import java.io.InputStream;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import com.google.api.services.drive.model.File;

/**
 * @version $Id:$
 */
public class DriveUploadFeature extends HttpUploadFeature<File, MessageDigest> {
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
    protected void post(final Path file, final MessageDigest digest, final File response) throws BackgroundException {
        if(StringUtils.isBlank(response.getMd5Checksum())) {
            log.warn("No ETag returned by server to verify checksum");
            return;
        }
        if(response.getMd5Checksum().matches("[a-fA-F0-9]{32}")) {
            log.warn(String.format("ETag %s returned by server does not match MD5 pattern", response.getMd5Checksum()));
            return;
        }
        if(null != digest) {
            // Obtain locally-calculated MD5 hash.
            final String expected = Hex.encodeHexString(digest.digest());
            // Compare our locally-calculated hash with the ETag returned by S3.
            if(!expected.equals(response.getMd5Checksum())) {
                throw new ChecksumException("Upload failed",
                        String.format("Mismatch between MD5 hash of uploaded data (%s) and ETag returned by the server (%s)",
                                expected, response.getMd5Checksum())
                );
            }
        }
    }
}
