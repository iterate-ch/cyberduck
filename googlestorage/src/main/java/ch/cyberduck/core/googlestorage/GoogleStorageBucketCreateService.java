package ch.cyberduck.core.googlestorage;

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

import ch.cyberduck.core.LocaleFactory;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.exception.InteroperabilityException;
import ch.cyberduck.core.preferences.PreferencesFactory;
import ch.cyberduck.core.s3.S3ExceptionMappingService;
import ch.cyberduck.core.s3.S3PathContainerService;

import org.jets3t.service.ServiceException;
import org.jets3t.service.acl.AccessControlList;
import org.jets3t.service.acl.gs.GSAccessControlList;
import org.jets3t.service.utils.ServiceUtils;

public class GoogleStorageBucketCreateService {

    private final GoogleStorageSession session;

    public GoogleStorageBucketCreateService(final GoogleStorageSession session) {
        this.session = session;
    }

    public void create(final Path bucket, final String location) throws BackgroundException {
        // Create bucket
        if(!ServiceUtils.isBucketNameValidDNSName(bucket.getName())) {
            throw new InteroperabilityException(LocaleFactory.localizedString("Bucket name is not DNS compatible", "S3"));
        }
        AccessControlList acl;
        if(PreferencesFactory.get().getProperty("s3.bucket.acl.default").equals("public-read")) {
            acl = GSAccessControlList.REST_CANNED_PUBLIC_READ;
        }
        else {
            acl = GSAccessControlList.REST_CANNED_PRIVATE;
        }
        try {
            session.getClient().createBucket(new S3PathContainerService().getContainer(bucket).getName(), location, acl);
        }
        catch(ServiceException e) {
            throw new S3ExceptionMappingService().map("Cannot create folder {0}", e, bucket);
        }
    }
}