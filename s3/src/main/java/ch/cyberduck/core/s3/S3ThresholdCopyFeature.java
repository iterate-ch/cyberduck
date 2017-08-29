package ch.cyberduck.core.s3;

/*
 * Copyright (c) 2002-2015 David Kocher. All rights reserved.
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
import ch.cyberduck.core.Path;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.preferences.Preferences;
import ch.cyberduck.core.preferences.PreferencesFactory;
import ch.cyberduck.core.transfer.TransferStatus;

public class S3ThresholdCopyFeature extends S3CopyFeature {

    private final Preferences preferences
            = PreferencesFactory.get();

    private final S3Session session;
    private final S3AccessControlListFeature accessControlListFeature;

    private final Long multipartThreshold
            = preferences.getLong("s3.upload.multipart.required.threshold");

    public S3ThresholdCopyFeature(final S3Session session) {
        this(session, new S3AccessControlListFeature(session));
    }

    public S3ThresholdCopyFeature(final S3Session session, final S3AccessControlListFeature accessControlListFeature) {
        super(session, accessControlListFeature);
        this.session = session;
        this.accessControlListFeature = accessControlListFeature;
    }

    public Path copy(final Path source, final Path copy, final TransferStatus status, final ConnectionCallback callback) throws BackgroundException {
        if(status.getLength() > multipartThreshold) {
            return new S3MultipartCopyFeature(session, accessControlListFeature).copy(source, copy, status, callback);
        }
        else {
            return new S3CopyFeature(session, accessControlListFeature).copy(source, copy, status, callback);
        }
    }
}
