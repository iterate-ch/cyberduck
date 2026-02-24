package ch.cyberduck.core.s3;

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
import ch.cyberduck.core.CredentialsConfigurator;
import ch.cyberduck.core.Host;
import ch.cyberduck.core.Local;
import ch.cyberduck.core.LocalFactory;
import ch.cyberduck.core.Profile;
import ch.cyberduck.core.TemporaryAccessTokens;
import ch.cyberduck.core.exception.AccessDeniedException;
import ch.cyberduck.core.exception.LoginCanceledException;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Scanner;

import com.amazonaws.auth.profile.internal.AbstractProfilesConfigFileScanner;
import com.amazonaws.auth.profile.internal.AllProfiles;
import com.amazonaws.auth.profile.internal.BasicProfile;

/**
 * Configure credentials from AWS CLI configuration and SSO cache
 */
public class S3CredentialsConfigurator implements CredentialsConfigurator {
    private static final Logger log = LogManager.getLogger(S3CredentialsConfigurator.class);

    private final Local directory;

    /**
     * Profiles by name
     */
    private final Map<String, BasicProfile> profiles = new LinkedHashMap<>();

    public S3CredentialsConfigurator() {
        this(LocalFactory.get(LocalFactory.get(), ".aws"));
    }

    public S3CredentialsConfigurator(final Local directory) {
        this.directory = directory;
    }

    @Override
    public Credentials configure(final Host host) {
        final Credentials credentials = new Credentials(host.getCredentials());
        if(credentials.isPasswordAuthentication()) {
            return credentials;
        }
        final BasicProfile profile = profiles.entrySet().stream().filter(entry -> {
            // Matching access key or profile name
            if(StringUtils.equals(entry.getKey(), credentials.getUsername())) {
                log.debug("Found matching profile {} for profile name {}", credentials.getUsername(), entry.getKey());
                return true;
            }
            else if(StringUtils.equals(entry.getValue().getAwsAccessIdKey(), credentials.getUsername())) {
                log.debug("Found matching profile {} for access key {}", credentials.getUsername(), entry.getValue().getAwsAccessIdKey());
                return true;
            }
            return false;
        }).map(Map.Entry::getValue).findFirst().orElse(StringUtils.isBlank(host.getCredentials().getUsername()) ? profiles.get("default") : null);
        if(null != profile) {
            if(profile.isRoleBasedProfile()) {
                log.debug("Configure credentials from role based profile {}", profile.getProfileName());
                if(StringUtils.isBlank(profile.getRoleSourceProfile())) {
                    log.warn("Missing source profile reference in profile {}", profile.getProfileName());
                    return credentials;
                }
                else if(!profiles.containsKey(profile.getRoleSourceProfile())) {
                    log.warn("Missing source profile with name {}", profile.getRoleSourceProfile());
                    return credentials;
                }
                else {
                    final BasicProfile sourceProfile = profiles.get(profile.getRoleSourceProfile());
                    // If a profile defines the role_arn property then the profile is treated as an assume role profile
                    return credentials.setTokens(new TemporaryAccessTokens(
                                    sourceProfile.getAwsAccessIdKey(), sourceProfile.getAwsSecretAccessKey(), sourceProfile.getAwsSessionToken()))
                            .setProperty(Profile.STS_ROLE_ARN_PROPERTY_KEY, profile.getRoleArn())
                            .setProperty(Profile.STS_MFA_ARN_PROPERTY_KEY, profile.getPropertyValue("mfa_serial"));
                }
            }
            else {
                log.debug("Configure credentials from basic profile {}", profile.getProfileName());
                if(profile.getProperties().containsKey("sso_start_url") || profile.getProperties().containsKey("sso_session")) {
                    log.debug("Configure with SSO properties {}", profile.getProperties());
                    profile.getProperties().forEach(credentials::setProperty);
                    if(profile.getProperties().containsKey("sso_session")) {
                        // The sso-session section of the config file is used to group configuration variables for acquiring SSO access tokens
                        // Example section definition line: [sso-session my-sso]
                        final BasicProfile ssoProfile = profiles.get(String.format("sso-session %s", profile.getProperties().get("sso_session")));
                        if(null == ssoProfile) {
                            log.warn("Missing SSO profile with name {}", profile.getProperties().get("sso_session"));
                        }
                        else {
                            log.debug("Configure with SSO properties {} from {}", ssoProfile.getProperties(), ssoProfile);
                            ssoProfile.getProperties().forEach(credentials::setProperty);
                        }
                    }
                    return credentials;
                }
                log.debug("Set credentials from profile {}", profile.getProfileName());
                return credentials
                        .setTokens(new TemporaryAccessTokens(
                                profile.getAwsAccessIdKey(),
                                profile.getAwsSecretAccessKey(),
                                profile.getAwsSessionToken()))
                        .setProperty(Profile.STS_ROLE_ARN_PROPERTY_KEY, profile.getRoleArn())
                        .setProperty(Profile.STS_MFA_ARN_PROPERTY_KEY, profile.getPropertyValue("mfa_serial"));
            }
        }
        else {
            log.warn("No matching configuration for profile {} in {}", profile, profiles);
        }
        return credentials;
    }

