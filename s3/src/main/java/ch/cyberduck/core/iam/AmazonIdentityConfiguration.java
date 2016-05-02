package ch.cyberduck.core.iam;

/*
 * Copyright (c) 2002-2013 David Kocher. All rights reserved.
 * http://cyberduck.ch/
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * Bug fixes, suggestions and comments should be sent to feedback@cyberduck.ch
 */

import ch.cyberduck.core.Credentials;
import ch.cyberduck.core.Host;
import ch.cyberduck.core.KeychainLoginService;
import ch.cyberduck.core.LocaleFactory;
import ch.cyberduck.core.LoginCallback;
import ch.cyberduck.core.LoginOptions;
import ch.cyberduck.core.PasswordStoreFactory;
import ch.cyberduck.core.PreferencesUseragentProvider;
import ch.cyberduck.core.UseragentProvider;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.exception.LoginFailureException;
import ch.cyberduck.core.identity.IdentityConfiguration;
import ch.cyberduck.core.preferences.PreferencesFactory;
import ch.cyberduck.core.proxy.Proxy;
import ch.cyberduck.core.proxy.ProxyFactory;

import org.apache.log4j.Logger;

import java.util.concurrent.Callable;

import com.amazonaws.AmazonClientException;
import com.amazonaws.ClientConfiguration;
import com.amazonaws.services.identitymanagement.AmazonIdentityManagementClient;
import com.amazonaws.services.identitymanagement.model.*;

public class AmazonIdentityConfiguration implements IdentityConfiguration {
    private static final Logger log = Logger.getLogger(AmazonIdentityConfiguration.class);

    private final Host host;

    private final AmazonIdentityManagementClient client;

    /**
     * Prefix in preferences
     */
    private static final String prefix = "iam.";

    public AmazonIdentityConfiguration(final Host host) {
        this(host, PreferencesFactory.get().getInteger("connection.timeout.seconds") * 1000);
    }

    public AmazonIdentityConfiguration(final Host host, final int timeout) {
        this.host = host;
        final ClientConfiguration configuration = new ClientConfiguration();
        configuration.setConnectionTimeout(timeout);
        configuration.setSocketTimeout(timeout);
        final UseragentProvider ua = new PreferencesUseragentProvider();
        configuration.setUserAgent(ua.get());
        configuration.setMaxErrorRetry(0);
        configuration.setMaxConnections(1);
        final Proxy proxy = ProxyFactory.get().find(host);
        switch(proxy.getType()) {
            case HTTP:
            case HTTPS:
                configuration.setProxyHost(proxy.getHostname());
                configuration.setProxyPort(proxy.getPort());
        }
        // Create new IAM credentials
        client = new AmazonIdentityManagementClient(
                new com.amazonaws.auth.AWSCredentials() {
                    @Override
                    public String getAWSAccessKeyId() {
                        return host.getCredentials().getUsername();
                    }

                    @Override
                    public String getAWSSecretKey() {
                        return host.getCredentials().getPassword();
                    }
                }, configuration
        );
    }

    private interface Authenticated<T> extends Callable<T> {
        T call() throws BackgroundException;
    }

    private <T> T authenticated(final Authenticated<T> run, final LoginCallback prompt) throws BackgroundException {
        final LoginOptions options = new LoginOptions();
        try {
            final KeychainLoginService login = new KeychainLoginService(prompt, PasswordStoreFactory.get());
            login.validate(host, LocaleFactory.localizedString("AWS Identity and Access Management", "S3"), options);
            return run.call();
        }
        catch(LoginFailureException failure) {
            prompt.prompt(host, host.getCredentials(),
                    LocaleFactory.localizedString("Login failed", "Credentials"), failure.getMessage(), options);
            return this.authenticated(run, prompt);
        }
    }

    @Override
    public void delete(final String username, final LoginCallback prompt) throws BackgroundException {
        if(log.isInfoEnabled()) {
            log.info(String.format("Delete user %s", username));
        }
        this.authenticated(new Authenticated<Void>() {
            @Override
            public Void call() throws BackgroundException {
                PreferencesFactory.get().deleteProperty(String.format("%s%s", prefix, username));
                try {
                    final ListAccessKeysResult keys
                            = client.listAccessKeys(new ListAccessKeysRequest().withUserName(username));

                    for(AccessKeyMetadata key : keys.getAccessKeyMetadata()) {
                        if(log.isDebugEnabled()) {
                            log.debug(String.format("Delete access key %s for user %s", key, username));
                        }
                        client.deleteAccessKey(new DeleteAccessKeyRequest(username, key.getAccessKeyId()));
                    }

                    final ListUserPoliciesResult policies = client.listUserPolicies(new ListUserPoliciesRequest(username));
                    for(String policy : policies.getPolicyNames()) {
                        if(log.isDebugEnabled()) {
                            log.debug(String.format("Delete policy %s for user %s", policy, username));
                        }
                        client.deleteUserPolicy(new DeleteUserPolicyRequest(username, policy));
                    }
                    client.deleteUser(new DeleteUserRequest(username));
                }
                catch(NoSuchEntityException e) {
                    log.warn(String.format("User %s already removed", username));
                }
                catch(AmazonClientException e) {
                    throw new AmazonServiceExceptionMappingService().map("Cannot write user configuration", e);
                }
                return null;
            }
        }, prompt);
    }

    @Override
    public Credentials getCredentials(final String username) {
        // Resolve access key id
        final String key = PreferencesFactory.get().getProperty(String.format("%s%s", prefix, username));
        if(log.isDebugEnabled()) {
            log.debug(String.format("Lookup access key for user %s with %s", username, key));
        }
        if(null == key) {
            log.warn(String.format("No access key found for user %s", username));
            return null;
        }
        return new Credentials(key, PasswordStoreFactory.get().getPassword(host.getProtocol().getScheme(), host.getPort(),
                host.getHostname(), key));
    }

    @Override
    public void create(final String username, final String policy, final LoginCallback prompt) throws BackgroundException {
        if(log.isInfoEnabled()) {
            log.info(String.format("Create user %s with policy %s", username, policy));
        }
        this.authenticated(new Authenticated<Void>() {
            @Override
            public Void call() throws BackgroundException {
                try {
                    // Create new IAM credentials
                    User user;
                    try {
                        user = client.createUser(new CreateUserRequest().withUserName(username)).getUser();
                    }
                    catch(EntityAlreadyExistsException e) {
                        user = client.getUser(new GetUserRequest().withUserName(username)).getUser();
                    }
                    final CreateAccessKeyResult key = client.createAccessKey(
                            new CreateAccessKeyRequest().withUserName(user.getUserName()));
                    if(log.isDebugEnabled()) {
                        log.debug(String.format("Created access key %s for user %s", key, username));
                    }
                    // Write policy document to get read access
                    client.putUserPolicy(new PutUserPolicyRequest(user.getUserName(), "Policy", policy));
                    // Map virtual user name to IAM access key
                    final String id = key.getAccessKey().getAccessKeyId();
                    if(log.isInfoEnabled()) {
                        log.info(String.format("Map user %s to access key %s", String.format("%s%s", prefix, username), id));
                    }
                    PreferencesFactory.get().setProperty(String.format("%s%s", prefix, username), id);
                    // Save secret
                    PasswordStoreFactory.get().addPassword(
                            host.getProtocol().getScheme(), host.getPort(), host.getHostname(),
                            id, key.getAccessKey().getSecretAccessKey());
                }
                catch(AmazonClientException e) {
                    throw new AmazonServiceExceptionMappingService().map("Cannot write user configuration", e);
                }
                return null;
            }
        }, prompt);
    }
}
