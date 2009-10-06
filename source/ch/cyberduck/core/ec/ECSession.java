package ch.cyberduck.core.ec;

/*
 *  Copyright (c) 2008 David Kocher. All rights reserved.
 *  http://cyberduck.ch/
 *
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 2 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  Bug fixes, suggestions and comments should be sent to:
 *  dkocher@cyberduck.ch
 */

import ch.cyberduck.core.*;
import ch.cyberduck.core.s3.S3Session;

import org.jets3t.service.CloudFrontServiceException;
import org.jets3t.service.model.cloudfront.Distribution;

/**
 * Elastic Utility Computing Architecture for Linking Your Programs To Useful Systems - is an open-source software
 * infrastructure for implementing "cloud computing" on clusters. The current interface
 * to EUCALYPTUS is compatible with Amazon's EC2 interface, but the infrastructure
 * is designed to support multiple client-side interfaces. EUCALYPTUS is implemented
 * using commonly available Linux tools and basic Web-service technologies making it easy to install and maintain.
 *
 * @version $Id$
 * @see "http://eucalyptus.cs.ucsb.edu/"
 */
public class ECSession extends S3Session {

    static {
        SessionFactory.addFactory(Protocol.EUCALYPTUS, new Factory());
    }

    private static class Factory extends SessionFactory {
        @Override
        protected Session create(Host h) {
            return new ECSession(h);
        }
    }

    protected ECSession(Host h) {
        super(h);
    }

    @Override
    protected void configure() {
        super.configure();
        configuration.setProperty("s3service.s3-endpoint", host.getHostname());
        configuration.setProperty("s3service.disable-dns-buckets", String.valueOf(true));
        configuration.setProperty("s3service.s3-endpoint-virtual-path", Path.normalize("/services/Walrus"));
    }

    /**
     * Amazon CloudFront Extension used to list all configured distributions
     *
     * @return All distributions for the given AWS Credentials
     */
    @Override
    public Distribution[] listDistributions(String bucket) throws CloudFrontServiceException {
        return new Distribution[]{};
    }


    /**
     * @return
     */
    @Override
    public ch.cyberduck.core.cloud.Distribution readDistribution(String container) {
        throw new UnsupportedOperationException();
    }

    /**
     * Amazon CloudFront Extension
     *
     * @param enabled
     * @param cnames
     * @param logging
     */
    @Override
    public void writeDistribution(String container, final boolean enabled, final String[] cnames, boolean logging) {
        throw new UnsupportedOperationException();
    }
}