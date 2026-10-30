package ch.cyberduck.core.io;

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

import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.exception.ChecksumException;
import ch.cyberduck.core.transfer.TransferStatus;

import java.io.InputStream;
import java.security.MessageDigest;

public class SHA512ChecksumCompute extends AbstractChecksumCompute {

    @Override
    public MessageDigest digest() throws ChecksumException {
        return instance("SHA-512");
    }

    @Override
    public Checksum compute(final InputStream in, final TransferStatus status) throws BackgroundException {
        return new Checksum(HashAlgorithm.sha512, this.digest(this.normalize(in, status), this.digest(), status));
    }

    @Override
    public HashAlgorithm getAlgorithm() {
        return HashAlgorithm.sha512;
    }
}
