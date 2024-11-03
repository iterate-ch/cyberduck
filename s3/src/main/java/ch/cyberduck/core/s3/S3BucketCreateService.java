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
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jets3t.service.ServiceException;
import org.jets3t.service.acl.AccessControlList;
import org.jets3t.service.utils.ServiceUtils;

public class S3BucketCreateService {
    private static final Logger log = LogManager.getLogger(S3BucketCreateService.class);

    private final S3Session session;
    private final PathContainerService containerService;

    public S3BucketCreateService(final S3Session session) {
        this.session = session;
        this.containerService = session.getFeature(PathContainerService.class);
    }

    public void create(final Path bucket, final String region) throws BackgroundException {
        if(log.isDebugEnabled()) {
            log.debug("Create bucket {} in region {}", bucket, region);
        }
        if(!new HostPreferences(session.getHost()).getBoolean("s3.bucket.virtualhost.disable")) {
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
                    // Adjust default region to be used when searching for existing bucket will return 404
                    session.getHost().setProperty("s3.location", region);
                }
            }
            else {
                log.warn("Missing region for bucket location");
            }
            // Create bucket
            session.getClient().createBucket(URIEncoder.encode(containerService.getContainer(bucket).getName()),
                    S3LocationFeature.DEFAULT_REGION.getIdentifier().equals(region) ? "US" : region, acl);
        }
        catch(ServiceException e) {
            throw new S3ExceptionMappingService().map("Cannot create folder {0}", e, bucket);
        }
    }
}
