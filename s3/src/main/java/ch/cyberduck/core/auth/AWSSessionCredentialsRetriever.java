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
import ch.cyberduck.core.DisabledLoginCallback;
import ch.cyberduck.core.DisabledTranscriptListener;
import ch.cyberduck.core.Host;
import ch.cyberduck.core.HostParser;
import ch.cyberduck.core.HostUrlProvider;
import ch.cyberduck.core.ProtocolFactory;
import ch.cyberduck.core.TemporaryAccessTokens;
import ch.cyberduck.core.date.ISO8601DateFormatter;
import ch.cyberduck.core.date.InvalidDateException;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.exception.LoginCanceledException;
import ch.cyberduck.core.exception.LoginFailureException;
import ch.cyberduck.core.http.HttpConnectionPoolBuilder;
import ch.cyberduck.core.proxy.ProxyFactory;
import ch.cyberduck.core.s3.S3CredentialsStrategy;
import ch.cyberduck.core.ssl.ThreadLocalHostnameDelegatingTrustManager;
import ch.cyberduck.core.ssl.X509KeyManager;
import ch.cyberduck.core.ssl.X509TrustManager;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpResponseException;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Date;

import com.google.gson.stream.JsonReader;

public class AWSSessionCredentialsRetriever implements S3CredentialsStrategy {
    private static final Logger log = LogManager.getLogger(AWSSessionCredentialsRetriever.class);

    private final String url;
    private final X509TrustManager trust;
    private final X509KeyManager key;

    public AWSSessionCredentialsRetriever(final X509TrustManager trust, final X509KeyManager key, final String url) {
        this.trust = trust;
        this.key = key;
        this.url = url;
    }

    public static class Configurator implements CredentialsConfigurator {
        private final AWSSessionCredentialsRetriever retriever;
        private final String url;

        public Configurator(final X509TrustManager trust, final X509KeyManager key, final String url) {
            this.url = url;
            this.retriever = new AWSSessionCredentialsRetriever(trust, key, url);
        }

        @Override
        public Configurator reload() throws LoginCanceledException {
            return this;
        }

        @Override
        public Credentials configure(final Host host) {
            try {
                return retriever.get();
            }
            catch(BackgroundException e) {
                log.warn("Ignore failure {} retrieving credentials from {}", e, url);
                return host.getCredentials();
            }
        }
    }

    @Override
    public Credentials get() throws BackgroundException {
        log.debug("Configure credentials from {}", url);
        final Host address = new HostParser(ProtocolFactory.get()).get(url);
        final HttpConnectionPoolBuilder builder = new HttpConnectionPoolBuilder(address,
                new ThreadLocalHostnameDelegatingTrustManager(trust, address.getHostname()), key, ProxyFactory.get());
        final HttpClientBuilder configuration = builder.build(ProxyFactory.get(),
                new DisabledTranscriptListener(), new DisabledLoginCallback());
        try (CloseableHttpClient client = configuration.build()) {
            final HttpRequestBase resource = new HttpGet(new HostUrlProvider().withUsername(false).withPath(true).get(address));
            return client.execute(resource, new ResponseHandler<Credentials>() {
                @Override
                public Credentials handleResponse(final HttpResponse response) throws IOException {
                    switch(response.getStatusLine().getStatusCode()) {
                        case HttpStatus.SC_OK:
                            final HttpEntity entity = response.getEntity();
                            if(entity == null) {
                                log.warn("Missing response entity in {}", response);
                                throw new ClientProtocolException("Empty response");
                            }
                            else {
                                return parse(entity.getContent());
                            }
                    }
                    throw new HttpResponseException(response.getStatusLine().getStatusCode(), response.getStatusLine().getReasonPhrase());
                }
            });
        }
        catch(IOException e) {
            log.warn("Failure {} to retrieve session credentials", e);
            throw new LoginFailureException(e.getMessage(), e);
        }
    }

    protected Credentials parse(final InputStream in) throws IOException {
        final JsonReader reader = new JsonReader(new InputStreamReader(in, StandardCharsets.UTF_8));
        reader.beginObject();
        String key = null;
        String secret = null;
        String token = null;
        Date expiration = null;
        while(reader.hasNext()) {
            final String name = reader.nextName();
            final String value = reader.nextString();
            switch(name) {
                case "AccessKeyId":
                    key = value;
                    break;
                case "SecretAccessKey":
                    secret = value;
                    break;
                case "Token":
                    token = value;
                    break;
                case "Expiration":
                    try {
                        expiration = new ISO8601DateFormatter().parse(value);
                    }
                    catch(InvalidDateException e) {
                        log.warn("Failure {} parsing {}", e, value);
                    }
                    break;
            }
        }
        reader.endObject();
        return new Credentials().withTokens(new TemporaryAccessTokens(key, secret, token, expiration != null ? expiration.getTime() : -1L));
    }
}
