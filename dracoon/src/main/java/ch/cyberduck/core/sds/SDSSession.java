package ch.cyberduck.core.sds;

/*
 * Copyright (c) 2002-2017 iterate GmbH. All rights reserved.
 * https://cyberduck.io/
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
 */

import ch.cyberduck.core.ConnectionTimeoutFactory;
import ch.cyberduck.core.Credentials;
import ch.cyberduck.core.DefaultIOExceptionMappingService;
import ch.cyberduck.core.ExpiringObjectHolder;
import ch.cyberduck.core.Host;
import ch.cyberduck.core.HostKeyCallback;
import ch.cyberduck.core.HostUrlProvider;
import ch.cyberduck.core.ListService;
import ch.cyberduck.core.LocaleFactory;
import ch.cyberduck.core.LoginCallback;
import ch.cyberduck.core.PreferencesUseragentProvider;
import ch.cyberduck.core.Scheme;
import ch.cyberduck.core.UrlProvider;
import ch.cyberduck.core.Version;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.exception.InteroperabilityException;
import ch.cyberduck.core.exception.LoginCanceledException;
import ch.cyberduck.core.features.*;
import ch.cyberduck.core.http.CustomServiceUnavailableRetryStrategy;
import ch.cyberduck.core.http.DefaultHttpRateLimiter;
import ch.cyberduck.core.http.ExecutionCountServiceUnavailableRetryStrategy;
import ch.cyberduck.core.http.HttpSession;
import ch.cyberduck.core.http.RateLimitingHttpRequestInterceptor;
import ch.cyberduck.core.jersey.HttpComponentsProvider;
import ch.cyberduck.core.oauth.OAuth2AuthorizationService;
import ch.cyberduck.core.oauth.OAuth2ErrorResponseInterceptor;
import ch.cyberduck.core.oauth.OAuth2RequestInterceptor;
import ch.cyberduck.core.preferences.HostPreferences;
import ch.cyberduck.core.preferences.PreferencesReader;
import ch.cyberduck.core.proxy.ProxyFinder;
import ch.cyberduck.core.sds.io.swagger.client.ApiException;
import ch.cyberduck.core.sds.io.swagger.client.JSON;
import ch.cyberduck.core.sds.io.swagger.client.api.ConfigApi;
import ch.cyberduck.core.sds.io.swagger.client.api.PublicApi;
import ch.cyberduck.core.sds.io.swagger.client.api.UserApi;
import ch.cyberduck.core.sds.io.swagger.client.model.AlgorithmVersionInfo;
import ch.cyberduck.core.sds.io.swagger.client.model.AlgorithmVersionInfoList;
import ch.cyberduck.core.sds.io.swagger.client.model.ClassificationPoliciesConfig;
import ch.cyberduck.core.sds.io.swagger.client.model.CreateKeyPairRequest;
import ch.cyberduck.core.sds.io.swagger.client.model.GeneralSettingsInfo;
import ch.cyberduck.core.sds.io.swagger.client.model.SoftwareVersionData;
import ch.cyberduck.core.sds.io.swagger.client.model.SystemDefaults;
import ch.cyberduck.core.sds.io.swagger.client.model.UserAccount;
import ch.cyberduck.core.sds.io.swagger.client.model.UserKeyPairContainer;
import ch.cyberduck.core.sds.triplecrypt.TripleCryptConverter;
import ch.cyberduck.core.sds.triplecrypt.TripleCryptExceptionMappingService;
import ch.cyberduck.core.sds.triplecrypt.TripleCryptKeyPair;
import ch.cyberduck.core.shared.DefaultUploadFeature;
import ch.cyberduck.core.ssl.X509KeyManager;
import ch.cyberduck.core.ssl.X509TrustManager;
import ch.cyberduck.core.threading.CancelCallback;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpException;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpRequest;
import org.apache.http.HttpRequestInterceptor;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpRequestWrapper;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.protocol.HttpContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.client.ClientProperties;
import org.glassfish.jersey.jackson.JacksonFeature;
import org.glassfish.jersey.media.multipart.MultiPartFeature;
import org.glassfish.jersey.message.internal.InputStreamProvider;

