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

import ch.cyberduck.core.Credentials;
import ch.cyberduck.core.DefaultIOExceptionMappingService;
import ch.cyberduck.core.Host;
import ch.cyberduck.core.HostKeyCallback;
import ch.cyberduck.core.HostPasswordStore;
import ch.cyberduck.core.ListService;
import ch.cyberduck.core.LoginCallback;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PasswordStoreFactory;
import ch.cyberduck.core.PreferencesUseragentProvider;
import ch.cyberduck.core.UrlProvider;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.features.AttributesFinder;
import ch.cyberduck.core.features.Copy;
import ch.cyberduck.core.features.Delete;
import ch.cyberduck.core.features.Directory;
import ch.cyberduck.core.features.Find;
import ch.cyberduck.core.features.Lock;
import ch.cyberduck.core.features.Move;
import ch.cyberduck.core.features.MultipartWrite;
import ch.cyberduck.core.features.Read;
import ch.cyberduck.core.features.Share;
import ch.cyberduck.core.features.Timestamp;
import ch.cyberduck.core.features.Touch;
import ch.cyberduck.core.features.Upload;
import ch.cyberduck.core.features.Write;
import ch.cyberduck.core.http.CustomServiceUnavailableRetryStrategy;
import ch.cyberduck.core.http.HttpSession;
import ch.cyberduck.core.proxy.ProxyFactory;
import ch.cyberduck.core.proxy.ProxyFinder;
import ch.cyberduck.core.shared.DefaultHomeFinderService;
import ch.cyberduck.core.ssl.X509KeyManager;
import ch.cyberduck.core.ssl.X509TrustManager;
import ch.cyberduck.core.threading.CancelCallback;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.net.InetAddress;

public class BrickSession extends HttpSession<BrickApiClient> {
    private static final Logger log = LogManager.getLogger(BrickSession.class);

    private BrickUnauthorizedRetryStrategy retryHandler;
    private ProxyFinder proxy = ProxyFactory.get();
    private final HostPasswordStore store;

    public BrickSession(final Host host, final X509TrustManager trust, final X509KeyManager key) {
        this(host, trust, key, PasswordStoreFactory.get());
    }

    BrickSession(final Host host, final X509TrustManager trust, final X509KeyManager key, final HostPasswordStore store) {
        super(host, trust, key);
        this.store = store;
    }

    @Override
    protected BrickApiClient connect(final ProxyFinder proxy, final HostKeyCallback key, final LoginCallback prompt, final CancelCallback cancel) {
        this.proxy = proxy;
        final HttpClientBuilder configuration = builder.build(proxy, this, prompt);
        configuration.setServiceUnavailableRetryStrategy(new CustomServiceUnavailableRetryStrategy(host));
        configuration.addInterceptorLast(retryHandler = new BrickUnauthorizedRetryStrategy(this, prompt, cancel));
        configuration.addInterceptorLast(new BrickPreferencesRequestInterceptor());
        return new BrickApiClient(host, configuration.build(), retryHandler);
    }

    @Override
    public void login(final LoginCallback prompt, final CancelCallback cancel) throws BackgroundException {
        final Credentials credentials = host.getCredentials();
        if(StringUtils.isBlank(credentials.getPassword())) {
            final String apiKey = store.findLoginPassword(host);
            if(StringUtils.isNotBlank(apiKey)) {
                credentials.setPassword(apiKey).setSaved(false);
            }
        }
        if(credentials.isPasswordAuthentication()) {
            retryHandler.setApiKey(credentials.getPassword());
            // Test credentials
            final Path home = new DefaultHomeFinderService(this).find();
            log.debug("Retrieved {}", home);
        }
        else {
            this.authorize(prompt, cancel);
            retryHandler.setApiKey(credentials.getPassword());
        }
    }

    @Override
    public void disconnect() throws BackgroundException {
        try {
            if(client != null) {
                client.getHttpClient().close();
            }
        }
        finally {
            super.disconnect();
        }
    }

    public Credentials authorize(final LoginCallback prompt, final CancelCallback cancel) throws BackgroundException {
        cancel.verify();
        try(CloseableHttpClient oauth = builder.build(proxy, this, prompt).build()) {
            final BrickOAuth2AuthorizationService service = new BrickOAuth2AuthorizationService(oauth, host,
                    host.getProtocol().getOAuthClientId(), prompt);
            service.setParameter("computer", InetAddress.getLocalHost().getHostName());
            service.setParameter("platform", new PreferencesUseragentProvider().get());
            service.authorize(cancel);
            cancel.verify();
            return host.getCredentials();
        }
        catch(IOException e) {
            throw new DefaultIOExceptionMappingService().map(e);
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T _getFeature(final Class<T> type) {
        if(type == Upload.class) {
            return (T) new BrickThresholdUploadFeature(this);
        }
        if(type == MultipartWrite.class) {
            return (T) new BrickMultipartWriteFeature(this);
        }
        if(type == Write.class) {
            return (T) new BrickWriteFeature(this);
        }
        if(type == Touch.class) {
            return (T) new BrickTouchFeature(this);
        }
        if(type == Timestamp.class) {
            return (T) new BrickTimestampFeature(this);
        }
        if(type == UrlProvider.class) {
            return (T) new BrickUrlProvider(host);
        }
        if(type == ListService.class) {
            return (T) new BrickListService(this);
        }
        if(type == Read.class) {
            return (T) new BrickReadFeature(this);
        }
        if(type == Move.class) {
            return (T) new BrickMoveFeature(this);
        }
        if(type == Copy.class) {
            return (T) new BrickCopyFeature(this);
        }
        if(type == Directory.class) {
            return (T) new BrickDirectoryFeature(this);
        }
        if(type == Delete.class) {
            return (T) new BrickDeleteFeature(this);
        }
        if(type == Find.class) {
            return (T) new BrickFindFeature(this);
        }
        if(type == AttributesFinder.class) {
            return (T) new BrickAttributesFinderFeature(this);
        }
        if(type == Lock.class) {
            return (T) new BrickLockFeature(this);
        }
        if(type == Share.class) {
            return (T) new BrickShareFeature(this);
        }
        return super._getFeature(type);
    }

}
