package ch.cyberduck.core.dropbox;


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
import ch.cyberduck.core.http.HttpUploadFeature;
import ch.cyberduck.core.io.Checksum;
import ch.cyberduck.core.io.ChecksumCompute;
import ch.cyberduck.core.io.ChecksumComputeFactory;
import ch.cyberduck.core.io.HashAlgorithm;
import ch.cyberduck.core.transfer.TransferStatus;

import org.apache.commons.codec.binary.Hex;

import java.security.MessageDigest;
import java.util.EnumSet;

import com.dropbox.core.v2.files.Metadata;

public class DropboxUploadFeature extends HttpUploadFeature<Metadata> {

    final DropboxSession session;

    public DropboxUploadFeature(final DropboxSession session) {
        this.session = session;
    }

    @Override
    public ChecksumCompute checksum(final Path file, final TransferStatus status) {
        return ChecksumComputeFactory.get(HashAlgorithm.dropbox_content_hash);
    }

    @Override
    public EnumSet<Flags> features(final Path file, final TransferStatus status) {
        return EnumSet.of(Flags.checksumResponse);
    }

    @Override
    protected Checksum checksum(final MessageDigest digest) {
        // Same length as SHA-256 but a different algorithm
        return new Checksum(HashAlgorithm.dropbox_content_hash, Hex.encodeHexString(digest.digest()));
    }
}
