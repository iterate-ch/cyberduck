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

import ch.cyberduck.core.Path;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.exception.ServiceExceptionMappingService;
import ch.cyberduck.core.features.Location;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.jets3t.service.ServiceException;

/**
 * @version $Id$
 */
public class S3LocationFeature implements Location {
    private static final Logger log = Logger.getLogger(S3LocationFeature.class);

    private S3Session session;

    public S3LocationFeature(final S3Session session) {
        this.session = session;
    }

    @Override
    public String getLocation(final Path container) throws BackgroundException {
        if(session.getHost().getCredentials().isAnonymousLogin()) {
            log.info("Anonymous cannot access bucket location");
            return null;
        }
        try {
            final String location = session.getClient().getBucketLocation(container.getContainer().getName());
            if(StringUtils.isBlank(location)) {
                return "US"; //Default location US is null
            }
            return location;
        }
        catch(ServiceException e) {
            throw new ServiceExceptionMappingService().map("Cannot read container configuration", e);
        }
    }
}
