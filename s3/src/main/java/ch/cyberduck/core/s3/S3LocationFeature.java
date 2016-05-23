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
import ch.cyberduck.core.preferences.PreferencesFactory;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.jets3t.service.ServiceException;
import org.jets3t.service.impl.rest.httpclient.RegionEndpointCache;

import java.util.Collections;
import java.util.Set;

public class S3LocationFeature implements Location {
    private static final Logger log = Logger.getLogger(S3LocationFeature.class);

    private S3Session session;

    private PathContainerService containerService
            = new S3PathContainerService();

    private RegionEndpointCache cache
            = new RegionEndpointCache();

    public S3LocationFeature(final S3Session session) {
        this.session = session;
    }

    public S3LocationFeature(final S3Session session, final RegionEndpointCache cache) {
        this.session = session;
        this.cache = cache;
    }

    @Override
    public Set<Name> getLocations() {
        // Only for AWS
        if(session.getHost().getHostname().endsWith(PreferencesFactory.get().getProperty("s3.hostname.default"))) {
            return session.getHost().getProtocol().getRegions();
        }
        return Collections.emptySet();
    }

    @Override
    public Name getLocation(final Path file) throws BackgroundException {
        final Path container = containerService.getContainer(file);
        if(cache.containsRegionForBucketName(container.getName())) {
            return new S3Region(cache.getRegionForBucketName(container.getName()));
        }
        try {
            final String location = session.getClient().getBucketLocation(container.getName());
            if(StringUtils.isBlank(location)) {
                log.warn(String.format("No region known for bucket %s", container.getName()));
                return new S3Region(null);
            }
            if("US".equals(location)) {
                return new S3Region("us-east-1");
            }
            if("EU".equals(location)) {
                return new S3Region("eu-west-1");
            }
            return new S3Region(location);
        }
        catch(ServiceException e) {
            try {
                throw new S3ExceptionMappingService().map("Cannot read bucket location", e);
            }
            catch(AccessDeniedException l) {
                log.warn(String.format("Missing permission to read location for %s %s", container, e.getMessage()));
                return unknown;
            }
            catch(InteroperabilityException i) {
                log.warn(String.format("Not supported to read location for %s %s", container, e.getMessage()));
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
