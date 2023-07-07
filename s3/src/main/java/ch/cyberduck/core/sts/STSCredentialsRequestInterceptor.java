package ch.cyberduck.core.sts;

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
import ch.cyberduck.core.aws.CustomClientConfiguration;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.exception.LoginFailureException;
import ch.cyberduck.core.oauth.OAuth2RequestInterceptor;
import ch.cyberduck.core.preferences.HostPreferences;
import ch.cyberduck.core.s3.S3Session;
import ch.cyberduck.core.ssl.ThreadLocalHostnameDelegatingTrustManager;
import ch.cyberduck.core.ssl.X509KeyManager;
import ch.cyberduck.core.ssl.X509TrustManager;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.client.HttpClient;
import org.apache.http.protocol.HttpContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jets3t.service.security.AWSSessionCredentials;

import java.io.IOException;

import com.amazonaws.ClientConfiguration;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.auth.AnonymousAWSCredentials;
import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.securitytoken.AWSSecurityTokenService;
import com.amazonaws.services.securitytoken.AWSSecurityTokenServiceClientBuilder;
import com.amazonaws.services.securitytoken.model.AWSSecurityTokenServiceException;
import com.amazonaws.services.securitytoken.model.AssumeRoleWithWebIdentityRequest;
import com.amazonaws.services.securitytoken.model.AssumeRoleWithWebIdentityResult;
import com.google.api.client.auth.oauth2.Credential;

public class STSCredentialsRequestInterceptor extends OAuth2RequestInterceptor {
    private static final Logger log = LogManager.getLogger(STSCredentialsRequestInterceptor.class);

    private final X509TrustManager trust;
    private final X509KeyManager key;
    private final S3Session session;

    private long stsExpiryInMilliseconds;

    public STSCredentialsRequestInterceptor(HttpClient client, Host host, final X509TrustManager trust, final X509KeyManager key,
                                            S3Session session) {
        super(client, host);
        this.trust = trust;
        this.key = key;
        this.session = session;
    }

    @Override
    public void process(final org.apache.http.HttpRequest request, final HttpContext context) throws IOException {
        if(System.currentTimeMillis() >= stsExpiryInMilliseconds) {
            if(getTokens().isExpired()) {
                try {
                    this.save(this.refresh(getTokens()));
                }
                catch(BackgroundException e) {
                    log.warn(String.format("Failure %s refreshing OAuth tokens %s", e, getTokens()));
                    // Follow-up error 401 handled in web identity token expired interceptor
                }
            }
            try {
                Credentials credentials = assumeRoleWithWebIdentity();
                session.getClient().setProviderCredentials(credentials.isAnonymousLogin() ? null :
                        new AWSSessionCredentials(credentials.getUsername(), credentials.getPassword(),
                                credentials.getToken()));
            }
            catch(BackgroundException e) {
                log.warn(String.format("Failure %s to fetch temporary sts credentials", e));
                // Follow-up error 400 or 403 handled in web identity token expired interceptor
            }
        }
    }

    public Credentials assumeRoleWithWebIdentity() throws BackgroundException {
        AWSSecurityTokenService service = this.getTokenService(host);

        AssumeRoleWithWebIdentityRequest webIdReq = new AssumeRoleWithWebIdentityRequest();
        if(StringUtils.isNotBlank(getTokens().getIdToken())) {
            webIdReq.withWebIdentityToken(getTokens().getIdToken());
        }
        else {
            webIdReq.withWebIdentityToken(getTokens().getAccessToken());
        }

        if (new HostPreferences(host).getInteger("s3.assumerole.durationseconds") != 0) {
            webIdReq.withDurationSeconds(new HostPreferences(host).getInteger("s3.assumerole.durationseconds"));
        }

        if (StringUtils.isNotBlank(new HostPreferences(host).getProperty("s3.assumerole.policy"))) {
            webIdReq.withPolicy(new HostPreferences(host).getProperty("s3.assumerole.policy"));
        }

        if (StringUtils.isNotBlank(new HostPreferences(host).getProperty("s3.assumerole.rolearn"))) {
            webIdReq.withRoleArn(new HostPreferences(host).getProperty("s3.assumerole.rolearn"));
        }

        if (StringUtils.isNotBlank(new HostPreferences(host).getProperty("s3.assumerole.rolesessionname"))) {
            webIdReq.withRoleSessionName(new HostPreferences(host).getProperty("s3.assumerole.rolesessionname"));
        }

        Credentials credentials = new Credentials();
        try {
            AssumeRoleWithWebIdentityResult result = service.assumeRoleWithWebIdentity(webIdReq);
            com.amazonaws.services.securitytoken.model.Credentials cred = result.getCredentials();

            if(log.isDebugEnabled()) { log.debug(cred.toString()); }

            stsExpiryInMilliseconds = cred.getExpiration().getTime();

            credentials.setUsername(cred.getAccessKeyId());
            credentials.setPassword(cred.getSecretAccessKey());
            credentials.setToken(cred.getSessionToken());
        }
        catch(AWSSecurityTokenServiceException e) {
            throw new LoginFailureException(e.getMessage(), e);
        }
        return credentials;
    }

    private AWSSecurityTokenService getTokenService(final Host host) {
        final ClientConfiguration configuration = new CustomClientConfiguration(host,
                new ThreadLocalHostnameDelegatingTrustManager(trust, host.getProtocol().getSTSEndpoint()), key);
        return AWSSecurityTokenServiceClientBuilder
                .standard()
                .withEndpointConfiguration(new AwsClientBuilder.EndpointConfiguration(host.getProtocol().getSTSEndpoint(), null))
                .withCredentials(new AWSCredentialsProvider() {
                    @Override
                    public AWSCredentials getCredentials() {
                        return new AnonymousAWSCredentials();
                    }

                    @Override
                    public void refresh() {

                    }
                })
                .withClientConfiguration(configuration)
                .build();
    }

    @Override
    public STSCredentialsRequestInterceptor withMethod(final Credential.AccessMethod method) {
        super.withMethod(method);
        return this;
    }

    @Override
    public STSCredentialsRequestInterceptor withRedirectUri(final String redirectUri) {
        super.withRedirectUri(redirectUri);
        return this;
    }

    @Override
    public STSCredentialsRequestInterceptor withFlowType(final FlowType flowType) {
        super.withFlowType(flowType);
        return this;
    }

    @Override
    public STSCredentialsRequestInterceptor withParameter(final String key, final String value) {
        super.withParameter(key, value);
        return this;
    }
}
