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
import ch.cyberduck.core.features.Upload;
import ch.cyberduck.core.features.Write;
import ch.cyberduck.core.io.BandwidthThrottle;
import ch.cyberduck.core.io.Checksum;
import ch.cyberduck.core.io.SkippingDigestInputStream;
import ch.cyberduck.core.io.StatusOutputStream;
import ch.cyberduck.core.io.StreamCancelation;
import ch.cyberduck.core.io.StreamCopier;
import ch.cyberduck.core.io.StreamListener;
import ch.cyberduck.core.io.StreamProgress;
import ch.cyberduck.core.io.ThrottledOutputStream;
import ch.cyberduck.core.transfer.TransferStatus;

import org.apache.commons.codec.binary.Hex;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.InputStream;
import java.security.MessageDigest;
import java.text.MessageFormat;

public class DefaultUploadFeature<Reply> implements Upload<Reply> {
    private static final Logger log = LogManager.getLogger(DefaultUploadFeature.class);

    @Override
    public Reply upload(final Write<Reply> write, final Path file, final Local local, final BandwidthThrottle throttle,
                        final ProgressListener message, final StreamListener listener, final TransferStatus status, final ConnectionCallback callback) throws BackgroundException {
        return this.upload(write, file, local, throttle, message, listener, status, status, status, callback);
    }

    public Reply upload(final Write<Reply> write, final Path file, final Local local, final BandwidthThrottle throttle,
                        final ProgressListener message, final StreamListener listener, final TransferStatus status,
                        final StreamCancelation cancel, final StreamProgress progress, final ConnectionCallback callback) throws BackgroundException {
        final MessageDigest digest;
        // Checksum of segments is not part of the verification of the whole file
        if(status.isVerify()) {
            digest = this.digest(write, file, status);
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
            in = new SkippingDigestInputStream(local.getInputStream(), digest);
        }
        final StatusOutputStream<Reply> out = write.write(file, status, callback);
        new StreamCopier(cancel, progress)
                .withOffset(status.getOffset())
                .withLimit(status.getLength())
                .withListener(listener)
                .transfer(in, new ThrottledOutputStream(out, throttle));
        final Reply response = out.getStatus();
        log.debug("Received response {}", response);
        if(digest != null) {
            message.message(MessageFormat.format(LocaleFactory.localizedString("Calculate checksum for {0}", "Status"), file.getName()));
            // Verified when upload is complete comparing with the checksum in the response of the server
            status.setDigest(this.checksum(digest));
        }
        return response;
    }

    /**
     * @return Message digest to calculate the checksum while streaming the content. Defaults to the algorithm of the
     * checksum of the write feature. Null to disable verification.
     * @see Write#checksum(Path, TransferStatus)
     */
    public MessageDigest digest(final Write<Reply> write, final Path file, final TransferStatus status) throws BackgroundException {
        return write.checksum(file, status).digest();
    }

    /**
     * @param digest Digest of the uploaded data
     * @return Locally calculated checksum with the algorithm inferred from the length of the hash. Override when the
     * algorithm cannot be determined from the hash.
     */
    protected Checksum checksum(final MessageDigest digest) {
        return Checksum.parse(Hex.encodeHexString(digest.digest()));
    }
}
