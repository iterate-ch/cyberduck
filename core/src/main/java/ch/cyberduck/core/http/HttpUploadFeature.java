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

import ch.cyberduck.core.Cache;
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
import ch.cyberduck.core.io.HashAlgorithm;
import ch.cyberduck.core.io.StatusOutputStream;
import ch.cyberduck.core.io.StreamCancelation;
import ch.cyberduck.core.io.StreamCopier;
import ch.cyberduck.core.io.StreamListener;
import ch.cyberduck.core.io.StreamProgress;
import ch.cyberduck.core.io.ThrottledOutputStream;
import ch.cyberduck.core.transfer.TransferStatus;

import org.apache.commons.codec.binary.Hex;
import org.apache.http.client.HttpResponseException;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.text.MessageFormat;

public class HttpUploadFeature<Reply, Digest> implements Upload<Reply> {
    private static final Logger log = Logger.getLogger(HttpUploadFeature.class);

    private Write<Reply> writer;

    public HttpUploadFeature(final Write<Reply> writer) {
        this.writer = writer;
    }

    @Override
    public Write.Append append(final Path file, final Long length, final Cache<Path> cache) throws BackgroundException {
        return writer.append(file, length, cache);
    }

    @Override
    public Reply upload(final Path file, final Local local, final BandwidthThrottle throttle,
                        final StreamListener listener, final TransferStatus status, final ConnectionCallback callback) throws BackgroundException {
        return this.upload(file, local, throttle, listener, status, status, status, callback);
    }

    public Reply upload(final Path file, final Local local, final BandwidthThrottle throttle,
                        final StreamListener listener, final TransferStatus status,
                        final StreamCancelation cancel, final StreamProgress progress, final ConnectionCallback callback) throws BackgroundException {
        try {
            final Digest digest = this.digest();
            // Wrap with digest stream if available
            final InputStream in = this.decorate(local.getInputStream(), digest);
            final StatusOutputStream<Reply> out = writer.write(file, status, callback);
            new StreamCopier(cancel, progress)
                    .withOffset(status.getOffset())
                    .withLimit(status.getLength())
                    .withListener(listener)
                    .transfer(in, new ThrottledOutputStream(out, throttle));
            final Reply response = out.getStatus();
            this.post(file, digest, response);
            return response;
        }
        catch(HttpResponseException e) {
            throw new HttpResponseExceptionMappingService().map("Upload {0} failed", e, file);
        }
        catch(IOException e) {
            throw new HttpExceptionMappingService().map("Upload {0} failed", e, file);
        }
    }

    protected InputStream decorate(final InputStream in, final Digest digest) throws IOException {
        return in;
    }

    protected Digest digest() throws IOException {
        return null;
    }

    protected void post(final Path file, final Digest digest, final Reply response) throws BackgroundException {
        if(log.isDebugEnabled()) {
            log.debug(String.format("Received response %s", response));
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
        if(null == checksum || !checksum.algorithm.equals(HashAlgorithm.md5)) {
            log.warn("ETag returned by server is unknown checksum algorithm");
            return;
        }
        if(!checksum.algorithm.equals(HashAlgorithm.md5)) {
            log.warn(String.format("ETag %s returned by server is %s but expected MD5", checksum.hash, checksum.algorithm));
            return;
        }
        // Obtain locally-calculated MD5 hash.
        final String expected = Hex.encodeHexString(digest.digest());
        // Compare our locally-calculated hash with the ETag returned by S3.
        if(!checksum.equals(Checksum.parse(expected))) {
            throw new ChecksumException(MessageFormat.format(LocaleFactory.localizedString("Upload {0} failed", "Error"), file.getName()),
                    MessageFormat.format("Mismatch between MD5 hash {0} of uploaded data and ETag {1} returned by the server",
                            expected, checksum.hash));
        }
    }

    @Override
    public Upload<Reply> withWriter(final Write<Reply> writer) {
        this.writer = writer;
        return this;
    }
}