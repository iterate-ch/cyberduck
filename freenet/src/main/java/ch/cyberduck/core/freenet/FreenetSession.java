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

import ch.cyberduck.core.Host;
import ch.cyberduck.core.HostKeyCallback;
import ch.cyberduck.core.HostUrlProvider;
import ch.cyberduck.core.LoginCallback;
import ch.cyberduck.core.MacUniqueIdService;
import ch.cyberduck.core.UrlProvider;
import ch.cyberduck.core.dav.DAVClient;
import ch.cyberduck.core.dav.DAVRedirectStrategy;
import ch.cyberduck.core.dav.DAVSession;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.exception.ChecksumException;
import ch.cyberduck.core.exception.LocalAccessDeniedException;
import ch.cyberduck.core.features.Find;
import ch.cyberduck.core.features.Metadata;
import ch.cyberduck.core.features.Timestamp;
import ch.cyberduck.core.http.PreferencesRedirectCallback;
import ch.cyberduck.core.io.MD5ChecksumCompute;
import ch.cyberduck.core.proxy.ProxyFinder;
import ch.cyberduck.core.ssl.X509KeyManager;
import ch.cyberduck.core.ssl.X509TrustManager;
import ch.cyberduck.core.threading.CancelCallback;

import org.apache.http.HttpRequest;
import org.apache.http.HttpRequestInterceptor;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicHeader;
import org.apache.http.protocol.HttpContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class FreenetSession extends DAVSession {
    private static final Logger log = LogManager.getLogger(FreenetSession.class);

    public FreenetSession(final Host host, final X509TrustManager trust, final X509KeyManager key) {
        super(host, trust, key);
    }

    @Override
    protected DAVClient connect(final ProxyFinder proxy, final HostKeyCallback key, final LoginCallback prompt, final CancelCallback cancel) throws BackgroundException {
        // Always inject new pool to builder on connect because the pool is shutdown on disconnect
        final HttpClientBuilder configuration = builder.build(proxy, this, prompt);
        configuration.setRedirectStrategy(new DAVRedirectStrategy(new PreferencesRedirectCallback()));
        configuration.setUserAgent(new FreenetUserAgentProvider().get());
        try {
            final String hash = new MD5ChecksumCompute().compute(new MacUniqueIdService().getUUID()).hash;
            configuration.addInterceptorLast(new HttpRequestInterceptor() {
                @Override
                public void process(final HttpRequest request, final HttpContext context) {
                    request.addHeader(new BasicHeader("X-Freenet-Insid", hash));
                }
            });
        }
        catch(LocalAccessDeniedException | ChecksumException e) {
            log.warn(String.format("Failure %s retrieving MAC address", e));
            final String identifier = new MD5ChecksumCompute().compute(System.getProperty("user.name")).hash;
            configuration.addInterceptorLast(new HttpRequestInterceptor() {
                @Override
                public void process(final HttpRequest request, final HttpContext context) {
                    request.addHeader(new BasicHeader("X-Freenet-Insid", identifier));
                }
            });
        }
        return new DAVClient(new HostUrlProvider().withUsername(false).get(host), configuration);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T getFeature(final Class<T> type) {
        if(type == UrlProvider.class) {
            return (T) new FreenetUrlProvider(host);
        }
        if(type == Find.class) {
            return (T) new FreenetFindFeature(this);
        }
        if(type == Timestamp.class) {
            return null;
        }
        if(type == Metadata.class) {
            return null;
        }
        return super.getFeature(type);
    }
}
