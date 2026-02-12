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
import ch.cyberduck.core.OAuthTokens;
import ch.cyberduck.core.aws.AmazonServiceExceptionMappingService;
import ch.cyberduck.core.aws.CustomClientConfiguration;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.ssl.ThreadLocalHostnameDelegatingTrustManager;
import ch.cyberduck.core.ssl.X509KeyManager;
import ch.cyberduck.core.ssl.X509TrustManager;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.amazonaws.AmazonClientException;
import com.amazonaws.services.sso.AWSSSO;
import com.amazonaws.services.sso.AWSSSOClient;
import com.amazonaws.services.sso.model.GetRoleCredentialsRequest;
import com.amazonaws.services.sso.model.RoleCredentials;

public class IdentityCenterAuthorizationService {
    private static final Logger log = LogManager.getLogger(IdentityCenterAuthorizationService.class);

    private final Host host;
    private final X509TrustManager trust;
    private final X509KeyManager key;

    public IdentityCenterAuthorizationService(final Host host, final X509TrustManager trust, final X509KeyManager key) {
        this.host = host;
        this.trust = trust;
        this.key = key;
    }

    /**
     *
     * @param tokens    The token issued by the CreateToken API call. For more information, see CreateToken in the IAM Identity Center OIDC API Reference Guide.
     * @param accountId The identifier for the AWS account that is assigned to the user.
     * @param roleName  The friendly name of the role that is assigned to the user.
     * @return Short-lived access tokens
     */
    public RoleCredentials getRoleCredentials(final OAuthTokens tokens, final String accountId, final String roleName) throws BackgroundException {
        final AWSSSO client = AWSSSOClient.builder()
                .withClientConfiguration(new CustomClientConfiguration(host,
                        new ThreadLocalHostnameDelegatingTrustManager(trust, host.getHostname()), key)).build();
        try {
            log.debug("Getting role credentials for account {} and role {} with access token {}",
                    accountId, roleName, tokens);
            // Gets STS role credentials using the SSO access token for a given role name that is assigned to the user.
            return client.getRoleCredentials(new GetRoleCredentialsRequest()
                    .withAccountId(accountId)
                    .withRoleName(roleName)
                    .withAccessToken(tokens.getAccessToken())).getRoleCredentials();
        }
        catch(AmazonClientException e) {
            throw new AmazonServiceExceptionMappingService().map(e);
        }
    }
}