import javax.ws.rs.client.ClientBuilder;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.dracoon.sdk.crypto.Crypto;
import com.dracoon.sdk.crypto.error.CryptoException;
import com.dracoon.sdk.crypto.error.UnknownVersionException;
import com.dracoon.sdk.crypto.model.EncryptedFileKey;
import com.dracoon.sdk.crypto.model.UserKeyPair;

import static ch.cyberduck.core.oauth.OAuth2AuthorizationService.CYBERDUCK_REDIRECT_URI;

public class SDSSession extends HttpSession<SDSApiClient> {
    private static final Logger log = LogManager.getLogger(SDSSession.class);

    public static final String SDS_AUTH_TOKEN_HEADER = "X-Sds-Auth-Token";
    public static final int DEFAULT_CHUNKSIZE = 16;

    public static final String VERSION_REGEX = "(([0-9]+)\\.([0-9]+)\\.([0-9]+)).*";

    private OAuth2RequestInterceptor authorizationService;

    private final PreferencesReader preferences = new HostPreferences(host);

    private final ExpiringObjectHolder<UserAccountWrapper> userAccount
            = new ExpiringObjectHolder<>(preferences.getLong("sds.useracount.ttl"));

    private final ExpiringObjectHolder<UserKeyPairContainer> keyPair
            = new ExpiringObjectHolder<>(preferences.getLong("sds.encryption.keys.ttl"));

    private final ExpiringObjectHolder<UserKeyPairContainer> keyPairDeprecated
            = new ExpiringObjectHolder<>(preferences.getLong("sds.encryption.keys.ttl"));

    private final ExpiringObjectHolder<SystemDefaults> systemDefaults
            = new ExpiringObjectHolder<>(preferences.getLong("sds.useracount.ttl"));

    private final ExpiringObjectHolder<GeneralSettingsInfo> generalSettingsInfo
            = new ExpiringObjectHolder<>(preferences.getLong("sds.useracount.ttl"));

    private final ExpiringObjectHolder<ClassificationPoliciesConfig> classificationPolicies
            = new ExpiringObjectHolder<>(preferences.getLong("sds.useracount.ttl"));

    private final ExpiringObjectHolder<SoftwareVersionData> softwareVersion
            = new ExpiringObjectHolder<>(preferences.getLong("sds.useracount.ttl"));

    private UserKeyPair.Version requiredKeyPairVersion;

    private final SDSNodeIdProvider nodeid = new SDSNodeIdProvider(this);
    private final SDSMissingFileKeysSchedulerFeature scheduler = new SDSMissingFileKeysSchedulerFeature(this, nodeid);

    public SDSSession(final Host host, final X509TrustManager trust, final X509KeyManager key) {
        super(host, trust, key);
    }

