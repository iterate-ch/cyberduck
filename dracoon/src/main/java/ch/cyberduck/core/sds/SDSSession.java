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

import ch.cyberduck.core.Credentials;
import ch.cyberduck.core.DefaultIOExceptionMappingService;
import ch.cyberduck.core.ExpiringObjectHolder;
import ch.cyberduck.core.Host;
import ch.cyberduck.core.HostKeyCallback;
import ch.cyberduck.core.HostUrlProvider;
import ch.cyberduck.core.ListService;
import ch.cyberduck.core.LocaleFactory;
import ch.cyberduck.core.LoginCallback;
import ch.cyberduck.core.LoginOptions;
import ch.cyberduck.core.PreferencesUseragentProvider;
import ch.cyberduck.core.Scheme;
import ch.cyberduck.core.UrlProvider;
import ch.cyberduck.core.Version;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.exception.LoginCanceledException;
import ch.cyberduck.core.exception.PartialLoginFailureException;
import ch.cyberduck.core.features.*;
import ch.cyberduck.core.http.HttpSession;
import ch.cyberduck.core.oauth.OAuth2ErrorResponseInterceptor;
import ch.cyberduck.core.oauth.OAuth2RequestInterceptor;
import ch.cyberduck.core.preferences.PreferencesFactory;
import ch.cyberduck.core.proxy.Proxy;
import ch.cyberduck.core.sds.io.swagger.client.ApiException;
import ch.cyberduck.core.sds.io.swagger.client.JSON;
import ch.cyberduck.core.sds.io.swagger.client.api.AuthApi;
import ch.cyberduck.core.sds.io.swagger.client.api.ConfigApi;
import ch.cyberduck.core.sds.io.swagger.client.api.PublicApi;
import ch.cyberduck.core.sds.io.swagger.client.api.UserApi;
import ch.cyberduck.core.sds.io.swagger.client.model.KeyValueEntry;
import ch.cyberduck.core.sds.io.swagger.client.model.LoginRequest;
import ch.cyberduck.core.sds.io.swagger.client.model.SoftwareVersionData;
import ch.cyberduck.core.sds.io.swagger.client.model.UserAccount;
import ch.cyberduck.core.sds.io.swagger.client.model.UserKeyPairContainer;
import ch.cyberduck.core.sds.provider.HttpComponentsProvider;
import ch.cyberduck.core.sds.triplecrypt.TripleCryptExceptionMappingService;
import ch.cyberduck.core.sds.triplecrypt.TripleCryptKeyPair;
import ch.cyberduck.core.ssl.X509KeyManager;
import ch.cyberduck.core.ssl.X509TrustManager;
import ch.cyberduck.core.threading.CancelCallback;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpRequest;
import org.apache.http.HttpRequestInterceptor;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.protocol.HttpContext;
import org.apache.log4j.Logger;
import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.jackson.JacksonFeature;
import org.glassfish.jersey.media.multipart.MultiPartFeature;
import org.glassfish.jersey.message.internal.InputStreamProvider;

import javax.ws.rs.client.ClientBuilder;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.dracoon.sdk.crypto.CryptoException;
import com.dracoon.sdk.crypto.model.UserKeyPair;
import com.dracoon.sdk.crypto.model.UserPrivateKey;
import com.migcomponents.migbase64.Base64;

import static ch.cyberduck.core.oauth.OAuth2AuthorizationService.CYBERDUCK_REDIRECT_URI;

public class SDSSession extends HttpSession<SDSApiClient> {
    private static final Logger log = Logger.getLogger(SDSSession.class);

    public static final String SDS_AUTH_TOKEN_HEADER = "X-Sds-Auth-Token";
    public static final int DEFAULT_CHUNKSIZE = 16;

    private static final String VERSION_REGEX = "(([0-9]+)\\.([0-9]+)\\.([0-9]+)).*";

    protected SDSErrorResponseInterceptor retryHandler;
    protected OAuth2RequestInterceptor authorizationService;

