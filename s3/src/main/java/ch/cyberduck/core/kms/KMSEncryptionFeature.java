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

import ch.cyberduck.core.PreferencesUseragentProvider;
import ch.cyberduck.core.UseragentProvider;
import ch.cyberduck.core.preferences.PreferencesFactory;
import ch.cyberduck.core.proxy.Proxy;
import ch.cyberduck.core.proxy.ProxyFactory;
import ch.cyberduck.core.s3.S3EncryptionFeature;
import ch.cyberduck.core.s3.S3Session;

import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.List;

import com.amazonaws.ClientConfiguration;
import com.amazonaws.services.kms.AWSKMSClient;
import com.amazonaws.services.kms.model.KeyListEntry;

public class KMSEncryptionFeature extends S3EncryptionFeature {
    private static final Logger log = Logger.getLogger(KMSEncryptionFeature.class);

    public final AWSKMSClient client;

    public KMSEncryptionFeature(final S3Session session) {
        this(session, PreferencesFactory.get().getInteger("connection.timeout.seconds") * 1000);
    }

    public KMSEncryptionFeature(final S3Session session, final int timeout) {
        super(session);
        final ClientConfiguration configuration = new ClientConfiguration();
        configuration.setConnectionTimeout(timeout);
        configuration.setSocketTimeout(timeout);
        final UseragentProvider ua = new PreferencesUseragentProvider();
        configuration.setUserAgent(ua.get());
        configuration.setMaxErrorRetry(0);
        configuration.setMaxConnections(1);
        final Proxy proxy = ProxyFactory.get().find(session.getHost());
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
                        return session.getHost().getCredentials().getUsername();
                    }

                    @Override
                    public String getAWSSecretKey() {
                        return session.getHost().getCredentials().getPassword();
                    }
                }, configuration
        );

    }

    /**
     * @return List of IDs of KMS managed keys
     */
    @Override
    public List<String> getKeys() {
        final List<String> keys = new ArrayList<>();
        for(KeyListEntry entry : client.listKeys().getKeys()) {
            keys.add(entry.getKeyId());
        }
        return keys;
    }
}
