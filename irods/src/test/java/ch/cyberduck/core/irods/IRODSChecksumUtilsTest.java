package ch.cyberduck.core.irods;

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

import ch.cyberduck.core.io.Checksum;

import ch.cyberduck.core.io.HashAlgorithm;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.binary.Hex;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class IRODSChecksumUtilsTest {

    @Test
    public void testInvalidInputs() {
        assertEquals(Checksum.NONE, IRODSChecksumUtils.toChecksum(null));
        assertEquals(Checksum.NONE, IRODSChecksumUtils.toChecksum(""));
        assertEquals(Checksum.NONE, IRODSChecksumUtils.toChecksum("no_colon"));
        assertEquals(Checksum.NONE, IRODSChecksumUtils.toChecksum("sha2:"));
    }

    @Test
    public void testValidChecksum() {
        final String irodsChecksum = "sha2:Qu9ZCAIUVS3AR4gaILMZevr5PK7XAO9vqlhSF0u+ha0=";
        final String checksumNoPrefix = irodsChecksum.substring(irodsChecksum.indexOf(':') + 1);
        final String hexEncodedChecksum = Hex.encodeHexString(Base64.decodeBase64(checksumNoPrefix));
        final Checksum expectedChecksum = Checksum.parse(hexEncodedChecksum);
        assertEquals(expectedChecksum, IRODSChecksumUtils.toChecksum(irodsChecksum));
        assertEquals(HashAlgorithm.sha256, expectedChecksum.algorithm);
    }
}
