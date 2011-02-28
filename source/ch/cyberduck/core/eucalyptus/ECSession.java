package ch.cyberduck.core.eucalyptus;

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

import ch.cyberduck.core.Host;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.Session;
import ch.cyberduck.core.SessionFactory;
import ch.cyberduck.core.cdn.Distribution;
import ch.cyberduck.core.s3.S3Session;

import org.jets3t.service.Jets3tProperties;

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

    private static class Factory extends SessionFactory {
        @Override
        protected Session create(Host h) {
            return new ECSession(h);
        }
    }

    public static SessionFactory factory() {
        return new Factory();
    }

    public ECSession(Host h) {
        super(h);
    }

    @Override
    protected void configure(String hostname) {
        super.configure(hostname);
        Jets3tProperties configuration = this.getProperties();
        configuration.setProperty("s3service.s3-endpoint-virtual-path", Path.normalize("/services/Walrus"));
        configuration.setProperty("s3service.disable-dns-buckets", String.valueOf(true));
        configuration.setProperty("s3service.enable-storage-classes", String.valueOf(false));
    }

    @Override
    public List<String> getSupportedStorageClasses() {
        return Collections.emptyList();
    }

    @Override
    public boolean isCDNSupported() {
        return false;
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
    public boolean isBucketLocationSupported() {
        return false;
    }

    @Override
    public boolean isRequesterPaysSupported() {
        return false;
    }
}