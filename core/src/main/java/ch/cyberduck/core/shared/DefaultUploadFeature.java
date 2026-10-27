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
import ch.cyberduck.core.io.ChecksumCompute;
import ch.cyberduck.core.io.ChecksumComputeFactory;
import ch.cyberduck.core.io.DisabledChecksumCompute;
import ch.cyberduck.core.io.HashAlgorithm;
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
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.text.MessageFormat;
import java.util.EnumSet;

public class DefaultUploadFeature<Reply> implements Upload<Reply> {
    private static final Logger log = LogManager.getLogger(DefaultUploadFeature.class);

    private final EnumSet<Flags> flags;
    private final HashAlgorithm algorithm;

    public DefaultUploadFeature() {
        this(EnumSet.noneOf(Flags.class), null);
    }

    /**
     * @param flags     Supported features
     * @param algorithm Algorithm to calculate the checksum of the file for the upload. Null if not supported.
     */
    public DefaultUploadFeature(final EnumSet<Flags> flags, final HashAlgorithm algorithm) {
        this.flags = flags;
        this.algorithm = algorithm;
    }

    @Override
    public ChecksumCompute checksum(final Path file, final TransferStatus status) {
        if(null == algorithm) {
            return new DisabledChecksumCompute();
        }
        return ChecksumComputeFactory.get(algorithm);
    }

    @Override
    public EnumSet<Flags> features(final Path file, final TransferStatus status) {
        return flags;
    }

    @Override
    public Reply upload(final Write<Reply> write, final Path file, final Local local, final BandwidthThrottle throttle,
                        final ProgressListener message, final StreamListener listener, final TransferStatus status, final ConnectionCallback callback) throws BackgroundException {
        return this.upload(write, file, local, throttle, message, listener, status, status, status, callback);
    }

    public Reply upload(final Write<Reply> write, final Path file, final Local local, final BandwidthThrottle throttle,
                        final ProgressListener message, final StreamListener listener, final TransferStatus status,
                        final StreamCancelation cancel, final StreamProgress progress, final ConnectionCallback callback) throws BackgroundException {
        final MessageDigest digest = status.isVerify() ? this.digest(file, status) : null;
        final InputStream in;
        // Wrap with digest stream if available
        if(null == digest) {
            in = local.getInputStream();
        }
        else if(status.isSegment()) {
            // Only the content of the segment with offset and length is part of the checksum
            in = new DigestInputStream(local.getInputStream(), digest);
        }
        else {
            // Include content skipped when resuming in the checksum of the whole file
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
     * @return Message digest to calculate the checksum while streaming the content with the algorithm to verify. Null
     * to disable verification or when the digest is applied by the write feature to transformed content.
     * @see TransferStatus#getVerify()
     * @see Flags#transform
     * @see TransferStatus#transform(ChecksumCompute)
     */
    public MessageDigest digest(final Path file, final TransferStatus status) throws BackgroundException {
        if(status.transform(this.checksum(file, status)).isTransformed()) {
            // Digest is applied by the write feature to the transformed content
            return null;
        }
        return ChecksumComputeFactory.get(status.getVerify()).digest();
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
