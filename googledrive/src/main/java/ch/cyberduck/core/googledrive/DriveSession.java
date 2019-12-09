package ch.cyberduck.core.googledrive;

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

import ch.cyberduck.core.Credentials;
import ch.cyberduck.core.DefaultIOExceptionMappingService;
import ch.cyberduck.core.Host;
import ch.cyberduck.core.HostKeyCallback;
import ch.cyberduck.core.ListService;
import ch.cyberduck.core.LoginCallback;
import ch.cyberduck.core.PreferencesUseragentProvider;
import ch.cyberduck.core.UrlProvider;
import ch.cyberduck.core.UseragentProvider;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.features.*;
import ch.cyberduck.core.http.DefaultHttpRateLimiter;
import ch.cyberduck.core.http.HttpSession;
import ch.cyberduck.core.http.RateLimitingHttpRequestInterceptor;
import ch.cyberduck.core.oauth.OAuth2ErrorResponseInterceptor;
import ch.cyberduck.core.oauth.OAuth2RequestInterceptor;
import ch.cyberduck.core.preferences.PreferencesFactory;
import ch.cyberduck.core.proxy.Proxy;
import ch.cyberduck.core.ssl.X509KeyManager;
import ch.cyberduck.core.ssl.X509TrustManager;
import ch.cyberduck.core.threading.CancelCallback;

import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.log4j.Logger;

import java.io.IOException;

import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.client.http.apache.v2.ApacheHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.About;

public class DriveSession extends HttpSession<Drive> {
    private static final Logger log = Logger.getLogger(DriveSession.class);

    private ApacheHttpTransport transport;

    private final UseragentProvider useragent
        = new PreferencesUseragentProvider();

    private OAuth2RequestInterceptor authorizationService;

    private final DriveFileidProvider fileid = new DriveFileidProvider(this);

    public DriveSession(final Host host, final X509TrustManager trust, final X509KeyManager key) {
        super(host, trust, key);
    }

    @Override
    protected Drive connect(final Proxy proxy, final HostKeyCallback callback, final LoginCallback prompt) {
        final HttpClientBuilder configuration = builder.build(proxy, this, prompt);
        authorizationService = new OAuth2RequestInterceptor(configuration.build(), host.getProtocol())
            .withRedirectUri(host.getProtocol().getOAuthRedirectUrl());
        configuration.addInterceptorLast(authorizationService);
        configuration.setServiceUnavailableRetryStrategy(new OAuth2ErrorResponseInterceptor(host, authorizationService, prompt));
        configuration.addInterceptorLast(new RateLimitingHttpRequestInterceptor(new DefaultHttpRateLimiter(
            PreferencesFactory.get().getInteger("googledrive.limit.requests.second")
        )));
        this.transport = new ApacheHttpTransport(configuration.build());
        return new Drive.Builder(transport, new JacksonFactory(), new HttpRequestInitializer() {
            @Override
            public void initialize(HttpRequest request) {
                request.setSuppressUserAgentSuffix(true);
                // OAuth Bearer added in interceptor
            }
        })
            .setApplicationName(useragent.get())
            .build();
    }

    @Override
    public void login(final Proxy proxy, final LoginCallback prompt, final CancelCallback cancel) throws BackgroundException {
        authorizationService.setTokens(authorizationService.authorize(host, prompt, cancel));
        final About about;
        try {
            about = client.about().get().setFields("user").execute();
        }
        catch(IOException e) {
            throw new DriveExceptionMappingService().map(e);
        }
        if(log.isDebugEnabled()) {
            log.debug(String.format("Authenticated as user %s", about.getUser()));
        }
        final Credentials credentials = host.getCredentials();
        credentials.setUsername(about.getUser().getEmailAddress());
    }

    @Override
    protected void logout() throws BackgroundException {
        try {
            transport.shutdown();
        }
        catch(IOException e) {
            throw new DefaultIOExceptionMappingService().map(e);
        }
    }

    public HttpClient getHttpClient() {
        return transport.getHttpClient();
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T _getFeature(Class<T> type) {
        if(type == ListService.class) {
            return (T) new DriveListService(this, fileid);
        }
        if(type == Read.class) {
            return (T) new DriveReadFeature(this, fileid);
        }
        if(type == Write.class) {
            return (T) new DriveWriteFeature(this, fileid);
        }
        if(type == Upload.class) {
            return (T) new DriveUploadFeature(new DriveWriteFeature(this, fileid));
        }
        if(type == Directory.class) {
            return (T) new DriveDirectoryFeature(this, fileid);
        }
        if(type == Delete.class) {
            return (T) new DriveBatchDeleteFeature(this, fileid);
        }
        if(type == Move.class) {
            return (T) new DriveMoveFeature(this, fileid);
        }
        if(type == Copy.class) {
            return (T) new DriveCopyFeature(this, fileid);
        }
        if(type == Touch.class) {
            return (T) new DriveTouchFeature(this, fileid);
        }
        if(type == UrlProvider.class) {
            return (T) new DriveUrlProvider();
        }
        if(type == PromptUrlProvider.class) {
            return (T) new DriveSharingUrlProvider(this, fileid);
        }
        if(type == Home.class) {
            return (T) new DriveHomeFinderService(this);
        }
        if(type == IdProvider.class) {
            return (T) fileid;
        }
        if(type == Quota.class) {
            return (T) new DriveQuotaFeature(this);
        }
        if(type == Timestamp.class) {
            return (T) new DriveTimestampFeature(this, fileid);
        }
        if(type == Metadata.class) {
            return (T) new DriveMetadataFeature(this, fileid);
        }
        if(type == Search.class) {
            return (T) new DriveSearchFeature(this, fileid);
        }
        if(type == Find.class) {
            return (T) new DriveFindFeature(this, fileid);
        }
        if(type == AttributesFinder.class) {
            return (T) new DriveAttributesFinderFeature(this, fileid);
        }
        return super._getFeature(type);
    }

}
