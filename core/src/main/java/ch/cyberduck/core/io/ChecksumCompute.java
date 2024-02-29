package ch.cyberduck.core.io;

/*
 * Copyright (c) 2012 David Kocher. All rights reserved.
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
 * Bug fixes, suggestions and comments should be sent to:
 * dkocher@cyberduck.ch
 */

import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.exception.ChecksumCanceledException;
import ch.cyberduck.core.exception.ChecksumException;
import ch.cyberduck.core.transfer.TransferStatus;

import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Hex;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

public interface ChecksumCompute {

    /**
     * Implementation for given algorithm
     *
     * @param in     Stream that will be closed when the checksum is computed
     * @param status Offset and limit to read from stream
     * @return Calculated fingerprint
     * @throws ChecksumException         Error calculating checksum
     * @throws ChecksumCanceledException Calculating checksum was interrupted
     */
    Checksum compute(InputStream in, TransferStatus status) throws BackgroundException;

    /**
     * @param data Hex encoded string
     * @return Calculated fingerprint
     * @throws ChecksumException         Error calculating checksum
     * @throws ChecksumCanceledException Calculating checksum was interrupted
     */
    default Checksum compute(final String data) throws BackgroundException {
        try {
            return this.compute(new ByteArrayInputStream(Hex.decodeHex(data.toCharArray())),
                    new TransferStatus());
        }
        catch(DecoderException e) {
            throw new ChecksumException(e);
        }
    }
}
