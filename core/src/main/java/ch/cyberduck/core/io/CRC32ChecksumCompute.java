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

import ch.cyberduck.core.LocaleFactory;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.exception.ChecksumException;
import ch.cyberduck.core.transfer.TransferStatus;

import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.InputStream;
import java.util.zip.CRC32;

public class CRC32ChecksumCompute implements ChecksumCompute {

    @Override
    public Checksum compute(final Path file, final InputStream in, final TransferStatus status) throws ChecksumException {
        final CRC32 crc32 = new CRC32();
        try {
            byte[] buffer = new byte[16384];
            int bytesRead;
            while((bytesRead = in.read(buffer, 0, buffer.length)) != -1) {
                crc32.update(buffer, 0, bytesRead);
            }
        }
        catch(IOException e) {
            throw new ChecksumException(LocaleFactory.localizedString("Checksum failure", "Error"), e.getMessage(), e);
        }
        finally {
            IOUtils.closeQuietly(in);
        }
        return new Checksum(HashAlgorithm.crc32, Long.toHexString(crc32.getValue()));
    }
}
