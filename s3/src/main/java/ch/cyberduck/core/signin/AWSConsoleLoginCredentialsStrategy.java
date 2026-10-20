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
import ch.cyberduck.core.HostPasswordStore;
import ch.cyberduck.core.LoginCallback;
import ch.cyberduck.core.LoginOptions;
import ch.cyberduck.core.OAuthTokens;
import ch.cyberduck.core.PasswordStoreFactory;
import ch.cyberduck.core.TemporaryAccessTokens;
import ch.cyberduck.core.UUIDRandomStringService;
import ch.cyberduck.core.exception.AccessDeniedException;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.exception.InteroperabilityException;
import ch.cyberduck.core.exception.LoginCanceledException;
import ch.cyberduck.core.exception.LoginFailureException;
import ch.cyberduck.core.oauth.LoopbackOAuth2AuthorizationCodeProvider;
import ch.cyberduck.core.oauth.OAuth2AuthorizationService;
import ch.cyberduck.core.random.SecureRandomProviderFactory;
import ch.cyberduck.core.s3.S3CredentialsStrategy;
import ch.cyberduck.core.threading.BackgroundAction;
import ch.cyberduck.core.threading.CancelCallback;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.client.HttpResponseException;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.util.EntityUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.util.Base64;
import java.util.Date;
import java.util.UUID;
import java.util.concurrent.locks.ReentrantLock;

