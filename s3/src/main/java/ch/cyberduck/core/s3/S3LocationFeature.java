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
import ch.cyberduck.core.exception.AccessDeniedException;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.exception.InteroperabilityException;
import ch.cyberduck.core.features.Location;
import ch.cyberduck.core.preferences.HostPreferences;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jets3t.service.ServiceException;
import org.jets3t.service.impl.rest.httpclient.RegionEndpointCache;

import java.util.Collections;
import java.util.Set;

public class S3LocationFeature implements Location {
    private static final Logger log = LogManager.getLogger(S3LocationFeature.class);

    private final S3Session session;
    private final PathContainerService containerService;
    private final RegionEndpointCache cache;

    public static final S3Region DEFAULT_REGION = new S3Region("us-east-1");

    public S3LocationFeature(final S3Session session) {
        this(session, new RegionEndpointCache());
    }

    public S3LocationFeature(final S3Session session, final RegionEndpointCache cache) {
        this.session = session;
        this.cache = cache;
        this.containerService = session.getFeature(PathContainerService.class);
    }

    @Override
    public Name getDefault() {
        return new S3Region(new HostPreferences(session.getHost()).getProperty("s3.location"));
    }

    @Override
    public Set<Name> getLocations() {
        if(StringUtils.isNotEmpty(RequestEntityRestStorageService.findBucketInHostname(session.getHost()))) {
            log.debug("Return empty set for hostname {}", session.getHost());
            // Connected to single bucket
            return Collections.emptySet();
        }
        return session.getHost().getProtocol().getRegions();
    }

    @Override
    public Name getLocation(final Path file) throws BackgroundException {
        if(StringUtils.isNotBlank(session.getHost().getRegion())) {
            return new S3Region(session.getHost().getRegion());
        }
        final Path bucket = containerService.getContainer(file);
        return this.getLocation(bucket.isRoot() ? StringUtils.EMPTY : bucket.getName());
    }

    protected Name getLocation(final String bucketname) throws BackgroundException {
        try {
            if(cache.containsRegionForBucketName(bucketname)) {
                return new S3Region(cache.getRegionForBucketName(bucketname));
            }
            if(session.getHost().getCredentials().isAnonymousLogin()) {
                // To use this implementation of the operation, you must be the bucket owner
                log.warn("Skip attempt to read bucket location with missing credentials");
                return Location.unknown;
            }
            log.debug("Query location for bucket {}", bucketname);
            final String location = session.getClient().getBucketLocation(bucketname);
            final S3Region region;
            if(StringUtils.isBlank(location)) {
                log.warn("No region known for bucket {}", bucketname);
                if(StringUtils.isNotBlank(session.getHost().getRegion())) {
                    region = new S3Region(session.getHost().getRegion());
                }
                else {
                    region = DEFAULT_REGION;
                }
            }
            else {
                switch(location) {
                    case "US":
                        region = DEFAULT_REGION;
                        break;
                    case "EU":
                        region = new S3Region("eu-west-1");
                        break;
                    default:
                        region = new S3Region(location);
                        break;
                }
            }
            if(StringUtils.isBlank(bucketname)) {
                if(StringUtils.isNotBlank(RequestEntityRestStorageService.findBucketInHostname(session.getHost()))) {
                    cache.putRegionForBucketName(RequestEntityRestStorageService.findBucketInHostname(session.getHost()), region.getIdentifier());
                }
                else {
                    cache.putRegionForBucketName(bucketname, region.getIdentifier());
                }
            }
            else {
                cache.putRegionForBucketName(bucketname, region.getIdentifier());
            }
            return region;
        }
        catch(ServiceException e) {
            try {
                throw new S3ExceptionMappingService().map("Cannot read bucket location", e);
            }
            catch(AccessDeniedException l) {
                log.warn("Missing permission to read location for {} {}", bucketname, e.getMessage());
                return unknown;
            }
            catch(InteroperabilityException i) {
                log.warn("Not supported to read location for {} {}", bucketname, e.getMessage());
                return unknown;
            }
        }
    }

    public static final class S3Region extends Name {

        public S3Region(final String identifier) {
            super(identifier);
        }

        @Override
        public String toString() {
            final String identifier = getIdentifier();
            if(null == identifier) {
                return LocaleFactory.localizedString("Unknown");
            }
            return LocaleFactory.localizedString(identifier, "S3");
        }
    }
}
