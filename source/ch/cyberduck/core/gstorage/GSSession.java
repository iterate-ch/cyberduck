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

import ch.cyberduck.core.*;
import ch.cyberduck.core.cloud.Distribution;
import ch.cyberduck.core.i18n.Locale;
import ch.cyberduck.core.s3.S3Session;

import org.jets3t.service.Jets3tProperties;
import org.jets3t.service.acl.Permission;
import org.jets3t.service.model.S3Object;

import java.util.*;

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
        Jets3tProperties configuration = super.getProperties();
        configuration.setProperty("s3service.enable-storage-classes", String.valueOf(false));
    }

//    private static final String GOOGLE_SIGNATURE_IDENTIFIER = "GOOG1";
//    private static final String GOOGLE_REST_HEADER_PREFIX = "x-goog-";
//    private static final String GOOGLE_REST_METADATA_PREFIX = "x-goog-meta-";
//
//    /**
//     * @return the identifier for the signature algorithm.
//     */
//    @Override
//    protected String getSignatureIdentifier() {
//        return GOOGLE_SIGNATURE_IDENTIFIER;
//    }
//
//    /**
//     * @return header prefix for general Google Storage headers: x-goog-.
//     */
//    @Override
//    public String getRestHeaderPrefix() {
//        return GOOGLE_REST_HEADER_PREFIX;
//    }
//
//    /**
//     * @return header prefix for Google Storage metadata headers: x-goog-meta-.
//     */
//    @Override
//    public String getRestMetadataPrefix() {
//        return GOOGLE_REST_METADATA_PREFIX;
//    }

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
    public List<Acl.User> getAvailableAclUsers() {
        final List<Acl.User> users = super.getAvailableAclUsers();
        for(Iterator<Acl.User> iter = users.iterator(); iter.hasNext();) {
            if(iter.next() instanceof Acl.EmailUser) {
                iter.remove();
            }
        }
        users.add(new Acl.EmailUser() {
            @Override
            public String getPlaceholder() {
                return Locale.localizedString("Google Account Email Address", "S3");
            }
        });
//        users.add(new Acl.DomainUser("") {
//            @Override
//            public String getPlaceholder() {
//                return Locale.localizedString("Google Apps Domain", "S3");
//            }
//        });
//        users.add(new Acl.GroupUser("", true) {
//            @Override
//            public String getPlaceholder() {
//                return Locale.localizedString("Google Group Email Address", "S3");
//            }
//        });
        return users;
    }

    @Override
    public List<Acl.Role> getAvailableAclRoles(List<Path> files) {
        List<Acl.Role> roles = new ArrayList<Acl.Role>(Arrays.asList(
                new Acl.Role(org.jets3t.service.acl.Permission.PERMISSION_FULL_CONTROL.toString()),
                new Acl.Role(Permission.PERMISSION_READ.toString()))
        );
        for(Path file : files) {
            if(file.attributes().isVolume()) {
                // When applied to a bucket, this permission lets a user create objects, overwrite objects, and
                // delete objects in a bucket. This permission also lets a user list the contents of a bucket.
                // You cannot apply this permission to objects because bucket ACLs control who can upload,
                // overwrite, and delete objects. Also, you must grant READ permission if you grant WRITE permission.
                roles.add(new Acl.Role(Permission.PERMISSION_WRITE.toString()));
                break;
            }
        }
        return roles;
    }

    @Override
    public boolean isBucketLocationSupported() {
        return false;
    }
}