    @Override
    protected SDSApiClient connect(final ProxyFinder proxy, final HostKeyCallback key, final LoginCallback prompt, final CancelCallback cancel) throws BackgroundException {
        final HttpClientBuilder configuration = builder.build(proxy, this, prompt);
        authorizationService = new OAuth2RequestInterceptor(builder.build(proxy, this, prompt).addInterceptorLast(new HttpRequestInterceptor() {
            @Override
            public void process(final HttpRequest request, final HttpContext context) {
                if(request instanceof HttpRequestWrapper) {
                    final HttpRequestWrapper wrapper = (HttpRequestWrapper) request;
                    if(null != wrapper.getTarget()) {
                        if(StringUtils.equals(wrapper.getTarget().getHostName(), host.getHostname())) {
                            request.addHeader(HttpHeaders.AUTHORIZATION,
                                    String.format("Basic %s", Base64.getEncoder().encodeToString(String.format("%s:%s", host.getProtocol().getOAuthClientId(), host.getProtocol().getOAuthClientSecret()).getBytes(StandardCharsets.UTF_8))));
                        }
                    }
                }
            }
        }).build(), host, prompt) {
            @Override
            public void process(final HttpRequest request, final HttpContext context) throws HttpException, IOException {
                if(request instanceof HttpRequestWrapper) {
                    final HttpRequestWrapper wrapper = (HttpRequestWrapper) request;
                    if(null != wrapper.getTarget()) {
                        if(StringUtils.equals(wrapper.getTarget().getHostName(), host.getHostname())) {
                            super.process(request, context);
                        }
                    }
                }
            }
        }
                .withFlowType(SDSProtocol.Authorization.valueOf(host.getProtocol().getAuthorization()) == SDSProtocol.Authorization.password
                        ? OAuth2AuthorizationService.FlowType.PasswordGrant : OAuth2AuthorizationService.FlowType.AuthorizationCode)
                .withRedirectUri(CYBERDUCK_REDIRECT_URI.equals(host.getProtocol().getOAuthRedirectUrl()) ? host.getProtocol().getOAuthRedirectUrl() :
                        Scheme.isURL(host.getProtocol().getOAuthRedirectUrl()) ? host.getProtocol().getOAuthRedirectUrl() : new HostUrlProvider().withUsername(false).withPath(true).get(
                                host.getProtocol().getScheme(), host.getPort(), null, host.getHostname(), host.getProtocol().getOAuthRedirectUrl())
                );
        try {
            authorizationService.withParameter("user_agent_info", Base64.getEncoder().encodeToString(InetAddress.getLocalHost().getHostName().getBytes(StandardCharsets.UTF_8)));
        }
        catch(UnknownHostException e) {
            throw new DefaultIOExceptionMappingService().map(e);
        }
        configuration.setServiceUnavailableRetryStrategy(new CustomServiceUnavailableRetryStrategy(host,

                        new ExecutionCountServiceUnavailableRetryStrategy(new PreconditionFailedResponseInterceptor(host, authorizationService, prompt),
                        new OAuth2ErrorResponseInterceptor(host, authorizationService))));
        if(new HostPreferences(host).getBoolean("sds.limit.requests.enable")) {
            configuration.addInterceptorLast(new RateLimitingHttpRequestInterceptor(new DefaultHttpRateLimiter(
                    new HostPreferences(host).getInteger("sds.limit.requests.second")
            )));
        }
        configuration.addInterceptorLast(authorizationService);
        configuration.addInterceptorLast(new HttpRequestInterceptor() {
            @Override
            public void process(final HttpRequest request, final HttpContext context) {
                request.removeHeaders(SDSSession.SDS_AUTH_TOKEN_HEADER);
            }
        });
        final CloseableHttpClient apache = configuration.build();
        final SDSApiClient client = new SDSApiClient(apache);
        client.setBasePath(new HostUrlProvider().withUsername(false).withPath(true).get(host.getProtocol().getScheme(), host.getPort(),
                null, host.getHostname(), host.getProtocol().getContext()));
        client.setHttpClient(ClientBuilder.newClient(new ClientConfig()
                .property(ClientProperties.SUPPRESS_HTTP_COMPLIANCE_VALIDATION, true)
                .register(new InputStreamProvider())
                .register(MultiPartFeature.class)
                .register(new JSON())
                .register(JacksonFeature.class)
                .connectorProvider(new HttpComponentsProvider(apache))));
        final int timeout = ConnectionTimeoutFactory.get(preferences).getTimeout() * 1000;
        client.setConnectTimeout(timeout);
        client.setReadTimeout(timeout);
        client.setUserAgent(new PreferencesUseragentProvider().get());
        return client;
    }

