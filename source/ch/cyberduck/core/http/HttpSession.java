package ch.cyberduck.core.http;

/*
 * Copyright (c) 2002-2010 David Kocher. All rights reserved.
 *
 * http://cyberduck.ch/
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
 *
 * Bug fixes, suggestions and comments should be sent to:
 * dkocher@cyberduck.ch
 */

import ch.cyberduck.core.Host;
import ch.cyberduck.core.MappingMimeTypeService;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.Preferences;
import ch.cyberduck.core.PreferencesUseragentProvider;
import ch.cyberduck.core.Proxy;
import ch.cyberduck.core.ProxyFactory;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.ssl.CustomTrustSSLProtocolSocketFactory;
import ch.cyberduck.core.ssl.SSLSession;
import ch.cyberduck.core.threading.NamedThreadFactory;

import org.apache.http.Header;
import org.apache.http.HttpException;
import org.apache.http.HttpHost;
import org.apache.http.HttpRequest;
import org.apache.http.HttpRequestInterceptor;
import org.apache.http.HttpResponse;
import org.apache.http.HttpResponseInterceptor;
import org.apache.http.auth.params.AuthParams;
import org.apache.http.client.params.CookiePolicy;
import org.apache.http.client.params.HttpClientParams;
import org.apache.http.client.protocol.RequestAcceptEncoding;
import org.apache.http.client.protocol.ResponseContentEncoding;
import org.apache.http.conn.params.ConnRouteParams;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.conn.ssl.X509HostnameVerifier;
import org.apache.http.impl.client.AbstractHttpClient;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.PoolingClientConnectionManager;
import org.apache.http.message.BasicHeader;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;
import org.apache.http.protocol.ExecutionContext;
import org.apache.http.protocol.HTTP;
import org.apache.http.protocol.HttpContext;
import org.apache.log4j.Logger;

import javax.net.ssl.SSLException;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.X509TrustManager;
import java.io.IOException;
import java.io.OutputStream;
import java.security.cert.X509Certificate;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ThreadFactory;

/**
 * @version $Id$
 */
public abstract class HttpSession<C> extends SSLSession<C> {
    private static final Logger log = Logger.getLogger(HttpSession.class);

    /**
     * Target hostname of current request stored as thread local
     */
    private ThreadLocal<String> target
            = new ThreadLocal<String>();

    protected AbstractHttpClient route;

    private final ThreadFactory factory
            = new NamedThreadFactory("http");

    protected HttpSession(final Host h) {
        super(h);
    }

    protected HttpSession(final Host host, final X509TrustManager manager) {
        super(host, manager);
    }

    public AbstractHttpClient connect() {
        final HttpParams params = this.parameters();
        final SchemeRegistry registry = new SchemeRegistry();
        // Always register HTTP for possible use with proxy. Contains a number of protocol properties such as the default port and the socket
        // factory to be used to create the java.net.Socket instances for the given protocol
        registry.register(new Scheme(ch.cyberduck.core.Scheme.http.toString(), ch.cyberduck.core.Scheme.http.getPort(),
                PlainSocketFactory.getSocketFactory()));
        registry.register(new Scheme(ch.cyberduck.core.Scheme.https.toString(), ch.cyberduck.core.Scheme.https.getPort(),
                new SSLSocketFactory(
                        new CustomTrustSSLProtocolSocketFactory(this.getTrustManager()).getSSLContext(),
                        new X509HostnameVerifier() {
                            @Override
                            public void verify(String host, SSLSocket ssl) throws IOException {
                                log.debug(String.format("Hostname verification disabled for %s handled in system trust manager", host));
                            }

                            @Override
                            public void verify(String host, X509Certificate cert) throws SSLException {
                                log.debug(String.format("Hostname verification disabled for %s handled in system trust manager", host));
                            }

                            @Override
                            public void verify(String host, String[] cns, String[] subjectAlts) throws SSLException {
                                log.debug(String.format("Hostname verification disabled for %s handled in system trust manager", host));
                            }

                            @Override
                            public boolean verify(String s, javax.net.ssl.SSLSession sslSession) {
                                log.debug(String.format("Hostname verification disabled for %s handled in system trust manager", host));
                                return true;
                            }
                        }
                )));
        if(Preferences.instance().getBoolean("connection.proxy.enable")) {
            this.proxy(params);
        }
        final PoolingClientConnectionManager manager = this.pool(registry);
        route = new DefaultHttpClient(manager, params);
        route.addRequestInterceptor(new HttpRequestInterceptor() {
            @Override
            public void process(final HttpRequest request, final HttpContext context) throws HttpException, IOException {
                target.set(
                        ((HttpHost) context.getAttribute(ExecutionContext.HTTP_TARGET_HOST)).getHostName());
            }
        });
        route.addRequestInterceptor(new HttpRequestInterceptor() {
            @Override
            public void process(final HttpRequest request, final HttpContext context) throws HttpException, IOException {
                log(true, request.getRequestLine().toString());
                for(Header header : request.getAllHeaders()) {
                    log(true, header.toString());
                }
            }
        });
        route.addResponseInterceptor(new HttpResponseInterceptor() {
            @Override
            public void process(final HttpResponse response, final HttpContext context) throws HttpException, IOException {
                log(false, response.getStatusLine().toString());
                for(Header header : response.getAllHeaders()) {
                    log(false, header.toString());
                }
            }
        });
        if(Preferences.instance().getBoolean("http.compression.enable")) {
            route.addRequestInterceptor(new RequestAcceptEncoding());
            route.addResponseInterceptor(new ResponseContentEncoding());
        }
        return route;
    }

