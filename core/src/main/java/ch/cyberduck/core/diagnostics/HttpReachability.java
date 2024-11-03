package ch.cyberduck.core.diagnostics;

/*
 * Copyright (c) 2002-2023 iterate GmbH. All rights reserved.
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

import ch.cyberduck.core.CertificateStore;
import ch.cyberduck.core.CertificateStoreFactory;
import ch.cyberduck.core.DefaultIOExceptionMappingService;
import ch.cyberduck.core.DisabledCertificateIdentityCallback;
import ch.cyberduck.core.DisabledCertificateTrustCallback;
import ch.cyberduck.core.DisabledLoginCallback;
import ch.cyberduck.core.DisabledTranscriptListener;
import ch.cyberduck.core.Host;
import ch.cyberduck.core.HostUrlProvider;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.exception.ConnectionCanceledException;
import ch.cyberduck.core.http.DefaultHttpResponseExceptionMappingService;
import ch.cyberduck.core.http.HttpConnectionPoolBuilder;
import ch.cyberduck.core.proxy.ProxyFactory;
import ch.cyberduck.core.proxy.ProxyFinder;
import ch.cyberduck.core.ssl.DefaultTrustManagerHostnameCallback;
import ch.cyberduck.core.ssl.KeychainX509KeyManager;
import ch.cyberduck.core.ssl.KeychainX509TrustManager;
import ch.cyberduck.core.ssl.SSLExceptionMappingService;
import ch.cyberduck.core.ssl.ThreadLocalHostnameDelegatingTrustManager;
import ch.cyberduck.core.ssl.X509KeyManager;
import ch.cyberduck.core.ssl.X509TrustManager;
import ch.cyberduck.core.worker.DefaultExceptionMappingService;

import org.apache.http.HttpStatus;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpResponseException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpHead;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.net.ssl.SSLException;
import java.io.IOException;
import java.net.SocketException;

public class HttpReachability implements Reachability {
    private static final Logger log = LogManager.getLogger(HttpReachability.class);

    private final ProxyFinder proxy;
    private final CertificateStore store;

    public HttpReachability() {
        this(ProxyFactory.get(), CertificateStoreFactory.get());
    }

    public HttpReachability(final ProxyFinder proxy, final CertificateStore store) {
        this.proxy = proxy;
        this.store = store;
    }

    @Override
    public void test(final Host bookmark) throws BackgroundException {
        if(log.isDebugEnabled()) {
            log.debug("Test reachability for {}", bookmark);
        }
        final X509TrustManager trust = new KeychainX509TrustManager(new DisabledCertificateTrustCallback(),
                new DefaultTrustManagerHostnameCallback(bookmark), store);
        final X509KeyManager key = new KeychainX509KeyManager(new DisabledCertificateIdentityCallback(), bookmark,
                store);
        final HttpConnectionPoolBuilder builder = new HttpConnectionPoolBuilder(bookmark,
                new ThreadLocalHostnameDelegatingTrustManager(trust, bookmark.getHostname()), key, Reachability.timeout, proxy);
        final HttpClientBuilder configuration = builder.build(proxy,
                new DisabledTranscriptListener(), new DisabledLoginCallback());
        configuration.disableRedirectHandling();
        configuration.disableAutomaticRetries();
        try (CloseableHttpClient client = configuration.build()) {
            final HttpRequestBase resource = new HttpHead(new HostUrlProvider().withUsername(false).withPath(true).get(bookmark));
            final CloseableHttpResponse response = client.execute(resource);
            if(log.isDebugEnabled()) {
                log.debug("Received response {}", response);
            }
            EntityUtils.consume(response.getEntity());
            switch(response.getStatusLine().getStatusCode()) {
                case HttpStatus.SC_BAD_GATEWAY:
                case HttpStatus.SC_INTERNAL_SERVER_ERROR:
                case HttpStatus.SC_SERVICE_UNAVAILABLE:
                case HttpStatus.SC_GATEWAY_TIMEOUT:
                    if(log.isWarnEnabled()) {
                        log.warn("HTTP error {} determined offline status", response);
                    }
                    throw new DefaultHttpResponseExceptionMappingService().map(new HttpResponseException(response.getStatusLine().getStatusCode(),
                            response.getStatusLine().getReasonPhrase()));
            }
        }
        catch(ClientProtocolException e) {
            if(log.isWarnEnabled()) {
                log.warn("Ignore HTTP error response {}", e);
            }
        }
        catch(SSLException e) {
            try {
                throw new SSLExceptionMappingService().map(e);
            }
            catch(ConnectionCanceledException c) {
                // Certificate error only
                if(log.isWarnEnabled()) {
                    log.warn("Ignore SSL failure {}", e);
                }
            }
        }
        catch(SocketException e) {
            if(log.isWarnEnabled()) {
                log.warn("Failure {} opening socket for {}", e, bookmark);
            }
            throw new DefaultIOExceptionMappingService().map(e);
        }
        catch(IOException e) {
            if(log.isWarnEnabled()) {
                log.warn("Generic failure {} for {}", e, bookmark);
            }
            throw new DefaultIOExceptionMappingService().map(e);
        }
        catch(IllegalArgumentException e) {
            if(log.isWarnEnabled()) {
                log.warn("Parsing URI {}: {}", bookmark, e);
            }
            throw new DefaultExceptionMappingService().map(e);
        }
        // Ignore
    }

    @Override
    public Monitor monitor(final Host bookmark, final Callback callback) {
        return Monitor.disabled;
    }
}
