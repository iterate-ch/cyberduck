package ch.cyberduck.core.gmxcloud;

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
import ch.cyberduck.core.io.StreamCopier;
import ch.cyberduck.core.transfer.TransferStatus;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.IOUtils;
import org.apache.http.impl.io.ChunkedInputStream;

import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class GmxcloudCdash64Compute extends AbstractChecksumCompute {

    @Override
    public Checksum compute(final InputStream in, final TransferStatus status) throws ChecksumException {
        try {
            MessageDigest messageDigest = MessageDigest.getInstance("SHA-256");
            final int length = Long.valueOf(status.getLength()).intValue();
            final InputStream offsetSkippedInputStream = StreamCopier.skip(in, status.getOffset());
            final byte[] readbytes = new byte[length];
            IOUtils.read(offsetSkippedInputStream, readbytes);
            messageDigest.update(readbytes);
            final byte[] digestedBytes = messageDigest.digest();
            messageDigest.reset();
            messageDigest.update(digestedBytes);
            messageDigest.update(Util.intToBytes(readbytes.length, 4));
            final String cdash64 = Base64.encodeBase64URLSafeString(messageDigest.digest());
            return new Checksum(HashAlgorithm.sha256, cdash64);
        }
        catch(NoSuchAlgorithmException | IOException | BackgroundException e) {
            throw new ChecksumException(e.getMessage(), e);
        }
    }
}
