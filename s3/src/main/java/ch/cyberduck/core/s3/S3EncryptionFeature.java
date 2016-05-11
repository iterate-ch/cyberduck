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
import ch.cyberduck.core.LoginCallback;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathContainerService;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.features.AclPermission;
import ch.cyberduck.core.features.Encryption;
import ch.cyberduck.core.kms.KMSEncryptionFeature;
import ch.cyberduck.core.preferences.Preferences;
import ch.cyberduck.core.preferences.PreferencesFactory;

import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class S3EncryptionFeature implements Encryption {

    private final Preferences preferences = PreferencesFactory.get();

    private final PathContainerService containerService
            = new S3PathContainerService();

    private final S3Session session;

    public S3EncryptionFeature(final S3Session session) {
        this.session = session;
    }

    @Override
    public Set<Algorithm> getKeys(final LoginCallback prompt) throws BackgroundException {
        return Collections.emptySet();
    }

    @Override
    public Set<Algorithm> getAlgorithms() {
        return new HashSet<>(Arrays.asList(SSE_AES256, KMSEncryptionFeature.SSE_KMS_DEFAULT));
    }

    @Override
    public Algorithm getDefault(final Path file) {
        // Return setting in preferences
        final String setting = preferences.getProperty("s3.encryption.algorithm");
        if(StringUtils.equals(SSE_AES256.algorithm, setting)) {
            return SSE_AES256;
        }
        if(StringUtils.equals(KMSEncryptionFeature.SSE_KMS_DEFAULT.algorithm, setting)) {
            final String key = String.format("s3.encryption.key.%s", containerService.getContainer(file).getName());
            if(StringUtils.isNotBlank(preferences.getProperty(key))) {
                return new Algorithm(KMSEncryptionFeature.SSE_KMS_DEFAULT.algorithm, preferences.getProperty(key));
            }
            return KMSEncryptionFeature.SSE_KMS_DEFAULT;
        }
        return Algorithm.NONE;
    }

    /**
     * @return Encryption algorithm used for file or null if not encrypted
     */
    @Override
    public Algorithm getEncryption(final Path file) throws BackgroundException {
        if(file.isFile()) {
            return new S3AttributesFeature(session).find(file).getEncryption();
        }
        return null;
    }

    /**
     * @param file      File to copy with new setting
     * @param algorithm A supported algorithm for server side encryption
     */
    @Override
    public void setEncryption(final Path file, final Algorithm algorithm) throws BackgroundException {
        if(file.isFile()) {
            final S3ThresholdCopyFeature copy = new S3ThresholdCopyFeature(session);
            // Copy item in place to write new attributes
            final AclPermission feature = session.getFeature(AclPermission.class);
            if(null == feature) {
                copy.copy(file, file, new S3StorageClassFeature(session).getClass(file), algorithm,
                        Acl.EMPTY);
            }
            else {
                copy.copy(file, file, new S3StorageClassFeature(session).getClass(file), algorithm,
                        feature.getPermission(file));
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
