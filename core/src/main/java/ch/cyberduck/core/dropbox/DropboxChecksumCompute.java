package ch.cyberduck.core.dropbox;

/*
 * Copyright (c) 2002-2021 iterate GmbH. All rights reserved.
 * https://cyberduck.io/
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 */

import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.io.AbstractChecksumCompute;
import ch.cyberduck.core.io.Checksum;
import ch.cyberduck.core.io.HashAlgorithm;
import ch.cyberduck.core.transfer.TransferStatus;

import java.io.InputStream;
import java.security.MessageDigest;

public class DropboxChecksumCompute extends AbstractChecksumCompute {

    @Override
    public MessageDigest digest() {
        return new DropboxContentHasher();
    }

    @Override
    public Checksum compute(final InputStream in, final TransferStatus status) throws BackgroundException {
        return new Checksum(HashAlgorithm.dropbox_content_hash, this.digest(this.normalize(in, status), this.digest(), status));
    }

    @Override
    public HashAlgorithm getAlgorithm() {
        return HashAlgorithm.dropbox_content_hash;
    }
}
