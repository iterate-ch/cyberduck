package ch.cyberduck.core.io;

/*
 * Copyright (c) 2002-2015 David Kocher. All rights reserved.
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

import ch.cyberduck.core.exception.ChecksumException;

import java.io.InputStream;

/**
 * @version $Id$
 */
public final class ChecksumComputeFactory {

    private ChecksumComputeFactory() {
        //
    }

    public static ChecksumCompute get(final HashAlgorithm algorithm) {
        switch(algorithm) {
            case md5:
                return new MD5ChecksumCompute();
            case sha1:
                return new SHA1ChecksumCompute();
            case sha256:
                return new SHA256ChecksumCompute();
            case sha512:
                return new SHA512ChecksumCompute();
            case crc32:
                return new CRC32ChecksumCompute();
            default:
                return new ChecksumCompute() {
                    @Override
                    public Checksum compute(final InputStream in) throws ChecksumException {
                        return null;
                    }
                };
        }
    }
}
