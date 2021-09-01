package ch.cyberduck.core.gmxcloud;/*
 * Copyright (c) 2002-2021 iterate GmbH. All rights reserved.
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

import ch.cyberduck.core.Host;
import ch.cyberduck.core.HostKeyCallback;
import ch.cyberduck.core.HostUrlProvider;
import ch.cyberduck.core.LoginCallback;
import ch.cyberduck.core.MacUniqueIdService;
import ch.cyberduck.core.OAuthTokens;
import ch.cyberduck.core.PreferencesUseragentProvider;
import ch.cyberduck.core.Scheme;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.http.HttpSession;
import ch.cyberduck.core.http.UserAgentHttpRequestInitializer;
import ch.cyberduck.core.proxy.Proxy;
import ch.cyberduck.core.ssl.X509KeyManager;
import ch.cyberduck.core.ssl.X509TrustManager;
import ch.cyberduck.core.threading.CancelCallback;

import org.apache.log4j.Logger;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import com.google.api.client.auth.oauth2.PasswordTokenRequest;
import com.google.api.client.auth.oauth2.TokenResponse;
import com.google.api.client.http.BasicAuthentication;
import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.apache.v2.ApacheHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.migcomponents.migbase64.Base64;

public class GmxcloudSession extends HttpSession<Void> {
    private static final Logger log = Logger.getLogger(GmxcloudSession.class);

    public GmxcloudSession(final Host host, final X509TrustManager trust, final X509KeyManager key) {
        super(host, trust, key);
    }

    @Override
    protected Void connect(final Proxy proxy, final HostKeyCallback key, final LoginCallback prompt, final CancelCallback cancel) throws BackgroundException {
        return null;
    }

    @Override
    public void login(final Proxy proxy, final LoginCallback prompt, final CancelCallback cancel) throws BackgroundException {
        try {
            final List<String> scopes = new ArrayList<>();
            scopes.add("onlinestorage_user_meta_read");
            scopes.add("onlinestorage_user_meta_writed");
            final TokenResponse response = new PasswordTokenRequest(new ApacheHttpTransport(builder.build(proxy, this, prompt).build()),
                new GsonFactory(), new GenericUrl(Scheme.isURL(host.getProtocol().getOAuthTokenUrl()) ? host.getProtocol().getOAuthTokenUrl() : new HostUrlProvider().withUsername(false).withPath(true).get(
                host.getProtocol().getScheme(), host.getPort(), null, host.getHostname(), host.getProtocol().getOAuthTokenUrl())),
                host.getCredentials().getUsername(), host.getCredentials().getPassword()
            )
                .setClientAuthentication(new BasicAuthentication(host.getProtocol().getOAuthClientId(), host.getProtocol().getOAuthClientSecret()))
                .setRequestInitializer(new UserAgentHttpRequestInitializer(new PreferencesUseragentProvider()))
                .setScopes(scopes)
                .set("userAgentB64", Base64.encodeToString(new PreferencesUseragentProvider().get().getBytes(StandardCharsets.UTF_8), false))
                .set("deviceNameB64", Base64.encodeToString(new MacUniqueIdService().getUUID().getBytes(StandardCharsets.UTF_8), false))
                .execute();


            final OAuthTokens tokens = new OAuthTokens(response.getAccessToken(), response.getRefreshToken(), response.getExpiresInSeconds() * 1000);
            host.getCredentials().setOauth(tokens);
            host.getCredentials().setSaved(true);

            // TODO service target auflösen via "https://os-mc.ui-onlinestorage.net/serviceTarget/onlinestorage.qa.mc"
            // curl -v -Hx-ui-api-key:$apikey -Hx-ui-app:curl/1 -H"Authorization: Bearer $accessToken" https://os-webde.ui-onlinestorage.net/serviceTarget/onlinestorage.qa.webde
        }
        catch(IOException e) {
            throw new BackgroundException("Failure running password flow", e);
        }
    }

    @Override
    protected void logout() throws BackgroundException {
        //
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T getFeature(final Class<T> type) {
        return super.getFeature(type);
    }
}
