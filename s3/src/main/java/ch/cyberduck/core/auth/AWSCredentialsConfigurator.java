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
import ch.cyberduck.core.preferences.PreferencesFactory;

import org.apache.log4j.Logger;

import com.amazonaws.SdkClientException;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSCredentialsProvider;

public class AWSCredentialsConfigurator implements CredentialsConfigurator {
    private static final Logger log = Logger.getLogger(AWSCredentialsConfigurator.class);

    private final AWSCredentialsProvider[] providers;

    public AWSCredentialsConfigurator(final AWSCredentialsProvider... providers) {
        this.providers = providers;
    }

    @Override
    public Credentials configure(final Host host) {
        final Credentials credentials = host.getCredentials();
        // Only for AWS
        if(host.getHostname().endsWith(PreferencesFactory.get().getProperty("s3.hostname.default"))) {
            if(!credentials.validate(host.getProtocol(), new LoginOptions(host.getProtocol()))) {
                for(AWSCredentialsProvider provider : providers) {
                    try {
                        final AWSCredentials c = provider.getCredentials();
                        credentials.setUsername(c.getAWSAccessKeyId());
                        credentials.setPassword(c.getAWSSecretKey());
                        break;
                    }
                    catch(SdkClientException e) {
                        log.debug(String.format("Ignore failure loading credentials from provider %s", provider));
                        // Continue searching with next provider
                    }
                }
            }
        }
        return credentials;
    }

    @Override
    public void reload() {
        for(AWSCredentialsProvider provider : providers) {
            provider.refresh();
        }
    }
}
