package ch.cyberduck.core.http;

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
import ch.cyberduck.core.Local;
import ch.cyberduck.core.LocaleFactory;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.exception.ChecksumException;
import ch.cyberduck.core.features.Upload;
import ch.cyberduck.core.features.Write;
import ch.cyberduck.core.io.BandwidthThrottle;
import ch.cyberduck.core.io.Checksum;
import ch.cyberduck.core.io.StatusOutputStream;
import ch.cyberduck.core.io.StreamCancelation;
import ch.cyberduck.core.io.StreamCopier;
import ch.cyberduck.core.io.StreamListener;
import ch.cyberduck.core.io.StreamProgress;
import ch.cyberduck.core.io.ThrottledOutputStream;
import ch.cyberduck.core.transfer.TransferStatus;

import org.apache.commons.codec.binary.Hex;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.http.client.HttpResponseException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.text.MessageFormat;

public class HttpUploadFeature<Reply, Digest> implements Upload<Reply> {
    private static final Logger log = LogManager.getLogger(HttpUploadFeature.class);

    private Write<Reply> writer;

    public HttpUploadFeature(final Write<Reply> writer) {
        this.writer = writer;
    }

    @Override
    public Reply upload(final Path file, final Local local, final BandwidthThrottle throttle,
                        final StreamListener listener, final TransferStatus status, final ConnectionCallback callback) throws BackgroundException {
        final Reply response = this.upload(file, local, throttle, listener, status, status, status, callback);
        if(log.isDebugEnabled()) {
            log.debug(String.format("Received response %s", response));
        }
        return response;
    }

    public Reply upload(final Path file, final Local local, final BandwidthThrottle throttle,
                        final StreamListener listener, final TransferStatus status,
                        final StreamCancelation cancel, final StreamProgress progress, final ConnectionCallback callback) throws BackgroundException {
        try {
            final Digest digest = this.digest();
            final Reply response = this.transfer(file, local, throttle, listener, status, cancel, progress, callback, digest);
            this.post(file, digest, response);
            return response;
        }
        catch(HttpResponseException e) {
            throw new DefaultHttpResponseExceptionMappingService().map("Upload {0} failed", e, file);
        }
        catch(IOException e) {
            throw new HttpExceptionMappingService().map("Upload {0} failed", e, file);
        }
    }

    protected Reply transfer(final Path file, final Local local, final BandwidthThrottle throttle, final StreamListener listener,
                             final TransferStatus status, final StreamCancelation cancel, final StreamProgress progress,
                             final ConnectionCallback callback, final Digest digest) throws IOException, BackgroundException {
        // Wrap with digest stream if available
        final InputStream in = this.decorate(local.getInputStream(), digest);
        final StatusOutputStream<Reply> out = writer.write(file, status, callback);
        new StreamCopier(cancel, progress)
                .withOffset(status.getOffset())
                .withLimit(status.getLength())
                .withListener(listener)
                .transfer(in, new ThrottledOutputStream(out, throttle));
        return out.getStatus();
    }

    protected InputStream decorate(final InputStream in, final Digest digest) throws IOException {
        return in;
    }

    protected Digest digest() throws IOException {
        return null;
    }

    protected void post(final Path file, final Digest digest, final Reply response) throws BackgroundException {
        // No-op with no checksum verification by default
        if(log.isDebugEnabled()) {
            log.debug(String.format("Missing checksum verification for %s", file));
        }
    }

    protected void verify(final Path file, final MessageDigest digest, final Checksum checksum) throws ChecksumException {
        if(file.getType().contains(Path.Type.encrypted)) {
            log.warn(String.format("Skip checksum verification for %s with client side encryption enabled", file));
            return;
        }
        if(null == digest) {
            log.debug(String.format("Digest disabled for file %s", file));
            return;
        }
        // Obtain locally-calculated MD5 hash.
        final Checksum expected = Checksum.parse(Hex.encodeHexString(digest.digest()));
        if(ObjectUtils.notEqual(expected.algorithm, checksum.algorithm)) {
            log.warn(String.format("ETag %s returned by server is %s but expected %s", checksum.hash, checksum.algorithm, expected.algorithm));
        }
        else {
            // Compare our locally-calculated hash with the ETag returned by S3.
            if(!checksum.equals(expected)) {
                throw new ChecksumException(MessageFormat.format(LocaleFactory.localizedString("Upload {0} failed", "Error"), file.getName()),
                        MessageFormat.format("Mismatch between MD5 hash {0} of uploaded data and ETag {1} returned by the server",
                                expected, checksum.hash));
            }
        }
    }

    @Override
    public Upload<Reply> withWriter(final Write<Reply> writer) {
        this.writer = writer;
        return this;
    }
}
