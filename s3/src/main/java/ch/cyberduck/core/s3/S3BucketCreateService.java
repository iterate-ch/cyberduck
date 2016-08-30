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

import ch.cyberduck.core.LocaleFactory;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathContainerService;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.exception.InteroperabilityException;
import ch.cyberduck.core.preferences.PreferencesFactory;

import org.jets3t.service.ServiceException;
import org.jets3t.service.acl.AccessControlList;
import org.jets3t.service.utils.ServiceUtils;

public class S3BucketCreateService {

    private S3Session session;

    private PathContainerService containerService
            = new S3PathContainerService();

    public S3BucketCreateService(final S3Session session) {
        this.session = session;
    }

    public void create(final Path bucket, final String location) throws BackgroundException {
        // Create bucket
        if(!ServiceUtils.isBucketNameValidDNSName(bucket.getName())) {
            throw new InteroperabilityException(LocaleFactory.localizedString("Bucket name is not DNS compatible", "S3"));
        }
        AccessControlList acl;
        if(PreferencesFactory.get().getProperty("s3.bucket.acl.default").equals("public-read")) {
            acl = AccessControlList.REST_CANNED_PUBLIC_READ;
        }
        else {
            acl = AccessControlList.REST_CANNED_PRIVATE;
        }
        try {
            final String region;
            if("us-east-1".equals(location)) {
                region = "US";
            }
            else {
                region = location;
            }
            session.getClient().createBucket(containerService.getContainer(bucket).getName(), region, acl);
        }
        catch(ServiceException e) {
            throw new S3ExceptionMappingService().map("Cannot create folder {0}", e, bucket);
        }
    }
}