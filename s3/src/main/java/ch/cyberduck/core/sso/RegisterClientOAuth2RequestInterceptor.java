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

import ch.cyberduck.core.DefaultIOExceptionMappingService;
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
import ch.cyberduck.core.oauth.OAuth2RequestInterceptor;
import ch.cyberduck.core.s3.S3CredentialsConfigurator;
import ch.cyberduck.core.ssl.ThreadLocalHostnameDelegatingTrustManager;
import ch.cyberduck.core.ssl.X509KeyManager;
import ch.cyberduck.core.ssl.X509TrustManager;

import org.apache.http.HttpRequest;
import org.apache.http.client.HttpClient;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.lang.reflect.Field;
import java.net.Inet4Address;
import java.net.ServerSocket;

import com.amazonaws.AmazonClientException;
import com.amazonaws.services.ssooidc.AWSSSOOIDC;
import com.amazonaws.services.ssooidc.AWSSSOOIDCClientBuilder;
import com.amazonaws.services.ssooidc.model.CreateTokenRequest;
import com.amazonaws.services.ssooidc.model.CreateTokenResult;
import com.amazonaws.services.ssooidc.model.RegisterClientRequest;
import com.amazonaws.services.ssooidc.model.RegisterClientResult;
import com.google.api.client.auth.oauth2.AuthorizationCodeFlow;
import com.google.api.client.auth.openidconnect.IdTokenResponse;

public class RegisterClientOAuth2RequestInterceptor extends OAuth2RequestInterceptor {
    private static final Logger log = LogManager.getLogger(RegisterClientOAuth2RequestInterceptor.class);

    private final Host host;
    private final X509TrustManager trust;
    private final X509KeyManager key;

    private final String region;
    private final String startUrl;
    private final String issuerUrl;

    /**
     * The duration (in milliseconds) after which the client ID expires.
     * This variable is used to track the validity of the client registration with the Identity Center.
     * When the specified time elapses, the client must renew its registration.
     */
    private Long clientIdExpiry = -1L;

    public RegisterClientOAuth2RequestInterceptor(final HttpClient client, final Host host,
                                                  final X509TrustManager trust, final X509KeyManager key, final LoginCallback prompt) throws LoginCanceledException {
        super(client, host, null, null, null, null, host.getProtocol().getOAuthScopes(), true, prompt);
        this.host = host.setCredentials(new S3CredentialsConfigurator().reload().configure(host));
        this.trust = trust;
        this.key = key;
        this.region = prompt(host, prompt, Profile.SSO_REGION_KEY, LocaleFactory.localizedString(
                String.format("SSO Region (%s)", Profile.SSO_REGION_KEY), "Credentials"), host.getProperty(Profile.SSO_REGION_KEY));
        this.startUrl = prompt(host, prompt, Profile.SSO_START_URL_KEY, LocaleFactory.localizedString(
                String.format("SSO Start Url (%s)", Profile.SSO_START_URL_KEY), "Credentials"), host.getProperty(Profile.SSO_START_URL_KEY));
        this.issuerUrl = startUrl;
    }

