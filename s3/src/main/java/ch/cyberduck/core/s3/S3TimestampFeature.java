package ch.cyberduck.core.s3;

/*
 * Copyright (c) 2002-2021 iterate GmbH. All rights reserved.
 * https://cyberduck.io/
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 */

import ch.cyberduck.core.Header;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.shared.DefaultTimestampFeature;
import ch.cyberduck.core.transfer.TransferStatus;

import java.util.HashMap;
import java.util.Map;

public class S3TimestampFeature extends DefaultTimestampFeature {

    // Interoperable with rclone
    private static final String METADATA_MODIFICATION_DATE = "Mtime";

    private final S3Session session;

    public S3TimestampFeature(final S3Session session) {
        this.session = session;
    }

    @Override
    public void setTimestamp(final Path file, final TransferStatus status) throws BackgroundException {
        final S3MetadataFeature feature = new S3MetadataFeature(session, new S3AccessControlListFeature(session));
        final Map<String, String> metadata = feature.getMetadata(file);
        feature.setMetadata(file, status.withMetadata(metadata));
    }

    public static Header toHeader(final Long millis) {
        return new Header(S3TimestampFeature.METADATA_MODIFICATION_DATE, String.valueOf(millis / 1000));
    }

    public static Long fromHeaders(final HashMap<String, String> response) {
        if(response.containsKey(S3TimestampFeature.METADATA_MODIFICATION_DATE)) {
            try {
                return Double.valueOf(response.get(S3TimestampFeature.METADATA_MODIFICATION_DATE)).longValue() * 1000;
            }
            catch(NumberFormatException ignored) {
                // ignore
            }
        }
        return -1L;
    }
}
