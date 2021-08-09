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
import ch.cyberduck.core.URIEncoder;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.exception.InteroperabilityException;
import ch.cyberduck.core.preferences.HostPreferences;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.jets3t.service.ServiceException;
import org.jets3t.service.acl.AccessControlList;
import org.jets3t.service.utils.ServiceUtils;

public class S3BucketCreateService {
    private static final Logger log = Logger.getLogger(S3BucketCreateService.class);

    private final S3Session session;
    private final PathContainerService containerService;

    public S3BucketCreateService(final S3Session session) {
        this.session = session;
        this.containerService = session.getFeature(PathContainerService.class);
    }

    public void create(final Path bucket, final String region) throws BackgroundException {
        if(!session.getClient().getConfiguration().getBoolProperty("s3service.disable-dns-buckets", false)) {
            if(!ServiceUtils.isBucketNameValidDNSName(bucket.getName())) {
                throw new InteroperabilityException(LocaleFactory.localizedString("Bucket name is not DNS compatible", "S3"));
            }
        }
        AccessControlList acl;
        if(new HostPreferences(session.getHost()).getProperty("s3.acl.default").equals("public-read")) {
            acl = AccessControlList.REST_CANNED_PUBLIC_READ;
        }
        else {
            acl = AccessControlList.REST_CANNED_PRIVATE;
        }
        try {
            if(StringUtils.isNotBlank(region)) {
                if(S3Session.isAwsHostname(session.getHost().getHostname())) {
                    session.getClient().getConfiguration().setProperty("s3service.s3-endpoint", String.format("s3.dualstack.%s.amazonaws.com", region));
                }
            }
            // Create bucket
            session.getClient().createBucket(URIEncoder.encode(containerService.getContainer(bucket).getName()),
                "us-east-1".equals(region) ? "US" : region, acl);
        }
        catch(ServiceException e) {
            throw new S3ExceptionMappingService().map("Cannot create folder {0}", e, bucket);
        }
    }
}
