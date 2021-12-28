package ch.cyberduck.core.brick;

/*
 * Copyright (c) 2002-2019 iterate GmbH. All rights reserved.
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

import ch.cyberduck.core.CertificateStoreFactory;
import ch.cyberduck.core.DefaultIOExceptionMappingService;
import ch.cyberduck.core.DisabledCertificateIdentityCallback;
import ch.cyberduck.core.DisabledCertificateTrustCallback;
import ch.cyberduck.core.DisabledLoginCallback;
import ch.cyberduck.core.DisabledTranscriptListener;
import ch.cyberduck.core.Host;
import ch.cyberduck.core.HostPasswordStore;
import ch.cyberduck.core.HostUrlProvider;
import ch.cyberduck.core.PasswordStoreFactory;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.features.Pairing;
import ch.cyberduck.core.http.DefaultHttpResponseExceptionMappingService;
import ch.cyberduck.core.http.HttpConnectionPoolBuilder;
import ch.cyberduck.core.proxy.ProxyFactory;
import ch.cyberduck.core.proxy.ProxyHostUrlProvider;
import ch.cyberduck.core.ssl.DefaultTrustManagerHostnameCallback;
import ch.cyberduck.core.ssl.KeychainX509KeyManager;
import ch.cyberduck.core.ssl.KeychainX509TrustManager;
import ch.cyberduck.core.ssl.ThreadLocalHostnameDelegatingTrustManager;
import ch.cyberduck.core.ssl.X509KeyManager;
import ch.cyberduck.core.ssl.X509TrustManager;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpResponse;
import org.apache.http.auth.AuthSchemeProvider;
import org.apache.http.client.HttpResponseException;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;

public class BrickPairingFeature implements Pairing {
    private static final Logger log = LogManager.getLogger(BrickPairingFeature.class);

    private final HostPasswordStore store = PasswordStoreFactory.get();

    @Override
    public void delete(final Host bookmark) throws BackgroundException {
        try {
            final String token = store.findLoginPassword(bookmark);
            if(StringUtils.isNotBlank(token)) {
                log.warn(String.format("Delete pairing for %s", bookmark));
                final X509TrustManager trust = new KeychainX509TrustManager(new DisabledCertificateTrustCallback(),
                    new DefaultTrustManagerHostnameCallback(bookmark), CertificateStoreFactory.get());
                final X509KeyManager key = new KeychainX509KeyManager(new DisabledCertificateIdentityCallback(), bookmark,
                    CertificateStoreFactory.get());
                final HttpConnectionPoolBuilder builder = new HttpConnectionPoolBuilder(bookmark,
                    new ThreadLocalHostnameDelegatingTrustManager(trust, bookmark.getHostname()), key, ProxyFactory.get());
                final HttpClientBuilder configuration = builder.build(ProxyFactory.get().find(
                    new ProxyHostUrlProvider().get(bookmark)), new DisabledTranscriptListener(), new DisabledLoginCallback());
                configuration.setDefaultAuthSchemeRegistry(RegistryBuilder.<AuthSchemeProvider>create().build());
                final CloseableHttpClient client = configuration.build();
                final HttpRequestBase resource = new HttpDelete(
                    String.format("%s/api/rest/v1/api_key", new HostUrlProvider().withUsername(false).withPath(false).get(bookmark)));
                resource.setHeader("X-FilesAPI-Key", token);
                resource.setHeader(HttpHeaders.ACCEPT, "application/json");
                resource.setHeader(HttpHeaders.CONTENT_TYPE, "application/json");
                if(log.isInfoEnabled()) {
                    log.info(String.format("Delete paring key %s", token));
                }
                client.execute(resource, new ResponseHandler<Void>() {
                    @Override
                    public Void handleResponse(final HttpResponse response) {
                        return null;
                    }
                });
                client.close();
            }
        }
        catch(HttpResponseException e) {
            throw new DefaultHttpResponseExceptionMappingService().map(e);
        }
        catch(IOException e) {
            throw new DefaultIOExceptionMappingService().map(e);
        }
        finally {
            store.delete(bookmark);
        }
    }
}
