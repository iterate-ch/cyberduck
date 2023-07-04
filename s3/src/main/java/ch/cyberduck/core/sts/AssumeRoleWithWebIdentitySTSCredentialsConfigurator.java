package ch.cyberduck.core.sts;

/*
 * Copyright (c) 2002-2023 iterate GmbH. All rights reserved.
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
import ch.cyberduck.core.PasswordCallback;
import ch.cyberduck.core.aws.CustomClientConfiguration;
import ch.cyberduck.core.oauth.OAuth2RequestInterceptor;
import ch.cyberduck.core.preferences.HostPreferences;
import ch.cyberduck.core.ssl.ThreadLocalHostnameDelegatingTrustManager;
import ch.cyberduck.core.ssl.X509KeyManager;
import ch.cyberduck.core.ssl.X509TrustManager;

import org.apache.commons.lang3.StringUtils;

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

public class AssumeRoleWithWebIdentitySTSCredentialsConfigurator extends AbstractSTSCredentialsConfigurator {

    private OAuth2RequestInterceptor authorizationService;

    public AssumeRoleWithWebIdentitySTSCredentialsConfigurator(final X509TrustManager trust, final X509KeyManager key,
                                                               PasswordCallback prompt, OAuth2RequestInterceptor authorizationService) {
        super(trust, key, prompt);
        this.authorizationService = authorizationService;
    }

    @Override
    public Credentials configure(final Host host) {
        service = this.getTokenService(host, null, null, null, null);

        AssumeRoleWithWebIdentityRequest webIdReq = new AssumeRoleWithWebIdentityRequest();
        if(StringUtils.isNotBlank(authorizationService.getTokens().getIdToken())) {
            webIdReq.withWebIdentityToken(authorizationService.getTokens().getIdToken());
        }
        else {
            webIdReq.withWebIdentityToken(authorizationService.getTokens().getAccessToken());
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

            host.setProperty("sts.credentials.expiration", String.valueOf(result.getCredentials().getExpiration().getTime()));

            if(log.isDebugEnabled()) {
                log.debug(cred.toString());
            }

            credentials.setUsername(cred.getAccessKeyId());
            credentials.setPassword(cred.getSecretAccessKey());
            credentials.setToken(cred.getSessionToken());
        }
//        catch(Inva)
        catch(AWSSecurityTokenServiceException e) {
            log.error(e.getErrorMessage());
        }

        return credentials;
    }

    public AWSSecurityTokenService getTokenService(final Host host, final String region, final String accessKey, final String secretKey, final String sessionToken) {
        final ClientConfiguration configuration = new CustomClientConfiguration(host,
                new ThreadLocalHostnameDelegatingTrustManager(trust, host.getHostname()), key);
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
}
