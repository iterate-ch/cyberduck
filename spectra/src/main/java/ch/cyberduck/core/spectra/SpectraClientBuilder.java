/*
 * Copyright (c) 2015-2016 Spectra Logic Corporation. All rights reserved.
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

package ch.cyberduck.core.spectra;

import ch.cyberduck.core.ConnectionTimeoutFactory;
import ch.cyberduck.core.Host;
import ch.cyberduck.core.PreferencesUseragentProvider;
import ch.cyberduck.core.Scheme;
import ch.cyberduck.core.preferences.HostPreferences;

import org.apache.http.impl.client.CloseableHttpClient;
import org.jets3t.service.impl.rest.httpclient.RestStorageService;

import java.net.URI;

import com.spectralogic.ds3client.Ds3Client;
import com.spectralogic.ds3client.Ds3ClientImpl;
import com.spectralogic.ds3client.models.common.Credentials;
import com.spectralogic.ds3client.networking.ConnectionDetails;
import com.spectralogic.ds3client.networking.NetworkClientImpl;

public class SpectraClientBuilder {
    public Ds3Client wrap(final RestStorageService client, final Host bookmark) {
        return new Ds3ClientImpl(new NetworkClientImpl(new ConnectionDetails() {
            @Override
            public String getEndpoint() {
                return String.format("%s:%d", bookmark.getHostname(), bookmark.getPort());
            }

            @Override
            public Credentials getCredentials() {
                return new Credentials(client.getProviderCredentials().getAccessKey(),
                    client.getProviderCredentials().getSecretKey());
            }

            @Override
            public boolean isHttps() {
                return bookmark.getProtocol().getScheme() == Scheme.https;
            }

            @Override
            public URI getProxy() {
                return null;
            }

            @Override
            public int getRetries() {
                return 0;
            }

            @Override
            public int getBufferSize() {
                return new HostPreferences(bookmark).getInteger("connection.chunksize");
            }

            @Override
            public int getConnectionTimeout() {
                return ConnectionTimeoutFactory.get(bookmark).getTimeout() * 1000;
            }

            @Override
            public int getSocketTimeout() {
                return ConnectionTimeoutFactory.get(bookmark).getTimeout() * 1000;
            }

            @Override
            public boolean isCertificateVerification() {
                return true;
            }

            @Override
            public String getUserAgent() {
                return new PreferencesUseragentProvider().get();
            }
        }, (CloseableHttpClient) client.getHttpClient()));
    }
}