    /**
     * Registers a public client with IAM Identity Center. This allows clients to perform authorization using
     * the authorization code grant with Proof Key for Code Exchange (PKCE)
     *
     * @param issuerUrl The IAM Identity Center Issuer URL associated with an instance of IAM Identity Center
     */
    public void registerClient(final String startUrl, final String issuerUrl) throws BackgroundException {
        final String endpoint = String.format("https://oidc.%s.amazonaws.com", region);
        final AWSSSOOIDCClientBuilder configuration = AWSSSOOIDCClientBuilder.standard()
                .withRegion(region)
                .withClientConfiguration(new CustomClientConfiguration(host,
                        new ThreadLocalHostnameDelegatingTrustManager(trust, String.format("oidc.%s.amazonaws.com", region)), key));
        final AWSSSOOIDC client = configuration.build();
        log.debug("Registering client with issuer {}", issuerUrl);
        try {
            try(ServerSocket temp = new ServerSocket(0)) {
                final String redirectUri = String.format("http://%s:%d/oauth/callback",
                        Inet4Address.getLoopbackAddress().getHostAddress(), temp.getLocalPort());
                final RegisterClientResult registration = client.registerClient(new RegisterClientRequest()
                        // The friendly name of the client.
                        .withClientName(new PreferencesUseragentProvider().get())
                        // The service supports only public as a client type.
                        .withClientType("public")
                        .withIssuerUrl(issuerUrl)
                        .withGrantTypes("authorization_code", "refresh_token")
                        // SSO registration scopes
                        .withScopes(host.getProtocol().getOAuthScopes())
                        .withRedirectUris(redirectUri));
                log.debug("Client registered with {}", registration);
                this.setClientid(registration.getClientId());
                this.setClientsecret(registration.getClientSecret());
                this.setParameter("start_url", startUrl);
                this.setAuthorizationServerUrl(String.format("%s/authorize", endpoint));
                this.setTokenServerUrl(String.format("%s/token", endpoint));
                this.setRedirectUri(redirectUri);
                this.clientIdExpiry = registration.getClientSecretExpiresAt() * 1000;
            }
            catch(IOException e) {
                throw new DefaultIOExceptionMappingService().map(e);
            }
        }
        catch(AmazonClientException e) {
            throw new AmazonServiceExceptionMappingService().map(e);
        }
        finally {
            client.shutdown();
        }
    }

    @Override
    public OAuthTokens authorize() throws BackgroundException {
        if(-1L == clientIdExpiry) {
            this.registerClient(startUrl, issuerUrl);
        }
        return super.authorize();
    }

    @Override
    public OAuthTokens authorizeWithRefreshToken(final OAuthTokens tokens) throws BackgroundException {
        // Registers client if missing; persists registration details
        if(System.currentTimeMillis() >= clientIdExpiry) {
            log.warn("Client registration expired for {} at {}", host, clientIdExpiry);
            this.registerClient(startUrl, issuerUrl);
        }
        return super.authorizeWithRefreshToken(tokens);
    }

    @Override
    protected void addAuthorizationHeader(final HttpRequest request, final OAuthTokens tokens) {
        // Skip setting header later set in interceptor for AWS4 signature
    }

    /**
     * Send token request as application/json instead of default application/x-www-form-urlencoded
     */
    @Override
    protected IdTokenResponse exchangeToken(final AuthorizationCodeFlow flow, final String authorizationCode) throws BackgroundException {
        final AWSSSOOIDCClientBuilder configuration = AWSSSOOIDCClientBuilder.standard()
                .withRegion(region)
                .withClientConfiguration(new CustomClientConfiguration(host,
                        new ThreadLocalHostnameDelegatingTrustManager(trust, String.format("oidc.%s.amazonaws.com", region)), key));
        final AWSSSOOIDC client = configuration.build();
        try {
            // Use reflection to access the pkce field from AuthorizationCodeFlow
            final Field pkceField = AuthorizationCodeFlow.class.getDeclaredField("pkce");
            pkceField.setAccessible(true);
            final Object pkce = pkceField.get(flow);
            // Get the code verifier using reflection
            final Field codeVerifierField = pkce.getClass().getDeclaredField("verifier");
            codeVerifierField.setAccessible(true);
            final String codeVerifier = (String) codeVerifierField.get(pkce);
            final CreateTokenRequest tokenRequest = new CreateTokenRequest()
                    .withClientId(this.getClientid())
                    .withClientSecret(this.getClientsecret())
                    .withGrantType(this.getFlowType().toString())
                    .withCode(authorizationCode)
                    .withRedirectUri(this.getRedirectUri())
                    .withCodeVerifier(codeVerifier);
            final CreateTokenResult tokenResponse = client.createToken(tokenRequest);
            return new IdTokenResponse()
                    .setAccessToken(tokenResponse.getAccessToken())
                    .setRefreshToken(tokenResponse.getRefreshToken());
        }
        catch(NoSuchFieldException | IllegalAccessException e) {
            throw new DefaultIOExceptionMappingService().map(new IOException("Failed to access PKCE verifier field", e));
        }
        finally {
            client.shutdown();
        }
    }
}
