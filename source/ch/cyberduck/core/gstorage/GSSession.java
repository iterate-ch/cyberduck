package ch.cyberduck.core.gstorage;

/*
 * Copyright (c) 2002-2010 David Kocher. All rights reserved.
 *
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

import ch.cyberduck.core.Acl;
import ch.cyberduck.core.Host;
import ch.cyberduck.core.Session;
import ch.cyberduck.core.SessionFactory;
import ch.cyberduck.core.cloud.Distribution;
import ch.cyberduck.core.i18n.Locale;
import ch.cyberduck.core.s3.S3Session;

import org.jets3t.service.model.S3Object;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Google Storage for Developers is a new service for developers to store and
 * access data in Google's cloud. It offers developers direct access to Google's
 * scalable storage and networking infrastructure as well as powerful authentication
 * and data sharing mechanisms.
 *
 * @version $Id$
 */
public class GSSession extends S3Session {

    private static class Factory extends SessionFactory {
        @Override
        protected Session create(Host h) {
            return new GSSession(h);
        }
    }

    public static SessionFactory factory() {
        return new Factory();
    }

    protected GSSession(Host h) {
        super(h);
    }

    @Override
    protected void configure() {
        super.configure();
        configuration.setProperty("s3service.enable-storage-classes", String.valueOf(false));
    }

    @Override
    public List<String> getSupportedStorageClasses() {
        return Arrays.asList(S3Object.STORAGE_CLASS_STANDARD);
    }

    @Override
    public List<Distribution.Method> getSupportedDistributionMethods() {
        return Collections.emptyList();
    }

    @Override
    public String getLocation(String container) {
        return Locale.localizedString("US", "S3");
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
    public boolean isRequesterPaysSupported() {
        return false;
    }

    @Override
    public List<Acl.Role> getAvailableAclRoles() {
        return Arrays.asList(new Acl.Role(org.jets3t.service.acl.Permission.PERMISSION_FULL_CONTROL.toString()),
                new Acl.Role(org.jets3t.service.acl.Permission.PERMISSION_READ.toString()),
                new Acl.Role(org.jets3t.service.acl.Permission.PERMISSION_WRITE.toString()));
    }
}