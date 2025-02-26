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
import ch.cyberduck.core.exception.ConnectionCanceledException;
import ch.cyberduck.core.exception.HostParserException;
import ch.cyberduck.core.features.*;
import ch.cyberduck.core.http.CustomServiceUnavailableRetryStrategy;
import ch.cyberduck.core.http.DefaultHttpRateLimiter;
import ch.cyberduck.core.http.ExecutionCountServiceUnavailableRetryStrategy;
import ch.cyberduck.core.http.HttpSession;
import ch.cyberduck.core.http.RateLimitingHttpRequestInterceptor;
import ch.cyberduck.core.http.UserAgentHttpRequestInitializer;
import ch.cyberduck.core.oauth.OAuth2ErrorResponseInterceptor;
import ch.cyberduck.core.oauth.OAuth2RequestInterceptor;
import ch.cyberduck.core.preferences.HostPreferencesFactory;
import ch.cyberduck.core.proxy.ProxyFinder;
import ch.cyberduck.core.ssl.X509KeyManager;
import ch.cyberduck.core.ssl.X509TrustManager;
import ch.cyberduck.core.threading.CancelCallback;

import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;

import com.google.api.client.http.apache.v2.ApacheHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.About;

public class DriveSession extends HttpSession<Drive> {
    private static final Logger log = LogManager.getLogger(DriveSession.class);

    private ApacheHttpTransport transport;
    private OAuth2RequestInterceptor authorizationService;

    private final DriveFileIdProvider fileid = new DriveFileIdProvider(this);

    public DriveSession(final Host host, final X509TrustManager trust, final X509KeyManager key) {
        super(host, trust, key);
    }

    @Override
    protected Drive connect(final ProxyFinder proxy, final HostKeyCallback callback, final LoginCallback prompt, final CancelCallback cancel) throws HostParserException, ConnectionCanceledException {
        final HttpClientBuilder configuration = builder.build(proxy, this, prompt);
        authorizationService = new OAuth2RequestInterceptor(configuration.build(), host, prompt)
                .withRedirectUri(host.getProtocol().getOAuthRedirectUrl());
        configuration.addInterceptorLast(authorizationService);
        configuration.setServiceUnavailableRetryStrategy(new CustomServiceUnavailableRetryStrategy(host,
                new ExecutionCountServiceUnavailableRetryStrategy(new OAuth2ErrorResponseInterceptor(host, authorizationService))));
        if(HostPreferencesFactory.get(host).getBoolean("googledrive.limit.requests.enable")) {
            configuration.addInterceptorLast(new RateLimitingHttpRequestInterceptor(new DefaultHttpRateLimiter(
                    HostPreferencesFactory.get(host).getInteger("googledrive.limit.requests.second")
            )));
        }
        transport = new ApacheHttpTransport(configuration.build());
        final UseragentProvider ua = new PreferencesUseragentProvider();
        return new Drive.Builder(transport, new GsonFactory(), new UserAgentHttpRequestInitializer(ua))
                .setApplicationName(ua.get())
                .build();
    }

    @Override
    public void login(final LoginCallback prompt, final CancelCallback cancel) throws BackgroundException {
        final Credentials credentials = authorizationService.validate();
        final About about;
        try {
            about = client.about().get().setFields("user").execute();
        }
        catch(IOException e) {
            throw new DriveExceptionMappingService(fileid).map(e);
        }
        log.debug("Authenticated as user {}", about.getUser());
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
        finally {
            fileid.clear();
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
            return (T) new DriveUploadFeature(this, fileid);
        }
        if(type == Directory.class) {
            return (T) new DriveDirectoryFeature(this, fileid);
        }
        if(type == Delete.class) {
            return (T) new DriveThresholdDeleteFeature(this, fileid);
        }
        if(type == Trash.class) {
            return (T) new DriveThresholdTrashFeature(this, fileid);
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
        if(type == Share.class) {
            return (T) new DriveSharingUrlProvider(this, fileid);
        }
        if(type == FileIdProvider.class) {
            return (T) fileid;
        }
        if(type == Quota.class) {
            return (T) new DriveQuotaFeature(this, fileid);
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
        if(type == Versioning.class) {
            return (T) new DriveVersioningFeature(this, fileid);
        }
        return super._getFeature(type);
    }

}
