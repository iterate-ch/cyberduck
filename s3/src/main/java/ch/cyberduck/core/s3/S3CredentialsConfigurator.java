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

import ch.cyberduck.core.AsciiRandomStringService;
import ch.cyberduck.core.Credentials;
import ch.cyberduck.core.CredentialsConfigurator;
import ch.cyberduck.core.Host;
import ch.cyberduck.core.Local;
import ch.cyberduck.core.LocalFactory;
import ch.cyberduck.core.LocaleFactory;
import ch.cyberduck.core.LoginOptions;
import ch.cyberduck.core.PasswordCallback;
import ch.cyberduck.core.TemporaryAccessTokens;
import ch.cyberduck.core.aws.CustomClientConfiguration;
import ch.cyberduck.core.exception.AccessDeniedException;
import ch.cyberduck.core.exception.LoginCanceledException;
import ch.cyberduck.core.ssl.ThreadLocalHostnameDelegatingTrustManager;
import ch.cyberduck.core.ssl.X509KeyManager;
import ch.cyberduck.core.ssl.X509TrustManager;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Scanner;
import java.util.function.Predicate;

import com.amazonaws.ClientConfiguration;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSSessionCredentials;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.profile.internal.AbstractProfilesConfigFileScanner;
import com.amazonaws.auth.profile.internal.AllProfiles;
import com.amazonaws.auth.profile.internal.BasicProfile;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.securitytoken.AWSSecurityTokenService;
import com.amazonaws.services.securitytoken.AWSSecurityTokenServiceClientBuilder;
import com.amazonaws.services.securitytoken.model.AWSSecurityTokenServiceException;
import com.amazonaws.services.securitytoken.model.AssumeRoleRequest;
import com.amazonaws.services.securitytoken.model.AssumeRoleResult;
import com.amazonaws.services.securitytoken.model.GetSessionTokenRequest;
import com.amazonaws.services.securitytoken.model.GetSessionTokenResult;
import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.google.common.base.Charsets;
import com.google.common.hash.HashCode;
import com.google.common.hash.Hashing;
import com.google.common.io.BaseEncoding;

/**
 * Configure credentials from AWS CLI configuration and SSO cache
 */
public class S3CredentialsConfigurator implements CredentialsConfigurator {
    private static final Logger log = LogManager.getLogger(S3CredentialsConfigurator.class);

    private final Local directory;
    private final X509TrustManager trust;
    private final X509KeyManager key;
    private final PasswordCallback prompt;
    private final Map<String, BasicProfile> profiles = new LinkedHashMap<>();

    public S3CredentialsConfigurator(final X509TrustManager trust, final X509KeyManager key, final PasswordCallback prompt) {
        this(LocalFactory.get(LocalFactory.get(), ".aws"), trust, key, prompt);
    }

    public S3CredentialsConfigurator(final Local directory, final X509TrustManager trust, final X509KeyManager key, final PasswordCallback prompt) {
        this.directory = directory;
        this.trust = trust;
        this.key = key;
        this.prompt = prompt;
    }