    @Override
    public void login(final LoginCallback prompt, final CancelCallback cancel) throws BackgroundException {
        final SoftwareVersionData version = this.softwareVersion();
        final Matcher matcher = Pattern.compile(VERSION_REGEX).matcher(version.getRestApiVersion());
        if(matcher.matches()) {
            if(new Version(matcher.group(1)).compareTo(new Version(preferences.getProperty("sds.version.lts"))) < 0) {
                throw new InteroperabilityException(
                        LocaleFactory.localizedString("DRACOON environment needs to be updated", "SDS"),
                        LocaleFactory.localizedString("Your DRACOON environment is outdated and no longer works with this application. Please contact your administrator.", "SDS"));
            }
        }
        final Credentials credentials;
        // The provided token is valid for two hours, every usage resets this period to two full hours again. Logging off invalidates the token.
        switch(SDSProtocol.Authorization.valueOf(host.getProtocol().getAuthorization())) {
            case oauth:
            case password:
                if("x-dracoon-action:oauth".equals(CYBERDUCK_REDIRECT_URI)) {
                    if(matcher.matches()) {
                        if(new Version(matcher.group(1)).compareTo(new Version("4.15.0")) >= 0) {
                            authorizationService.withRedirectUri(CYBERDUCK_REDIRECT_URI);
                        }
                    }
                    else {
                        log.warn("Failure to parse software version {}", version);
                    }
                }
                credentials = authorizationService.validate();
                break;
            default:
                credentials = host.getCredentials();
        }
        final UserAccount account;
        try {
            account = new UserApi(client).requestUserInfo(StringUtils.EMPTY, false, null);
            log.debug("Authenticated as user {}", account);
        }
        catch(ApiException e) {
            throw new SDSExceptionMappingService(nodeid).map(e);
        }
        switch(SDSProtocol.Authorization.valueOf(host.getProtocol().getAuthorization())) {
            case oauth:
                credentials.setUsername(account.getLogin());
        }
        userAccount.set(new UserAccountWrapper(account));
        requiredKeyPairVersion = this.getRequiredKeyPairVersion();
        this.unlockTripleCryptKeyPair(prompt, userAccount.get(), requiredKeyPairVersion);
        this.updateDirectS3UploadSetting();
    }

    private void updateDirectS3UploadSetting() {
        boolean isS3DirectUploadSupported = false;
        try {
            if(this.generalSettingsInfo().isUseS3Storage()) {
                final Matcher matcher = Pattern.compile(SDSSession.VERSION_REGEX).matcher(this.softwareVersion().getRestApiVersion());
                if(matcher.matches()) {
                    isS3DirectUploadSupported = new Version(matcher.group(1)).compareTo(new Version("4.22")) >= 0;
                }
            }
        }
        catch(BackgroundException e) {
            log.warn("Failure reading software version. {}", e.getMessage());
            isS3DirectUploadSupported = true;
        }
        host.setProperty("sds.upload.s3.enable", String.valueOf(isS3DirectUploadSupported));
    }

    private UserKeyPair.Version getRequiredKeyPairVersion() {
        final AlgorithmVersionInfoList algorithms;
        try {
            algorithms = new ConfigApi(client).requestAlgorithms(null);
            final List<AlgorithmVersionInfo> keyPairAlgorithms = algorithms.getKeyPairAlgorithms();
            for(AlgorithmVersionInfo kpa : keyPairAlgorithms) {
                if(kpa.getStatus() == AlgorithmVersionInfo.StatusEnum.REQUIRED) {
                    return UserKeyPair.Version.getByValue(kpa.getVersion());
                }
            }
            log.error("No available key pair algorithm with status required found.");
        }
        catch(ApiException e) {
            log.warn("Ignore failure reading key pair version. {}", e.getMessage());
        }
        catch(UnknownVersionException e) {
            log.warn("Ignore failure reading required key pair algorithm. {}", e.getMessage());
        }
        return UserKeyPair.Version.RSA2048;
    }

    private boolean isNewCryptoAvailable() throws BackgroundException {
        final Matcher matcher = Pattern.compile(VERSION_REGEX).matcher(this.softwareVersion().getRestApiVersion());
        if(matcher.matches()) {
            return new Version(matcher.group(1)).compareTo(new Version("4.24.0")) >= 0;
        }
        return false;
    }

