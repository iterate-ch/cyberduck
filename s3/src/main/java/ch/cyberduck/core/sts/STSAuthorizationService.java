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

import ch.cyberduck.core.AsciiRandomStringService;
import ch.cyberduck.core.Credentials;
import ch.cyberduck.core.Host;
import ch.cyberduck.core.LocaleFactory;
import ch.cyberduck.core.LoginOptions;
import ch.cyberduck.core.OAuthTokens;
import ch.cyberduck.core.PasswordCallback;
import ch.cyberduck.core.Profile;
import ch.cyberduck.core.TemporaryAccessTokens;
import ch.cyberduck.core.aws.CustomClientConfiguration;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.exception.LoginCanceledException;
import ch.cyberduck.core.exception.LoginFailureException;
import ch.cyberduck.core.preferences.HostPreferences;
import ch.cyberduck.core.preferences.HostPreferencesFactory;
import ch.cyberduck.core.preferences.PreferencesReader;
import ch.cyberduck.core.preferences.ProxyPreferencesReader;
import ch.cyberduck.core.s3.S3CredentialsStrategy;
import ch.cyberduck.core.ssl.ThreadLocalHostnameDelegatingTrustManager;
import ch.cyberduck.core.ssl.X509KeyManager;
import ch.cyberduck.core.ssl.X509TrustManager;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.stream.Collectors;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.AnonymousAWSCredentials;
import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.securitytoken.AWSSecurityTokenService;
import com.amazonaws.services.securitytoken.AWSSecurityTokenServiceClientBuilder;
import com.amazonaws.services.securitytoken.model.AWSSecurityTokenServiceException;
import com.amazonaws.services.securitytoken.model.AssumeRoleRequest;
import com.amazonaws.services.securitytoken.model.AssumeRoleResult;
import com.amazonaws.services.securitytoken.model.AssumeRoleWithSAMLRequest;
import com.amazonaws.services.securitytoken.model.AssumeRoleWithSAMLResult;
import com.amazonaws.services.securitytoken.model.AssumeRoleWithWebIdentityRequest;
import com.amazonaws.services.securitytoken.model.AssumeRoleWithWebIdentityResult;
import com.amazonaws.services.securitytoken.model.GetCallerIdentityRequest;
import com.amazonaws.services.securitytoken.model.GetCallerIdentityResult;
import com.amazonaws.services.securitytoken.model.GetSessionTokenRequest;
import com.amazonaws.services.securitytoken.model.GetSessionTokenResult;
import com.amazonaws.services.securitytoken.model.Tag;
import com.auth0.jwt.JWT;
import com.auth0.jwt.exceptions.JWTDecodeException;

public class STSAuthorizationService {
    private static final Logger log = LogManager.getLogger(STSAuthorizationService.class);

    private final AWSSecurityTokenService service;
    private final PasswordCallback prompt;
    private final Host bookmark;
    private final HostPreferences preferences;

    public STSAuthorizationService(final Host bookmark, final X509TrustManager trust, final X509KeyManager key, final PasswordCallback prompt) {
        this.bookmark = bookmark;
        this.service = AWSSecurityTokenServiceClientBuilder
                .standard()
                .withEndpointConfiguration(new AwsClientBuilder.EndpointConfiguration(bookmark.getProtocol().getSTSEndpoint(), null))
                .withCredentials(new AWSStaticCredentialsProvider(new AnonymousAWSCredentials()))
                .withClientConfiguration(new CustomClientConfiguration(bookmark,
                        new ThreadLocalHostnameDelegatingTrustManager(trust, bookmark.getProtocol().getSTSEndpoint()), key))
                .build();
        this.prompt = prompt;
        this.preferences = HostPreferencesFactory.get(bookmark);
    }

    /**
     * Validate credentials by requesting caller identity
     *
     * @param credentials AWS credentials
     * @return User ID
     */
    public String validate(final Credentials credentials) throws BackgroundException {
        try {
            final GetCallerIdentityResult identity = service.getCallerIdentity(new GetCallerIdentityRequest()
                    .withRequestCredentialsProvider(S3CredentialsStrategy.toCredentialsProvider(credentials)));
            log.debug("Successfully verified credentials for {}", identity);
            return identity.getUserId();
        }
        catch(AWSSecurityTokenServiceException e) {
            throw new STSExceptionMappingService().map(e);
        }
    }