    private final ExpiringObjectHolder<UserAccountWrapper> userAccount
        = new ExpiringObjectHolder<>(PreferencesFactory.get().getLong("sds.useracount.ttl"));

    private final ExpiringObjectHolder<UserKeyPairContainer> keyPair
        = new ExpiringObjectHolder<>(PreferencesFactory.get().getLong("sds.encryption.keys.ttl"));

    private final ExpiringObjectHolder<SoftwareVersionData> softwareVersion
        = new ExpiringObjectHolder<SoftwareVersionData>(PreferencesFactory.get().getLong("sds.useracount.ttl"));

    private final List<KeyValueEntry> configuration = new ArrayList<>();
    private final SDSNodeIdProvider nodeid = new SDSNodeIdProvider(this);

    public SDSSession(final Host host, final X509TrustManager trust, final X509KeyManager key) {
        super(host, trust, key);
    }

    @Override
    protected SDSApiClient connect(final Proxy proxy, final HostKeyCallback key, final LoginCallback prompt) throws BackgroundException {
        final HttpClientBuilder configuration = builder.build(proxy, this, prompt);
        switch(SDSProtocol.Authorization.valueOf(host.getProtocol().getAuthorization())) {
            case oauth:
                authorizationService = new OAuth2RequestInterceptor(builder.build(proxy, this, prompt).addInterceptorLast(new HttpRequestInterceptor() {
                    @Override
                    public void process(final HttpRequest request, final HttpContext context) {
                        request.addHeader(HttpHeaders.AUTHORIZATION,
                            String.format("Basic %s", Base64.encodeToString(String.format("%s:%s", host.getProtocol().getOAuthClientId(), host.getProtocol().getOAuthClientSecret()).getBytes(StandardCharsets.UTF_8), false)));
                    }
                }).build(), host)
                    .withRedirectUri(CYBERDUCK_REDIRECT_URI.equals(host.getProtocol().getOAuthRedirectUrl()) ? host.getProtocol().getOAuthRedirectUrl() :
                        Scheme.isURL(host.getProtocol().getOAuthRedirectUrl()) ? host.getProtocol().getOAuthRedirectUrl() : new HostUrlProvider().withUsername(false).withPath(true).get(
                            host.getProtocol().getScheme(), host.getPort(), null, host.getHostname(), host.getProtocol().getOAuthRedirectUrl())
                    );

                try {
                    authorizationService.withParameter("user_agent_info", Base64.encodeToString(InetAddress.getLocalHost().getHostName().getBytes(StandardCharsets.UTF_8), false));
                }
                catch(UnknownHostException e) {
                    throw new DefaultIOExceptionMappingService().map(e);
                }
                configuration.setServiceUnavailableRetryStrategy(new OAuth2ErrorResponseInterceptor(host, authorizationService, prompt));
                configuration.addInterceptorLast(authorizationService);
                configuration.addInterceptorLast(new HttpRequestInterceptor() {
                    @Override
                    public void process(final HttpRequest request, final HttpContext context) {
                        request.removeHeaders(SDSSession.SDS_AUTH_TOKEN_HEADER);
                    }
                });
                break;
            default:
                retryHandler = new SDSErrorResponseInterceptor(this);
                configuration.setServiceUnavailableRetryStrategy(retryHandler);
                configuration.addInterceptorLast(retryHandler);
                break;
        }
        final CloseableHttpClient apache = configuration.build();
        final SDSApiClient client = new SDSApiClient(apache);
        final int timeout = PreferencesFactory.get().getInteger("connection.timeout.seconds") * 1000;
        client.setConnectTimeout(timeout);
        client.setBasePath(new HostUrlProvider().withUsername(false).withPath(true).get(host.getProtocol().getScheme(), host.getPort(),
            null, host.getHostname(), host.getProtocol().getContext()));
        client.setHttpClient(ClientBuilder.newClient(new ClientConfig()
            .register(new InputStreamProvider())
            .register(MultiPartFeature.class)
            .register(new JSON())
            .register(JacksonFeature.class)
            .connectorProvider(new HttpComponentsProvider(apache))));
        client.setUserAgent(new PreferencesUseragentProvider().get());
        return client;
    }

