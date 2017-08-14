package ch.cyberduck.core.b2;

/*
 * Copyright (c) 2002-2016 iterate GmbH. All rights reserved.
 * https://cyberduck.io/
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

import ch.cyberduck.core.AttributedList;
import ch.cyberduck.core.Cache;
import ch.cyberduck.core.DefaultIOExceptionMappingService;
import ch.cyberduck.core.Host;
import ch.cyberduck.core.HostKeyCallback;
import ch.cyberduck.core.HostPasswordStore;
import ch.cyberduck.core.ListProgressListener;
import ch.cyberduck.core.LoginCallback;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.UrlProvider;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.features.*;
import ch.cyberduck.core.http.HttpSession;
import ch.cyberduck.core.proxy.ProxyFinder;
import ch.cyberduck.core.ssl.DefaultX509KeyManager;
import ch.cyberduck.core.ssl.DisabledX509TrustManager;
import ch.cyberduck.core.ssl.ThreadLocalHostnameDelegatingTrustManager;
import ch.cyberduck.core.ssl.X509KeyManager;
import ch.cyberduck.core.ssl.X509TrustManager;
import ch.cyberduck.core.threading.CancelCallback;

import org.apache.http.impl.client.HttpClientBuilder;

import javax.net.SocketFactory;
import java.io.IOException;

import synapticloop.b2.B2ApiClient;
import synapticloop.b2.exception.B2ApiException;

public class B2Session extends HttpSession<B2ApiClient> {

    private final B2ErrorResponseInterceptor retryHandler = new B2ErrorResponseInterceptor(
            this);

    public B2Session(final Host host) {
        super(host, new ThreadLocalHostnameDelegatingTrustManager(new DisabledX509TrustManager(), host.getHostname()), new DefaultX509KeyManager());
    }

    public B2Session(final Host host, final X509TrustManager trust, final X509KeyManager key) {
        super(host, new ThreadLocalHostnameDelegatingTrustManager(trust, host.getHostname()), key);
    }

    public B2Session(final Host host, final X509TrustManager trust, final X509KeyManager key, final ProxyFinder proxyFinder) {
        super(host, new ThreadLocalHostnameDelegatingTrustManager(trust, host.getHostname()), key, proxyFinder);
    }

    public B2Session(final Host host, final X509TrustManager trust, final X509KeyManager key, final SocketFactory socketFactory) {
        super(host, new ThreadLocalHostnameDelegatingTrustManager(trust, host.getHostname()), key, socketFactory);
    }

    @Override
    public B2ApiClient connect(final HostKeyCallback key) throws BackgroundException {
        final HttpClientBuilder configuration = builder.build(this);
        configuration.setServiceUnavailableRetryStrategy(retryHandler);
        configuration.addInterceptorLast(retryHandler);
        return new B2ApiClient(configuration.build());
    }

    @Override
    public void logout() throws BackgroundException {
        try {
            client.close();
        }
        catch(IOException e) {
            throw new DefaultIOExceptionMappingService().map(e);
        }
    }

    @Override
    public AttributedList<Path> list(final Path directory, final ListProgressListener listener) throws BackgroundException {
        return new B2ListService(this, new B2FileidProvider(this)).list(directory, listener);
    }

    @Override
    public void login(final HostPasswordStore keychain, final LoginCallback prompt, final CancelCallback cancel,
                      final Cache<Path> cache) throws BackgroundException {
        try {
            final String accountId = host.getCredentials().getUsername();
            final String applicationKey = host.getCredentials().getPassword();
            // Save tokens for 401 error response when expired
            retryHandler.setTokens(accountId, applicationKey, client.authenticate(accountId, applicationKey).getAuthorizationToken());
        }
        catch(B2ApiException e) {
            throw new B2ExceptionMappingService().map(e);
        }
        catch(IOException e) {
            throw new DefaultIOExceptionMappingService().map(e);
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T _getFeature(final Class<T> type) {
        if(type == Touch.class) {
            return (T) new B2TouchFeature(this);
        }
        if(type == Read.class) {
            return (T) new B2ReadFeature(this);
        }
        if(type == Upload.class) {
            return (T) new B2ThresholdUploadService(this);
        }
        if(type == MultipartWrite.class) {
            return (T) new B2LargeUploadWriteFeature(this);
        }
        if(type == Write.class) {
            return (T) new B2WriteFeature(this);
        }
        if(type == Directory.class) {
            return (T) new B2DirectoryFeature(this);
        }
        if(type == Delete.class) {
            return (T) new B2DeleteFeature(this);
        }
        if(type == UrlProvider.class) {
            return (T) new B2UrlProvider(this);
        }
        if(type == Find.class) {
            return (T) new B2FindFeature(this);
        }
        if(type == AttributesFinder.class) {
            return (T) new B2AttributesFinderFeature(this);
        }
        if(type == Home.class) {
            return (T) new B2HomeFinderService(this);
        }
        if(type == AclPermission.class) {
            return (T) new B2BucketTypeFeature(this);
        }
        if(type == Location.class) {
            return (T) new B2BucketTypeFeature(this);
        }
        if(type == IdProvider.class) {
            return (T) new B2FileidProvider(this);
        }
        if(type == AttributesFinder.class) {
            return (T) new B2AttributesFinderFeature(this);
        }
        if(type == Lifecycle.class) {
            return (T) new B2LifecycleFeature(this);
        }
        if(type == Search.class) {
            return (T) new B2SearchFeature(this);
        }
        return super._getFeature(type);
    }
}