    protected void unlockTripleCryptKeyPair(final LoginCallback prompt, final UserAccountWrapper user,
                                            final UserKeyPair.Version requiredKeyPairVersion) throws BackgroundException {
        try {
            Credentials deprecatedCredentials = null;
            if(this.isNewCryptoAvailable()) {
                final List<UserKeyPairContainer> pairs = new UserApi(client).requestUserKeyPairs(StringUtils.EMPTY, null);
                if(pairs.size() == 0) {
                    log.debug("No keypair found for user {}", user);
                    return;
                }
                boolean migrated = false;
                for(UserKeyPairContainer pair : pairs) {
                    if(requiredKeyPairVersion == TripleCryptConverter.toCryptoUserKeyPair(pair).getUserPublicKey().getVersion()) {
                        migrated = true;
                        break;
                    }
                }
                if(migrated && pairs.size() == 2) {
                    final UserKeyPairContainer deprecated = new UserApi(client).requestUserKeyPair(StringUtils.EMPTY, UserKeyPair.Version.RSA2048.getValue(), null);
                    final UserKeyPair keypair = TripleCryptConverter.toCryptoUserKeyPair(deprecated);
                    log.debug("Attempt to unlock deprecated private key {}", keypair.getUserPrivateKey());
                    deprecatedCredentials = new TripleCryptKeyPair(host).unlock(prompt, keypair);
                    keyPairDeprecated.set(deprecated);
                }
                if(!migrated) {
                    final UserKeyPairContainer deprecated = new UserApi(client).requestUserKeyPair(StringUtils.EMPTY, UserKeyPair.Version.RSA2048.getValue(), null);
                    final UserKeyPair keypair = TripleCryptConverter.toCryptoUserKeyPair(deprecated);
                    log.debug("Attempt to unlock and migrate deprecated private key {}", keypair.getUserPrivateKey());
                    deprecatedCredentials = new TripleCryptKeyPair(host).unlock(prompt, keypair);
                    final UserKeyPair newPair = Crypto.generateUserKeyPair(requiredKeyPairVersion, deprecatedCredentials.getPassword().toCharArray());
                    final CreateKeyPairRequest request = new CreateKeyPairRequest();
                    request.setPreviousPrivateKey(deprecated.getPrivateKeyContainer());
                    final UserKeyPairContainer userKeyPairContainer = TripleCryptConverter.toSwaggerUserKeyPairContainer(newPair);
                    request.setPrivateKeyContainer(userKeyPairContainer.getPrivateKeyContainer());
                    request.setPublicKeyContainer(userKeyPairContainer.getPublicKeyContainer());
                    log.debug("Create new key pair");
                    new UserApi(client).createAndPreserveUserKeyPair(request, null);
                    keyPairDeprecated.set(deprecated);
                }
            }
            final UserKeyPairContainer container = new UserApi(client).requestUserKeyPair(StringUtils.EMPTY, requiredKeyPairVersion.getValue(), null);
            keyPair.set(container);
            final UserKeyPair keypair = TripleCryptConverter.toCryptoUserKeyPair(keyPair.get());
            if(deprecatedCredentials != null) {
                log.debug("Attempt to unlock private key with passphrase from deprecated private key {}", keypair.getUserPrivateKey());
                if(Crypto.checkUserKeyPair(keypair, deprecatedCredentials.getPassword().toCharArray())) {
                    new TripleCryptKeyPair(host).unlock(prompt, keypair, deprecatedCredentials.getPassword());
                }
                else {
                    new TripleCryptKeyPair(host).unlock(prompt, keypair);
                }
            }
            else {
                log.debug("Attempt to unlock private key {}", keypair.getUserPrivateKey());
                new TripleCryptKeyPair(host).unlock(prompt, keypair);
            }
        }
        catch(CryptoException e) {
            throw new TripleCryptExceptionMappingService().map(e);
        }
        catch(ApiException e) {
            log.warn("Ignore failure unlocking user key pair. {}", e.getMessage());
        }
        catch(LoginCanceledException e) {
            log.warn("Ignore cancel unlocking triple crypt private key pair");
        }
    }

    /**
     * Invalidate cached key pairs
     */
    public void resetUserKeyPairs() {
        keyPair.set(null);
        keyPairDeprecated.set(null);
    }