    protected PoolingClientConnectionManager pool(final SchemeRegistry registry) {
        final PoolingClientConnectionManager manager = new PoolingClientConnectionManager(registry);
        manager.setMaxTotal(Preferences.instance().getInteger("http.connections.total"));
        manager.setDefaultMaxPerRoute(Preferences.instance().getInteger("http.connections.route"));
        return manager;
    }

    protected void proxy(final HttpParams params) {
        final Proxy proxy = ProxyFactory.get();
        if(ch.cyberduck.core.Scheme.https.equals(this.getHost().getProtocol().getScheme())) {
            if(proxy.isHTTPSProxyEnabled(host)) {
                ConnRouteParams.setDefaultProxy(params, new HttpHost(proxy.getHTTPSProxyHost(host), proxy.getHTTPSProxyPort(host)));
            }
        }
        if(ch.cyberduck.core.Scheme.http.equals(this.getHost().getProtocol().getScheme())) {
            if(proxy.isHTTPProxyEnabled(host)) {
                ConnRouteParams.setDefaultProxy(params, new HttpHost(proxy.getHTTPProxyHost(host), proxy.getHTTPProxyPort(host)));
            }
        }
    }

    protected HttpParams parameters() {
        final HttpParams params = new BasicHttpParams();
        HttpProtocolParams.setVersion(params, org.apache.http.HttpVersion.HTTP_1_1);
        HttpProtocolParams.setContentCharset(params, getEncoding());
        HttpProtocolParams.setUserAgent(params, new PreferencesUseragentProvider().get());

        AuthParams.setCredentialCharset(params, Preferences.instance().getProperty("http.credentials.charset"));

        HttpConnectionParams.setTcpNoDelay(params, true);
        HttpConnectionParams.setSoTimeout(params, timeout());
        HttpConnectionParams.setConnectionTimeout(params, timeout());
        HttpConnectionParams.setSocketBufferSize(params,
                Preferences.instance().getInteger("http.socket.buffer"));
        HttpConnectionParams.setStaleCheckingEnabled(params, true);

        HttpClientParams.setRedirecting(params, true);
        HttpClientParams.setAuthenticating(params, true);
        HttpClientParams.setCookiePolicy(params, CookiePolicy.BEST_MATCH);

        // Sets the timeout in milliseconds used when retrieving a connection from the ClientConnectionManager
        HttpClientParams.setConnectionManagerTimeout(params, Preferences.instance().getLong("http.manager.timeout"));
        return params;
    }

    @Override
    protected void logout() throws BackgroundException {
        route.getConnectionManager().shutdown();
    }

    @Override
    public String getTarget() {
        return target.get();
    }

    private abstract class FutureHttpResponse<T> implements Runnable {

        Exception exception;
        T response;

        public Exception getException() {
            return exception;
        }

        public T getResponse() {
            return response;
        }
    }

    /**
     * @param command Callable writing entity to stream and returning checksum
     * @param <T>     Type of returned checksum
     * @return Outputstream to write entity into.
     */
    public <T> ResponseOutputStream<T> write(final Path file, final DelayedHttpEntityCallable<T> command)
            throws BackgroundException {
        /**
         * Signal on enter streaming
         */
        final CountDownLatch entry = new CountDownLatch(1);
        final CountDownLatch exit = new CountDownLatch(1);

        try {
            final DelayedHttpEntity entity = new DelayedHttpEntity(entry) {
                @Override
                public long getContentLength() {
                    return command.getContentLength();
                }
            };
            final String type = new MappingMimeTypeService().getMime(file.getName());
            entity.setContentType(new BasicHeader(HTTP.CONTENT_TYPE, type));
            final FutureHttpResponse<T> target = new FutureHttpResponse<T>() {
                @Override
                public void run() {
                    try {
                        response = command.call(entity);
                    }
                    catch(BackgroundException e) {
                        exception = e;
                    }
                    finally {
                        // For zero byte files #writeTo is never called and the entry latch not triggered
                        entry.countDown();
                        // Continue reading the response
                        exit.countDown();
                    }
                }
            };
            final Thread t = factory.newThread(target);
            t.start();
            // Wait for output stream to become available
            entry.await();
            if(null != target.getException()) {
                if(target.getException() instanceof BackgroundException) {
                    throw (BackgroundException) target.getException();
                }
                throw new BackgroundException(target.getException());
            }
            final OutputStream stream = entity.getStream();
            return new ResponseOutputStream<T>(stream) {
                /**
                 * Only available after this stream is closed.
                 * @return Response from server for upload
                 */
                @Override
                public T getResponse() throws BackgroundException {
                    try {
                        // Block the calling thread until after the full response from the server
                        // has been consumed.
                        exit.await();
                    }
                    catch(InterruptedException e) {
                        throw new BackgroundException(e);
                    }
                    if(null != target.getException()) {
                        if(target.getException() instanceof BackgroundException) {
                            throw (BackgroundException) target.getException();
                        }
                        throw new BackgroundException(target.getException());
                    }
                    return target.getResponse();
                }
            };
        }
        catch(InterruptedException e) {
            log.error("Error waiting for output stream");
            throw new BackgroundException(e);
        }
    }
}