    @Override
    public void login(final Proxy proxy, final LoginCallback controller, final CancelCallback cancel) throws BackgroundException {
        final Credentials credentials = host.getCredentials();
        final String login = credentials.getUsername();
        final String password = credentials.getPassword();
        // The provided token is valid for two hours, every usage resets this period to two full hours again. Logging off invalidates the token.
        switch(SDSProtocol.Authorization.valueOf(host.getProtocol().getAuthorization())) {
            case oauth:
                if("x-dracoon-action:oauth".equals(CYBERDUCK_REDIRECT_URI)) {
                    final SoftwareVersionData softwareVersionData = this.softwareVersion();
                    Matcher matcher = Pattern.compile(VERSION_REGEX).matcher(softwareVersionData.getRestApiVersion());
                    if(matcher.matches()) {
                        if(new Version(matcher.group(1)).compareTo(new Version("4.15.0")) >= 0) {
                            authorizationService.withRedirectUri(CYBERDUCK_REDIRECT_URI);
                        }
                    }
                    else {
                        log.warn(String.format("Failure to parse software version %s", softwareVersionData));
                    }
                }
                authorizationService.setTokens(authorizationService.authorize(host, controller, cancel));
                break;
            case radius:
                final Credentials additional = controller.prompt(host, LocaleFactory.localizedString("Provide additional login credentials", "Credentials"),
                    LocaleFactory.localizedString("Multi-Factor Authentication", "S3"),
                    new LoginOptions()
                        .icon(host.getProtocol().disk())
                        .user(false)
                        .keychain(false)
                );
                // Save tokens for 401 error response when expired
                retryHandler.setTokens(login, password, this.login(controller, new LoginRequest()
                    .authType(LoginRequest.AuthTypeEnum.fromValue(host.getProtocol().getAuthorization()))
                    .login(login)
                    .password(additional.getPassword())
                ));
                break;
            default:
                // Save tokens for 401 error response when expired
                retryHandler.setTokens(login, password, this.login(controller, new LoginRequest()
                    .authType(LoginRequest.AuthTypeEnum.fromValue(host.getProtocol().getAuthorization()))
                    .login(login)
                    .password(password)
                ));
                break;
        }
        try {
            configuration.addAll(new ConfigApi(client).getSystemSettings(StringUtils.EMPTY).getItems());
        }
        catch(ApiException e) {
            // Precondition: Right "Config Read" required.
            log.warn(String.format("Ignore failure reading configuration. %s", new SDSExceptionMappingService().map(e)));
        }
        try {
            final UserAccount account = new UserApi(client).getUserInfo(StringUtils.EMPTY, null, false);
            if(log.isDebugEnabled()) {
                log.debug(String.format("Authenticated as user %s", account));
            }
            switch(SDSProtocol.Authorization.valueOf(host.getProtocol().getAuthorization())) {
                case oauth:
                    credentials.setUsername(account.getLogin());
            }
            userAccount.set(new UserAccountWrapper(account));
            keyPair.set(new UserApi(client).getUserKeyPair(StringUtils.EMPTY));
            final UserPrivateKey privateKey = new UserPrivateKey();
            final UserKeyPairContainer keyPairContainer = keyPair.get();
            privateKey.setPrivateKey(keyPairContainer.getPrivateKeyContainer().getPrivateKey());
            privateKey.setVersion(keyPairContainer.getPrivateKeyContainer().getVersion());
            final UserKeyPair userKeyPair = new UserKeyPair();
            userKeyPair.setUserPrivateKey(privateKey);
            if(log.isDebugEnabled()) {
                log.debug(String.format("Attempt to unlock private key %s", privateKey));
            }
            try {
                new TripleCryptKeyPair().unlock(controller, host, userKeyPair);
            }
            catch(LoginCanceledException e) {
                log.warn("Ignore cancel unlocking triple crypt private key pair");
            }
        }
        catch(CryptoException e) {
            throw new TripleCryptExceptionMappingService().map(e);
        }
        catch(ApiException e) {
            log.warn(String.format("Ignore failure reading user key pair. %s", new SDSExceptionMappingService().map(e)));
        }
        try {
            softwareVersion.set(new PublicApi(client).getSoftwareVersion(null));
        }
        catch(ApiException e) {
            log.warn(String.format("Ignore failure reading version. %s", new SDSExceptionMappingService().map(e)));
        }
    }

