package ch.cyberduck.core.cloudfront;

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
 * dkocher@cyberduck.ch
 */

import ch.cyberduck.core.Host;
import ch.cyberduck.core.LoginController;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.cdn.Distribution;
import ch.cyberduck.core.s3.S3Session;

import org.apache.log4j.Logger;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.List;

/**
 * @version $Id$
 */
public class CustomOriginCloudFrontDistributionConfiguration extends CloudFrontDistributionConfiguration {
    private static Logger log = Logger.getLogger(CustomOriginCloudFrontDistributionConfiguration.class);

    private Host origin;
    private S3Session session;

    public CustomOriginCloudFrontDistributionConfiguration(final Host origin,
                                                           final S3Session session,
                                                           final LoginController prompt) {
        super(session, prompt);
        this.origin = origin;
        this.session = session;
    }

    @Override
    public List<Distribution.Method> getMethods(final Path container) {
        return Arrays.asList(Distribution.CUSTOM);
    }

    @Override
    protected String getOrigin(final Path container, final Distribution.Method method) {
        try {
            return new URI(origin.getWebURL()).getHost();
        }
        catch(URISyntaxException e) {
            log.error(String.format("Failure parsing URI %s", origin.getWebURL()), e);
        }
        return origin.getHostname(true);
    }
}