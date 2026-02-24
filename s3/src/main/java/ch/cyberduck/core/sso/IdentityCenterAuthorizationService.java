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
import ch.cyberduck.core.LocationCallback;
import ch.cyberduck.core.LoginCallback;
import ch.cyberduck.core.OAuthTokens;
import ch.cyberduck.core.Profile;
import ch.cyberduck.core.aws.AmazonServiceExceptionMappingService;
import ch.cyberduck.core.aws.CustomClientConfiguration;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.exception.ConnectionCanceledException;
import ch.cyberduck.core.features.Location;
import ch.cyberduck.core.preferences.HostPreferencesFactory;
import ch.cyberduck.core.ssl.ThreadLocalHostnameDelegatingTrustManager;
import ch.cyberduck.core.ssl.X509KeyManager;
import ch.cyberduck.core.ssl.X509TrustManager;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import com.amazonaws.AmazonClientException;
import com.amazonaws.services.sso.AWSSSO;
import com.amazonaws.services.sso.AWSSSOClient;
import com.amazonaws.services.sso.model.AccountInfo;
import com.amazonaws.services.sso.model.GetRoleCredentialsRequest;
import com.amazonaws.services.sso.model.ListAccountRolesRequest;
import com.amazonaws.services.sso.model.ListAccountRolesResult;
import com.amazonaws.services.sso.model.ListAccountsRequest;
import com.amazonaws.services.sso.model.ListAccountsResult;
import com.amazonaws.services.sso.model.RoleCredentials;
import com.amazonaws.services.sso.model.RoleInfo;

public class IdentityCenterAuthorizationService {
    private static final Logger log = LogManager.getLogger(IdentityCenterAuthorizationService.class);

    private final Host host;
    private final X509TrustManager trust;
    private final X509KeyManager key;
    private final LoginCallback prompt;

    public IdentityCenterAuthorizationService(final Host host, final X509TrustManager trust, final X509KeyManager key, final LoginCallback prompt) {
        this.host = host;
        this.trust = trust;
        this.key = key;
        this.prompt = prompt;
    }

    /**
     * Retrieves role credentials using the provided SSO access token, AWS region, account ID, and role name.
     *
     * @param tokens    The token issued by the CreateToken API call. For more information, see CreateToken in the IAM Identity Center OIDC API Reference Guide.
     * @param region    The AWS region where the request should be made.
     * @param accountId The identifier for the AWS account that is assigned to the user.
     * @param roleName  The friendly name of the role that is assigned to the user.
     * @return Short-lived access tokens
     */
    public RoleCredentials getRoleCredentials(final OAuthTokens tokens, final String region,
                                              @Nullable String accountId, @Nullable String roleName) throws BackgroundException {
        final AWSSSO client = AWSSSOClient.builder()
                .withRegion(region)
                .withClientConfiguration(new CustomClientConfiguration(host,
                        new ThreadLocalHostnameDelegatingTrustManager(trust, host.getHostname()), key)).build();
        try {
            if(null == accountId) {
                final List<AccountInfo> list = new ArrayList<>();
                String nextToken = null;
                do {
                    final ListAccountsResult result = client.listAccounts(new ListAccountsRequest()
                            .withNextToken(nextToken)
                            .withAccessToken(tokens.getAccessToken()));
                    list.addAll(result.getAccountList());
                    nextToken = result.getNextToken();
                }
                while(null != nextToken);
                accountId = prompt(host, prompt.getFeature(LocationCallback.class), list.stream().map(info -> new Location.Name(info.getAccountId()) {
                    @Override
                    public String toString() {
                        return info.getAccountName();
                    }
                }).collect(Collectors.toSet()), Profile.SSO_ACCOUNT_ID_KEY, LocaleFactory.localizedString(
                        String.format("AWS Account ID (%s)", Profile.SSO_ACCOUNT_ID_KEY), "Credentials"), host.getProperty(Profile.SSO_ACCOUNT_ID_KEY)).getIdentifier();
            }
            if(null == roleName) {
                final List<RoleInfo> list = new ArrayList<>();
                String nextToken = null;
                do {
                    final ListAccountRolesResult result = client.listAccountRoles(new ListAccountRolesRequest()
                            .withNextToken(nextToken)
                            .withAccountId(accountId)
                            .withAccessToken(tokens.getAccessToken()));
                    list.addAll(result.getRoleList());
                    nextToken = result.getNextToken();
                }
                while(null != nextToken);
                roleName = prompt(host, prompt.getFeature(LocationCallback.class), list.stream().map(info -> new Location.Name(info.getRoleName())).collect(Collectors.toSet()),
                        Profile.SSO_ROLE_NAME_KEY, LocaleFactory.localizedString(
                                String.format("Permission set name (%s)", Profile.SSO_ROLE_NAME_KEY), "Credentials"), host.getProperty(Profile.SSO_ROLE_NAME_KEY)).getIdentifier();
            }
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
        finally {
            client.shutdown();
        }
    }

    public static Location.Name prompt(final Host bookmark, final LocationCallback prompt, final Set<Location.Name> options,
                                       final String property, final String message, final String value) throws ConnectionCanceledException {
        if(null == value) {
            final Location.Name input = prompt.select(bookmark,
                    LocaleFactory.localizedString("Provide additional login credentials", "Credentials"), message, options, null);
            HostPreferencesFactory.get(bookmark).setProperty(property, input.getIdentifier());
            return input;
        }
        return new Location.Name(value);
    }
}
