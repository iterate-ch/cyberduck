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

import ch.cyberduck.core.Credentials;
import ch.cyberduck.core.LocaleFactory;
import ch.cyberduck.core.LoginOptions;
import ch.cyberduck.core.PasswordCallback;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathContainerService;
import ch.cyberduck.core.VersioningConfiguration;
import ch.cyberduck.core.cache.LRUCache;
import ch.cyberduck.core.exception.AccessDeniedException;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.exception.ConnectionCanceledException;
import ch.cyberduck.core.exception.InteroperabilityException;
import ch.cyberduck.core.features.Encryption;
import ch.cyberduck.core.features.Versioning;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.jets3t.service.ServiceException;
import org.jets3t.service.model.S3BucketVersioningStatus;
import org.jets3t.service.model.S3Object;

public class S3VersioningFeature implements Versioning {
    private static final Logger log = Logger.getLogger(S3VersioningFeature.class);

    private final S3Session session;
    private final PathContainerService containerService;
    private final S3AccessControlListFeature accessControlListFeature;

    private final LRUCache<Path, VersioningConfiguration> cache
        = LRUCache.build(10);

    public S3VersioningFeature(final S3Session session, final S3AccessControlListFeature accessControlListFeature) {
        this.session = session;
        this.accessControlListFeature = accessControlListFeature;
        this.containerService = session.getFeature(PathContainerService.class);
    }

    @Override
    public void setConfiguration(final Path file, final PasswordCallback prompt, final VersioningConfiguration configuration) throws BackgroundException {
        final Path bucket = containerService.getContainer(file);
        try {
            final VersioningConfiguration current = this.getConfiguration(bucket);
            if(current.isMultifactor()) {
                // The bucket is already MFA protected.
                final Credentials factor = this.getToken(StringUtils.EMPTY, prompt);
                if(configuration.isEnabled()) {
                    if(current.isEnabled()) {
                        log.debug(String.format("Versioning already enabled for bucket %s", bucket));
                    }
                    else {
                        // Enable versioning if not already active.
                        log.debug(String.format("Enable bucket versioning with MFA %s for %s", factor.getUsername(), bucket));
                        session.getClient().enableBucketVersioningWithMFA(bucket.isRoot() ? StringUtils.EMPTY : bucket.getName(),
                                factor.getUsername(), factor.getPassword());
                    }
                }
                else {
                    log.debug(String.format("Suspend bucket versioning with MFA %s for %s", factor.getUsername(), bucket));
                    session.getClient().suspendBucketVersioningWithMFA(bucket.isRoot() ? StringUtils.EMPTY : bucket.getName(),
                            factor.getUsername(), factor.getPassword());
                }
                if(configuration.isEnabled() && !configuration.isMultifactor()) {
                    log.debug(String.format("Disable MFA %s for %s", factor.getUsername(), bucket));
                    // User has choosen to disable MFA
                    final Credentials factor2 = this.getToken(StringUtils.EMPTY, prompt);
                    session.getClient().disableMFAForVersionedBucket(bucket.isRoot() ? StringUtils.EMPTY : bucket.getName(),
                            factor2.getUsername(), factor2.getPassword());
                }
            }
            else {
                if(configuration.isEnabled()) {
                    if(configuration.isMultifactor()) {
                        final Credentials factor = this.getToken(StringUtils.EMPTY, prompt);
                        log.debug(String.format("Enable bucket versioning with MFA %s for %s", factor.getUsername(), bucket));
                        session.getClient().enableBucketVersioningWithMFA(bucket.isRoot() ? StringUtils.EMPTY : bucket.getName(),
                                factor.getUsername(), factor.getPassword());
                    }
                    else {
                        if(current.isEnabled()) {
                            log.debug(String.format("Versioning already enabled for bucket %s", bucket));
                        }
                        else {
                            log.debug(String.format("Enable bucket versioning for %s", bucket));
                            session.getClient().enableBucketVersioning(bucket.isRoot() ? StringUtils.EMPTY : bucket.getName());
                        }
                    }
                }
                else {
                    log.debug(String.format("Susped bucket versioning for %s", bucket));
                    session.getClient().suspendBucketVersioning(bucket.isRoot() ? StringUtils.EMPTY : bucket.getName());
                }
            }
            cache.remove(bucket);
        }
        catch(ServiceException e) {
            throw new S3ExceptionMappingService().map("Failure to write attributes of {0}", e, bucket);
        }
    }

