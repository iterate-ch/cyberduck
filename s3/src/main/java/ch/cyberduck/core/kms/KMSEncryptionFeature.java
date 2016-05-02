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
import ch.cyberduck.core.PreferencesUseragentProvider;
import ch.cyberduck.core.UseragentProvider;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.exception.LoginFailureException;
import ch.cyberduck.core.iam.AmazonServiceExceptionMappingService;
import ch.cyberduck.core.preferences.PreferencesFactory;
import ch.cyberduck.core.proxy.Proxy;
import ch.cyberduck.core.proxy.ProxyFactory;
import ch.cyberduck.core.s3.S3EncryptionFeature;
import ch.cyberduck.core.s3.S3Session;

import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

import com.amazonaws.AmazonClientException;
import com.amazonaws.ClientConfiguration;
import com.amazonaws.services.kms.AWSKMSClient;
import com.amazonaws.services.kms.model.KeyListEntry;

public class KMSEncryptionFeature extends S3EncryptionFeature {
    private static final Logger log = Logger.getLogger(KMSEncryptionFeature.class);

    private final Host host;

    public final AWSKMSClient client;

    public KMSEncryptionFeature(final S3Session session) {
        this(session, PreferencesFactory.get().getInteger("connection.timeout.seconds") * 1000);
    }

    public KMSEncryptionFeature(final S3Session session, final int timeout) {
        super(session);
        this.host = session.getHost();
        final ClientConfiguration configuration = new ClientConfiguration();
        configuration.setConnectionTimeout(timeout);
        configuration.setSocketTimeout(timeout);
        final UseragentProvider ua = new PreferencesUseragentProvider();
        configuration.setUserAgent(ua.get());
        configuration.setMaxErrorRetry(0);
        configuration.setMaxConnections(1);
        final Proxy proxy = ProxyFactory.get().find(host);
        switch(proxy.getType()) {
            case HTTP:
            case HTTPS:
                configuration.setProxyHost(proxy.getHostname());
                configuration.setProxyPort(proxy.getPort());
        }
        // Create new IAM credentials
        client = new AWSKMSClient(
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

    /**
     * @return List of IDs of KMS managed keys
     */
    @Override
    public List<String> getKeys(final LoginCallback prompt) throws BackgroundException {
        return this.authenticated(new Authenticated<List<String>>() {
            @Override
            public List<String> call() throws BackgroundException {
                try {
                    final List<String> keys = new ArrayList<>();
                    for(KeyListEntry entry : client.listKeys().getKeys()) {
                        keys.add(entry.getKeyId());
                    }
                    return keys;
                }
                catch(AmazonClientException e) {
                    throw new AmazonServiceExceptionMappingService().map("Cannot read AWS KMS configuration", e);
                }
            }
        }, prompt);
    }
}
