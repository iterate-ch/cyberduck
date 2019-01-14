package ch.cyberduck.core.io;

/*
 * Copyright (c) 2002-2018 iterate GmbH. All rights reserved.
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

import ch.cyberduck.core.LocaleFactory;
import ch.cyberduck.core.exception.ChecksumException;
import ch.cyberduck.core.transfer.TransferStatus;

import org.apache.commons.codec.binary.Hex;
import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.InputStream;

import com.twmacinta.util.MD5;

public class MD5FastChecksumCompute extends AbstractChecksumCompute {

    static {
        // Indicate that the native library should not be loaded
        MD5.initNativeLibrary(true);
    }

    @Override
    public Checksum compute(final InputStream in, final TransferStatus status) throws ChecksumException {
        return new Checksum(HashAlgorithm.md5, Hex.encodeHexString(this.digest("MD5",
            this.normalize(in, status))));
    }

    protected byte[] digest(final String algorithm, final InputStream in) throws ChecksumException {
        final MD5 md = new MD5();

        try {
            byte[] buffer = new byte[16384];
            int bytesRead;
            while((bytesRead = in.read(buffer, 0, buffer.length)) != -1) {
                md.Update(buffer, 0, bytesRead);
            }
        }
        catch(IOException e) {
            throw new ChecksumException(LocaleFactory.localizedString("Checksum failure", "Error"), e.getMessage(), e);
        }
        finally {
            IOUtils.closeQuietly(in);
        }
        return md.Final();
    }

}