    @Override
    public Credentials configure(final Host host) {
        final Credentials credentials = new Credentials(host.getCredentials());
        final String profile = credentials.getUsername();
        final Optional<Map.Entry<String, BasicProfile>> optional = profiles.entrySet().stream().filter(new Predicate<Map.Entry<String, BasicProfile>>() {
            @Override
            public boolean test(final Map.Entry<String, BasicProfile> entry) {
                final String profileName = entry.getKey();
                final BasicProfile basicProfile = entry.getValue();
                final String awsAccessIdKey = basicProfile.getAwsAccessIdKey();
                // Matching access key or profile name
                if(StringUtils.equals(profileName, profile)) {
                    if(log.isDebugEnabled()) {
                        log.debug(String.format("Found matching profile %s for profile name %s", profile, profileName));
                    }
                    return true;
                }
                else if(StringUtils.equals(awsAccessIdKey, profile)) {
                    if(log.isDebugEnabled()) {
                        log.debug(String.format("Found matching profile %s for access key %s", profile, awsAccessIdKey));
                    }
                    return true;
                }
                return false;
            }
        }).findFirst();
        if(optional.isPresent()) {
            final Map.Entry<String, BasicProfile> entry = optional.get();
            final BasicProfile basicProfile = entry.getValue();
            final String tokenCode;
            if(basicProfile.getProperties().containsKey("mfa_serial")) {
                try {
                    tokenCode = prompt.prompt(
                            host, LocaleFactory.localizedString("Provide additional login credentials", "Credentials"),
                            String.format("%s %s", LocaleFactory.localizedString("Multi-Factor Authentication", "S3"),
                                    basicProfile.getPropertyValue("mfa_serial")),
                            new LoginOptions(host.getProtocol())
                                    .password(true)
                                    .passwordPlaceholder(LocaleFactory.localizedString("MFA Authentication Code", "S3"))
                                    .keychain(false)
                    ).getPassword();
                }
                catch(LoginCanceledException e) {
                    log.warn(String.format("Canceled MFA prompt for profile %s", basicProfile));
                    return credentials;
                }
            }
            else {
                tokenCode = null;
            }
            final Integer durationSeconds;
            if(basicProfile.getProperties().containsKey("duration_seconds")) {
                durationSeconds = Integer.valueOf(basicProfile.getPropertyValue("duration_seconds"));
            }
            else {
                durationSeconds = null;
            }
            if(basicProfile.isRoleBasedProfile()) {
                if(log.isDebugEnabled()) {
                    log.debug(String.format("Configure credentials from role based profile %s", basicProfile.getProfileName()));
                }
                if(StringUtils.isBlank(basicProfile.getRoleSourceProfile())) {
                    log.warn(String.format("Missing source profile reference in profile %s", basicProfile.getProfileName()));
                    return credentials;
                }
                else if(!profiles.containsKey(basicProfile.getRoleSourceProfile())) {
                    log.warn(String.format("Missing source profile with name %s", basicProfile.getRoleSourceProfile()));
                    return credentials;
                }
                else {
                    final BasicProfile sourceProfile = profiles.get(basicProfile.getRoleSourceProfile());
                    final AWSSecurityTokenService service;
                    if(sourceProfile.getProperties().containsKey("sso_start_url")) {
                        // Read cached SSO credentials
                        final CachedCredential cached = this.fetchSsoCredentials(sourceProfile.getProperties());
                        if(null == cached) {
                            return credentials;
                        }
                        service = this.getTokenService(host, host.getRegion(),
                                cached.accessKey, cached.secretKey, cached.sessionToken);
                    }
                    else {
                        // If a profile defines the role_arn property then the profile is treated as an assume role profile
                        service = this.getTokenService(host, host.getRegion(),
                                sourceProfile.getAwsAccessIdKey(),
                                sourceProfile.getAwsSecretAccessKey(),
                                sourceProfile.getAwsSessionToken());
                    }
                    // Starts a new session by sending a request to the AWS Security Token Service (STS) to assume a
                    // role using the long-lived AWS credentials
                    final AssumeRoleRequest assumeRoleRequest = new AssumeRoleRequest()
                            .withExternalId(basicProfile.getRoleExternalId())
                            .withRoleArn(basicProfile.getRoleArn())
                            // Specify this value if the IAM user has a policy that requires MFA authentication
                            .withSerialNumber(basicProfile.getPropertyValue("mfa_serial"))
                            // The value provided by the MFA device, if MFA is required
                            .withTokenCode(tokenCode
                                    // mfa_serial - The identification number of the MFA device to use when assuming a role. This is an optional parameter.
                                    // Specify this value if the trust policy of the role being assumed includes a condition that requires MFA authentication.
                                    // The value is either the serial number for a hardware device (such as GAHT12345678) or an Amazon Resource Name (ARN) for
                                    // a virtual device (such as arn:aws:iam::123456789012:mfa/user).
                            )
                            .withRoleSessionName(basicProfile.getRoleSessionName() == null ? new AsciiRandomStringService().random() : basicProfile.getRoleSessionName())
                            .withDurationSeconds(durationSeconds
                                    // duration_seconds - Specifies the maximum duration of the role session, in seconds. The value can range from 900 seconds
                                    // (15 minutes) up to the maximum session duration setting for the role (which can be a maximum of 43200). This is an
                                    // optional parameter and by default, the value is set to 3600 seconds.
                            );
                    if(log.isDebugEnabled()) {
                        log.debug(String.format("Request %s from %s", assumeRoleRequest, service));
                    }
                    try {
                        final AssumeRoleResult assumeRoleResult = service.assumeRole(assumeRoleRequest);
                        if(log.isDebugEnabled()) {
                            log.debug(String.format("Set credentials from %s", assumeRoleResult));
                        }
                        credentials.setTokens(new TemporaryAccessTokens(
                                assumeRoleResult.getCredentials().getAccessKeyId(),
                                assumeRoleResult.getCredentials().getSecretAccessKey(),
                                assumeRoleResult.getCredentials().getSessionToken(),
                                assumeRoleResult.getCredentials().getExpiration().getTime()));
                    }
                    catch(AWSSecurityTokenServiceException e) {
                        log.warn(e.getErrorMessage(), e);
                        return credentials;
                    }
                }
            }
            else {
                if(log.isDebugEnabled()) {
                    log.debug(String.format("Configure credentials from basic profile %s", basicProfile.getProfileName()));
                }
                final Map<String, String> profileProperties = basicProfile.getProperties();
                if(profileProperties.containsKey("sso_start_url") || profileProperties.containsKey("sso_session")) {
                    // Read cached SSO credentials
                    final CachedCredential cached = this.fetchSsoCredentials(profileProperties);
                    if(null == cached) {
                        return credentials;
                    }
                    return credentials.withTokens(new TemporaryAccessTokens(
                            cached.accessKey, cached.secretKey, cached.sessionToken,
                            Instant.parse(cached.expiration).toEpochMilli()));
                }
                if(tokenCode != null) {
                    // Obtain session token
                    if(log.isDebugEnabled()) {
                        log.debug(String.format("Get session token from credentials in profile %s", basicProfile.getProfileName()));
                    }
                    final AWSSecurityTokenService service = this.getTokenService(host,
                            host.getRegion(),
                            basicProfile.getAwsAccessIdKey(),
                            basicProfile.getAwsSecretAccessKey(),
                            basicProfile.getAwsSessionToken());
                    //  The purpose of the sts:GetSessionToken operation is to authenticate the user using MFA.
                    final GetSessionTokenRequest sessionTokenRequest = new GetSessionTokenRequest()
                            // The value provided by the MFA device, if MFA is required
                            .withTokenCode(tokenCode)
                            // Specify this value if the IAM user has a policy that requires MFA authentication
                            .withSerialNumber(basicProfile.getPropertyValue("mfa_serial"))
                            .withDurationSeconds(durationSeconds);
                    if(log.isDebugEnabled()) {
                        log.debug(String.format("Request %s from %s", sessionTokenRequest, service));
                    }
                    try {
                        final GetSessionTokenResult sessionTokenResult = service.getSessionToken(sessionTokenRequest);
                        if(log.isDebugEnabled()) {
                            log.debug(String.format("Set credentials from %s", sessionTokenResult));
                        }
                        return credentials.withTokens(new TemporaryAccessTokens(
                                sessionTokenResult.getCredentials().getAccessKeyId(),
                                sessionTokenResult.getCredentials().getSecretAccessKey(),
                                sessionTokenResult.getCredentials().getSessionToken(),
                                sessionTokenResult.getCredentials().getExpiration().getTime()));
                    }
                    catch(AWSSecurityTokenServiceException e) {
                        log.warn(e.getErrorMessage(), e);
                        return credentials;
                    }
                }
                if(log.isDebugEnabled()) {
                    log.debug(String.format("Set credentials from profile %s", basicProfile.getProfileName()));
                }
                return credentials.withTokens(new TemporaryAccessTokens(
                        basicProfile.getAwsAccessIdKey(),
                        basicProfile.getAwsSecretAccessKey(),
                        basicProfile.getAwsSessionToken(),
                        -1L));
            }
        }
        else {
            log.warn(String.format("No matching configuration for profile %s in %s", profile, profiles));
        }
        return credentials;
    }

