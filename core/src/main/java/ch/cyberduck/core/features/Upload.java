package ch.cyberduck.core.features;

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

import ch.cyberduck.core.ConnectionCallback;
import ch.cyberduck.core.Local;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.ProgressListener;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.io.BandwidthThrottle;
import ch.cyberduck.core.io.ChecksumCompute;
import ch.cyberduck.core.io.DisabledChecksumCompute;
import ch.cyberduck.core.io.StreamListener;
import ch.cyberduck.core.transfer.TransferStatus;

import java.util.EnumSet;

@Optional
public interface Upload<Reply> {

    /**
     * Copy file on disk to server
     *
     * @param write    Write feature
     * @param file     File on server
     * @param local    File on local disk
     * @param throttle Bandwidth management
     * @param progress Progress callback
     * @param listener Stream copy callback
     * @param status   Transfer status holder
     * @param callback Prompt
     * @see AttributesAdapter#toAttributes(Reply)
     */
    Reply upload(Write<Reply> write, Path file, Local local, BandwidthThrottle throttle, final ProgressListener progress, StreamListener listener,
                 TransferStatus status, ConnectionCallback callback) throws BackgroundException;

    /**
     * Determine if appending to file is supported
     *
     * @param file   File
     * @param status Transfer status including attributes of file on server and size of file to write
     * @return True if can append to existing file
     */
    default Write.Append append(Path file, TransferStatus status) throws BackgroundException {
        return new Write.Append(false).withStatus(status);
    }

    /**
     * @param file   File on server
     * @param status Transfer status including the size of file to write to differentiate between single and large uploads
     * @return Algorithm to calculate the checksum of the file for the upload. Disabled if not supported.
     */
    default ChecksumCompute checksum(Path file, TransferStatus status) {
        return new DisabledChecksumCompute();
    }

    /**
     * @param file   File on server
     * @param status Transfer status including the size of file to write to differentiate between single and large uploads
     * @return Supported features
     */
    default EnumSet<Flags> features(Path file, TransferStatus status) {
        return EnumSet.noneOf(Flags.class);
    }

    /**
     * Feature flags
     */
    enum Flags {
        /**
         * A checksum of the file is required with the upload request, such as the MD5 for uploads to an S3 bucket
         * with Object Lock. Calculated prior to the transfer regardless of the preference to verify uploads.
         */
        checksumRequired,
        /**
         * A checksum of the file is accepted with the upload request to allow verification on the server. Calculated
         * prior to the transfer when verifying uploads.
         */
        checksumOptional,
        /**
         * The attributes in the response of the server include a checksum of the uploaded file to compare with the
         * checksum calculated by the client. Calculated while streaming when verifying uploads unless calculated
         * prior to the transfer.
         */
        checksumResponse
    }
}
