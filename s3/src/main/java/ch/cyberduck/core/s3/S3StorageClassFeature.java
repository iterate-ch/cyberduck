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
 * Bug fixes, suggestions and comments should be sent to feedback@cyberduck.ch
 */

import ch.cyberduck.core.DisabledListProgressListener;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.exception.NotfoundException;
import ch.cyberduck.core.features.AclPermission;
import ch.cyberduck.core.features.Redundancy;

import org.jets3t.service.model.S3Object;

import java.util.Arrays;
import java.util.List;

/**
 * @version $Id$
 */
public class S3StorageClassFeature implements Redundancy {

    private S3Session session;

    public S3StorageClassFeature(final S3Session session) {
        this.session = session;
    }

    @Override
    public List<String> getClasses() {
        return Arrays.asList(
                S3Object.STORAGE_CLASS_STANDARD,
                "STANDARD_IA", // This storage class (IA, for infrequent access) is optimized for long-lived and less frequently accessed data
                S3Object.STORAGE_CLASS_REDUCED_REDUNDANCY,
                S3Object.STORAGE_CLASS_GLACIER);
    }

    @Override
    public String getClass(final Path file) throws BackgroundException {
        if(file.isFile()) {
            // HEAD request does not include storage class header
            final Path list = new S3ObjectListService(session).list(
                    file.getParent(), new DisabledListProgressListener()).get(file);
            if(null == list) {
                throw new NotfoundException(file.getAbsolute());
            }
            return list.attributes().getStorageClass();
        }
        return null;
    }

    @Override
    public void setClass(final Path file, final String redundancy) throws BackgroundException {
        if(file.isFile()) {
            final S3ThresholdCopyFeature copy = new S3ThresholdCopyFeature(session);
            copy.copy(file, file, redundancy, new S3EncryptionFeature(session).getEncryption(file),
                    session.getFeature(AclPermission.class).getPermission(file));
        }
    }
}