    @Override
    public CredentialsConfigurator reload() throws LoginCanceledException {
        // See https://docs.aws.amazon.com/sdkref/latest/guide/creds-config-files.html for configuration behavior
        final Local configFile = LocalFactory.get(directory, "config");
        final Local credentialsFile = LocalFactory.get(directory, "credentials");
        log.debug("Load profiles from {} and {}", configFile, credentialsFile);
        // Profile can be null. The default profile from the configuration will be loaded
        // Iterating all profiles on our own because AWSProfileCredentialsConfigurator does not support MFA tokens
        final Map<String, Map<String, String>> allProfileProperties = new HashMap<>();
        try {
            final Map<String, Map<String, String>> credentialsFileProfileProperties = new ProfilesConfigFileLoaderHelper()
                    .parseProfileProperties(credentialsFile);
            allProfileProperties.putAll(credentialsFileProfileProperties);
            final Map<String, Map<String, String>> configFileProfileProperties = new ProfilesConfigFileLoaderHelper()
                    .parseProfileProperties(configFile);
            for(Map.Entry<String, Map<String, String>> entry : configFileProfileProperties.entrySet()) {
                final String profileName = entry.getKey();
                final Map<String, String> configFileProperties = entry.getValue();
                final Map<String, String> credentialsFileProperties = allProfileProperties.get(profileName);
                // If the credentials file had properties, then merge them in
                if(credentialsFileProperties != null) {
                    configFileProperties.putAll(credentialsFileProperties);
                }
                allProfileProperties.put(profileName, configFileProperties);
            }
        }
        catch(AccessDeniedException | IllegalArgumentException | IOException e) {
            log.warn("Failure reading {} and {}", configFile, credentialsFile);
            return this;
        }
        if(allProfileProperties.isEmpty()) {
            log.warn("Missing configuration file ~/.aws/credentials or ~/.aws/config. Skip auto configuration");
            return this;
        }
        // Convert the loaded property map to credential objects
        final Map<String, BasicProfile> profilesByName = new LinkedHashMap<>();
        for(Map.Entry<String, Map<String, String>> entry : allProfileProperties.entrySet()) {
            String profileName = entry.getKey();
            Map<String, String> properties = entry.getValue();
            profilesByName.put(profileName, new BasicProfile(profileName, properties));
        }
        profiles.clear();
        profiles.putAll(new AllProfiles(profilesByName).getProfiles());
        return this;
    }

    public Map<String, BasicProfile> getProfiles() {
        return profiles;
    }

    /**
     * Implementation of AbstractProfilesConfigFileScanner that groups profile properties into a map while scanning
     * through the credentials profile.
     */
    private static final class ProfilesConfigFileLoaderHelper extends AbstractProfilesConfigFileScanner {

        /**
         * Map from the parsed profile name to the map of all the property values included the specific profile
         */
        private final Map<String, Map<String, String>> allProfileProperties = new LinkedHashMap<>();

        /**
         * Parses the input and returns a map of all the profile properties.
         */
        public Map<String, Map<String, String>> parseProfileProperties(Local file) throws AccessDeniedException, IOException {
            if(!file.exists()) {
                return new LinkedHashMap<>();
            }
            log.debug("Reading AWS file {}", file);
            try(InputStream inputStream = file.getInputStream(); Scanner scanner = new Scanner(inputStream, StandardCharsets.UTF_8.name())) {
                run(scanner);
                return new LinkedHashMap<>(allProfileProperties);
            }
        }

        private String sanitizeProfile(String profileName) {
            // The config file has sections that start with `profile `
            return profileName.replaceAll("^profile ", "");
        }

        @Override
        protected void onEmptyOrCommentLine(String profileName, String line) {
            // Ignore empty or comment line
        }

        @Override
        protected void onProfileStartingLine(String newProfileName, String line) {
            // If the same profile name has already been declared, clobber the
            // previous one
            allProfileProperties.put(sanitizeProfile(newProfileName), new HashMap<>());
        }

        @Override
        protected void onProfileEndingLine(String prevProfileName) {
            // No-op
        }

        @Override
        protected void onProfileProperty(String profileName, String propertyKey,
                                         String propertyValue, boolean isSupportedProperty,
                                         String line) {
            profileName = sanitizeProfile(profileName);
            Map<String, String> properties = allProfileProperties.get(profileName);

            if(properties.containsKey(propertyKey)) {
                log.warn("Duplicate property values for [{}].", propertyKey);
            }

            properties.put(propertyKey, propertyValue);
        }

        @Override
        protected void onEndOfFile() {
            // No-op
        }
    }
}
