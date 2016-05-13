package ch.cyberduck.core.dav;

/*
 * Copyright (c) 2002-2011 David Kocher. All rights reserved.
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

import ch.cyberduck.core.AttributedList;
import ch.cyberduck.core.Cache;
import ch.cyberduck.core.ConnectionCallback;
import ch.cyberduck.core.DisabledListProgressListener;
import ch.cyberduck.core.Host;
import ch.cyberduck.core.HostKeyCallback;
import ch.cyberduck.core.HostPasswordStore;
import ch.cyberduck.core.HostUrlProvider;
import ch.cyberduck.core.ListProgressListener;
import ch.cyberduck.core.LocaleFactory;
import ch.cyberduck.core.LoginCallback;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.Scheme;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.exception.LoginCanceledException;
import ch.cyberduck.core.features.Attributes;
import ch.cyberduck.core.features.Copy;
import ch.cyberduck.core.features.Delete;
import ch.cyberduck.core.features.Directory;
import ch.cyberduck.core.features.Headers;
import ch.cyberduck.core.features.Move;
import ch.cyberduck.core.features.Read;
import ch.cyberduck.core.features.Timestamp;
import ch.cyberduck.core.features.Upload;
import ch.cyberduck.core.features.Write;
import ch.cyberduck.core.http.HttpExceptionMappingService;
import ch.cyberduck.core.http.HttpSession;
import ch.cyberduck.core.http.PreferencesRedirectCallback;
import ch.cyberduck.core.http.RedirectCallback;
import ch.cyberduck.core.preferences.Preferences;
import ch.cyberduck.core.preferences.PreferencesFactory;
import ch.cyberduck.core.proxy.ProxyFinder;
import ch.cyberduck.core.shared.DefaultHomeFinderService;
import ch.cyberduck.core.ssl.DefaultX509KeyManager;
import ch.cyberduck.core.ssl.DisabledX509TrustManager;
import ch.cyberduck.core.ssl.ThreadLocalHostnameDelegatingTrustManager;
import ch.cyberduck.core.ssl.X509KeyManager;
import ch.cyberduck.core.ssl.X509TrustManager;
import ch.cyberduck.core.threading.CancelCallback;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.Header;
import org.apache.http.HttpEntityEnclosingRequest;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.ProtocolException;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;
import org.apache.http.client.methods.HttpHead;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.protocol.HttpContext;
import org.apache.log4j.Logger;

import javax.net.SocketFactory;
import java.io.IOException;
import java.net.URL;
import java.text.MessageFormat;

import com.github.sardine.impl.SardineException;
import com.github.sardine.impl.SardineRedirectStrategy;
import com.github.sardine.impl.handler.ValidatingResponseHandler;
import com.github.sardine.impl.handler.VoidResponseHandler;

public class DAVSession extends HttpSession<DAVClient> {
    private static final Logger log = Logger.getLogger(DAVSession.class);

    private RedirectCallback redirect
            = new PreferencesRedirectCallback();

    private Preferences preferences
            = PreferencesFactory.get();

    public DAVSession(final Host host) {
        super(host, new ThreadLocalHostnameDelegatingTrustManager(new DisabledX509TrustManager(), host.getHostname()), new DefaultX509KeyManager());
    }

    public DAVSession(final Host host, final X509TrustManager trust, final X509KeyManager key) {
        super(host, new ThreadLocalHostnameDelegatingTrustManager(trust, host.getHostname()), key);
    }

    public DAVSession(final Host host, final RedirectCallback redirect) {
        super(host, new ThreadLocalHostnameDelegatingTrustManager(new DisabledX509TrustManager(), host.getHostname()), new DefaultX509KeyManager());
        this.redirect = redirect;
    }

    public DAVSession(final Host host, final X509TrustManager trust, final X509KeyManager key, final RedirectCallback redirect) {
        super(host, new ThreadLocalHostnameDelegatingTrustManager(trust, host.getHostname()), key);
        this.redirect = redirect;
    }

    public DAVSession(final Host host, final X509TrustManager trust, final X509KeyManager key, final SocketFactory socketFactory) {
        super(host, new ThreadLocalHostnameDelegatingTrustManager(trust, host.getHostname()), key, socketFactory);
    }

    public DAVSession(final Host host, final X509TrustManager trust, final X509KeyManager key, final ProxyFinder proxy) {
        super(host, new ThreadLocalHostnameDelegatingTrustManager(trust, host.getHostname()), key, proxy);
    }

    public DAVSession(final Host host, final X509TrustManager trust, final X509KeyManager key, final SocketFactory socketFactory, final RedirectCallback redirect) {
        super(host, new ThreadLocalHostnameDelegatingTrustManager(trust, host.getHostname()), key, socketFactory);
        this.redirect = redirect;
    }

    @Override
    public DAVClient connect(final HostKeyCallback key) throws BackgroundException {
        // Always inject new pool to builder on connect because the pool is shutdown on disconnect
        final HttpClientBuilder pool = builder.build(this);
        pool.setRedirectStrategy(new SardineRedirectStrategy() {
            @Override
            protected boolean isRedirectable(final String method) {
                return redirect.redirect(method);
            }

            @Override
            public HttpUriRequest getRedirect(HttpRequest request, HttpResponse response, HttpContext context) throws ProtocolException {
                final String method = request.getRequestLine().getMethod();
                if(method.equalsIgnoreCase(HttpPut.METHOD_NAME)) {
                    return this.copyEntity(new HttpPut(this.getLocationURI(request, response, context)), request);
                }
                return super.getRedirect(request, response, context);
            }

            private HttpUriRequest copyEntity(
                    final HttpEntityEnclosingRequestBase redirect, final HttpRequest original) {
                if(original instanceof HttpEntityEnclosingRequest) {
                    redirect.setEntity(((HttpEntityEnclosingRequest) original).getEntity());
                }
                return redirect;
            }
        });
        return new DAVClient(new HostUrlProvider(false).get(host), pool);
    }

    @Override
    protected void logout() throws BackgroundException {
        try {
            client.shutdown();
        }
        catch(IOException e) {
            throw new HttpExceptionMappingService().map(e);
        }
    }

    @Override
    public void login(final HostPasswordStore keychain, final LoginCallback prompt, final CancelCallback cancel,
                      final Cache<Path> cache) throws BackgroundException {
        client.setCredentials(host.getCredentials().getUsername(), host.getCredentials().getPassword(),
                // Windows credentials. Provide empty string for NTLM domain by default.
                preferences.getProperty("webdav.ntlm.workstation"),
                preferences.getProperty("webdav.ntlm.domain"));
        if(preferences.getBoolean("webdav.basic.preemptive")) {
            // Enable preemptive authentication. See HttpState#setAuthenticationPreemptive
            client.enablePreemptiveAuthentication(host.getHostname(),
                    host.getPort(),
                    host.getPort());
        }
        else {
            client.disablePreemptiveAuthentication();
        }
        if(host.getCredentials().isPassed()) {
            log.warn(String.format("Skip verifying credentials with previous successful authentication event for %s", this));
            return;
        }
        try {
            final Path home = new DefaultHomeFinderService(this).find();
            try {
                client.execute(new HttpHead(new DAVPathEncoder().encode(home)), new VoidResponseHandler());
            }
            catch(SardineException e) {
                switch(e.getStatusCode()) {
                    case HttpStatus.SC_FORBIDDEN:
                    case HttpStatus.SC_NOT_FOUND:
                    case HttpStatus.SC_UNSUPPORTED_MEDIA_TYPE:
                    case HttpStatus.SC_METHOD_NOT_ALLOWED:
                        log.warn(String.format("Failed HEAD request to %s with %s. Retry with PROPFIND.",
                                host, e.getResponsePhrase()));
                        cancel.verify();
                        // Possibly only HEAD requests are not allowed
                        cache.put(home, this.list(home, new DisabledListProgressListener()));
                        break;
                    case HttpStatus.SC_BAD_REQUEST:
                        if(preferences.getBoolean("webdav.basic.preemptive")) {
                            log.warn(String.format("Disable preemptive authentication for %s due to failure %s",
                                    host, e.getResponsePhrase()));
                            cancel.verify();
                            client.disablePreemptiveAuthentication();
                            client.execute(new HttpHead(new DAVPathEncoder().encode(home)), new VoidResponseHandler());
                        }
                        else {
                            throw new DAVExceptionMappingService().map(e);
                        }
                        break;
                    default:
                        throw new DAVExceptionMappingService().map(e);
                }
            }
        }
        catch(SardineException e) {
            throw new DAVExceptionMappingService().map(e);
        }
        catch(IOException e) {
            throw new HttpExceptionMappingService().map(e);
        }
    }

    @Override
    public boolean alert(final ConnectionCallback callback) throws BackgroundException {
        if(super.alert(callback)) {
            // Propose protocol change if HEAD request redirects to HTTPS
            final Path home = new DefaultHomeFinderService(this).find();
            try {
                final RequestConfig context = client.context().getRequestConfig();
                final HttpHead request = new HttpHead(new DAVPathEncoder().encode(home));
                request.setConfig(RequestConfig.copy(context).setRedirectsEnabled(false).build());
                final Header location = client.execute(request, new ValidatingResponseHandler<Header>() {
                    @Override
                    public Header handleResponse(final HttpResponse response) throws ClientProtocolException, IOException {
                        if(response.getStatusLine().getStatusCode() == HttpStatus.SC_MOVED_PERMANENTLY) {
                            return response.getFirstHeader(HttpHeaders.LOCATION);
                        }
                        return null;
                    }
                });
                // Reset default redirect configuration in context
                client.context().setRequestConfig(RequestConfig.copy(context).setRedirectsEnabled(true).build());
                if(null != location) {
                    final URL url = new URL(location.getValue());
                    if(StringUtils.equals(Scheme.https.name(), url.getProtocol())) {
                        try {
                            callback.warn(host.getProtocol(),
                                    MessageFormat.format(LocaleFactory.localizedString("Unsecured {0} connection", "Credentials"), host.getProtocol().getName()),
                                    MessageFormat.format("{0} {1}.", MessageFormat.format(LocaleFactory.localizedString("The server supports encrypted connections. Do you want to switch to {0}?", "Credentials"),
                                            new DAVSSLProtocol().getName()), LocaleFactory.localizedString("Please contact your web hosting service provider for assistance", "Support")),
                                    LocaleFactory.localizedString("Continue", "Credentials"),
                                    LocaleFactory.localizedString("Change", "Credentials"),
                                    String.format("connection.unsecure.%s", host.getHostname()));
                            // Continue chosen. Login using plain FTP.
                        }
                        catch(LoginCanceledException e) {
                            // Protocol switch
                            host.setHostname(url.getHost());
                            host.setProtocol(new DAVSSLProtocol());
                            return false;
                        }
                    }
                }
                // Continue with default alert
            }
            catch(SardineException e) {
                // Ignore failure
                log.warn(String.format("Ignore failed HEAD request to %s with %s.", host, e.getResponsePhrase()));
            }
            catch(IOException e) {
                throw new HttpExceptionMappingService().map(e);
            }
            return preferences.getBoolean("webdav.basic.preemptive");
        }
        return false;
    }

    @Override
    public AttributedList<Path> list(final Path directory, final ListProgressListener listener) throws BackgroundException {
        return new DAVListService(this).list(directory, listener);
    }

    @Override
    public <T> T getFeature(final Class<T> type) {
        if(type == Directory.class) {
            return (T) new DAVDirectoryFeature(this);
        }
        if(type == Read.class) {
            return (T) new DAVReadFeature(this);
        }
        if(type == Write.class) {
            return (T) new DAVWriteFeature(this);
        }
        if(type == Upload.class) {
            return (T) new DAVUploadFeature(this);
        }
        if(type == Delete.class) {
            return (T) new DAVDeleteFeature(this);
        }
        if(type == Move.class) {
            return (T) new DAVMoveFeature(this);
        }
        if(type == Headers.class) {
            return (T) new DAVMetadataFeature(this);
        }
        if(type == Copy.class) {
            return (T) new DAVCopyFeature(this);
        }
        if(type == Attributes.class) {
            return (T) new DAVAttributesFeature(this);
        }
        if(type == Timestamp.class) {
            return (T) new DAVTimestampFeature(this);
        }
        return super.getFeature(type);
    }
}