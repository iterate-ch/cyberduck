package ch.cyberduck.core.sts;

/*
 * Copyright (c) 2002-2018 iterate GmbH. All rights reserved.
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

import ch.cyberduck.core.AsciiRandomStringService;
import ch.cyberduck.core.Credentials;
import ch.cyberduck.core.Host;
import ch.cyberduck.core.LocaleFactory;
import ch.cyberduck.core.LoginOptions;
import ch.cyberduck.core.PasswordCallback;
import ch.cyberduck.core.PreferencesUseragentProvider;
import ch.cyberduck.core.UseragentProvider;
import ch.cyberduck.core.exception.LoginCanceledException;
import ch.cyberduck.core.exception.LoginFailureException;
import ch.cyberduck.core.preferences.Preferences;
import ch.cyberduck.core.preferences.PreferencesFactory;
import ch.cyberduck.core.proxy.Proxy;
import ch.cyberduck.core.proxy.ProxyFactory;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import java.io.File;
import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;

import com.amazonaws.ClientConfiguration;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSSessionCredentials;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.profile.ProfilesConfigFile;
import com.amazonaws.auth.profile.internal.BasicProfile;
import com.amazonaws.profile.path.AwsProfileFileLocationProvider;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.securitytoken.AWSSecurityTokenService;
import com.amazonaws.services.securitytoken.AWSSecurityTokenServiceClientBuilder;
import com.amazonaws.services.securitytoken.model.AWSSecurityTokenServiceException;
import com.amazonaws.services.securitytoken.model.AssumeRoleRequest;
import com.amazonaws.services.securitytoken.model.AssumeRoleResult;
import com.amazonaws.services.securitytoken.model.GetSessionTokenRequest;
import com.amazonaws.services.securitytoken.model.GetSessionTokenResult;

public class STSCredentialsConfigurator {
    private static final Logger log = Logger.getLogger(STSCredentialsConfigurator.class);

    private final Preferences preferences = PreferencesFactory.get();
    private final PasswordCallback prompt;

    public STSCredentialsConfigurator(final PasswordCallback prompt) {
        this.prompt = prompt;
    }

    public Credentials configure(final Host host) throws LoginFailureException, LoginCanceledException {
        final Credentials credentials = new Credentials(host.getCredentials());
        // Find matching profile name or AWS access key in ~/.aws/credentials
        final String profile = host.getCredentials().getUsername();
        // Profile can be null – the default profile from the configuration will be loaded
        if(log.isDebugEnabled()) {
            log.debug(String.format("Look for profile name %s in ~/.aws/credentials", profile));
        }
        final File file = AwsProfileFileLocationProvider.DEFAULT_CREDENTIALS_LOCATION_PROVIDER.getLocation();
        if(null == file) {
            log.warn("Missing configuration file ~/.aws/ccredentials. Skip auto configuration");
            return host.getCredentials();
        }
        // Iterating all profiles on our own because AWSProfileCredentialsConfigurator does not support MFA tokens
        final ProfilesConfigFile config = new ProfilesConfigFile(file);
        final Map<String, BasicProfile> profiles = config.getAllBasicProfiles();
        final Optional<Map.Entry<String, BasicProfile>> optional = profiles.entrySet().stream().filter(new Predicate<Map.Entry<String, BasicProfile>>() {
            @Override
            public boolean test(final Map.Entry<String, BasicProfile> entry) {
                final String profileName = entry.getKey();
                final BasicProfile basicProfile = entry.getValue();
                final String awsAccessIdKey = basicProfile.getAwsAccessIdKey();
                // Matching access key or profile name
                if(StringUtils.equals(profileName, profile) || StringUtils.equals(awsAccessIdKey, profile)) {
                    if(log.isDebugEnabled()) {
                        log.debug(String.format("Found matching profile %s", profile));
                    }
                    return true;
                }
                return false;
            }
        }).findFirst();
        if(optional.isPresent()) {
            final Map.Entry<String, BasicProfile> entry = optional.get();
            final BasicProfile basicProfile = entry.getValue();
            if(basicProfile.isRoleBasedProfile()) {
                if(log.isDebugEnabled()) {
                    log.debug(String.format("Configure credentials from role based profile %s", basicProfile.getProfileName()));
                }
                if(StringUtils.isBlank(basicProfile.getRoleSourceProfile())) {
                    throw new LoginFailureException(String.format("Missing source profile reference in profile %s", basicProfile.getProfileName()));
                }
                else if(!profiles.containsKey(basicProfile.getRoleSourceProfile())) {
                    throw new LoginFailureException(String.format("Missing source profile with name %s", basicProfile.getRoleSourceProfile()));
                }
                else {
                    final BasicProfile sourceProfile = profiles.get(basicProfile.getRoleSourceProfile());
                    // If a profile defines the role_arn property then the profile is treated as an assume role profile
                    final AWSSecurityTokenService service = this.getTokenService(ProxyFactory.get().find(host),
                        host.getRegion(),
                        sourceProfile.getAwsAccessIdKey(), sourceProfile.getAwsSecretAccessKey(), sourceProfile.getAwsSessionToken());
                    // Starts a new session by sending a request to the AWS Security Token Service (STS) to assume a
                    // Role using the long lived AWS credentials
                    final AssumeRoleRequest assumeRoleRequest = new AssumeRoleRequest()
                        .withRoleArn(basicProfile.getRoleArn())
                        // Specify this value if the IAM user has a policy that requires MFA authentication
                        .withSerialNumber(basicProfile.getProperties().getOrDefault("mfa_serial", null))
                        // The value provided by the MFA device, if MFA is required
                        .withTokenCode(basicProfile.getProperties().containsKey("mfa_serial") ?
                            // mfa_serial - The identification number of the MFA device to use when assuming a role. This is an optional parameter.
                            // Specify this value if the trust policy of the role being assumed includes a condition that requires MFA authentication.
                            // The value is either the serial number for a hardware device (such as GAHT12345678) or an Amazon Resource Name (ARN) for
                            // a virtual device (such as arn:aws:iam::123456789012:mfa/user).
                            prompt.prompt(
                                host, LocaleFactory.localizedString("Provide additional login credentials", "Credentials"),
                                String.format("%s %s", LocaleFactory.localizedString("Multi-Factor Authentication", "S3"),
                                    basicProfile.getProperties().get("mfa_serial")),
                                new LoginOptions(host.getProtocol())
                                    .password(true)
                                    .passwordPlaceholder(LocaleFactory.localizedString("MFA Authentication Code", "S3"))
                                    .keychain(false)
                            ).getPassword() : null
                        )
                        .withRoleSessionName(String.format("%s-%s", preferences.getProperty("application.name"), new AsciiRandomStringService().random()));
                    if(log.isDebugEnabled()) {
                        log.debug(String.format("Request %s from %s", assumeRoleRequest, service));
                    }
                    try {
                        final AssumeRoleResult assumeRoleResult = service.assumeRole(assumeRoleRequest);
                        if(log.isDebugEnabled()) {
                            log.debug(String.format("Set credentials from %s", assumeRoleResult));
                        }
                        credentials.setUsername(assumeRoleResult.getCredentials().getAccessKeyId());
                        credentials.setPassword(assumeRoleResult.getCredentials().getSecretAccessKey());
                        credentials.setToken(assumeRoleResult.getCredentials().getSessionToken());
                    }
                    catch(AWSSecurityTokenServiceException e) {
                        throw new LoginFailureException(e.getErrorMessage(), e);
                    }
                }
            }
            else {
                if(log.isDebugEnabled()) {
                    log.debug(String.format("Configure credentials from basic profile %s", basicProfile.getProfileName()));
                }
                if(StringUtils.isNotBlank(basicProfile.getAwsSessionToken())) {
                    // No need to obtain session token if preconfigured in profile
                    if(log.isDebugEnabled()) {
                        log.debug(String.format("Set session token credentials from profile %s", profile));
                    }
                    credentials.setUsername(basicProfile.getAwsAccessIdKey());
                    credentials.setPassword(basicProfile.getAwsSecretAccessKey());
                    credentials.setToken(basicProfile.getAwsSessionToken());
                }
                else {
                    if(host.getProtocol().isTokenConfigurable()) {
                        // Obtain session token
                        if(log.isDebugEnabled()) {
                            log.debug(String.format("Get session token from credentials in profile %s", basicProfile.getProfileName()));
                        }
                        final AWSSecurityTokenService service = this.getTokenService(ProxyFactory.get().find(host),
                            host.getRegion(),
                            basicProfile.getAwsAccessIdKey(), basicProfile.getAwsSecretAccessKey(), basicProfile.getAwsSessionToken());
                        final GetSessionTokenRequest sessionTokenRequest = new GetSessionTokenRequest();
                        if(log.isDebugEnabled()) {
                            log.debug(String.format("Request %s from %s", sessionTokenRequest, service));
                        }
                        try {
                            final GetSessionTokenResult sessionTokenResult = service.getSessionToken(sessionTokenRequest);
                            if(log.isDebugEnabled()) {
                                log.debug(String.format("Set credentials from %s", sessionTokenResult));
                            }
                            credentials.setUsername(sessionTokenResult.getCredentials().getAccessKeyId());
                            credentials.setPassword(sessionTokenResult.getCredentials().getSecretAccessKey());
                            credentials.setToken(sessionTokenResult.getCredentials().getSessionToken());
                        }
                        catch(AWSSecurityTokenServiceException e) {
                            throw new LoginFailureException(e.getErrorMessage(), e);
                        }
                    }
                    else {
                        if(log.isDebugEnabled()) {
                            log.debug(String.format("Set static credentials from profile %s", basicProfile.getProfileName()));
                        }
                        credentials.setUsername(basicProfile.getAwsAccessIdKey());
                        credentials.setPassword(basicProfile.getAwsSecretAccessKey());
                    }
                }
            }
        }
        return credentials;
    }

    protected AWSSecurityTokenService getTokenService(final Proxy proxy, final String region, final String accessKey, final String secretKey, final String sessionToken) {
        final ClientConfiguration configuration = new ClientConfiguration();
        final int timeout = PreferencesFactory.get().getInteger("connection.timeout.seconds") * 1000;
        configuration.setConnectionTimeout(timeout);
        configuration.setSocketTimeout(timeout);
        final UseragentProvider ua = new PreferencesUseragentProvider();
        configuration.setUserAgentPrefix(ua.get());
        configuration.setMaxErrorRetry(0);
        configuration.setMaxConnections(1);
        configuration.setUseGzip(PreferencesFactory.get().getBoolean("http.compression.enable"));
        switch(proxy.getType()) {
            case HTTP:
            case HTTPS:
                configuration.setProxyHost(proxy.getHostname());
                configuration.setProxyPort(proxy.getPort());
        }
        return AWSSecurityTokenServiceClientBuilder.standard()
            .withCredentials(new AWSStaticCredentialsProvider(StringUtils.isBlank(sessionToken) ? new AWSCredentials() {
                @Override
                public String getAWSAccessKeyId() {
                    return accessKey;
                }

                @Override
                public String getAWSSecretKey() {
                    return secretKey;
                }
            } : new AWSSessionCredentials() {
                @Override
                public String getAWSAccessKeyId() {
                    return accessKey;
                }

                @Override
                public String getAWSSecretKey() {
                    return secretKey;
                }

                @Override
                public String getSessionToken() {
                    return sessionToken;
                }
            }))
            .withClientConfiguration(configuration)
            .withRegion(StringUtils.isNotBlank(region) ? Regions.fromName(region) : Regions.DEFAULT_REGION).build();
    }
}
