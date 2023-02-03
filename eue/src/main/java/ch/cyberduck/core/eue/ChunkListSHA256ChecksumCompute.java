package ch.cyberduck.core.eue;

/*
 * Copyright (c) 2002-2014 David Kocher. All rights reserved.
 * http://cyberduck.io/
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
 * Bug fixes, suggestions and comments should be sent to:
 * feedback@cyberduck.io
 */

import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.exception.ChecksumException;
import ch.cyberduck.core.io.AbstractChecksumCompute;
import ch.cyberduck.core.io.Checksum;
import ch.cyberduck.core.io.HashAlgorithm;
import ch.cyberduck.core.transfer.TransferStatus;

import org.apache.commons.codec.binary.Base64;

import java.io.InputStream;
import java.nio.ByteBuffer;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class ChunkListSHA256ChecksumCompute extends AbstractChecksumCompute {

    public static byte[] intToBytes(final int i) {
        final ByteBuffer bb = ByteBuffer.allocate(4);
        bb.putInt(i);
        return bb.array();
    }

    @Override
    public Checksum compute(final InputStream in, final TransferStatus status) throws BackgroundException {
        return this.compute(status.getLength(), super.digest("SHA-256", this.normalize(in, status), status));
    }

    /**
     * @param length        File length
     * @param contentDigest SHA-256
     */
    public Checksum compute(final Number length, final byte[] contentDigest) throws ChecksumException {
        final MessageDigest digest;
        try {
            digest = MessageDigest.getInstance("SHA-256");
        }
        catch(NoSuchAlgorithmException e) {
            throw new ChecksumException(e.getMessage(), e);
        }
        digest.update(contentDigest);
        digest.update(intToBytes(length.intValue()));
        return new Checksum(HashAlgorithm.cdash64, Base64.encodeBase64URLSafeString(digest.digest()));
    }
}