    public TemporaryAccessTokens getSessionToken(final Credentials credentials) throws BackgroundException {
        final PreferencesReader settings = new ProxyPreferencesReader(credentials, bookmark);
        //  The purpose of the sts:GetSessionToken operation is to authenticate the user using MFA.
        final GetSessionTokenRequest request = new GetSessionTokenRequest()
                .withRequestCredentialsProvider(S3CredentialsStrategy.toCredentialsProvider(credentials));
        final String mfaArn = settings.getProperty(Profile.STS_MFA_ARN_PROPERTY_KEY);
        if(StringUtils.isNotBlank(mfaArn)) {
            log.debug("Found MFA ARN {} for {}", mfaArn, bookmark);
            request.setSerialNumber(mfaArn);
        }
        else {
            if(bookmark.getProtocol().isMultiFactorConfigurable()) {
                // When defined in connection profile but with empty value
                log.debug("Prompt for MFA ARN");
                try {
                    final Credentials input = prompt.prompt(bookmark,
                            LocaleFactory.localizedString("MFA Device Identifier", "S3"),
                            LocaleFactory.localizedString("Provide additional login credentials", "Credentials"),
                            new LoginOptions().icon(bookmark.getProtocol().disk()).password(false)
                                    .passwordPlaceholder(LocaleFactory.localizedString("Serial Number or Amazon Resource Name (ARN)", "S3")));
                    if(input.isSaved()) {
                        preferences.setProperty(Profile.STS_MFA_ARN_PROPERTY_KEY, input.getPassword());
                    }
                    request.setSerialNumber(input.getPassword());
                }
                catch(LoginCanceledException e) {
                    log.warn("Canceled MFA ARN input for {}", bookmark);
                }
            }
        }
        if(request.getSerialNumber() != null) {
            log.debug("Prompt for MFA token code");
            final String tokenCode = prompt.prompt(
                    bookmark, String.format("%s %s", LocaleFactory.localizedString("Multi-Factor Authentication", "S3"),
                            mfaArn),
                    LocaleFactory.localizedString("Provide additional login credentials", "Credentials"),
                    new LoginOptions(bookmark.getProtocol())
                            .password(true)
                            .passwordPlaceholder(LocaleFactory.localizedString("MFA Authentication Code", "S3"))
                            .keychain(false)
            ).getPassword();
            if(StringUtils.isNotBlank(tokenCode)) {
                request.setTokenCode(tokenCode);
            }
        }
        log.debug("Request {} from {}", request, service);
        try {
            final GetSessionTokenResult result = service.getSessionToken(request);
            log.debug("Set credentials from {}", result);
            return new TemporaryAccessTokens(
                    result.getCredentials().getAccessKeyId(),
                    result.getCredentials().getSecretAccessKey(),
                    result.getCredentials().getSessionToken(),
                    result.getCredentials().getExpiration().getTime());
        }
        catch(AWSSecurityTokenServiceException e) {
            throw new STSExceptionMappingService().map(e);
        }
    }

