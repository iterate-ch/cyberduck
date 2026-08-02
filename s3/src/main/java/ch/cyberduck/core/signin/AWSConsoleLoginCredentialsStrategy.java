package ch.cyberduck.core.signin;

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

import ch.cyberduck.core.Credentials;
import ch.cyberduck.core.DefaultIOExceptionMappingService;
import ch.cyberduck.core.Host;
import ch.cyberduck.core.LoginCallback;
import ch.cyberduck.core.PasswordStore;
import ch.cyberduck.core.PasswordStoreFactory;
import ch.cyberduck.core.TemporaryAccessTokens;
import ch.cyberduck.core.exception.AccessDeniedException;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.exception.LoginCanceledException;
import ch.cyberduck.core.exception.LoginFailureException;
import ch.cyberduck.core.http.DefaultHttpResponseExceptionMappingService;
import ch.cyberduck.core.oauth.LoopbackOAuth2AuthorizationCodeProvider;
import ch.cyberduck.core.s3.S3CredentialsStrategy;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.client.HttpClient;
import org.apache.http.client.HttpResponseException;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.AbstractResponseHandler;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.text.ParseException;
import java.util.Base64;
import java.util.Date;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

import com.auth0.jwt.JWT;
import com.auth0.jwt.exceptions.JWTDecodeException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JOSEObjectType;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.crypto.ECDSASigner;
import com.nimbusds.jose.jwk.Curve;
import com.nimbusds.jose.jwk.ECKey;
import com.nimbusds.jose.jwk.gen.ECKeyGenerator;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;

public class AWSConsoleLoginCredentialsStrategy implements S3CredentialsStrategy {
    private static final Logger log = LogManager.getLogger(AWSConsoleLoginCredentialsStrategy.class);
    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static final SecureRandom RANDOM = new SecureRandom();
    static final String CLIENT_ID = "arn:aws:signin:::devtools/same-device";
    static final String IDENTITY_PROPERTY = "s3.login.identity";
    private static final String SERVICE = "AWS Console Sign-In";

    private final HttpClient client;
    private final Host host;
    private final LoginCallback prompt;
    private final PasswordStore store;
    private final String endpoint;

    private TemporaryAccessTokens tokens = TemporaryAccessTokens.EMPTY;
    private String refreshToken;
    private ECKey privateKey;
    private boolean loaded;

    public AWSConsoleLoginCredentialsStrategy(final HttpClient client, final Host host, final LoginCallback prompt) {
        this(client, host, prompt, PasswordStoreFactory.get());
    }

    protected AWSConsoleLoginCredentialsStrategy(final HttpClient client, final Host host,
                                                 final LoginCallback prompt, final PasswordStore store) {
        this.client = client;
        this.host = host;
        this.prompt = prompt;
        this.store = store;
        final String region = StringUtils.defaultIfBlank(host.getRegion(), "us-east-1");
        if(!StringUtils.containsOnly(region, "abcdefghijklmnopqrstuvwxyz0123456789-")) {
            throw new IllegalArgumentException("Invalid AWS region");
        }
        this.endpoint = String.format("https://%s.signin.aws.amazon.com", region);
    }

    @Override
    public synchronized Credentials get() throws BackgroundException {
        if(tokens.isExpired()) {
            this.load();
            if(StringUtils.isBlank(refreshToken) || null == privateKey) {
                tokens = this.authorize();
            }
            else {
                try {
                    tokens = this.refresh();
                }
                catch(LoginFailureException e) {
                    log.warn("AWS sign-in session expired for {}", host);
                    tokens = this.authorize();
                }
            }
        }
        return new Credentials().setTokens(tokens).setSaved(false);
    }

    protected TemporaryAccessTokens authorize() throws BackgroundException {
        final ECKey key;
        try {
            key = new ECKeyGenerator(Curve.P_256).generate();
        }
        catch(JOSEException e) {
            throw failure(e);
        }
        final byte[] random = new byte[48];
        RANDOM.nextBytes(random);
        final String verifier = Base64.getUrlEncoder().withoutPadding().encodeToString(random);
        final String challenge = Base64.getUrlEncoder().withoutPadding().encodeToString(
                DigestUtils.sha256(verifier.getBytes(StandardCharsets.US_ASCII)));
        final String state = UUID.randomUUID().toString();
        final AtomicReference<String> redirectUri = new AtomicReference<>();
        final String code = new LoopbackOAuth2AuthorizationCodeProvider().prompt(host, prompt,
                        redirect -> {
                            redirectUri.set(redirect);
                            return new URIBuilder(URI.create(String.format("%s/v1/authorize", endpoint)))
                                .addParameter("response_type", "code")
                                .addParameter("client_id", CLIENT_ID)
                                .addParameter("state", state)
                                .addParameter("code_challenge", challenge)
                                .addParameter("code_challenge_method", "SHA-256")
                                .addParameter("scope", "openid")
                                .addParameter("redirect_uri", redirect).toString();
                        }, state);
        if(StringUtils.isBlank(code)) {
            throw new LoginCanceledException();
        }
        final ObjectNode request = MAPPER.createObjectNode().put("clientId", CLIENT_ID)
                .put("grantType", "authorization_code").put("code", code)
                .put("codeVerifier", verifier).put("redirectUri", redirectUri.get());
        return this.accept(this.exchange(request, key), key, true);
    }

    private TemporaryAccessTokens refresh() throws BackgroundException {
        final ObjectNode request = MAPPER.createObjectNode().put("clientId", CLIENT_ID)
                .put("grantType", "refresh_token").put("refreshToken", refreshToken);
        return this.accept(this.exchange(request, privateKey), privateKey, false);
    }

