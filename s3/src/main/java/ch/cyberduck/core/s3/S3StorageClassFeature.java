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

import ch.cyberduck.core.Acl;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathContainerService;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.features.AclPermission;
import ch.cyberduck.core.features.Redundancy;
import ch.cyberduck.core.preferences.Preferences;
import ch.cyberduck.core.preferences.PreferencesFactory;

import org.apache.commons.lang3.StringUtils;
import org.jets3t.service.model.S3Object;

import java.util.Arrays;
import java.util.List;

public class S3StorageClassFeature implements Redundancy {

    private final S3Session session;

    private final S3AccessControlListFeature accessControlListFeature;

    private final Preferences preferences = PreferencesFactory.get();

    private final PathContainerService containerService
            = new S3PathContainerService();

    public S3StorageClassFeature(final S3Session session) {
        this(session, (S3AccessControlListFeature) session.getFeature(AclPermission.class));
    }

    public S3StorageClassFeature(final S3Session session, final S3AccessControlListFeature accessControlListFeature) {
        this.session = session;
        this.accessControlListFeature = accessControlListFeature;
    }

    @Override
    public String getDefault() {
        return PreferencesFactory.get().getProperty("s3.storage.class");
    }

    @Override
    public List<String> getClasses() {
        return Arrays.asList(
                S3Object.STORAGE_CLASS_STANDARD,
                "STANDARD_IA", // This storage class (IA, for infrequent access) is optimized for long-lived and less frequently accessed data
                S3Object.STORAGE_CLASS_REDUCED_REDUNDANCY,
                S3Object.STORAGE_CLASS_GLACIER);
    }

    @Override
    public String getClass(final Path file) throws BackgroundException {
        if(file.isFile() || file.isPlaceholder()) {
            // HEAD request provides storage class information of the object.
            // S3 returns this header for all objects except for Standard storage class objects.
            final String redundancy = new S3AttributesFeature(session).find(file).getStorageClass();
            if(StringUtils.isBlank(redundancy)) {
                return S3Object.STORAGE_CLASS_STANDARD;
            }
            return redundancy;
        }
        if(containerService.isContainer(file)) {
            final String key = String.format("s3.storageclass.%s", containerService.getContainer(file).getName());
            if(StringUtils.isNotBlank(preferences.getProperty(key))) {
                return preferences.getProperty(key);
            }
        }
        return S3Object.STORAGE_CLASS_STANDARD;
    }

    @Override
    public void setClass(final Path file, final String redundancy) throws BackgroundException {
        if(containerService.isContainer(file)) {
            final String key = String.format("s3.storageclass.%s", containerService.getContainer(file).getName());
            preferences.setProperty(key, redundancy);
        }
        if(file.isFile() || file.isPlaceholder()) {
            final S3ThresholdCopyFeature copy = new S3ThresholdCopyFeature(session);
            if(null == accessControlListFeature) {
                copy.copy(file, file, redundancy, new S3EncryptionFeature(session).getEncryption(file),
                        Acl.EMPTY);
            }
            else {
                copy.copy(file, file, redundancy, new S3EncryptionFeature(session).getEncryption(file),
                        accessControlListFeature.getPermission(file));
            }
        }
    }
}
