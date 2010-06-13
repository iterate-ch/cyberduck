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
import ch.cyberduck.core.cloud.Distribution;
import ch.cyberduck.core.s3.S3Session;

import java.util.Collections;
import java.util.List;

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
        configuration.setProperty("s3service.s3-endpoint-virtual-path", Path.normalize("/services/Walrus"));
        configuration.setProperty("s3service.disable-dns-buckets", String.valueOf(true));
        configuration.setProperty("s3service.enable-storage-classes", String.valueOf(false));
    }

    @Override
    public List<String> getSupportedStorageClasses() {
        return Collections.emptyList();
    }

    @Override
    public List<Distribution.Method> getSupportedDistributionMethods() {
        return Collections.emptyList();
    }

    @Override
    public boolean isLoggingSupported() {
        return false;
    }

    @Override
    public boolean isVersioningSupported() {
        return false;
    }

    @Override
    public String getLocation(String container) {
        return null;
    }

    @Override
    public boolean isVersioning(String container) {
        return false;
    }

    @Override
    public boolean isMultiFactorAuthentication(String container) {
        return false;
    }

    @Override
    public boolean isRequesterPays(String container) {
        return false;
    }

    /**
     * @return
     */
    @Override
    public Distribution readDistribution(String container, Distribution.Method method) {
        return new Distribution();
    }

    /**
     * Amazon CloudFront Extension
     *
     * @param enabled
     * @param method
     * @param cnames
     * @param logging
     */
    @Override
    public void writeDistribution(final boolean enabled, String container, Distribution.Method method,
                                  final String[] cnames, boolean logging) {
        ;
    }
}