    /**
     * Assume role with previously obtained AWS credentials
     * <p>
     * - Prompts for ARN for role to assume when missing
     * - Prompts for MFA token code when required
     *
     * @param credentials AWS static or session token credentials
     * @see Profile#STS_ROLE_ARN_PROPERTY_KEY
     * @see Profile#STS_MFA_ARN_PROPERTY_KEY
     */
    public TemporaryAccessTokens assumeRole(final Credentials credentials) throws BackgroundException {
        final PreferencesReader settings = new ProxyPreferencesReader(credentials, bookmark);
        final AssumeRoleRequest request = new AssumeRoleRequest()
                .withRequestCredentialsProvider(S3CredentialsStrategy.toCredentialsProvider(credentials));
        if(StringUtils.isNotBlank(settings.getProperty("s3.assumerole.durationseconds", Profile.STS_DURATION_SECONDS_PROPERTY_KEY))) {
            request.setDurationSeconds(PreferencesReader.toInteger(settings.getProperty("s3.assumerole.durationseconds", Profile.STS_DURATION_SECONDS_PROPERTY_KEY)));
        }
        request.setTags(settings.getMap(Profile.STS_TAGS_PROPERTY_KEY).entrySet().stream().map(
                entry -> new Tag().withKey(entry.getKey()).withValue(entry.getValue())).collect(Collectors.toList())
        );
        final String roleArn = settings.getProperty(Profile.STS_ROLE_ARN_PROPERTY_KEY, "s3.assumerole.rolearn");
        if(StringUtils.isNotBlank(roleArn)) {
            log.debug("Found Role ARN {} for {}", roleArn, bookmark);
            request.setRoleArn(roleArn);
        }
        else {
            if(bookmark.getProtocol().isRoleConfigurable()) {
                // When defined in connection profile but with empty value
                log.debug("Prompt for Role ARN");
                final Credentials input = prompt.prompt(bookmark,
                        LocaleFactory.localizedString("Role Amazon Resource Name (ARN)", "S3"),
                        LocaleFactory.localizedString("Provide additional login credentials", "Credentials"),
                        new LoginOptions().icon(bookmark.getProtocol().disk()).password(false)
                                .passwordPlaceholder(LocaleFactory.localizedString("Amazon Resource Name (ARN)", "S3")));
                if(input.isSaved()) {
                    preferences.setProperty(Profile.STS_ROLE_ARN_PROPERTY_KEY, input.getPassword());
                }
                request.setRoleArn(input.getPassword());
            }
        }
        final String mfaArn = settings.getProperty(Profile.STS_MFA_ARN_PROPERTY_KEY);
        if(StringUtils.isNotBlank(mfaArn)) {
            log.debug("Found MFA ARN {} for {}", mfaArn, bookmark);
            request.setSerialNumber(mfaArn);
        }
        else {
            if(bookmark.getProtocol().isMultiFactorConfigurable()) {
                // When defined in connection profile but with empty value
                log.debug("Prompt for MFA ARN");
                try {
                    final Credentials input = prompt.prompt(bookmark,
                            LocaleFactory.localizedString("MFA Device Identifier", "S3"),
                            LocaleFactory.localizedString("Provide additional login credentials", "Credentials"),
                            new LoginOptions().icon(bookmark.getProtocol().disk()).password(false)
                                    .passwordPlaceholder(LocaleFactory.localizedString("Serial Number or Amazon Resource Name (ARN)", "S3")));
                    if(input.isSaved()) {
                        preferences.setProperty(Profile.STS_MFA_ARN_PROPERTY_KEY, input.getPassword());
                    }
                    request.setSerialNumber(input.getPassword());
                }
                catch(LoginCanceledException e) {
                    log.warn("Canceled MFA ARN input for {}", bookmark);
                }
            }
        }
        if(request.getSerialNumber() != null) {
            log.debug("Prompt for MFA token code");
            final String tokenCode = prompt.prompt(
                    bookmark, String.format("%s %s", LocaleFactory.localizedString("Multi-Factor Authentication", "S3"),
                            mfaArn),
                    LocaleFactory.localizedString("Provide additional login credentials", "Credentials"),
                    new LoginOptions(bookmark.getProtocol())
                            .password(true)
                            .passwordPlaceholder(LocaleFactory.localizedString("MFA Authentication Code", "S3"))
                            .keychain(false)
            ).getPassword();
            if(StringUtils.isNotBlank(tokenCode)) {
                request.setTokenCode(tokenCode);
            }
        }
        if(StringUtils.isNotBlank(settings.getProperty(Profile.STS_ROLE_SESSION_NAME_PROPERTY_KEY, "s3.assumerole.rolesessionname"))) {
            request.setRoleSessionName(settings.getProperty(Profile.STS_ROLE_SESSION_NAME_PROPERTY_KEY, "s3.assumerole.rolesessionname"));
        }
        else {
            request.setRoleSessionName(new AsciiRandomStringService().random());
        }
        log.debug("Request {} from {}", request, service);
        try {
            final AssumeRoleResult result = service.assumeRole(request);
            log.debug("Received assume role identity result {}", result);
            return new TemporaryAccessTokens(result.getCredentials().getAccessKeyId(),
                    result.getCredentials().getSecretAccessKey(),
                    result.getCredentials().getSessionToken(),
                    result.getCredentials().getExpiration().getTime());
        }
        catch(AWSSecurityTokenServiceException e) {
            throw new STSExceptionMappingService().map(e);
        }
    }

    public TemporaryAccessTokens assumeRoleWithSAML(final Credentials credentials) throws BackgroundException {
        final PreferencesReader settings = new ProxyPreferencesReader(credentials, bookmark);
        final AssumeRoleWithSAMLRequest request = new AssumeRoleWithSAMLRequest().withSAMLAssertion(credentials.getToken());
        if(StringUtils.isNotBlank(settings.getProperty("s3.assumerole.durationseconds", Profile.STS_DURATION_SECONDS_PROPERTY_KEY))) {
            request.setDurationSeconds(PreferencesReader.toInteger(settings.getProperty("s3.assumerole.durationseconds", Profile.STS_DURATION_SECONDS_PROPERTY_KEY)));
        }
        request.setPolicy(settings.getProperty("s3.assumerole.policy"));
        final String roleArn = settings.getProperty(Profile.STS_ROLE_ARN_PROPERTY_KEY, "s3.assumerole.rolearn");
        if(StringUtils.isNotBlank(roleArn)) {
            log.debug("Found Role ARN {} for {}", roleArn, bookmark);
            request.setRoleArn(roleArn);
        }
        try {
            final AssumeRoleWithSAMLResult result = service.assumeRoleWithSAML(request);
            log.debug("Received assume role identity result {}", result);
            return new TemporaryAccessTokens(result.getCredentials().getAccessKeyId(),
                    result.getCredentials().getSecretAccessKey(),
                    result.getCredentials().getSessionToken(),
                    result.getCredentials().getExpiration().getTime());
        }
        catch(AWSSecurityTokenServiceException e) {
            throw new STSExceptionMappingService().map(e);
        }
    }

