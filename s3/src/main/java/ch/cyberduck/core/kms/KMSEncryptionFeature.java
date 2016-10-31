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
import ch.cyberduck.core.PasswordStoreFactory;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathContainerService;
import ch.cyberduck.core.PreferencesUseragentProvider;
import ch.cyberduck.core.UseragentProvider;
import ch.cyberduck.core.exception.AccessDeniedException;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.exception.LoginFailureException;
import ch.cyberduck.core.features.Location;
import ch.cyberduck.core.iam.AmazonServiceExceptionMappingService;
import ch.cyberduck.core.preferences.Preferences;
import ch.cyberduck.core.preferences.PreferencesFactory;
import ch.cyberduck.core.proxy.Proxy;
import ch.cyberduck.core.proxy.ProxyFactory;
import ch.cyberduck.core.s3.S3EncryptionFeature;
import ch.cyberduck.core.s3.S3PathContainerService;
import ch.cyberduck.core.s3.S3Session;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;

import com.amazonaws.AmazonClientException;
import com.amazonaws.ClientConfiguration;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.kms.AWSKMSClient;
import com.amazonaws.services.kms.model.AliasListEntry;
import com.amazonaws.services.kms.model.KeyListEntry;

public class KMSEncryptionFeature extends S3EncryptionFeature {
    private static final Logger log = Logger.getLogger(KMSEncryptionFeature.class);

    private final Host host;
    private final S3Session session;

    private final Preferences preferences = PreferencesFactory.get();

    private final PathContainerService containerService
            = new S3PathContainerService();

    private final ClientConfiguration configuration;

    public KMSEncryptionFeature(final S3Session session) {
        this(session, PreferencesFactory.get().getInteger("connection.timeout.seconds") * 1000);
    }

    public KMSEncryptionFeature(final S3Session session, final int timeout) {
        super(session);
        host = session.getHost();
        this.session = session;
        configuration = new ClientConfiguration();
        configuration.setConnectionTimeout(timeout);
        configuration.setSocketTimeout(timeout);
        final UseragentProvider ua = new PreferencesUseragentProvider();
        configuration.setUserAgentPrefix(ua.get());
        configuration.setMaxErrorRetry(0);
        configuration.setMaxConnections(1);
        configuration.setUseGzip(PreferencesFactory.get().getBoolean("http.compression.enable"));
        final Proxy proxy = ProxyFactory.get().find(host);
        switch(proxy.getType()) {
            case HTTP:
            case HTTPS:
                configuration.setProxyHost(proxy.getHostname());
                configuration.setProxyPort(proxy.getPort());
        }
    }

    private interface Authenticated<T> extends Callable<T> {
        T call() throws BackgroundException;
    }

    private <T> T authenticated(final Authenticated<T> run, final LoginCallback prompt) throws BackgroundException {
        final LoginOptions options = new LoginOptions();
        try {
            final KeychainLoginService login = new KeychainLoginService(prompt, PasswordStoreFactory.get());
            login.validate(host, LocaleFactory.localizedString("AWS Key Management Service", "S3"), options);
            return run.call();
        }
        catch(LoginFailureException failure) {
            prompt.prompt(host, host.getCredentials(),
                    LocaleFactory.localizedString("Login failed", "Credentials"), failure.getMessage(), options);
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

    @Override
    public void setEncryption(final Path file, final Algorithm setting) throws BackgroundException {
        if(containerService.isContainer(file)) {
            final String key = String.format("s3.encryption.key.%s", containerService.getContainer(file).getName());
            preferences.setProperty(key, setting.toString());
        }
        super.setEncryption(file, setting);
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
                    // Create new IAM credentials
                    final AWSKMSClient client = new AWSKMSClient(
                            new com.amazonaws.auth.AWSCredentials() {
                                @Override
                                public String getAWSAccessKeyId() {
                                    return host.getCredentials().getUsername();
                                }

                                @Override
                                public String getAWSSecretKey() {
                                    return host.getCredentials().getPassword();
                                }
                            }, configuration
                    );
                    final Location feature = session.getFeature(Location.class);
                    final Location.Name region = feature.getLocation(container);
                    client.setRegion(Region.getRegion(Regions.fromName(region.getIdentifier())));
                    try {
                        final Map<String, String> aliases = new HashMap<String, String>();
                        for(AliasListEntry entry : client.listAliases().getAliases()) {
                            aliases.put(entry.getTargetKeyId(), entry.getAliasName());
                        }
                        final Set<Algorithm> keys = new HashSet<Algorithm>();
                        for(KeyListEntry entry : client.listKeys().getKeys()) {
                            keys.add(new AliasedAlgorithm(entry, aliases.get(entry.getKeyId()), region));
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
        private final Location.Name region;

        public AliasedAlgorithm(final KeyListEntry entry, final String alias, final Location.Name region) {
            super(KMSEncryptionFeature.SSE_KMS_DEFAULT.algorithm, entry.getKeyArn());
            this.entry = entry;
            this.alias = alias;
            this.region = region;
        }

        @Override
        public String getDescription() {
            if(StringUtils.isBlank(alias)) {
                return String.format("SSE-KMS (%s)", entry.getKeyArn());
            }
            return String.format("SSE-KMS (%s - %s)", alias, entry.getKeyArn());
        }

        public Location.Name getRegion() {
            return region;
        }
    }
}
