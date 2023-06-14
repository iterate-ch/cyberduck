package ch.cyberduck.core.sts;/*
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
import ch.cyberduck.core.exception.LoginCanceledException;
import ch.cyberduck.core.exception.LoginFailureException;
import ch.cyberduck.core.ssl.ThreadLocalHostnameDelegatingTrustManager;
import ch.cyberduck.core.ssl.X509KeyManager;
import ch.cyberduck.core.ssl.X509TrustManager;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.amazonaws.ClientConfiguration;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.auth.AnonymousAWSCredentials;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.auth.WebIdentityTokenCredentialsProvider;
import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.securitytoken.AWSSecurityTokenService;
import com.amazonaws.services.securitytoken.AWSSecurityTokenServiceClientBuilder;
import com.amazonaws.services.securitytoken.model.AssumeRoleWithWebIdentityRequest;
import com.amazonaws.services.securitytoken.model.AssumeRoleWithWebIdentityResult;

public class NonAwsSTSCredentialsConfigurator extends STSCredentialsConfigurator {
    private static final Logger log = LogManager.getLogger(NonAwsSTSCredentialsConfigurator.class);


    public NonAwsSTSCredentialsConfigurator(final X509TrustManager trust, final X509KeyManager key, final PasswordCallback prompt) {
       super(trust, key, prompt);
    }

    @Override
    public Credentials configure(final Host host) throws LoginFailureException, LoginCanceledException {
        final Credentials credentials = new Credentials(host.getCredentials());

        final ClientConfiguration configuration = new CustomClientConfiguration(host,
                new ThreadLocalHostnameDelegatingTrustManager(trust, host.getHostname()), key);

        final AWSSecurityTokenService service = AWSSecurityTokenServiceClientBuilder
                .standard()
                .withEndpointConfiguration(new AwsClientBuilder.EndpointConfiguration(String.format("%s://%s:%s", host.getProtocol().getScheme(), host.getHostname(),
                        host.getPort()), null))
                .withCredentials(new AWSCredentialsProvider() {
                    @Override
                    public AWSCredentials getCredentials() {
                        return new AnonymousAWSCredentials();
                    }

                    @Override
                    public void refresh() {

                    }
                })
                .build();

        AssumeRoleWithWebIdentityRequest webIdReq = new AssumeRoleWithWebIdentityRequest()
                .withWebIdentityToken(credentials.getOauth().getAccessToken());
//                .withPolicy("policy")
//                .withRoleArn("consoleAdmin")
//                .withRoleSessionName("testSession");
//                .putCustomQueryParameter("Version", "2011-06-15");

        AssumeRoleWithWebIdentityResult result = service.assumeRoleWithWebIdentity(webIdReq);
        com.amazonaws.services.securitytoken.model.Credentials cred = result.getCredentials();
        System.out.println(cred.toString());

        credentials.setUsername(cred.getAccessKeyId());
        credentials.setPassword(cred.getSecretAccessKey());
        credentials.setToken(cred.getSessionToken());

        return credentials;
    }
}
