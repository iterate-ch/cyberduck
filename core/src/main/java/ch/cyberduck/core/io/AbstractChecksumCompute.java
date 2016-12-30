package ch.cyberduck.core.io;

/*
 * Copyright (c) 2002-2015 David Kocher. All rights reserved.
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

import ch.cyberduck.core.LocaleFactory;
import ch.cyberduck.core.exception.ChecksumException;
import ch.cyberduck.core.transfer.TransferStatus;

import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Hex;
import org.apache.commons.io.IOUtils;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public abstract class AbstractChecksumCompute implements ChecksumCompute {

    public Checksum compute(final String data, final TransferStatus status) throws ChecksumException {
        try {
            return this.compute(null, new ByteArrayInputStream(Hex.decodeHex(data.toCharArray())), status);
        }
        catch(DecoderException e) {
            throw new ChecksumException(LocaleFactory.localizedString("Checksum failure", "Error"), e.getMessage(), e);
        }
    }

    protected byte[] digest(final String algorithm, final InputStream in) throws ChecksumException {
        final MessageDigest md;
        try {
            md = MessageDigest.getInstance(algorithm);
        }
        catch(NoSuchAlgorithmException e) {
            throw new ChecksumException(LocaleFactory.localizedString("Checksum failure", "Error"), e.getMessage(), e);
        }
        try {
            byte[] buffer = new byte[16384];
            int bytesRead;
            while((bytesRead = in.read(buffer, 0, buffer.length)) != -1) {
                md.update(buffer, 0, bytesRead);
            }
        }
        catch(IOException e) {
            throw new ChecksumException(LocaleFactory.localizedString("Checksum failure", "Error"), e.getMessage(), e);
        }
        finally {
            IOUtils.closeQuietly(in);
        }
        return md.digest();
    }
}
