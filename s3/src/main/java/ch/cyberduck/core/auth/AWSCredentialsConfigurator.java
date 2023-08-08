package ch.cyberduck.core.auth;

/*
 * Copyright (c) 2002-2017 iterate GmbH. All rights reserved.
 * https://cyberduck.io/
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 */

import ch.cyberduck.core.Credentials;
import ch.cyberduck.core.CredentialsConfigurator;
import ch.cyberduck.core.Host;
import ch.cyberduck.core.LoginOptions;
import ch.cyberduck.core.exception.LoginCanceledException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jets3t.service.security.ProviderCredentials;

import com.amazonaws.SdkClientException;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.auth.AWSSessionCredentials;
import com.amazonaws.auth.AWSSessionCredentialsProvider;
import com.amazonaws.auth.AWSStaticCredentialsProvider;

public class AWSCredentialsConfigurator implements CredentialsConfigurator {
    private static final Logger log = LogManager.getLogger(AWSCredentialsConfigurator.class);

    private final AWSCredentialsProvider[] providers;

    public AWSCredentialsConfigurator(final AWSCredentialsProvider... providers) {
        this.providers = providers;
    }

    @Override
    public Credentials configure(final Host host) {
        if(!host.getCredentials().validate(host.getProtocol(), new LoginOptions(host.getProtocol()).password(false))) {
            final Credentials credentials = new Credentials(host.getCredentials());
            // Lookup from default profile if no access key is set in bookmark
            for(AWSCredentialsProvider provider : providers) {
                try {
                    final AWSCredentials c = provider.getCredentials();
                    credentials.setUsername(c.getAWSAccessKeyId());
                    credentials.setPassword(c.getAWSSecretKey());
                    if(c instanceof AWSSessionCredentials) {
                        credentials.setToken(((AWSSessionCredentials) c).getSessionToken());
                    }
                    break;
                }
                catch(SdkClientException e) {
                    log.debug(String.format("Ignore failure loading credentials from provider %s", provider));
                    // Continue searching with next provider
                }
            }
            return credentials;
        }
        return host.getCredentials();
    }

    @Override
    public CredentialsConfigurator reload() throws LoginCanceledException {
        for(AWSCredentialsProvider provider : providers) {
            provider.refresh();
        }
        return this;
    }

    public static AWSCredentialsProvider toAWSCredentialsProvider(final ProviderCredentials credentials) {
        return credentials instanceof org.jets3t.service.security.AWSSessionCredentials ?
            new AWSSessionCredentialsProvider() {
                @Override
                public AWSSessionCredentials getCredentials() {
                    return new AWSSessionCredentials() {
                        @Override
                        public String getSessionToken() {
                            return ((org.jets3t.service.security.AWSSessionCredentials) credentials).getSessionToken();
                        }

                        @Override
                        public String getAWSAccessKeyId() {
                            return credentials.getAccessKey();
                        }

                        @Override
                        public String getAWSSecretKey() {
                            return credentials.getSecretKey();
                        }
                    };
                }

                @Override
                public void refresh() {
                    // Not supported
                }
            } :
            new AWSStaticCredentialsProvider(new AWSCredentials() {
                @Override
                public String getAWSAccessKeyId() {
                    return credentials.getAccessKey();
                }

                @Override
                public String getAWSSecretKey() {
                    return credentials.getSecretKey();
                }
            });
    }
}