    public UserAccountWrapper userAccount() throws BackgroundException {
        if(this.userAccount.get() == null) {
            try {
                userAccount.set(new UserAccountWrapper(new UserApi(client).requestUserInfo(StringUtils.EMPTY, false, null)));
            }
            catch(ApiException e) {
                log.warn("Failure updating user info. {}", e.getMessage());
                throw new SDSExceptionMappingService(nodeid).map(e);
            }
        }
        return userAccount.get();
    }

    public UserKeyPairContainer getKeyPairForFileKey(final EncryptedFileKey.Version version) throws BackgroundException {
        switch(version) {
            case RSA2048_AES256GCM:
                final UserKeyPairContainer keyPairDeprecated = this.keyPairDeprecated();
                if(null == keyPairDeprecated) {
                    throw new InteroperabilityException(String.format("No keypair for version %s", version));
                }
                return keyPairDeprecated;
            case RSA4096_AES256GCM:
                return this.keyPair();
            default:
                throw new InteroperabilityException(String.format("Unknown version %s", version));
        }
    }

    public UserKeyPairContainer keyPairDeprecated() throws BackgroundException {
        if(keyPairDeprecated.get() == null) {
            try {
                keyPairDeprecated.set(new UserApi(client).requestUserKeyPair(StringUtils.EMPTY, UserKeyPair.Version.RSA2048.getValue(), null));
            }
            catch(ApiException e) {
                if(e.getCode() == HttpStatus.SC_NOT_FOUND) {
                    log.debug("User does not have a keypair for version {}", UserKeyPair.Version.RSA2048.getValue());
                }
                else {
                    log.warn("Failure updating user key pair. {}", e.getMessage());
                    throw new SDSExceptionMappingService(nodeid).map(e);
                }
            }
        }
        return keyPairDeprecated.get();
    }

    public UserKeyPairContainer keyPair() throws BackgroundException {
        if(keyPair.get() == null) {
            try {
                keyPair.set(new UserApi(client).requestUserKeyPair(StringUtils.EMPTY, requiredKeyPairVersion.getValue(), null));
            }
            catch(ApiException e) {
                log.warn("Failure updating user key pair for required algorithm. {}", e.getMessage());
                // fallback
                final UserKeyPairContainer keyPairDeprecated = this.keyPairDeprecated();
                if(null == keyPairDeprecated) {
                    throw new SDSExceptionMappingService(nodeid).map(e);
                }
                keyPair.set(keyPairDeprecated);
            }
        }
        return keyPair.get();
    }

    public SoftwareVersionData softwareVersion() throws BackgroundException {
        if(softwareVersion.get() == null) {
            try {
                softwareVersion.set(new PublicApi(client).requestSoftwareVersion(null));
                log.info("Server version {}", softwareVersion.get());
            }
            catch(ApiException e) {
                log.warn("Failure {} updating software version", e.getMessage());
                throw new SDSExceptionMappingService(nodeid).map(e);
            }
        }
        return softwareVersion.get();
    }

    public SystemDefaults systemDefaults() throws BackgroundException {
        if(systemDefaults.get() == null) {
            try {
                systemDefaults.set(new ConfigApi(client).requestSystemDefaultsInfo(StringUtils.EMPTY));
            }
            catch(ApiException e) {
                // Precondition: Right "Config Read" required.
                log.warn("Failure {} reading system defaults", e.getMessage());
                throw new SDSExceptionMappingService(nodeid).map(e);
            }
        }
        return systemDefaults.get();
    }

    public GeneralSettingsInfo generalSettingsInfo() throws BackgroundException {
        if(generalSettingsInfo.get() == null) {
            try {
                generalSettingsInfo.set(new ConfigApi(client).requestGeneralSettingsInfo(StringUtils.EMPTY));
            }
            catch(ApiException e) {
                // Precondition: Right "Config Read" required.
                log.warn("Failure {} reading configuration", e.getMessage());
                throw new SDSExceptionMappingService(nodeid).map(e);
            }
        }
        return generalSettingsInfo.get();
    }