    @Override
    public CredentialsConfigurator reload() throws LoginCanceledException {
        // See https://docs.aws.amazon.com/sdkref/latest/guide/creds-config-files.html for configuration behavior
        final Local configFile = LocalFactory.get(directory, "config");
        final Local credentialsFile = LocalFactory.get(directory, "credentials");
        if(log.isDebugEnabled()) {
            log.debug(String.format("Load profiles from %s and %s", configFile, credentialsFile));
        }
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
            log.warn(String.format("Failure reading %s and %s", configFile, credentialsFile), e);
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

    /**
     * Read SSO credentials from cache file of AWS CLI
     *
     * @return Null on error reading from file or expired SSO credentials in cache
     */
    private CachedCredential fetchSsoCredentials(final Map<String, String> properties) {
        // See https://github.com/boto/botocore/blob/412aeb96c9a6ebc72aa1bdf33e58ddd48c7b048d/botocore/credentials.py#L2078-L2098
        try {
            final ObjectMapper mapper = JsonMapper.builder()
                    .serializationInclusion(Include.NON_NULL)
                    .enable(MapperFeature.SORT_PROPERTIES_ALPHABETICALLY)
                    .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
                    .visibility(PropertyAccessor.FIELD, Visibility.ANY).build();
            final CacheKey cacheKey = new CacheKey();
            cacheKey.accountId = properties.get("sso_account_id");
            cacheKey.roleName = properties.get("sso_role_name");
            final String ssoSession = properties.get("sso_session");
            if(ssoSession != null) {
                cacheKey.sessionName = ssoSession;
            }
            else {
                cacheKey.startUrl = properties.get("sso_start_url");
            }
            final String cacheKeyJson = mapper.writeValueAsString(cacheKey);
            final HashCode hashCode = Hashing.sha1().newHasher().putString(cacheKeyJson, Charsets.UTF_8).hash();
            final String hash = BaseEncoding.base16().lowerCase().encode(hashCode.asBytes());
            final String cachedCredentialsJson = String.format("%s.json", hash);
            final Local cachedCredentialsFile =
                    LocalFactory.get(LocalFactory.get(LocalFactory.get(directory, "cli"), "cache"), cachedCredentialsJson);
            if(log.isDebugEnabled()) {
                log.debug(String.format("Attempting to read SSO credentials from %s", cachedCredentialsFile.getAbsolute()));
            }
            if(!cachedCredentialsFile.exists()) {
                log.warn(String.format("Missing file %s with cached SSO credentials.", cachedCredentialsFile.getAbsolute()));
                return null;
            }
            try (InputStream in = cachedCredentialsFile.getInputStream()) {
                final CachedCredentials cached = mapper.readValue(in, CachedCredentials.class);
                if(null == cached.credentials) {
                    log.warn("Failure parsing SSO credentials.");
                    return null;
                }
                final Instant expiration = Instant.parse(cached.credentials.expiration);
                if(expiration.isBefore(Instant.now())) {
                    log.warn("Expired AWS SSO credentials.");
                    return null;
                }
                return cached.credentials;
            }
        }
        catch(IOException | AccessDeniedException e) {
            log.warn("Failure retrieving SSO credentials.", e);
            return null;
        }
    }

    private static class CacheKey {
        private String accountId;
        private String roleName;
        private String sessionName;
        private String startUrl;
    }

    private static class CachedCredentials {
        @JsonProperty("Credentials")
        private CachedCredential credentials;
    }

    private static class CachedCredential {
        @JsonProperty("AccessKeyId")
        private String accessKey;
        @JsonProperty("SecretAccessKey")
        private String secretKey;
        @JsonProperty("SessionToken")
        private String sessionToken;
        @JsonProperty("Expiration")
        private String expiration;
    }

    protected AWSSecurityTokenService getTokenService(final Host host, final String region, final String accessKey, final String secretKey, final String sessionToken) {
        final ClientConfiguration configuration = new CustomClientConfiguration(host,
                new ThreadLocalHostnameDelegatingTrustManager(trust, host.getHostname()), key);
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
            if(log.isDebugEnabled()) {
                log.debug(String.format("Reading AWS file %s", file));
            }
            try (InputStream inputStream = file.getInputStream(); Scanner scanner = new Scanner(inputStream, StandardCharsets.UTF_8.name())) {
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
                log.warn("Duplicate property values for [" + propertyKey + "].");
            }

            properties.put(propertyKey, propertyValue);
        }

        @Override
        protected void onEndOfFile() {
            // No-op
        }
    }
}
