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
import ch.cyberduck.core.LoginCallback;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathContainerService;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.exception.NotfoundException;
import ch.cyberduck.core.features.Encryption;
import ch.cyberduck.core.io.DisabledStreamListener;
import ch.cyberduck.core.preferences.HostPreferences;
import ch.cyberduck.core.preferences.PreferencesFactory;
import ch.cyberduck.core.transfer.TransferStatus;

import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class S3EncryptionFeature implements Encryption {

    private final PathContainerService containerService;
    private final S3Session session;
    private final S3AccessControlListFeature acl;

    public S3EncryptionFeature(final S3Session session, final S3AccessControlListFeature acl) {
        this.session = session;
        this.containerService = session.getFeature(PathContainerService.class);
        this.acl = acl;
    }

    @Override
    public Set<Algorithm> getKeys(final Path file, final LoginCallback prompt) throws BackgroundException {
        return new HashSet<>(Arrays.asList(Algorithm.NONE, SSE_AES256));
    }

    /**
     * @param file Default encryption setting for file
     * @return Return custom key for AWS-KMS if set for bucket in preferences. Otherwise default SSE algorithm.
     */
    @Override
    public Algorithm getDefault(final Path file) {
        final String key = String.format("s3.encryption.key.%s", containerService.getContainer(file).getName());
        if(StringUtils.isNotBlank(new HostPreferences(session.getHost()).getProperty(key))) {
            return Algorithm.fromString(new HostPreferences(session.getHost()).getProperty(key));
        }
        // Return default setting in preferences
        final String setting = new HostPreferences(session.getHost()).getProperty("s3.encryption.algorithm");
        if(StringUtils.equals(SSE_AES256.algorithm, setting)) {
            return SSE_AES256;
        }
        return Algorithm.NONE;
    }

    /**
     * @param file File or bucket
     * @return Encryption algorithm used for file or null if not encrypted. For buckets, return setting in preferences
     * if any.
     */
    @Override
    public Algorithm getEncryption(final Path file) throws BackgroundException {
        return new S3AttributesFinderFeature(session, acl).find(file).getEncryption();
    }

    /**
     * @param file    File to copy with new setting or bucket to change default preference
     * @param setting A supported algorithm for server side encryption
     */
    @Override
    public void setEncryption(final Path file, final Algorithm setting) throws BackgroundException {
        if(containerService.isContainer(file)) {
            final String key = String.format("s3.encryption.key.%s", containerService.getContainer(file).getName());
            PreferencesFactory.get().setProperty(key, setting.toString());
        }
        else {
            try {
                final S3ThresholdCopyFeature copy = new S3ThresholdCopyFeature(session);
                // Copy item in place to write new attributes
                final TransferStatus status = new TransferStatus();
                status.setEncryption(setting);
                status.setLength(file.attributes().getSize());
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

    /**
     * Default AES256 SSE
     */
    public static final Algorithm SSE_AES256 = new Algorithm("AES256", null) {
        @Override
        public String getDescription() {
            return "SSE-S3 (AES-256)";
        }
    };
}
