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
import ch.cyberduck.core.preferences.HostPreferences;
import ch.cyberduck.core.preferences.PreferencesFactory;
import ch.cyberduck.core.ssl.ThreadLocalHostnameDelegatingTrustManager;
import ch.cyberduck.core.ssl.X509KeyManager;
import ch.cyberduck.core.ssl.X509TrustManager;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.amazonaws.ClientConfiguration;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.auth.AnonymousAWSCredentials;
import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.securitytoken.AWSSecurityTokenService;
import com.amazonaws.services.securitytoken.AWSSecurityTokenServiceClientBuilder;
import com.amazonaws.services.securitytoken.model.AssumeRoleWithWebIdentityRequest;
import com.amazonaws.services.securitytoken.model.AssumeRoleWithWebIdentityResult;

public class AssumeRoleWithWebIdentitySTSCredentialsConfigurator extends AWSProfileSTSCredentialsConfigurator {
    private static final Logger log = LogManager.getLogger(AssumeRoleWithWebIdentitySTSCredentialsConfigurator.class);

    public AssumeRoleWithWebIdentitySTSCredentialsConfigurator(final X509TrustManager trust, final X509KeyManager key, final PasswordCallback prompt) {
        super(trust, key, prompt);
    }

    @Override
    public Credentials configure(final Host host) {
        final Credentials credentials = new Credentials(host.getCredentials());

        final AWSSecurityTokenService service = this.getTokenService(host, null, null, null, null);

        AssumeRoleWithWebIdentityRequest webIdReq = new AssumeRoleWithWebIdentityRequest()
                .withWebIdentityToken(credentials.getOauth().getAccessToken());

        String durationsecondsProp = "s3.assumerole.durationseconds";
        String policyProp = "s3.assumerole.policy";
        String rolearnProp = "s3.assumerole.rolearn";
        String rolesessionnameProp = "s3.assumerole.rolesessionname";

        if (StringUtils.isBlank(host.getProperty(durationsecondsProp))) {
            webIdReq = webIdReq.withDurationSeconds(Integer.getInteger(host.getProperty(durationsecondsProp)));
        }
        else if(host.getProtocol().getProperties().containsKey(durationsecondsProp)) {
            System.out.println("set duration second with profile value: " + host.getProtocol().getProperties().get(durationsecondsProp));
            webIdReq = webIdReq.withDurationSeconds(Integer.getInteger(host.getProtocol().getProperties().get(durationsecondsProp)));
        }
        else if(StringUtils.isNotBlank(PreferencesFactory.get().getProperty(durationsecondsProp))) {
            webIdReq = webIdReq.withDurationSeconds(new HostPreferences(host).getInteger(durationsecondsProp));
        }

        if (StringUtils.isBlank(host.getProperty(policyProp))) {
            webIdReq = webIdReq.withPolicy(host.getProperty(policyProp));
        }
        else if(host.getProtocol().getProperties().containsKey(policyProp)) {
            webIdReq = webIdReq.withPolicy(host.getProtocol().getProperties().get(policyProp));
        }
        else if(StringUtils.isNotBlank(PreferencesFactory.get().getProperty(policyProp))) {
            webIdReq = webIdReq.withPolicy(new HostPreferences(host).getProperty(policyProp));
        }

        if (StringUtils.isBlank(host.getProperty(rolearnProp))) {
            webIdReq = webIdReq.withRoleArn(host.getProperty(rolearnProp));
        }
        else if(host.getProtocol().getProperties().containsKey(rolearnProp)) {
            webIdReq = webIdReq.withRoleArn(host.getProtocol().getProperties().get(rolearnProp));
        }
        else if(StringUtils.isNotBlank(PreferencesFactory.get().getProperty(rolearnProp))) {
            webIdReq = webIdReq.withRoleArn(new HostPreferences(host).getProperty(rolearnProp));
        }

        if (StringUtils.isBlank(host.getProperty(rolesessionnameProp))) {
            webIdReq = webIdReq.withRoleSessionName(host.getProperty(rolesessionnameProp));
        }
        else if(host.getProtocol().getProperties().containsKey(rolesessionnameProp)) {
            webIdReq = webIdReq.withRoleSessionName(host.getProtocol().getProperties().get(rolesessionnameProp));
        }
        else if(StringUtils.isNotBlank(PreferencesFactory.get().getProperty(rolesessionnameProp))) {
            webIdReq = webIdReq.withRoleSessionName(new HostPreferences(host).getProperty(rolesessionnameProp));
        }

        AssumeRoleWithWebIdentityResult result = service.assumeRoleWithWebIdentity(webIdReq);
        com.amazonaws.services.securitytoken.model.Credentials cred = result.getCredentials();

        if(log.isDebugEnabled()) {
            log.debug(cred.toString());
        }

        credentials.setUsername(cred.getAccessKeyId());
        credentials.setPassword(cred.getSecretAccessKey());
        credentials.setToken(cred.getSessionToken());

        return credentials;
    }

    @Override
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
//                TODO may be obsolete because of 'withEndpointConfiguration'. Needs to be tested against AWS
//                .withRegion(StringUtils.isNotBlank(region) ? Regions.fromName(region) : Regions.US_EAST_1)
                .build();
    }
}
