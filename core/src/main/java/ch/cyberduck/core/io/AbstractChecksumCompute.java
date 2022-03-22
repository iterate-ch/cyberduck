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

import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.exception.ChecksumCanceledException;
import ch.cyberduck.core.exception.ChecksumException;
import ch.cyberduck.core.exception.ConnectionCanceledException;
import ch.cyberduck.core.transfer.TransferStatus;

import org.apache.commons.io.IOUtils;
import org.apache.commons.io.input.BoundedInputStream;

import java.io.IOException;
import java.io.InputStream;
import java.nio.channels.ClosedChannelException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public abstract class AbstractChecksumCompute implements ChecksumCompute {

    protected byte[] digest(final String algorithm, final InputStream in, final StreamCancelation cancelation)
            throws ConnectionCanceledException, ChecksumException {
        final MessageDigest md;
        try {
            md = MessageDigest.getInstance(algorithm);
        }
        catch(NoSuchAlgorithmException e) {
            throw new ChecksumException(e);
        }
        return this.digest(in, md, cancelation);
    }

    protected byte[] digest(final InputStream in, final MessageDigest md, final StreamCancelation cancelation)
            throws ConnectionCanceledException, ChecksumException {
        try {
            byte[] buffer = new byte[16384];
            int bytesRead;
            while((bytesRead = in.read(buffer, 0, buffer.length)) != -1) {
                cancelation.validate();
                md.update(buffer, 0, bytesRead);
            }
        }
        catch(ClosedChannelException e) {
            throw new ChecksumCanceledException(e);
        }
        catch(IOException e) {
            throw new ChecksumException(e);
        }
        finally {
            IOUtils.closeQuietly(in);
        }
        return md.digest();
    }

    protected InputStream normalize(final InputStream in, final TransferStatus status) throws ChecksumException {
        try {
            final InputStream bounded = status.getLength() > 0 ?
                    new BoundedInputStream(in, status.getOffset() + status.getLength()) : in;
            return status.getOffset() > 0 ? StreamCopier.skip(bounded, status.getOffset()) : bounded;
        }
        catch(BackgroundException e) {
            throw new ChecksumException(e);
        }
    }
}
