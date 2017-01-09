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
import ch.cyberduck.core.LoginCallback;
import ch.cyberduck.core.LoginOptions;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathContainerService;
import ch.cyberduck.core.VersioningConfiguration;
import ch.cyberduck.core.exception.AccessDeniedException;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.exception.ConnectionCanceledException;
import ch.cyberduck.core.exception.InteroperabilityException;
import ch.cyberduck.core.features.Encryption;
import ch.cyberduck.core.features.Versioning;
import ch.cyberduck.core.preferences.PreferencesFactory;

import org.apache.commons.collections4.map.LRUMap;
import org.apache.log4j.Logger;
import org.jets3t.service.ServiceException;
import org.jets3t.service.model.S3BucketVersioningStatus;
import org.jets3t.service.model.S3Object;

import java.util.Collections;
import java.util.Map;

public class S3VersioningFeature implements Versioning {
    private static final Logger log = Logger.getLogger(S3VersioningFeature.class);

    private final S3Session session;

    private final PathContainerService containerService
            = new S3PathContainerService();

    private final S3AccessControlListFeature accessControlListFeature;

    @SuppressWarnings("unchecked")
    private Map<Path, VersioningConfiguration> cache
            = Collections.synchronizedMap(new LRUMap<Path, VersioningConfiguration>(10));

    public S3VersioningFeature(final S3Session session, final S3AccessControlListFeature accessControlListFeature) {
        this.session = session;
        this.accessControlListFeature = accessControlListFeature;
    }

    @Override
    public S3VersioningFeature withCache(final Map<Path, VersioningConfiguration> cache) {
        this.cache = cache;
        return this;
    }

    @Override
    public void setConfiguration(final Path file, final LoginCallback prompt, final VersioningConfiguration configuration) throws BackgroundException {
        final Path container = containerService.getContainer(file);
        try {
            final VersioningConfiguration current = this.getConfiguration(container);
            if(current.isMultifactor()) {
                // The bucket is already MFA protected.
                final Credentials factor = this.getToken(prompt);
                if(configuration.isEnabled()) {
                    if(current.isEnabled()) {
                        log.debug(String.format("Versioning already enabled for bucket %s", container));
                    }
                    else {
                        // Enable versioning if not already active.
                        log.debug(String.format("Enable bucket versioning with MFA %s for %s", factor.getUsername(), container));
                        session.getClient().enableBucketVersioningWithMFA(container.getName(),
                                factor.getUsername(), factor.getPassword());
                    }
                }
                else {
                    log.debug(String.format("Suspend bucket versioning with MFA %s for %s", factor.getUsername(), container));
                    session.getClient().suspendBucketVersioningWithMFA(container.getName(),
                            factor.getUsername(), factor.getPassword());
                }
                if(configuration.isEnabled() && !configuration.isMultifactor()) {
                    log.debug(String.format("Disable MFA %s for %s", factor.getUsername(), container));
                    // User has choosen to disable MFA
                    final Credentials factor2 = this.getToken(prompt);
                    session.getClient().disableMFAForVersionedBucket(container.getName(),
                            factor2.getUsername(), factor2.getPassword());
                }
            }
            else {
                if(configuration.isEnabled()) {
                    if(configuration.isMultifactor()) {
                        final Credentials factor = this.getToken(prompt);
                        log.debug(String.format("Enable bucket versioning with MFA %s for %s", factor.getUsername(), container));
                        session.getClient().enableBucketVersioningWithMFA(container.getName(),
                                factor.getUsername(), factor.getPassword());
                    }
                    else {
                        if(current.isEnabled()) {
                            log.debug(String.format("Versioning already enabled for bucket %s", container));
                        }
                        else {
                            log.debug(String.format("Enable bucket versioning for %s", container));
                            session.getClient().enableBucketVersioning(container.getName());
                        }
                    }
                }
                else {
                    log.debug(String.format("Susped bucket versioning for %s", container));
                    session.getClient().suspendBucketVersioning(container.getName());
                }
            }
            cache.remove(container);
        }
        catch(ServiceException e) {
            throw new S3ExceptionMappingService().map("Failure to write attributes of {0}", e);
        }
    }

    @Override
    public VersioningConfiguration getConfiguration(final Path file) throws BackgroundException {
        final Path container = containerService.getContainer(file);
        if(container.isRoot()) {
            return VersioningConfiguration.empty();
        }
        if(cache.containsKey(container)) {
            return cache.get(container);
        }
        try {
            final S3BucketVersioningStatus status
                    = session.getClient().getBucketVersioningStatus(container.getName());
            final VersioningConfiguration configuration = new VersioningConfiguration(status.isVersioningEnabled(),
                    status.isMultiFactorAuthDeleteRequired());
            cache.put(container, configuration);
            return configuration;
        }
        catch(ServiceException e) {
            try {
                throw new S3ExceptionMappingService().map("Cannot read bucket versioning status", e);
            }
            catch(AccessDeniedException l) {
                log.warn(String.format("Missing permission to read versioning configuration for %s %s", container, e.getMessage()));
                return VersioningConfiguration.empty();
            }
            catch(InteroperabilityException i) {
                log.warn(String.format("Not supported to read versioning configuration for %s %s", container, e.getMessage()));
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
                // Apply non standard ACL
                if(null == accessControlListFeature) {
                    destination.setAcl(null);
                }
                else {
                    destination.setAcl(accessControlListFeature.convert(accessControlListFeature.getPermission(file)));
                }
                session.getClient().copyVersionedObject(file.attributes().getVersionId(),
                        containerService.getContainer(file).getName(), containerService.getKey(file), containerService.getContainer(file).getName(), destination, false);
            }
            catch(ServiceException e) {
                throw new S3ExceptionMappingService().map("Cannot revert file", e, file);
            }
        }
    }

    /**
     * Prompt for MFA credentials
     *
     * @param controller Prompt controller
     * @return MFA one time authentication password.
     * @throws ch.cyberduck.core.exception.ConnectionCanceledException Prompt dismissed
     */
    @Override
    public Credentials getToken(final LoginCallback controller) throws ConnectionCanceledException {
        final Credentials credentials = new MultifactorCredentials();
        // Prompt for multi factor authentication credentials.
        controller.prompt(session.getHost(), credentials,
                LocaleFactory.localizedString("Provide additional login credentials", "Credentials"),
                LocaleFactory.localizedString("Multi-Factor Authentication", "S3"), new LoginOptions());

        PreferencesFactory.get().setProperty("s3.mfa.serialnumber", credentials.getUsername());
        return credentials;
    }
}
