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

import ch.cyberduck.core.AttributedList;
import ch.cyberduck.core.Cache;
import ch.cyberduck.core.DefaultIOExceptionMappingService;
import ch.cyberduck.core.Host;
import ch.cyberduck.core.HostKeyCallback;
import ch.cyberduck.core.HostPasswordStore;
import ch.cyberduck.core.ListProgressListener;
import ch.cyberduck.core.LocaleFactory;
import ch.cyberduck.core.LoginCallback;
import ch.cyberduck.core.LoginOptions;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PreferencesUseragentProvider;
import ch.cyberduck.core.UrlProvider;
import ch.cyberduck.core.UseragentProvider;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.features.Copy;
import ch.cyberduck.core.features.Delete;
import ch.cyberduck.core.features.Directory;
import ch.cyberduck.core.features.Home;
import ch.cyberduck.core.features.IdProvider;
import ch.cyberduck.core.features.Move;
import ch.cyberduck.core.features.Read;
import ch.cyberduck.core.features.Touch;
import ch.cyberduck.core.features.Upload;
import ch.cyberduck.core.features.Write;
import ch.cyberduck.core.http.HttpSession;
import ch.cyberduck.core.local.BrowserLauncherFactory;
import ch.cyberduck.core.preferences.Preferences;
import ch.cyberduck.core.preferences.PreferencesFactory;
import ch.cyberduck.core.ssl.ThreadLocalHostnameDelegatingTrustManager;
import ch.cyberduck.core.ssl.X509KeyManager;
import ch.cyberduck.core.ssl.X509TrustManager;
import ch.cyberduck.core.threading.CancelCallback;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.log4j.Logger;
import org.jets3t.service.utils.oauth.OAuthConstants;

import java.io.IOException;
import java.net.URI;
import java.util.Collections;

import com.google.api.client.auth.oauth2.AuthorizationCodeFlow;
import com.google.api.client.auth.oauth2.BearerToken;
import com.google.api.client.auth.oauth2.ClientParametersAuthentication;
import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.auth.oauth2.TokenResponse;
import com.google.api.client.googleapis.auth.oauth2.GoogleOAuthConstants;
import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.apache.ApacheHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.DriveScopes;

public class DriveSession extends HttpSession<Drive> {
    private static final Logger log = Logger.getLogger(DriveSession.class);

    private HttpTransport transport;

    private JsonFactory json = new GsonFactory();

    private Credential tokens;

    private Preferences preferences
            = PreferencesFactory.get();

    private final UseragentProvider useragent
            = new PreferencesUseragentProvider();

    public DriveSession(final Host host, final X509TrustManager trust, final X509KeyManager key) {
        super(host, new ThreadLocalHostnameDelegatingTrustManager(trust, host.getHostname()), key);
    }

    @Override
    protected Drive connect(final HostKeyCallback callback) throws BackgroundException {
        final CloseableHttpClient client = builder.build(this).build();
        transport = new ApacheHttpTransport(client);
        json = new GsonFactory();
        return new Drive.Builder(transport, json, new HttpRequestInitializer() {
            @Override
            public void initialize(HttpRequest httpRequest) throws IOException {
                httpRequest.setSuppressUserAgentSuffix(true);
                tokens.initialize(httpRequest);
            }
        })
                .setApplicationName(useragent.get())
                .build();
    }