    public ClassificationPoliciesConfig shareClassificationsPolicies() throws BackgroundException {
        if(classificationPolicies.get() == null) {
            final Matcher matcher = Pattern.compile(SDSSession.VERSION_REGEX).matcher(this.softwareVersion().getRestApiVersion());
            if(matcher.matches()) {
                if(new Version(matcher.group(1)).compareTo(new Version("4.30")) >= 0) {
                    try {
                        classificationPolicies.set(new ConfigApi(client).requestClassificationPoliciesConfigInfo(StringUtils.EMPTY));
                    }
                    catch(ApiException e) {
                        // Precondition: Right "Config Read" required.
                        log.warn("Failure {} reading configuration", e.getMessage());
                        throw new SDSExceptionMappingService(nodeid).map(e);
                    }
                }
            }
        }
        return classificationPolicies.get();
    }

    public UserKeyPair.Version requiredKeyPairVersion() {
        return requiredKeyPairVersion;
    }

    @Override
    protected void logout() {
        scheduler.shutdown();
        client.getHttpClient().close();
        nodeid.clear();
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T _getFeature(final Class<T> type) {
        if(type == ListService.class) {
            return (T) new SDSListService(this, nodeid);
        }
        if(type == Read.class) {
            return (T) new SDSDelegatingReadFeature(this, nodeid, new SDSReadFeature(this, nodeid));
        }
        if(type == Upload.class) {
            if(new HostPreferences(host).getBoolean("sds.upload.s3.enable")) {
                return (T) new SDSDirectS3UploadFeature(this, nodeid, new SDSDirectS3WriteFeature(this, nodeid));
            }
            return (T) new DefaultUploadFeature(new SDSDelegatingWriteFeature(this, nodeid, new SDSMultipartWriteFeature(this, nodeid)));
        }
        if(type == Write.class || type == MultipartWrite.class) {
            if(new HostPreferences(host).getBoolean("sds.upload.s3.enable")) {
                return (T) new SDSDelegatingWriteFeature(this, nodeid, new SDSDirectS3MultipartWriteFeature(this, nodeid));
            }
            return (T) new SDSDelegatingWriteFeature(this, nodeid, new SDSMultipartWriteFeature(this, nodeid));
        }
        if(type == Directory.class) {
            return (T) new SDSDirectoryFeature(this, nodeid);
        }
        if(type == Delete.class) {
            return (T) new SDSThresholdDeleteFeature(this, nodeid);
        }
        if(type == VersionIdProvider.class) {
            return (T) nodeid;
        }
        if(type == Touch.class) {
            return (T) new SDSTouchFeature(this, nodeid);
        }
        if(type == Find.class) {
            return (T) new SDSFindFeature(this, nodeid);
        }
        if(type == AttributesFinder.class) {
            return (T) new SDSAttributesFinderFeature(this, nodeid);
        }
        if(type == Timestamp.class) {
            return (T) new SDSTimestampFeature(this, nodeid);
        }
        if(type == Move.class) {
            return (T) new SDSDelegatingMoveFeature(this, nodeid, new SDSMoveFeature(this, nodeid));
        }
        if(type == Copy.class) {
            return (T) new SDSDelegatingCopyFeature(this, nodeid, new SDSCopyFeature(this, nodeid));
        }
        if(type == Bulk.class) {
            return (T) new SDSEncryptionBulkFeature(this, nodeid);
        }
        if(type == UrlProvider.class) {
            return (T) new SDSUrlProvider(this);
        }
        if(type == Share.class) {
            return (T) new SDSShareFeature(this, nodeid);
        }
        if(type == Quota.class) {
            return (T) new SDSQuotaFeature(this, nodeid);
        }
        if(type == Search.class) {
            return (T) new SDSSearchFeature(this, nodeid);
        }
        if(type == Versioning.class) {
            return (T) new SDSVersioningFeature(this, nodeid);
        }
        if(type == Encryptor.class) {
            return (T) new SDSTripleCryptEncryptorFeature(this, nodeid);
        }
        if(type == Scheduler.class) {
            return (T) scheduler;
        }
        return super._getFeature(type);
    }
}
