package ch.cyberduck.core.onedrive;

/*
 * Copyright (c) 2002-2017 iterate GmbH. All rights reserved.
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
import ch.cyberduck.core.PathContainerService;
import ch.cyberduck.core.URIEncoder;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.exception.LoginFailureException;
import ch.cyberduck.core.http.HttpSession;
import ch.cyberduck.core.oauth.OAuth2AuthorizationService;
import ch.cyberduck.core.ssl.ThreadLocalHostnameDelegatingTrustManager;
import ch.cyberduck.core.ssl.X509KeyManager;
import ch.cyberduck.core.ssl.X509TrustManager;
import ch.cyberduck.core.threading.CancelCallback;

import org.apache.log4j.Logger;
import org.nuxeo.onedrive.client.OneDriveAPI;
import org.nuxeo.onedrive.client.OneDriveFolder;

import java.io.IOException;
import java.util.Arrays;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.apache.ApacheHttpTransport;

public class OneDriveSession extends HttpSession<OneDriveAPI> {
    private static final Logger log = Logger.getLogger(OneDriveSession.class);

    private HttpTransport transport;
    private Credential credential;

    private OAuth2AuthorizationService authorizationService;

    public OneDriveSession(final Host host, final X509TrustManager trust, final X509KeyManager key) {
        super(host, new ThreadLocalHostnameDelegatingTrustManager(trust, host.getHostname()), key);
    }

    @Override
    protected OneDriveAPI connect(final HostKeyCallback key) throws BackgroundException {
        this.transport = new ApacheHttpTransport(builder.build(this).build());
        this.authorizationService = new OAuth2AuthorizationService(transport,
                "https://login.live.com/oauth20_token.srf", "https://login.live.com/oauth20_authorize.srf",
                "372770ba-bb24-436b-bbd4-19bc86310c0e",
                "mJjWVkmfD9FVHNFTpbrdowv",
                Arrays.asList("onedrive.readwrite", "wl.offline_access"))
                .withRedirectUri("https://cyberduck.io/oauth");

        return new OneDriveAPI() {
            @Override
            public boolean isBusinessConnection() {
                return false;
            }

            @Override
            public String getBaseURL() {
                return "https://api.onedrive.com/v1.0";
            }

            @Override
            public String getEmailURL() {
                return "https://apis.live.net/v5.0/me";
            }

            @Override
            public String getAccessToken() {
                return credential.getAccessToken();
            }
        };
    }

    @Override
    public void login(final HostPasswordStore keychain, final LoginCallback prompt, final CancelCallback cancel, final Cache<Path> cache) throws BackgroundException {
        final OAuth2AuthorizationService.Tokens tokens = authorizationService.find(keychain, host);
        this.login(keychain, prompt, cancel, cache, tokens);
    }

    private void login(final HostPasswordStore keychain, final LoginCallback prompt, final CancelCallback cancel, final Cache<Path> cache, final OAuth2AuthorizationService.Tokens tokens) throws BackgroundException {
        credential = authorizationService.authorize(host, keychain, prompt, cancel, tokens);
        if(host.getCredentials().isPassed()) {
            log.warn(String.format("Skip verifying credentials with previous successful authentication event for %s", this));
            return;
        }
        try {
            OneDriveFolder folder = OneDriveFolder.getRoot(client);
            OneDriveFolder.Metadata metadata = folder.getMetadata();
        }
        catch(IOException e) {
            try {
                throw new LoginFailureException("Fail");
                // throw new DriveExceptionMappingService().map(e);
            }
            catch(LoginFailureException f) {
                this.login(keychain, prompt, cancel, cache, OAuth2AuthorizationService.Tokens.EMPTY);
            }
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
        return new OneDriveListService(this).list(directory, listener);
    }

    public void resolveDriveQueryPath(final Path file, final StringBuilder builder) {
        PathContainerService pathContainerService = new PathContainerService();

        builder.append("/drives"); // query single drive

        if(!file.isRoot()) {
            Path driveId = pathContainerService.getContainer(file); // using pathContainerService for retrieving current drive id
            builder.append(String.format("/%s", driveId.getName()));

            if(!pathContainerService.isContainer(file)) {
                // append path to item via pathContainerService with format :/path:
                builder.append(String.format("/root:/%s:", URIEncoder.encode(pathContainerService.getKey(file))));
            }
        }
    }
}
