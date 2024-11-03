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
import ch.cyberduck.core.LoginCallback;
import ch.cyberduck.core.LoginOptions;
import ch.cyberduck.core.OAuthTokens;
import ch.cyberduck.core.TemporaryAccessTokens;
import ch.cyberduck.core.aws.CustomClientConfiguration;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.exception.LoginFailureException;
import ch.cyberduck.core.preferences.HostPreferences;
import ch.cyberduck.core.ssl.ThreadLocalHostnameDelegatingTrustManager;
import ch.cyberduck.core.ssl.X509KeyManager;
import ch.cyberduck.core.ssl.X509TrustManager;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.AnonymousAWSCredentials;
import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.securitytoken.AWSSecurityTokenService;
import com.amazonaws.services.securitytoken.AWSSecurityTokenServiceClientBuilder;
import com.amazonaws.services.securitytoken.model.AWSSecurityTokenServiceException;
import com.amazonaws.services.securitytoken.model.AssumeRoleWithSAMLRequest;
import com.amazonaws.services.securitytoken.model.AssumeRoleWithSAMLResult;
import com.amazonaws.services.securitytoken.model.AssumeRoleWithWebIdentityRequest;
import com.amazonaws.services.securitytoken.model.AssumeRoleWithWebIdentityResult;
import com.auth0.jwt.JWT;
import com.auth0.jwt.exceptions.JWTDecodeException;

public class STSAssumeRoleAuthorizationService {
    private static final Logger log = LogManager.getLogger(STSAssumeRoleAuthorizationService.class);

    private final AWSSecurityTokenService service;
    private final LoginCallback prompt;
    private final Host bookmark;

    public STSAssumeRoleAuthorizationService(final Host bookmark, final X509TrustManager trust, final X509KeyManager key, final LoginCallback prompt) {
        this(bookmark, AWSSecurityTokenServiceClientBuilder
                .standard()
                .withEndpointConfiguration(new AwsClientBuilder.EndpointConfiguration(bookmark.getProtocol().getSTSEndpoint(), null))
                .withCredentials(new AWSStaticCredentialsProvider(new AnonymousAWSCredentials()))
                .withClientConfiguration(new CustomClientConfiguration(bookmark,
                        new ThreadLocalHostnameDelegatingTrustManager(trust, bookmark.getProtocol().getSTSEndpoint()), key))
                .build(), prompt);
    }

    public STSAssumeRoleAuthorizationService(final Host bookmark, final AWSSecurityTokenService service, final LoginCallback prompt) {
        this.bookmark = bookmark;
        this.service = service;
        this.prompt = prompt;
    }

    public TemporaryAccessTokens authorize(final String sAMLAssertion) throws BackgroundException {
        final AssumeRoleWithSAMLRequest request = new AssumeRoleWithSAMLRequest().withSAMLAssertion(sAMLAssertion);
        final HostPreferences preferences = new HostPreferences(bookmark);
        if(preferences.getInteger("s3.assumerole.durationseconds") != -1) {
            request.setDurationSeconds(preferences.getInteger("s3.assumerole.durationseconds"));
        }
        if(StringUtils.isNotBlank(preferences.getProperty("s3.assumerole.policy"))) {
            request.setPolicy(preferences.getProperty("s3.assumerole.policy"));
        }
        if(StringUtils.isNotBlank(preferences.getProperty("s3.assumerole.rolearn"))) {
            request.setRoleArn(preferences.getProperty("s3.assumerole.rolearn"));
        }
        try {
            final AssumeRoleWithSAMLResult result = service.assumeRoleWithSAML(request);
            if(log.isDebugEnabled()) {
                log.debug("Received assume role identity result {}", result);
            }
            final Credentials credentials = bookmark.getCredentials();
            final TemporaryAccessTokens tokens = new TemporaryAccessTokens(result.getCredentials().getAccessKeyId(),
                    result.getCredentials().getSecretAccessKey(),
                    result.getCredentials().getSessionToken(),
                    result.getCredentials().getExpiration().getTime());
            credentials.setTokens(tokens);
            return tokens;
        }
        catch(AWSSecurityTokenServiceException e) {
            throw new STSExceptionMappingService().map(e);
        }
    }

    public TemporaryAccessTokens authorize(final OAuthTokens oauth) throws BackgroundException {
        final AssumeRoleWithWebIdentityRequest request = new AssumeRoleWithWebIdentityRequest();
        if(log.isDebugEnabled()) {
            log.debug("Assume role with OIDC Id token for {}", bookmark);
        }
        final String webIdentityToken = this.getWebIdentityToken(oauth);
        request.setWebIdentityToken(webIdentityToken);
        final HostPreferences preferences = new HostPreferences(bookmark);
        if(preferences.getInteger("s3.assumerole.durationseconds") != -1) {
            request.setDurationSeconds(preferences.getInteger("s3.assumerole.durationseconds"));
        }
        if(StringUtils.isNotBlank(preferences.getProperty("s3.assumerole.policy"))) {
            request.setPolicy(preferences.getProperty("s3.assumerole.policy"));
        }
        if(StringUtils.isNotBlank(preferences.getProperty("s3.assumerole.rolearn"))) {
            request.setRoleArn(preferences.getProperty("s3.assumerole.rolearn"));
        }
        else {
            if(StringUtils.EMPTY.equals(preferences.getProperty("s3.assumerole.rolearn"))) {
                // When defined in connection profile but with empty value
                if(log.isDebugEnabled()) {
                    log.debug("Prompt for Role ARN");
                }
                final Credentials input = prompt.prompt(bookmark,
                        LocaleFactory.localizedString("Role Amazon Resource Name (ARN)", "Credentials"),
                        LocaleFactory.localizedString("Provide additional login credentials", "Credentials"),
                        new LoginOptions().icon(bookmark.getProtocol().disk()));
                if(input.isSaved()) {
                    bookmark.setProperty("s3.assumerole.rolearn", input.getPassword());
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
        if(StringUtils.isNotBlank(preferences.getProperty("s3.assumerole.rolesessionname"))) {
            request.setRoleSessionName(preferences.getProperty("s3.assumerole.rolesessionname"));
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
            if(log.isDebugEnabled()) {
                log.debug("Use request {}", request);
            }
            final AssumeRoleWithWebIdentityResult result = service.assumeRoleWithWebIdentity(request);
            if(log.isDebugEnabled()) {
                log.debug("Received assume role identity result {}", result);
            }
            return new TemporaryAccessTokens(result.getCredentials().getAccessKeyId(),
                    result.getCredentials().getSecretAccessKey(),
                    result.getCredentials().getSessionToken(),
                    result.getCredentials().getExpiration().getTime());
        }
        catch(AWSSecurityTokenServiceException e) {
            throw new STSExceptionMappingService().map(e);
        }
    }

    protected String getWebIdentityToken(final OAuthTokens oauth) {
        return oauth.getIdToken();
    }
}
