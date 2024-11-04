package ch.cyberduck.core.s3;

/*
 * Copyright (c) 2013 David Kocher. All rights reserved.
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

import ch.cyberduck.core.AttributedList;
import ch.cyberduck.core.ListProgressListener;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathAttributes;
import ch.cyberduck.core.PathNormalizer;
import ch.cyberduck.core.RootListService;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.features.Location;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jets3t.service.ServiceException;
import org.jets3t.service.model.StorageBucket;

import java.util.EnumSet;

public class S3BucketListService implements RootListService {
    private static final Logger log = LogManager.getLogger(S3BucketListService.class);

    private final S3Session session;
    private final S3LocationFeature.S3Region region;

    public S3BucketListService(final S3Session session) {
        this(session, new S3LocationFeature.S3Region(session.getHost().getRegion()));
    }

    public S3BucketListService(final S3Session session, final S3LocationFeature.S3Region region) {
        this.session = session;
        this.region = region;
    }

    @Override
    public AttributedList<Path> list(final Path directory, final ListProgressListener listener) throws BackgroundException {
        log.debug("List containers for {}", session);
        try {
            final AttributedList<Path> buckets = new AttributedList<>();
            // List all buckets owned
            for(StorageBucket b : session.getClient().listAllBuckets()) {
                final PathAttributes attr = new PathAttributes();
                final Path bucket = new Path(PathNormalizer.normalize(b.getName()), EnumSet.of(Path.Type.volume, Path.Type.directory), attr);
                if(b.getOwner() != null) {
                    // Null if the owner is not available
                    attr.setOwner(b.getOwner().getId());
                }
                attr.setCreationDate(b.getCreationDate().getTime());
                if(b.isLocationKnown()) {
                    attr.setRegion(b.getLocation());
                }
                else {
                    if(region.getIdentifier() != null) {
                        final String location;
                        if(!b.isLocationKnown()) {
                            location = session.getFeature(Location.class).getLocation(bucket).getIdentifier();
                        }
                        else {
                            location = b.getLocation();
                        }
                        if(!StringUtils.equals(location, region.getIdentifier())) {
                            log.warn("Skip bucket {} in region {}", bucket, location);
                            continue;
                        }
                        attr.setRegion(location);
                    }
                }
                buckets.add(bucket);
                listener.chunk(directory, buckets);
            }
            return buckets;
        }
        catch(ServiceException e) {
            throw new S3ExceptionMappingService().map("Listing directory {0} failed", e, directory);
        }
    }
}