    @Override
    public void login(final HostPasswordStore keychain, final LoginCallback prompt, final CancelCallback cancel,
                      final Cache<Path> cache) throws BackgroundException {
        final String accesstoken = keychain.getPassword(host.getProtocol().getScheme(),
                host.getPort(), URI.create(OAuthConstants.GSOAuth2_10.Endpoints.Token).getHost(), "Google Drive OAuth2 Access Token");
        final String refreshtoken = keychain.getPassword(host.getProtocol().getScheme(),
                host.getPort(), URI.create(OAuthConstants.GSOAuth2_10.Endpoints.Token).getHost(), "Google Drive OAuth2 Refresh Token");
        if(StringUtils.isEmpty(accesstoken) || StringUtils.isEmpty(refreshtoken)) {
            final AuthorizationCodeFlow flow = new AuthorizationCodeFlow.Builder(
                    BearerToken.authorizationHeaderAccessMethod(),
                    transport, json,
                    new GenericUrl(GoogleOAuthConstants.TOKEN_SERVER_URL),
                    new ClientParametersAuthentication(
                            preferences.getProperty("google.drive.client.id"),
                            preferences.getProperty("google.drive.client.secret")
                    ),
                    preferences.getProperty("google.drive.client.id"),
                    GoogleOAuthConstants.AUTHORIZATION_SERVER_URL)
                    .setScopes(Collections.singletonList(DriveScopes.DRIVE))
                    .build();
            // Direct the user to an authorization page to grant access to their protected data.
            final String url = flow.newAuthorizationUrl()
                    .setRedirectUri(GoogleOAuthConstants.OOB_REDIRECT_URI).build();
            if(preferences.getBoolean("google.drive.oauth.openbrowser")) {
                BrowserLauncherFactory.get().open(url);
            }
            prompt.prompt(host, host.getCredentials(),
                    LocaleFactory.localizedString("OAuth2 Authentication", "Credentials"), url,
                    new LoginOptions().keychain(false).user(false)
            );
            try {
                // Swap the given authorization token for access/refresh tokens
                final TokenResponse response = flow.newTokenRequest(host.getCredentials().getPassword())
                        .setRedirectUri(GoogleOAuthConstants.OOB_REDIRECT_URI).execute();
                tokens = new Credential.Builder(BearerToken.authorizationHeaderAccessMethod())
                        .setTransport(transport)
                        .setClientAuthentication(new ClientParametersAuthentication(
                                preferences.getProperty("google.drive.client.id"),
                                preferences.getProperty("google.drive.client.secret")
                        ))
                        .setTokenServerEncodedUrl(GoogleOAuthConstants.TOKEN_SERVER_URL)
                        .setJsonFactory(json).build()
                        .setFromTokenResponse(response);

                // Save for future use
                keychain.addPassword(host.getProtocol().getScheme(),
                        host.getPort(), URI.create(OAuthConstants.GSOAuth2_10.Endpoints.Token).getHost(),
                        "Google Drive OAuth2 Access Token", tokens.getAccessToken());
                keychain.addPassword(host.getProtocol().getScheme(),
                        host.getPort(), URI.create(OAuthConstants.GSOAuth2_10.Endpoints.Token).getHost(),
                        "Google Drive OAuth2 Refresh Token", tokens.getRefreshToken());

                // Save expiry
                preferences.setProperty("google.storage.oauth.expiry", tokens.getExpirationTimeMilliseconds());
            }
            catch(IOException e) {
                throw new DriveExceptionMappingService().map(e);
            }
        }
        else {
            final Credential.AccessMethod tokens = BearerToken.authorizationHeaderAccessMethod();
            this.tokens = new Credential.Builder(tokens)
                    .setTransport(transport)
                    .setClientAuthentication(new ClientParametersAuthentication(
                            preferences.getProperty("google.drive.client.id"),
                            preferences.getProperty("google.drive.client.secret")
                    ))
                    .setTokenServerEncodedUrl(GoogleOAuthConstants.TOKEN_SERVER_URL)
                    .setJsonFactory(json).build()
                    .setAccessToken(accesstoken)
                    .setRefreshToken(refreshtoken)
                    .setExpirationTimeMilliseconds(preferences.getLong("google.drive.oauth.expiry"));
        }
        if(host.getCredentials().isPassed()) {
            log.warn(String.format("Skip verifying credentials with previous successful authentication event for %s", this));
            return;
        }
        try {
            this.getClient().files().list()
                    .setOauthToken(this.tokens.getAccessToken()).executeUsingHead();
        }
        catch(IOException e) {
            throw new DriveExceptionMappingService().map(e);
        }
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

    @Override
    public AttributedList<Path> list(final Path directory, final ListProgressListener listener) throws BackgroundException {
        return new DriveListService(this).list(directory, listener);
    }

    @Override
    public <T> T getFeature(Class<T> type) {
        if(type == Read.class) {
            return (T) new DriveReadFeature(this);
        }
        if(type == Write.class) {
            return (T) new DriveWriteFeature(this);
        }
        if(type == Upload.class) {
            return (T) new DriveUploadFeature(this);
        }
        if(type == Directory.class) {
            return (T) new DriveDirectoryFeature(this);
        }
        if(type == Delete.class) {
            return (T) new DriveDeleteFeature(this);
        }
        if(type == Move.class) {
            return (T) new DriveMoveFeature(this);
        }
        if(type == Copy.class) {
            return (T) new DriveCopyFeature(this);
        }
        if(type == Touch.class) {
            return (T) new DriveTouchFeature(this);
        }
        if(type == UrlProvider.class) {
            return (T) new DriveUrlProvider(this);
        }
        if(type == Home.class) {
            return (T) new DriveHomeFinderService(this);
        }
        if(type == IdProvider.class) {
            return (T) new DriveFileidProvider(this);
        }
        return super.getFeature(type);
    }

    public String getAccessToken() {
        return tokens.getAccessToken();
    }
}
