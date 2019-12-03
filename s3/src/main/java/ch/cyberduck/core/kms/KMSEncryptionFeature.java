package ch.cyberduck.core.kms;

/*
 * Copyright (c) 2002-2016 iterate GmbH. All rights reserved.
 * https://cyberduck.io/
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
 */

import ch.cyberduck.core.Host;
import ch.cyberduck.core.KeychainLoginService;
import ch.cyberduck.core.LocaleFactory;
import ch.cyberduck.core.LoginCallback;
import ch.cyberduck.core.LoginOptions;
import ch.cyberduck.core.LoginService;
import ch.cyberduck.core.PasswordStoreFactory;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathContainerService;
import ch.cyberduck.core.auth.AWSCredentialsConfigurator;
import ch.cyberduck.core.aws.CustomClientConfiguration;
import ch.cyberduck.core.exception.AccessDeniedException;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.exception.LoginFailureException;
import ch.cyberduck.core.features.Location;
import ch.cyberduck.core.iam.AmazonServiceExceptionMappingService;
import ch.cyberduck.core.preferences.Preferences;
import ch.cyberduck.core.preferences.PreferencesFactory;
import ch.cyberduck.core.s3.S3EncryptionFeature;
import ch.cyberduck.core.s3.S3PathContainerService;
import ch.cyberduck.core.s3.S3Session;
import ch.cyberduck.core.ssl.ThreadLocalHostnameDelegatingTrustManager;
import ch.cyberduck.core.ssl.X509KeyManager;
import ch.cyberduck.core.ssl.X509TrustManager;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;

import com.amazonaws.AmazonClientException;
import com.amazonaws.ClientConfiguration;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.kms.AWSKMS;
import com.amazonaws.services.kms.AWSKMSClientBuilder;
import com.amazonaws.services.kms.model.AliasListEntry;
import com.amazonaws.services.kms.model.KeyListEntry;

public class KMSEncryptionFeature extends S3EncryptionFeature {
    private static final Logger log = Logger.getLogger(KMSEncryptionFeature.class);

    private final Host bookmark;

    private final Preferences preferences = PreferencesFactory.get();

    private final PathContainerService containerService
        = new S3PathContainerService();

    private final ClientConfiguration configuration;

    private final Location locationFeature;

    public KMSEncryptionFeature(final S3Session session, final X509TrustManager trust, final X509KeyManager key) {
        super(session);
        this.bookmark = session.getHost();
        this.configuration = new CustomClientConfiguration(bookmark,
            new ThreadLocalHostnameDelegatingTrustManager(trust, bookmark.getHostname()), key);
        this.locationFeature = session.getFeature(Location.class);
    }

    private interface Authenticated<T> extends Callable<T> {
        T call() throws BackgroundException;
    }

    private <T> T authenticated(final Authenticated<T> run, final LoginCallback prompt) throws BackgroundException {
        final LoginOptions options = new LoginOptions(bookmark.getProtocol());
        try {
            final LoginService login = new KeychainLoginService(PasswordStoreFactory.get());
            login.validate(bookmark, LocaleFactory.localizedString("AWS Key Management Service", "S3"), prompt, options);
            return run.call();
        }
        catch(LoginFailureException failure) {
            bookmark.setCredentials(prompt.prompt(bookmark, bookmark.getCredentials().getUsername(),
                LocaleFactory.localizedString("Login failed", "Credentials"), failure.getMessage(), options));
            return this.authenticated(run, prompt);
        }
    }

    @Override
    public Algorithm getDefault(final Path file) {
        final String setting = preferences.getProperty("s3.encryption.algorithm");
        if(StringUtils.equals(KMSEncryptionFeature.SSE_KMS_DEFAULT.algorithm, setting)) {
            final String key = String.format("s3.encryption.key.%s", containerService.getContainer(file).getName());
            if(StringUtils.isNotBlank(preferences.getProperty(key))) {
                return Algorithm.fromString(preferences.getProperty(key));
            }
            return KMSEncryptionFeature.SSE_KMS_DEFAULT;
        }
        return super.getDefault(file);
    }

    @Override
    public Algorithm getEncryption(final Path file) throws BackgroundException {
        if(containerService.isContainer(file)) {
            final String key = String.format("s3.encryption.key.%s", containerService.getContainer(file).getName());
            if(StringUtils.isNotBlank(preferences.getProperty(key))) {
                return Algorithm.fromString(preferences.getProperty(key));
            }
        }
        return super.getEncryption(file);
    }

    /**
     * @return List of IDs of KMS managed keys
     */
    @Override
    public Set<Algorithm> getKeys(final Path file, final LoginCallback prompt) throws BackgroundException {
        final Path container = containerService.getContainer(file);
        final Set<Algorithm> keys = super.getKeys(container, prompt);
        if(container.isRoot()) {
            return keys;
        }
        try {
            keys.addAll(this.authenticated(new Authenticated<Set<Algorithm>>() {
                @Override
                public Set<Algorithm> call() throws BackgroundException {
                    final AWSKMS client = client(container);
                    try {
                        final Map<String, String> aliases = new HashMap<String, String>();
                        for(AliasListEntry entry : client.listAliases().getAliases()) {
                            aliases.put(entry.getTargetKeyId(), entry.getAliasName());
                        }
                        final Set<Algorithm> keys = new HashSet<Algorithm>();
                        for(KeyListEntry entry : client.listKeys().getKeys()) {
                            keys.add(new AliasedAlgorithm(entry, aliases.get(entry.getKeyId())));
                        }
                        return keys;
                    }
                    catch(AmazonClientException e) {
                        throw new AmazonServiceExceptionMappingService().map("Cannot read AWS KMS configuration", e);
                    }
                    finally {
                        client.shutdown();
                    }
                }
            }, prompt));
        }
        catch(AccessDeniedException e) {
            log.warn(String.format("Ignore failure reading keys from KMS. %s", e.getMessage()));
            keys.add(SSE_KMS_DEFAULT);
        }
        return keys;
    }

    private AWSKMS client(final Path container) throws BackgroundException {
        final AWSKMSClientBuilder builder = AWSKMSClientBuilder.standard()
            .withCredentials(AWSCredentialsConfigurator.toAWSCredentialsProvider(bookmark.getCredentials()))
            .withClientConfiguration(configuration);
        final Location.Name region = locationFeature.getLocation(container);
        if(Location.unknown.equals(region)) {
            builder.withRegion(Regions.DEFAULT_REGION);
        }
        else {
            builder.withRegion(region.getIdentifier());
        }
        return builder.build();
    }

    /**
     * Default KMS Managed SSE with default key
     */
    public static final Algorithm SSE_KMS_DEFAULT = new Algorithm("aws:kms", null) {
        @Override
        public String getDescription() {
            return "SSE-KMS";
        }
    };

    private static class AliasedAlgorithm extends Algorithm {
        private final KeyListEntry entry;
        private final String alias;

        public AliasedAlgorithm(final KeyListEntry entry, final String alias) {
            super(KMSEncryptionFeature.SSE_KMS_DEFAULT.algorithm, entry.getKeyArn());
            this.entry = entry;
            this.alias = alias;
        }

        @Override
        public String getDescription() {
            if(StringUtils.isBlank(alias)) {
                return String.format("SSE-KMS (%s)", entry.getKeyArn());
            }
            return String.format("SSE-KMS (%s - %s)", alias, entry.getKeyArn());
        }
    }
}