    /**
     * Assume role with web identity token
     *
     * @param credentials OIDC tokens
     * @return Temporary access tokens for the assumed role
     */
    public TemporaryAccessTokens assumeRoleWithWebIdentity(final Credentials credentials) throws BackgroundException {
        final PreferencesReader settings = new ProxyPreferencesReader(credentials, bookmark);
        final AssumeRoleWithWebIdentityRequest request = new AssumeRoleWithWebIdentityRequest();
        log.debug("Assume role with OIDC Id token for {}", bookmark);
        final String webIdentityToken = this.getWebIdentityToken(credentials.getOauth());
        request.setWebIdentityToken(webIdentityToken);
        if(StringUtils.isNotBlank(settings.getProperty("s3.assumerole.durationseconds", Profile.STS_DURATION_SECONDS_PROPERTY_KEY))) {
            request.setDurationSeconds(PreferencesReader.toInteger(settings.getProperty("s3.assumerole.durationseconds", Profile.STS_DURATION_SECONDS_PROPERTY_KEY)));
        }
        request.setPolicy(settings.getProperty("s3.assumerole.policy"));
        final String roleArn = settings.getProperty(Profile.STS_ROLE_ARN_PROPERTY_KEY, "s3.assumerole.rolearn");
        if(StringUtils.isNotBlank(roleArn)) {
            request.setRoleArn(roleArn);
        }
        else {
            if(bookmark.getProtocol().isRoleConfigurable()) {
                // When defined in connection profile but with empty value
                log.debug("Prompt for Role ARN");
                final Credentials input = prompt.prompt(bookmark,
                        LocaleFactory.localizedString("Role Amazon Resource Name (ARN)", "Credentials"),
                        LocaleFactory.localizedString("Provide additional login credentials", "Credentials"),
                        new LoginOptions().icon(bookmark.getProtocol().disk()).password(false)
                                .passwordPlaceholder(LocaleFactory.localizedString("Amazon Resource Name (ARN)", "S3")));
                if(input.isSaved()) {
                    preferences.setProperty(Profile.STS_ROLE_ARN_PROPERTY_KEY, input.getPassword());
                }
                request.setRoleArn(input.getPassword());
            }
        }
        final String sub;
        try {
            sub = JWT.decode(webIdentityToken).getSubject();
        }
        catch(JWTDecodeException e) {
            log.warn("Failure {} decoding JWT {}", e, webIdentityToken);
            throw new LoginFailureException("Invalid JWT or JSON format in authentication token", e);
        }
        if(StringUtils.isNotBlank(settings.getProperty(Profile.STS_ROLE_SESSION_NAME_PROPERTY_KEY, "s3.assumerole.rolesessionname"))) {
            request.setRoleSessionName(settings.getProperty(Profile.STS_ROLE_SESSION_NAME_PROPERTY_KEY, "s3.assumerole.rolesessionname"));
        }
        else {
            if(StringUtils.isNotBlank(sub)) {
                request.setRoleSessionName(sub);
            }
            else {
                log.warn("Missing subject in decoding JWT {}", webIdentityToken);
                request.setRoleSessionName(new AsciiRandomStringService().random());
            }
        }
        try {
            log.debug("Use request {}", request);
            final AssumeRoleWithWebIdentityResult result = service.assumeRoleWithWebIdentity(request);
            log.debug("Received assume role identity result {}", result);
            return new TemporaryAccessTokens(result.getCredentials().getAccessKeyId(),
                    result.getCredentials().getSecretAccessKey(),
                    result.getCredentials().getSessionToken(),
                    result.getCredentials().getExpiration().getTime());
        }
        catch(AWSSecurityTokenServiceException e) {
            throw new STSExceptionMappingService().map(e);
        }
    }

    /**
     * Get web identity token from OIDC tokens in JWT format
     *
     * @param oauth OIDC tokens
     * @return OIDC Id token value
     */
    protected String getWebIdentityToken(final OAuthTokens oauth) {
        return oauth.getIdToken();
    }
}
