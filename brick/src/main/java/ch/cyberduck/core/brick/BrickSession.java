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
import ch.cyberduck.core.Host;
import ch.cyberduck.core.HostKeyCallback;
import ch.cyberduck.core.ListService;
import ch.cyberduck.core.LoginCallback;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PreferencesUseragentProvider;
import ch.cyberduck.core.UrlProvider;
import ch.cyberduck.core.exception.AccessDeniedException;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.exception.LoginFailureException;
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
import ch.cyberduck.core.proxy.ProxyFinder;
import ch.cyberduck.core.shared.DefaultHomeFinderService;
import ch.cyberduck.core.ssl.X509KeyManager;
import ch.cyberduck.core.ssl.X509TrustManager;
import ch.cyberduck.core.threading.CancelCallback;

import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class BrickSession extends HttpSession<BrickApiClient> {
    private static final Logger log = LogManager.getLogger(BrickSession.class);

    private BrickOAuth2AuthorizationService authorizationService;

    public BrickSession(final Host host, final X509TrustManager trust, final X509KeyManager key) {
        super(host, trust, key);
    }

    @Override
    protected BrickApiClient connect(final ProxyFinder proxy, final HostKeyCallback key, final LoginCallback prompt, final CancelCallback cancel) throws BackgroundException {
        final HttpClientBuilder configuration = builder.build(proxy, this, prompt);
        authorizationService = new BrickOAuth2AuthorizationService(configuration.build(), host, prompt, cancel);
        authorizationService.setParameter("platform", new PreferencesUseragentProvider().get());
        configuration.setServiceUnavailableRetryStrategy(new CustomServiceUnavailableRetryStrategy(host,
                new BrickUnauthorizedRetryStrategy(host, authorizationService, cancel)));
        configuration.addInterceptorLast(authorizationService);
        configuration.addInterceptorLast(new BrickPreferencesRequestInterceptor());
        return new BrickApiClient(host, configuration.build());
    }

    @Override
    public void login(final LoginCallback prompt, final CancelCallback cancel) throws BackgroundException {
        final Credentials credentials = host.getCredentials();
        if(!credentials.isPasswordAuthentication()) {
            credentials.setOauth(authorizationService.validate(credentials.getOauth(), cancel));
        }
        try {
            final Path home = new DefaultHomeFinderService(this).find();
            log.debug("Retrieved {}", home);
        }
        catch(LoginFailureException e) {
            if(credentials.isPasswordAuthentication()) {
                // Explicit API key rejected. Report the failure without the OAuth retry prompt.
                throw new AccessDeniedException(e.getMessage(), e.getDetail(), e);
            }
            throw e;
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
