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

import ch.cyberduck.core.DisabledConnectionCallback;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathContainerService;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.exception.NotfoundException;
import ch.cyberduck.core.features.Redundancy;
import ch.cyberduck.core.io.DisabledStreamListener;
import ch.cyberduck.core.preferences.HostPreferences;
import ch.cyberduck.core.transfer.TransferStatus;

import org.apache.commons.lang3.StringUtils;
import org.jets3t.service.model.S3Object;

import java.util.LinkedHashSet;
import java.util.Set;

public class S3StorageClassFeature implements Redundancy {

    private final S3Session session;
    private final PathContainerService containerService;
    private final S3AccessControlListFeature acl;

    public S3StorageClassFeature(final S3Session session, final S3AccessControlListFeature acl) {
        this.session = session;
        this.containerService = session.getFeature(PathContainerService.class);
        this.acl = acl;
    }

    @Override
    public String getDefault() {
        return new HostPreferences(session.getHost()).getProperty("s3.storage.class");
    }

    @Override
    public Set<String> getClasses() {
        return new LinkedHashSet<>(new HostPreferences(session.getHost()).getList("s3.storage.class.options"));
    }

    @Override
    public String getClass(final Path file) throws BackgroundException {
        if(containerService.isContainer(file)) {
            final String key = String.format("s3.storageclass.%s", containerService.getContainer(file).getName());
            if(StringUtils.isNotBlank(new HostPreferences(session.getHost()).getProperty(key))) {
                return new HostPreferences(session.getHost()).getProperty(key);
            }
            return null;
        }
        // HEAD request provides storage class information of the object.
        // S3 returns this header for all objects except for Standard storage class objects.
        final String redundancy = new S3AttributesFinderFeature(session, acl).find(file).getStorageClass();
        if(StringUtils.isBlank(redundancy)) {
            return S3Object.STORAGE_CLASS_STANDARD;
        }
        return redundancy;
    }

    @Override
    public void setClass(final Path file, final String redundancy) throws BackgroundException {
        try {
            final S3ThresholdCopyFeature copy = new S3ThresholdCopyFeature(session);
            final TransferStatus status = new TransferStatus();
            status.setLength(file.attributes().getSize());
            status.setStorageClass(redundancy);
            copy.copy(file, file, status, new DisabledConnectionCallback(), new DisabledStreamListener());
        }
        catch(NotfoundException e) {
            if(file.isDirectory()) {
                // No placeholder file may exist but we just have a common prefix
                return;
            }
            throw e;
        }
    }
}
