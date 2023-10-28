package ch.cyberduck.core.s3;

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
 * Bug fixes, suggestions and comments should be sent to:
 * feedback@cyberduck.ch
 */

import ch.cyberduck.core.LocaleFactory;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.exception.AccessDeniedException;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.shared.DefaultTouchFeature;
import ch.cyberduck.core.transfer.TransferStatus;

import org.apache.commons.io.input.NullInputStream;
import org.apache.commons.lang3.StringUtils;
import org.jets3t.service.model.StorageObject;

public class S3TouchFeature extends DefaultTouchFeature<StorageObject> {

    private final S3Session session;

    public S3TouchFeature(final S3Session session, final S3AccessControlListFeature acl) {
        super(new S3WriteFeature(session, acl));
        this.session = session;
    }

    @Override
    public Path touch(final Path file, final TransferStatus status) throws BackgroundException {
        return super.touch(file, status.withChecksum(write.checksum(file, status).compute(new NullInputStream(0L), status)));
    }

    @Override
    public void preflight(final Path workdir, final String filename) throws BackgroundException {
        if(StringUtils.isEmpty(RequestEntityRestStorageService.findBucketInHostname(session.getHost()))) {
            // Creating files is only possible inside a bucket.
            if(workdir.isRoot()) {
                throw new AccessDeniedException(LocaleFactory.localizedString("Unsupported", "Error")).withFile(workdir);
            }
        }
    }
}
