package ch.cyberduck.core.sso;

/*
 * Copyright (c) 2002-2026 iterate GmbH. All rights reserved.
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
import ch.cyberduck.core.LocaleFactory;
import ch.cyberduck.core.LoginCallback;
import ch.cyberduck.core.OAuthTokens;
import ch.cyberduck.core.PreferencesUseragentProvider;
import ch.cyberduck.core.Profile;
import ch.cyberduck.core.aws.AmazonServiceExceptionMappingService;
import ch.cyberduck.core.aws.CustomClientConfiguration;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.exception.LoginCanceledException;
import ch.cyberduck.core.oauth.OAuth2AuthorizationService;
import ch.cyberduck.core.oauth.OAuth2RequestInterceptor;
import ch.cyberduck.core.ssl.ThreadLocalHostnameDelegatingTrustManager;
import ch.cyberduck.core.ssl.X509KeyManager;
import ch.cyberduck.core.ssl.X509TrustManager;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpRequest;
import org.apache.http.client.HttpClient;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.amazonaws.AmazonClientException;
import com.amazonaws.services.ssooidc.AWSSSOOIDC;
import com.amazonaws.services.ssooidc.AWSSSOOIDCClientBuilder;
import com.amazonaws.services.ssooidc.model.RegisterClientRequest;
import com.amazonaws.services.ssooidc.model.RegisterClientResult;

public class RegisterClientOAuth2RequestInterceptor extends OAuth2RequestInterceptor {
    private static final Logger log = LogManager.getLogger(RegisterClientOAuth2RequestInterceptor.class);

    private final Host host;
    private final X509TrustManager trust;
    private final X509KeyManager key;

    private final String startUrl;
    private final String issuerUrl;

    private String clientId = null;
    private Long clientIdExpiry = -1L;

    public RegisterClientOAuth2RequestInterceptor(final HttpClient client, final Host host,
                                                  final X509TrustManager trust, final X509KeyManager key, final LoginCallback prompt) throws LoginCanceledException {
        super(client, host, null, null, null, null, host.getProtocol().getOAuthScopes(), true, prompt);
        this.host = host;
        this.trust = trust;
        this.key = key;
        this.startUrl = prompt(host, prompt, Profile.SSO_START_URL_KEY, LocaleFactory.localizedString(
                "SSO Start Url", "Credentials"), host.getProperty(Profile.SSO_START_URL_KEY));
        this.issuerUrl = startUrl;
    }

    @Override
    public OAuth2AuthorizationService setClientid(final String clientid) {
        this.clientId = clientid;
        return super.setClientid(clientid);
    }


    /**
     * Registers a public client with IAM Identity Center. This allows clients to perform authorization using
     * the authorization code grant with Proof Key for Code Exchange (PKCE)
     *
     * @param issuerUrl The IAM Identity Center Issuer URL associated with an instance of IAM Identity Center
     */
    public void registerClient(final String startUrl, final String issuerUrl) throws BackgroundException {
        final AWSSSOOIDCClientBuilder configuration = AWSSSOOIDCClientBuilder.standard()
                .withRegion("us-east-1")
                .withClientConfiguration(new CustomClientConfiguration(host,
                        new ThreadLocalHostnameDelegatingTrustManager(trust, host.getHostname()), key));
        final AWSSSOOIDC client = configuration.build();
        log.debug("Registering client with issuer {}", issuerUrl);
        try {
            final RegisterClientResult registration = client.registerClient(new RegisterClientRequest()
                    // The friendly name of the client.
                    .withClientName(new PreferencesUseragentProvider().get())
                    // The service supports only public as a client type.
                    .withClientType("public")
                    .withIssuerUrl(issuerUrl)
                    .withGrantTypes("authorization_code", "refresh_token")
                    // SSO registration scopes
                    .withScopes(host.getProtocol().getOAuthScopes())
                    .withRedirectUris(host.getProtocol().getOAuthRedirectUrl()));
            log.debug("Client registerd with {}", registration);
            this.setClientid(registration.getClientId());
            this.setClientsecret(registration.getClientSecret());
            this.setParameter("start_url", startUrl);
            this.setAuthorizationServerUrl(String.format("%s/authorize", configuration.getEndpoint().getServiceEndpoint()));
            this.setTokenServerUrl(String.format("%s/token", configuration.getEndpoint().getServiceEndpoint()));
            this.clientIdExpiry = registration.getClientSecretExpiresAt() * 1000;
        }
        catch(AmazonClientException e) {
            throw new AmazonServiceExceptionMappingService().map(e);
        }
    }

    @Override
    public OAuthTokens authorize() throws BackgroundException {
        this.registerClient(startUrl, issuerUrl);
        return super.authorize();
    }

    @Override
    public OAuthTokens authorizeWithRefreshToken(final OAuthTokens tokens) throws BackgroundException {
        // Registers client if missing; persists registration details
        if(StringUtils.isBlank(clientId)) {
            log.debug("Client ID not found for {}, registering new client", host);
            this.registerClient(startUrl, issuerUrl);
        }
        if(System.currentTimeMillis() >= clientIdExpiry) {
            log.warn("Client registration expired for {}", host);
            this.registerClient(startUrl, issuerUrl);
        }
        return super.authorizeWithRefreshToken(tokens);
    }

    @Override
    protected void addAuthorizationHeader(final HttpRequest request, final OAuthTokens tokens) {
        // Skip setting header later set in interceptor for AWS4 signature
    }
}