    private String login(final LoginCallback controller, final LoginRequest request) throws BackgroundException {
        try {
            try {
                return new AuthApi(client).login(request).getToken();
            }
            catch(ApiException e) {
                throw new SDSExceptionMappingService().map(e);
            }
        }
        catch(PartialLoginFailureException e) {
            final Credentials additional = controller.prompt(host, host.getCredentials().getUsername(),
                LocaleFactory.localizedString("Provide additional login credentials", "Credentials"), e.getDetail(),
                new LoginOptions()
                    .icon(host.getProtocol().disk())
                    .user(false)
                    .keychain(false)
            );
            return this.login(controller, new LoginRequest()
                .authType(LoginRequest.AuthTypeEnum.fromValue(host.getProtocol().getAuthorization()))
                .password(additional.getPassword())
            );
        }
    }

    public UserAccountWrapper userAccount() throws BackgroundException {
        if(this.userAccount.get() == null) {
            try {
                userAccount.set(new UserAccountWrapper(new UserApi(client).getUserInfo(StringUtils.EMPTY, null, false)));
            }
            catch(ApiException e) {
                log.warn(String.format("Failure updating user info. %s", e.getMessage()));
                throw new SDSExceptionMappingService().map(e);
            }
        }
        return userAccount.get();
    }

    public UserKeyPairContainer keyPair() throws BackgroundException {
        if(keyPair.get() == null) {
            try {
                keyPair.set(new UserApi(client).getUserKeyPair(StringUtils.EMPTY));
            }
            catch(ApiException e) {
                log.warn(String.format("Failure updating user key pair. %s", e.getMessage()));
                throw new SDSExceptionMappingService().map(e);
            }
        }
        return keyPair.get();
    }

    public SoftwareVersionData softwareVersion() throws BackgroundException {
        if(softwareVersion.get() == null) {
            try {
                softwareVersion.set(new PublicApi(client).getSoftwareVersion(null));
            }
            catch(ApiException e) {
                log.warn(String.format("Failure updating user key pair. %s", e.getMessage()));
                throw new SDSExceptionMappingService().map(e);
            }
        }
        return softwareVersion.get();
    }

    public List<KeyValueEntry> configuration() {
        return configuration;
    }

    @Override
    protected void logout() {
        client.getHttpClient().close();
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
        if(type == Write.class) {
            return (T) new SDSDelegatingWriteFeature(this, nodeid, new SDSMultipartWriteFeature(this, nodeid));
        }
        if(type == MultipartWrite.class) {
            return (T) new SDSDelegatingWriteFeature(this, nodeid, new SDSMultipartWriteFeature(this, nodeid));
        }
        if(type == Directory.class) {
            return (T) new SDSDirectoryFeature(this, nodeid);
        }
        if(type == Delete.class) {
            return (T) new SDSDeleteFeature(this, nodeid);
        }
        if(type == IdProvider.class) {
            return (T) nodeid;
        }
        if(type == Touch.class) {
            return (T) new SDSTouchFeature(this, nodeid);
        }
        if(type == Find.class) {
            return (T) new SDSFindFeature(nodeid);
        }
        if(type == AttributesFinder.class) {
            return (T) new SDSAttributesFinderFeature(this, nodeid);
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
        if(type == PromptUrlProvider.class) {
            return (T) new SDSSharesUrlProvider(this, nodeid);
        }
        if(type == Quota.class) {
            return (T) new SDSQuotaFeature(this, nodeid);
        }
        if(type == Search.class) {
            return (T) new SDSSearchFeature(this, nodeid);
        }
        if(type == Home.class) {
            return (T) new SDSHomeFinderService(this, nodeid);
        }
        return super._getFeature(type);
    }
}