    private JsonNode exchange(final ObjectNode body, final ECKey key) throws BackgroundException {
        final String url = String.format("%s/v1/token", endpoint);
        final HttpPost request = new HttpPost(url);
        try {
            request.setHeader("DPoP", proof(key, url));
            request.setEntity(new StringEntity(MAPPER.writeValueAsString(body), ContentType.APPLICATION_JSON));
            return client.execute(request, new AbstractResponseHandler<JsonNode>() {
                @Override
                public JsonNode handleEntity(final HttpEntity entity) throws IOException {
                    return MAPPER.readTree(entity.getContent());
                }
            });
        }
        catch(HttpResponseException e) {
            if(e.getStatusCode() >= 400 && e.getStatusCode() < 500 && e.getStatusCode() != 429) {
                throw failure(e);
            }
            throw new DefaultHttpResponseExceptionMappingService().map(e);
        }
        catch(IOException e) {
            throw new DefaultIOExceptionMappingService().map(e);
        }
    }

    private TemporaryAccessTokens accept(final JsonNode response, final ECKey key,
                                         final boolean validateIdentity) throws LoginFailureException {
        if(null == response) {
            throw failure();
        }
        final JsonNode access = response.path("accessToken");
        final String accessKey = access.path("accessKeyId").asText();
        final String secretKey = access.path("secretAccessKey").asText();
        final String sessionToken = access.path("sessionToken").asText();
        final String refresh = response.path("refreshToken").asText();
        final long expires = response.path("expiresIn").asLong(-1L);
        if(StringUtils.isAnyBlank(accessKey, secretKey, sessionToken, refresh) || expires <= 0L) {
            throw failure();
        }
        if(validateIdentity) {
            validateIdentity(host, response.path("idToken").asText());
        }
        refreshToken = refresh;
        privateKey = key;
        this.save();
        return new TemporaryAccessTokens(accessKey, secretKey, sessionToken,
                System.currentTimeMillis() + expires * 1000L - 5L * 60L * 1000L);
    }

    private void load() {
        if(loaded || !host.getCredentials().isSaved()) {
            loaded = true;
            return;
        }
        loaded = true;
        try {
            refreshToken = store.getPassword(SERVICE, this.account("Refresh Token"));
            final String key = store.getPassword(SERVICE, this.account("DPoP Private Key"));
            if(StringUtils.isBlank(refreshToken) || StringUtils.isBlank(key)) {
                refreshToken = null;
                return;
            }
            privateKey = ECKey.parse(key);
        }
        catch(AccessDeniedException | ParseException e) {
            log.warn("Failure loading AWS sign-in session for {}", host);
            refreshToken = null;
            privateKey = null;
        }
    }

    private void save() {
        if(!host.getCredentials().isSaved()) {
            return;
        }
        try {
            store.addPassword(SERVICE, this.account("DPoP Private Key"), privateKey.toJSONString());
            store.addPassword(SERVICE, this.account("Refresh Token"), refreshToken);
        }
        catch(AccessDeniedException e) {
            log.warn("Failure saving AWS sign-in session for {}", host);
        }
    }

    private String account(final String secret) {
        return String.format("%s %s", host.getUuid(), secret);
    }

    private static String proof(final ECKey key, final String url) throws LoginFailureException {
        try {
            final SignedJWT jwt = new SignedJWT(new JWSHeader.Builder(JWSAlgorithm.ES256)
                            .type(new JOSEObjectType("dpop+jwt")).jwk(key.toPublicJWK()).build(),
                    new JWTClaimsSet.Builder().claim("htm", "POST").claim("htu", url)
                            .issueTime(new Date()).jwtID(UUID.randomUUID().toString()).build());
            jwt.sign(new ECDSASigner(key));
            return jwt.serialize();
        }
        catch(JOSEException e) {
            throw failure(e);
        }
    }

    private static String identity(final String account, final String arn) {
        String resource = StringUtils.substringAfterLast(arn, ":");
        if(StringUtils.startsWith(resource, "assumed-role/")) {
            resource = String.format("role/%s", StringUtils.substringBeforeLast(
                    StringUtils.removeStart(resource, "assumed-role/"), "/"));
        }
        return String.format("%s/%s", account, resource);
    }

    static void validateIdentity(final Host host, final String idToken) throws LoginFailureException {
        try {
            final String arn = JWT.decode(idToken).getSubject();
            validateIdentity(host, StringUtils.substringBetween(arn, "::", ":"), arn);
        }
        catch(JWTDecodeException e) {
            throw failure(e);
        }
    }

    static void validateIdentity(final Host host, final String account, final String arn) throws LoginFailureException {
        if(StringUtils.isAnyBlank(account, arn)) {
            throw failure();
        }
        final String selected = identity(account, arn);
        final String pinned = host.getProperty(IDENTITY_PROPERTY);
        if(StringUtils.isNotBlank(pinned) && !StringUtils.equals(pinned, selected)) {
            throw new LoginFailureException(String.format(
                    "AWS identity %s does not match this bookmark (%s). "
                            + "Create a new bookmark to use the other identity.", selected, pinned));
        }
        host.setProperty(IDENTITY_PROPERTY, selected);
        if(StringUtils.isBlank(host.getNickname())) {
            host.setNickname(selected);
        }
    }

    private static LoginFailureException failure() {
        return failure(null);
    }

    private static LoginFailureException failure(final Throwable cause) {
        return new LoginFailureException("AWS browser sign-in failed.", cause);
    }
}
