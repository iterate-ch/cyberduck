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
import ch.cyberduck.core.exception.LoginCanceledException;
import ch.cyberduck.core.exception.LoginFailureException;
import ch.cyberduck.core.ssl.X509KeyManager;
import ch.cyberduck.core.ssl.X509TrustManager;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.auth.AnonymousAWSCredentials;
import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.securitytoken.AWSSecurityTokenService;
import com.amazonaws.services.securitytoken.AWSSecurityTokenServiceClientBuilder;
import com.amazonaws.services.securitytoken.model.AWSSecurityTokenServiceException;
import com.amazonaws.services.securitytoken.model.AssumeRoleWithWebIdentityRequest;
import com.amazonaws.services.securitytoken.model.AssumeRoleWithWebIdentityResult;

public class AssumeRoleWithWebIdentitySTSCredentialsConfigurator extends STSCredentialsConfigurator {
    private static final Logger log = LogManager.getLogger(AssumeRoleWithWebIdentitySTSCredentialsConfigurator.class);


    public AssumeRoleWithWebIdentitySTSCredentialsConfigurator(final X509TrustManager trust, final X509KeyManager key, final PasswordCallback prompt) {
        super(trust, key, prompt);
    }

    @Override
    public Credentials configure(final Host host) throws LoginFailureException, LoginCanceledException {
        final Credentials credentials = new Credentials(host.getCredentials());

        // STS API is open, no authorization required
        final AWSSecurityTokenService service = AWSSecurityTokenServiceClientBuilder
                .standard()
                // TODO hard-coded STS-endpoint
                .withEndpointConfiguration(new AwsClientBuilder.EndpointConfiguration(String.format("https://sts.amazonaws.com"), null))
                .withCredentials(new AWSCredentialsProvider() {
                    @Override
                    public AWSCredentials getCredentials() {
                        //         // https://www.demo2s.com/java/amazon-aws-assumerolewithwebidentityrequest-tutorial-with-examples.html
                        return new AnonymousAWSCredentials();
                    }

                    @Override
                    public void refresh() {

                    }
                })
                .build();


        AssumeRoleWithWebIdentityRequest webIdReq = new AssumeRoleWithWebIdentityRequest()
                // TODO check with DK: make configurable in profile/bookmark?
                .withWebIdentityToken(credentials.getToken())
                // TODO hard-coded -> connection profile/bookmark?
                .withDurationSeconds(3000)
                // TODO hard-coded -> connection profile/bookmark?
                .withRoleArn("arn:aws:iam::930717317329:role/google-Test-Role")
                // TODO hard-coded -> connection profile/bookmark?
                .withRoleSessionName("cyberduck-test");
                // TODO do we need to make (ad-hoc) policy configurable as well?

        try {
            AssumeRoleWithWebIdentityResult result = service.assumeRoleWithWebIdentity(webIdReq);
            com.amazonaws.services.securitytoken.model.Credentials cred = result.getCredentials();

            // TODO is this the right way? Something goes wrong, the token gets empty, Caused by: com.amazonaws.services.securitytoken.model.AWSSecurityTokenServiceException: 1 validation error detected: Value at 'webIdentityToken' failed to satisfy constraint: Member must have length greater than or equal to 4
            credentials.setUsername(cred.getAccessKeyId());
            credentials.setPassword(cred.getSecretAccessKey());
            credentials.setToken(cred.getSessionToken());
            return credentials;
        }
        catch(AWSSecurityTokenServiceException e) {
            throw e;
        }


    }
}
