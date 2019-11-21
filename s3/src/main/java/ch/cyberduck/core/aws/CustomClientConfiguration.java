package ch.cyberduck.core.aws;

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

import ch.cyberduck.core.DisabledCancelCallback;
import ch.cyberduck.core.Host;
import ch.cyberduck.core.PreferencesUseragentProvider;
import ch.cyberduck.core.Resolver;
import ch.cyberduck.core.UseragentProvider;
import ch.cyberduck.core.exception.ResolveCanceledException;
import ch.cyberduck.core.exception.ResolveFailedException;
import ch.cyberduck.core.http.DisabledX509HostnameVerifier;
import ch.cyberduck.core.preferences.PreferencesFactory;
import ch.cyberduck.core.proxy.Proxy;
import ch.cyberduck.core.proxy.ProxyFactory;
import ch.cyberduck.core.proxy.ProxyFinder;
import ch.cyberduck.core.proxy.ProxySocketFactory;
import ch.cyberduck.core.ssl.CustomTrustSSLProtocolSocketFactory;
import ch.cyberduck.core.ssl.DefaultTrustManagerHostnameCallback;
import ch.cyberduck.core.ssl.KeychainX509KeyManager;
import ch.cyberduck.core.ssl.KeychainX509TrustManager;
import ch.cyberduck.core.ssl.ThreadLocalHostnameDelegatingTrustManager;

import org.apache.http.HttpHost;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.protocol.HttpContext;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.UnknownHostException;

import com.amazonaws.ClientConfiguration;
import com.amazonaws.DnsResolver;

public class CustomClientConfiguration extends ClientConfiguration {

    public CustomClientConfiguration(final Host bookmark) {
        this.setDnsResolver(new DnsResolver() {
            @Override
            public InetAddress[] resolve(final String host) throws UnknownHostException {
                try {
                    return new InetAddress[]{new Resolver().resolve(host, new DisabledCancelCallback())};
                }
                catch(ResolveFailedException | ResolveCanceledException e) {
                    throw new UnknownHostException(e.getDetail(false));
                }
            }
        });
        final int timeout = PreferencesFactory.get().getInteger("connection.timeout.seconds") * 1000;
        this.setConnectionTimeout(timeout);
        this.setSocketTimeout(timeout);
        final UseragentProvider ua = new PreferencesUseragentProvider();
        this.setUserAgentPrefix(ua.get());
        this.setMaxErrorRetry(0);
        this.setMaxConnections(1);
        this.setUseGzip(PreferencesFactory.get().getBoolean("http.compression.enable"));
        final ProxyFinder proxyFinder = ProxyFactory.get();
        final Proxy proxy = proxyFinder.find(bookmark);
        switch(proxy.getType()) {
            case HTTP:
            case HTTPS:
                this.setProxyHost(proxy.getHostname());
                this.setProxyPort(proxy.getPort());
        }
        final ThreadLocalHostnameDelegatingTrustManager trust = new ThreadLocalHostnameDelegatingTrustManager(
            new KeychainX509TrustManager(new DefaultTrustManagerHostnameCallback(bookmark)), bookmark.getHostname());
        this.getApacheHttpClientConfig().setSslSocketFactory(
            new SSLConnectionSocketFactory(
                new CustomTrustSSLProtocolSocketFactory(trust,
                    new KeychainX509KeyManager(bookmark)),
                new DisabledX509HostnameVerifier()
            ) {
                @Override
                public Socket createSocket(final HttpContext context) throws IOException {
                    return new ProxySocketFactory(bookmark, proxyFinder).disable(Proxy.Type.HTTP).disable(Proxy.Type.HTTPS).createSocket();
                }

                @Override
                public Socket connectSocket(final int connectTimeout,
                                            final Socket socket,
                                            final HttpHost host,
                                            final InetSocketAddress remoteAddress,
                                            final InetSocketAddress localAddress,
                                            final HttpContext context) throws IOException {
                    trust.setTarget(remoteAddress.getHostName());
                    return super.connectSocket(connectTimeout, socket, host, remoteAddress, localAddress, context);
                }
            }
        );
    }
}