import com.auth0.jwt.JWT;
import com.auth0.jwt.exceptions.JWTDecodeException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.MissingNode;
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

    public static final String CLIENT_ID = "arn:aws:signin:::devtools/same-device";
    private static final String DPOP_HEADER_NAME = "DPoP";

    private final ReentrantLock lock = new ReentrantLock();

    private final HttpClient client;
    private final Host host;
    private final LoginCallback prompt;
    private final HostPasswordStore store = PasswordStoreFactory.get();
    private final CancelCallback cancel;

    public AWSConsoleLoginCredentialsStrategy(final HttpClient client, final Host host, final LoginCallback prompt, final CancelCallback cancel) {
        this.client = client;
        this.host = host;
        this.prompt = prompt;
        this.cancel = cancel;
    }

    @Override
    public Credentials get() throws BackgroundException {
        lock.lock();
        try {
            final Credentials credentials = host.getCredentials();
            final TemporaryAccessTokens tokens = credentials.getTokens();
            if(tokens.isExpired()) {
                log.debug("Refresh expired tokens {} for {}", tokens, host);
                // Containing private key and refresh token
                final OAuthTokens oauth = credentials.getOauth();
                AWSConsoleLoginTokens console;
                if(oauth.validate()) {
                    try {
                        final AWSConsoleLoginTokens saved = new AWSConsoleLoginTokens(oauth);
                        log.debug("Refresh expired tokens {} for {} with {}", tokens, host, saved);
                        console = this.refresh(credentials.getUsername(),
                                saved.getPrivateKey(), saved.getOauth().getRefreshToken());
                    }
                    catch(LoginFailureException | InteroperabilityException | AccessDeniedException e) {
                        log.warn("AWS sign-in refresh failed for {}", host);
                        console = this.authorize(cancel);
                    }

                }
                else {
                    log.warn("No previous AWS sign-in for {}", host);
                    console = this.authorize(cancel);
                }
                return credentials
                        .setOauth(console.getOauth())
                        .setTokens(console.getTemporary());
            }
            return credentials;
        }
        finally {
            lock.unlock();
        }
    }

    /**
     * Initiates the OAuth 2.0 authorization flow to generate AWS Console login tokens.
     * This method performs the following steps:
     * 1. Generates a cryptographic key pair (ECKey) and a random code verifier.
     * 2. Constructs a code challenge based on the code verifier and sends an authorization
     * request to the OAuth 2.0 server.
     * 3. Prompts the user to complete the authentication process.
     * 4. Validates the authorization code returned, generates an authentication request
     * with the server, and retrieves AWS Console login tokens.
     *
     * @return An instance of {@code AWSConsoleLoginTokens} containing temporary access tokens,
     * a refresh token, and identity information (ARN and private key) for subsequent
     * AWS Console login sessions.
     * @throws BackgroundException If an error occurs during the authorization flow such as
     *                             network issues, user cancelation, invalid responses, or cryptographic failures.
     */
    protected AWSConsoleLoginTokens authorize(final CancelCallback cancel) throws BackgroundException {
        if(cancel.getContext() == BackgroundAction.Context.scheduled) {
            // Do not open web browser when user interaction is not possible
            log.warn("Skip interactive flow for {} from scheduled action", host);
            throw new LoginCanceledException();
        }
        try {
            final ECKey key = new ECKeyGenerator(Curve.P_256).generate();
            final byte[] random = new byte[48];
            SecureRandomProviderFactory.get().provide().nextBytes(random);
            final String verifier = Base64.getUrlEncoder().withoutPadding().encodeToString(random);
            final String challenge = Base64.getUrlEncoder().withoutPadding().encodeToString(
                    DigestUtils.sha256(verifier.getBytes(StandardCharsets.US_ASCII)));
            final String state = new UUIDRandomStringService().random();
            final String redirectUri = OAuth2AuthorizationService.toRedirectUri(host.getProtocol().getOAuthRedirectUrl());
            final String code = new LoopbackOAuth2AuthorizationCodeProvider().prompt(host, prompt,
                    new URIBuilder(URI.create(host.getProtocol().getOAuthAuthorizationUrl()))
                            .addParameter("response_type", "code")
                            .addParameter("client_id", host.getProtocol().getOAuthClientId())
                            .addParameter("state", state)
                            .addParameter("code_challenge", challenge)
                            .addParameter("code_challenge_method", "SHA-256")
                            .addParameter("scope", "openid")
                            .addParameter("redirect_uri", redirectUri).toString(), redirectUri, state);
            if(StringUtils.isBlank(code)) {
                throw new LoginCanceledException();
            }
            final ObjectNode request = MAPPER.createObjectNode()
                    .put("clientId", host.getProtocol().getOAuthClientId())
                    .put("grantType", "authorization_code")
                    .put("code", code)
                    .put("codeVerifier", verifier)
                    .put("redirectUri", redirectUri);
            return this.save(this.exchange(request, key));
        }
        catch(JOSEException e) {
            throw new InteroperabilityException(e.getMessage(), e);
        }
    }

    private AWSConsoleLoginTokens refresh(final String arn, final ECKey privateKey, final String refreshToken) throws BackgroundException {
        final ObjectNode request = MAPPER.createObjectNode()
                .put("clientId", host.getProtocol().getOAuthClientId())
                .put("grantType", "refresh_token")
                .put("refreshToken", refreshToken);
        return this.save(this.exchange(request, privateKey).setArn(arn));
    }

    /**
     * Exchanges an authentication request with the server to retrieve AWS Console login tokens.
     * The tokens include temporary access credentials and a refresh token that can be used
     * for subsequent reauthentication requests. Additionally, the method generates a DPoP proof
     * for the current request using the provided ECKey.
     *
     * @param body       The JSON object containing the authentication payload to be sent to the server.
     * @param privateKey The elliptic curve cryptographic key used to generate the DPoP (Demonstration of Proof-of-Possession) proof.
     * @return An instance of {@code AWSConsoleLoginTokens} containing temporary access credentials,
     * a refresh token, and the decoded AWS ARN identity.
     * @throws BackgroundException If the exchange process fails due to network issues, invalid responses,
     *                             or errors during authentication.
     */
    private AWSConsoleLoginTokens exchange(final ObjectNode body, final ECKey privateKey) throws BackgroundException {
        final HttpPost request = new HttpPost(host.getProtocol().getOAuthTokenUrl());
        try {
            request.setHeader(DPOP_HEADER_NAME, proof(privateKey, request.getURI().toString()));
            request.setEntity(new StringEntity(MAPPER.writeValueAsString(body), ContentType.APPLICATION_JSON));
            final JsonNode response = client.execute(request, new ResponseHandler<JsonNode>() {
                @Override
                public JsonNode handleResponse(final HttpResponse response) throws IOException {
                    final StatusLine status = response.getStatusLine();
                    final HttpEntity entity = response.getEntity();
                    if(status.getStatusCode() >= HttpStatus.SC_BAD_REQUEST) {
                        JsonNode details = MissingNode.getInstance();
                        try {
                            if(null != entity) {
                                details = MAPPER.readTree(entity.getContent());
                            }
                        }
                        catch(IOException e) {
                            log.warn("Failure {} parsing error response", e.toString());
                        }
                        finally {
                            EntityUtils.consumeQuietly(entity);
                        }
                        throw new AWSConsoleLoginExceptionMappingService.ErrorResponseException(
                                status.getStatusCode(), status.getReasonPhrase(),
                                details.path("error").asText(null),
                                details.path("message").asText(null));
                    }
                    return null == entity ? MissingNode.getInstance() : MAPPER.readTree(entity.getContent());
                }
            });
            final String accessKey = response.path("accessToken").path("accessKeyId").asText();
            final String secretKey = response.path("accessToken").path("secretAccessKey").asText();
            final String sessionToken = response.path("accessToken").path("sessionToken").asText();
            final String refreshToken = response.path("refreshToken").asText();
            // Maximum value is 900 seconds (15 minutes)
            final long expires = response.path("expiresIn").asLong(-1L);
            final JsonNode idToken = response.path("idToken");
            final String identity;
            if(idToken.isMissingNode()) {
                // No IdToken when refreshing
                identity = null;
            }
            else {
                identity = arn(idToken.asText());
            }
            return new AWSConsoleLoginTokens(privateKey, refreshToken,
                    new TemporaryAccessTokens(accessKey, secretKey, sessionToken,
                            System.currentTimeMillis() + expires * 1000)).setArn(identity);
        }
        catch(HttpResponseException e) {
            throw new AWSConsoleLoginExceptionMappingService().map(e);
        }
        catch(IOException e) {
            throw new DefaultIOExceptionMappingService().map(e);
        }
    }

    protected AWSConsoleLoginTokens save(final AWSConsoleLoginTokens tokens) throws AccessDeniedException {
        final Credentials credentials = host.getCredentials();
        if(StringUtils.isNotBlank(tokens.getArn())) {
            // No id token returned on refresh. Keep previously saved ARN
            credentials.setUsername(tokens.getArn());
        }
        credentials
                .setOauth(tokens.getOauth())
                .setTokens(tokens.getTemporary())
                .setSaved(new LoginOptions().save);
        if(credentials.isSaved()) {
            store.save(host);
        }
        return tokens;
    }

    public static final class AWSConsoleLoginTokens {

        public static final AWSConsoleLoginTokens EMPTY
                = new AWSConsoleLoginTokens(null, null, -1L, TemporaryAccessTokens.EMPTY);

        private final OAuthTokens oauth;
        /**
         * Temporary short-lived S3 access credentials not saved
         */
        private final TemporaryAccessTokens temporary;

        private String arn;

        public AWSConsoleLoginTokens(final OAuthTokens oauth) {
            this.oauth = oauth;
            this.temporary = TemporaryAccessTokens.EMPTY;
        }

        public AWSConsoleLoginTokens(final ECKey privateKey, final String refreshToken,
                                     final TemporaryAccessTokens temporary) {
            this(privateKey.toJSONString(), refreshToken, -1L, temporary);
        }

        public AWSConsoleLoginTokens(final String accessToken, final String refreshToken, final Long expiryInMilliseconds,
                                     final TemporaryAccessTokens temporary) {
            this.oauth = new OAuthTokens(accessToken, refreshToken, expiryInMilliseconds);
            this.temporary = temporary;
        }

        public OAuthTokens getOauth() {
            return oauth;
        }

        public TemporaryAccessTokens getTemporary() {
            return temporary;
        }

        public ECKey getPrivateKey() throws AccessDeniedException {
            try {
                return ECKey.parse(oauth.getAccessToken());
            }
            catch(ParseException e) {
                throw new AccessDeniedException(e.getMessage(), e);
            }
        }

        public AWSConsoleLoginTokens setArn(String arn) {
            this.arn = arn;
            return this;
        }

        public String getArn() {
            return arn;
        }

        @Override
        public String toString() {
            final StringBuilder sb = new StringBuilder("AWSConsoleLoginTokens{");
            sb.append("oauth=").append(oauth);
            sb.append(", temporary=").append(temporary);
            sb.append(", arn='").append(arn).append('\'');
            sb.append('}');
            return sb.toString();
        }
    }

    /**
     * Generates a DPoP (Demonstration of Proof-of-Possession) proof JWT (JSON Web Token)
     * used for authenticating HTTP requests. This proof is signed using the provided
     * ECKey and includes claims such as HTTP method, target URL, issue time, and a unique identifier.
     *
     * @param key The elliptic curve cryptographic key used to sign the JWT.
     * @param url The target URL for which the proof is being generated.
     * @return The serialized and signed JWT string.
     * @throws InteroperabilityException If an error occurs while generating or signing the JWT.
     */
    protected static String proof(final ECKey key, final String url) throws InteroperabilityException {
        try {
            final SignedJWT jwt = new SignedJWT(new JWSHeader.Builder(JWSAlgorithm.ES256)
                    .type(new JOSEObjectType("dpop+jwt")).jwk(key.toPublicJWK()).build(),
                    new JWTClaimsSet.Builder().claim("htm", "POST").claim("htu", url)
                            .issueTime(new Date()).jwtID(UUID.randomUUID().toString()).build());
            jwt.sign(new ECDSASigner(key));
            return jwt.serialize();
        }
        catch(JOSEException e) {
            throw new InteroperabilityException(e.getMessage(), e);
        }
    }

    /**
     * Decodes a JSON Web Token (JWT) to extract the subject, then parses the Amazon Resource Name (ARN)
     * from the subject and constructs a formatted identity string by delegating to another identity method.
     *
     * @param idToken The JWT to be decoded containing the subject with ARN details.
     * @return A formatted identity string combining the account and resource details parsed from the ARN.
     * @throws InteroperabilityException If the JWT cannot be decoded or processed.
     */
    protected static String arn(final String idToken) throws InteroperabilityException {
        try {
            return JWT.decode(idToken).getSubject();
        }
        catch(JWTDecodeException e) {
            log.warn("Failure {} decoding JWT {}", e, idToken);
            throw new InteroperabilityException(e.getMessage(), e);
        }
    }
}