    @Override
    public VersioningConfiguration getConfiguration(final Path file) throws BackgroundException {
        final Path bucket = containerService.getContainer(file);
        if(cache.contains(bucket)) {
            return cache.get(bucket);
        }
        try {
            final S3BucketVersioningStatus status
                    = session.getClient().getBucketVersioningStatus(bucket.isRoot() ? StringUtils.EMPTY : bucket.getName());
            if(null == status) {
                return VersioningConfiguration.empty();
            }
            final VersioningConfiguration configuration = new VersioningConfiguration(status.isVersioningEnabled(),
                    status.isMultiFactorAuthDeleteRequired());
            cache.put(bucket, configuration);
            return configuration;
        }
        catch(ServiceException e) {
            try {
                throw new S3ExceptionMappingService().map("Cannot read container configuration", e);
            }
            catch(AccessDeniedException l) {
                log.warn(String.format("Missing permission to read versioning configuration for %s %s", bucket, e.getMessage()));
                return VersioningConfiguration.empty();
            }
            catch(InteroperabilityException i) {
                log.warn(String.format("Not supported to read versioning configuration for %s %s", bucket, e.getMessage()));
                return VersioningConfiguration.empty();
            }
        }
    }

    /**
     * Versioning support. Copy a previous version of the object into the same bucket.
     * The copied object becomes the latest version of that object and all object versions are preserved.
     */
    @Override
    public void revert(final Path file) throws BackgroundException {
        if(file.isFile()) {
            try {
                final S3Object destination = new S3Object(containerService.getKey(file));
                // Keep same storage class
                destination.setStorageClass(file.attributes().getStorageClass());
                final Encryption.Algorithm encryption = file.attributes().getEncryption();
                destination.setServerSideEncryptionAlgorithm(encryption.algorithm);
                // Set custom key id stored in KMS
                destination.setServerSideEncryptionKmsKeyId(encryption.key);
                try {
                    // Apply non standard ACL
                    destination.setAcl(accessControlListFeature.toAcl(file, accessControlListFeature.getPermission(file)));
                }
                catch(AccessDeniedException | InteroperabilityException e) {
                    log.warn(String.format("Ignore failure %s", e));
                }
                session.getClient().copyVersionedObject(file.attributes().getVersionId(),
                    containerService.getContainer(file).getName(), containerService.getKey(file), containerService.getContainer(file).getName(), destination, false);
                if(file.getParent().attributes().getCustom().containsKey(S3VersionedObjectListService.KEY_DELETE_MARKER)) {
                    // revert placeholder
                    session.getClient().deleteVersionedObject(
                        file.getParent().attributes().getVersionId(),
                        containerService.getContainer(file).getName(), containerService.getKey(file.getParent()));
                }
            }
            catch(ServiceException e) {
                throw new S3ExceptionMappingService().map("Cannot revert file", e, file);
            }
        }
    }

    @Override
    public boolean isRevertable(final Path file) {
        return file.attributes().isDuplicate();
    }

    /**
     * Prompt for MFA credentials
     *
     * @param mfaSerial Serial number for a hardware device (such as GAHT12345678) or an Amazon Resource Name (ARN) for
     *                  a virtual device (such as arn:aws:iam::123456789012:mfa/user)
     * @param callback  Prompt controller
     * @return MFA one time authentication password.
     * @throws ch.cyberduck.core.exception.ConnectionCanceledException Prompt dismissed
     */
    @Override
    public Credentials getToken(final String mfaSerial, final PasswordCallback callback) throws ConnectionCanceledException {
        // Prompt for multi factor authentication credentials.
        return callback.prompt(
            session.getHost(), LocaleFactory.localizedString("Provide additional login credentials", "Credentials"),
            String.format("%s %s", LocaleFactory.localizedString("Multi-Factor Authentication", "S3"), mfaSerial),
            new LoginOptions()
                .icon(session.getHost().getProtocol().disk())
                .password(true)
                .user(false)
                .passwordPlaceholder(LocaleFactory.localizedString("MFA Authentication Code", "S3"))
                .keychain(false)
        );
    }
}
