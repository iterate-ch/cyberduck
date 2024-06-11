package ch.cyberduck.core.freenet;

/*
 * Copyright (c) 2002-2021 iterate GmbH. All rights reserved.
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
import ch.cyberduck.core.DescriptiveUrl;
import ch.cyberduck.core.DisabledCertificateIdentityCallback;
import ch.cyberduck.core.DisabledCertificateTrustCallback;
import ch.cyberduck.core.DisabledLoginCallback;
import ch.cyberduck.core.DisabledTranscriptListener;
import ch.cyberduck.core.Host;
import ch.cyberduck.core.HostPasswordStore;
import ch.cyberduck.core.PasswordStoreFactory;
import ch.cyberduck.core.WebUrlProvider;
import ch.cyberduck.core.dav.DAVSSLProtocol;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.http.HttpConnectionPoolBuilder;
import ch.cyberduck.core.http.HttpExceptionMappingService;
import ch.cyberduck.core.http.UserAgentHttpRequestInitializer;
import ch.cyberduck.core.preferences.HostPreferences;
import ch.cyberduck.core.proxy.ProxyFactory;
import ch.cyberduck.core.proxy.ProxyHostUrlProvider;
import ch.cyberduck.core.ssl.DefaultTrustManagerHostnameCallback;
import ch.cyberduck.core.ssl.KeychainX509KeyManager;
import ch.cyberduck.core.ssl.KeychainX509TrustManager;
import ch.cyberduck.core.ssl.ThreadLocalHostnameDelegatingTrustManager;
import ch.cyberduck.core.ssl.X509KeyManager;
import ch.cyberduck.core.ssl.X509TrustManager;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.AbstractResponseHandler;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.net.URI;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.api.client.auth.oauth2.PasswordTokenRequest;
import com.google.api.client.auth.oauth2.TokenResponse;
import com.google.api.client.http.BasicAuthentication;
import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.apache.v2.ApacheHttpTransport;
import com.google.api.client.json.gson.GsonFactory;

public class FreenetAuthenticatedUrlProvider implements WebUrlProvider {
    private static final Logger log = LogManager.getLogger(FreenetAuthenticatedUrlProvider.class);

    private final HostPasswordStore keychain;

    public FreenetAuthenticatedUrlProvider() {
        this(PasswordStoreFactory.get());
    }

    public FreenetAuthenticatedUrlProvider(final HostPasswordStore keychain) {
        this.keychain = keychain;
    }

    @Override
    public DescriptiveUrl toUrl(final Host bookmark) {
        try {
            // Run password flow
            final TokenResponse response;
            try {
                final Host target = new Host(new DAVSSLProtocol(), "oauth.freenet.de");
                final X509TrustManager trust = new KeychainX509TrustManager(new DisabledCertificateTrustCallback(),
                        new DefaultTrustManagerHostnameCallback(target), CertificateStoreFactory.get());
                final X509KeyManager key = new KeychainX509KeyManager(new DisabledCertificateIdentityCallback(), target,
                        CertificateStoreFactory.get());
                final CloseableHttpClient client = new HttpConnectionPoolBuilder(
                        target, new ThreadLocalHostnameDelegatingTrustManager(trust, target.getHostname()), key, ProxyFactory.get()
                ).build(ProxyFactory.get(), new DisabledTranscriptListener(), new DisabledLoginCallback())
                        .setUserAgent(new FreenetUserAgentProvider().get())
                        .build();
                final String username = bookmark.getCredentials().getUsername();
                final String password;
                if(StringUtils.isBlank(bookmark.getCredentials().getPassword())) {
                    password = keychain.findLoginPassword(bookmark);
                }
                else {
                    password = bookmark.getCredentials().getPassword();
                }
                if(null == password) {
                    log.warn(String.format("No password found for %s", bookmark));
                    return DescriptiveUrl.EMPTY;
                }
                response = new PasswordTokenRequest(new ApacheHttpTransport(client),
                        new GsonFactory(), new GenericUrl("https://oauth.freenet.de/oauth/token"), username, password)
                        .setClientAuthentication(new BasicAuthentication("desktop_client", "6LIGIHuOSkznLomu5xw0EPPBJOXb2jLp"))
                        .setRequestInitializer(new UserAgentHttpRequestInitializer(new FreenetUserAgentProvider()))
                        .set("world", new HostPreferences(bookmark).getProperty("world"))
                        .set("webLogin", Boolean.TRUE)
                        .execute();
                final FreenetTemporaryLoginResponse login = this.getLoginSession(client, response.getAccessToken());
                return new DescriptiveUrl(URI.create(login.urls.login), DescriptiveUrl.Type.authenticated);
            }
            catch(IOException e) {
                throw new HttpExceptionMappingService().map(e);
            }
        }
        catch(BackgroundException e) {
            log.warn(String.format("Failure %s retrieving authenticated URL for %s", e, bookmark));
            return DescriptiveUrl.EMPTY;
        }
    }

    private FreenetTemporaryLoginResponse getLoginSession(final HttpClient client, final String token) throws BackgroundException {
        final HttpGet request = new HttpGet("https://api.mail.freenet.de/v2.0/hash/create");
        request.addHeader("Token", token);
        try {
            return client.execute(request, new AbstractResponseHandler<FreenetTemporaryLoginResponse>() {
                @Override
                public FreenetTemporaryLoginResponse handleEntity(final HttpEntity entity) throws IOException {
                    final ObjectMapper mapper = new ObjectMapper();
                    return mapper.readValue(entity.getContent(), FreenetTemporaryLoginResponse.class);
                }
            });
        }
        catch(IOException e) {
            throw new HttpExceptionMappingService().map(e);
        }
    }
}
