package ch.cyberduck.core.shared;

/*
 * Copyright (c) 2002-2013 David Kocher. All rights reserved.
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

import ch.cyberduck.core.ConnectionCallback;
import ch.cyberduck.core.DefaultIOExceptionMappingService;
import ch.cyberduck.core.Local;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.Session;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.features.Download;
import ch.cyberduck.core.features.Read;
import ch.cyberduck.core.io.BandwidthThrottle;
import ch.cyberduck.core.io.Checksum;
import ch.cyberduck.core.io.ChecksumComputeFactory;
import ch.cyberduck.core.io.StreamCopier;
import ch.cyberduck.core.io.StreamListener;
import ch.cyberduck.core.io.ThrottledInputStream;
import ch.cyberduck.core.transfer.TransferStatus;

import org.apache.commons.io.IOUtils;
import org.apache.commons.io.output.NullOutputStream;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.DigestInputStream;
import java.security.MessageDigest;

public class DefaultDownloadFeature implements Download {

    private final Session<?> session;

    public DefaultDownloadFeature(final Session<?> session) {
        this.session = session;
    }

    @Override
    public void download(final Read read, final Path file, final Local local, final BandwidthThrottle throttle, final StreamListener listener,
                         final TransferStatus status, final ConnectionCallback callback) throws BackgroundException {
        final MessageDigest digest;
        if(status.isVerify()) {
            if(status.isSegment()) {
                // Segments are downloaded concurrently and cannot be verified with a single digest
                digest = null;
            }
            else {
                digest = ChecksumComputeFactory.get(status.getVerify()).digest();
            }
        }
        else {
            digest = null;
        }
        if(digest != null) {
            if(status.isAppend()) {
                this.update(digest, local, status);
            }
        }
        final InputStream in = read.read(file, status, callback);
        final OutputStream out = local.getOutputStream(!status.isSegment() && status.isAppend());
        new StreamCopier(status, status)
                .withOffset(0L)
                .withLimit(status.getLength())
                .withListener(listener)
                .transfer(new ThrottledInputStream(digest != null ? new DigestInputStream(in, digest) : in, throttle), out);
        if(digest != null) {
            // Verified when download is complete comparing with the checksum of the remote file
            status.setDigest(new Checksum(status.getVerify(), digest.digest()));
        }
    }

    /**
     * Include content previously downloaded in the checksum of the whole file when resuming
     */
    private void update(final MessageDigest digest, final Local local, final TransferStatus status) throws BackgroundException {
        try(InputStream in = local.getInputStream()) {
            IOUtils.copyLarge(new DigestInputStream(in, digest), NullOutputStream.INSTANCE, 0L, status.getOffset());
        }
        catch(IOException e) {
            throw new DefaultIOExceptionMappingService().map(e);
        }
    }

    @Override
    public boolean offset(final Path file) throws BackgroundException {
        return session.getFeature(Read.class).offset(file);
    }
}
