package ch.cyberduck.core.shared;

/*
 * Copyright (c) 2013 David Kocher. All rights reserved.
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
 * feedback@cyberduck.ch
 */

import ch.cyberduck.core.ConnectionCallback;
import ch.cyberduck.core.Local;
import ch.cyberduck.core.LocaleFactory;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.ProgressListener;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.exception.ChecksumException;
import ch.cyberduck.core.features.AttributesAdapter;
import ch.cyberduck.core.features.Upload;
import ch.cyberduck.core.features.Write;
import ch.cyberduck.core.http.DefaultHttpResponseExceptionMappingService;
import ch.cyberduck.core.http.HttpExceptionMappingService;
import ch.cyberduck.core.io.BandwidthThrottle;
import ch.cyberduck.core.io.Checksum;
import ch.cyberduck.core.io.StatusOutputStream;
import ch.cyberduck.core.io.StreamCancelation;
import ch.cyberduck.core.io.StreamCopier;
import ch.cyberduck.core.io.StreamListener;
import ch.cyberduck.core.io.StreamProgress;
import ch.cyberduck.core.io.ThrottledOutputStream;
import ch.cyberduck.core.preferences.PreferencesFactory;
import ch.cyberduck.core.transfer.TransferStatus;

import org.apache.commons.codec.binary.Hex;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.http.client.HttpResponseException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.text.MessageFormat;

public class DefaultUploadFeature<Reply> implements Upload<Reply> {
    private static final Logger log = LogManager.getLogger(DefaultUploadFeature.class);

    private final AttributesAdapter<Reply> adapter;

    public DefaultUploadFeature(final AttributesAdapter<Reply> adapter) {
        this.adapter = adapter;
    }

    @Override
    public Reply upload(final Write<Reply> write, final Path file, final Local local, final BandwidthThrottle throttle,
                        final ProgressListener progress, final StreamListener listener, final TransferStatus status, final ConnectionCallback callback) throws BackgroundException {
        return this.upload(write, file, local, throttle, listener, status, status, status, callback);
    }

    public Reply upload(final Write<Reply> write, final Path file, final Local local, final BandwidthThrottle throttle,
                        final StreamListener listener, final TransferStatus status,
                        final StreamCancelation cancel, final StreamProgress progress, final ConnectionCallback callback) throws BackgroundException {
        try {
            final MessageDigest digest;
            if(PreferencesFactory.get().getBoolean("queue.upload.checksum.calculate")) {
                digest = this.digest();
            }
            else {
                digest = null;
            }
            final InputStream in;
            // Wrap with digest stream if available
            if(null == digest) {
                in = local.getInputStream();
            }
            else {
                in = new DigestInputStream(local.getInputStream(), digest);
            }
            final StatusOutputStream<Reply> out = write.write(file, status, callback);
            new StreamCopier(cancel, progress)
                    .withOffset(status.getOffset())
                    .withLimit(status.getLength())
                    .withListener(listener)
                    .transfer(in, new ThrottledOutputStream(out, throttle));
            final Reply response = out.getStatus();
            log.debug("Received response {}", response);
            this.verify(file, digest, response);
            return response;
        }
        catch(HttpResponseException e) {
            throw new DefaultHttpResponseExceptionMappingService().map("Upload {0} failed", e, file);
        }
        catch(IOException e) {
            throw new HttpExceptionMappingService().map("Upload {0} failed", e, file);
        }
    }

    public MessageDigest digest() throws IOException {
        return null;
    }

    public void verify(final Path file, final MessageDigest digest, final Reply response) throws BackgroundException {
        if(file.getType().contains(Path.Type.encrypted)) {
            log.warn("Skip checksum verification for {} with client side encryption enabled", file);
            return;
        }
        if(null == digest) {
            log.debug("Digest disabled for file {}", file);
            return;
        }
        final Checksum checksum = adapter.toAttributes(response).getChecksum();
        if(Checksum.NONE.equals(checksum)) {
            log.debug("Missing checksum for {}", file);
            return;
        }
        // Get locally computed MD5 hash.
        final Checksum expected = Checksum.parse(Hex.encodeHexString(digest.digest()));
        if(ObjectUtils.notEqual(expected.algorithm, checksum.algorithm)) {
            log.warn("ETag {} returned by server is {} but expected {}", checksum.hash, checksum.algorithm, expected.algorithm);
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
}
