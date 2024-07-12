package ch.cyberduck.core.deepbox;

/*
 * Copyright (c) 2002-2024 iterate GmbH. All rights reserved.
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

import ch.cyberduck.core.ConnectionTimeoutFactory;
import ch.cyberduck.core.Credentials;
import ch.cyberduck.core.Host;
import ch.cyberduck.core.HostKeyCallback;
import ch.cyberduck.core.HostUrlProvider;
import ch.cyberduck.core.ListService;
import ch.cyberduck.core.LocaleFactory;
import ch.cyberduck.core.LoginCallback;
import ch.cyberduck.core.PreferencesUseragentProvider;
import ch.cyberduck.core.UrlProvider;
import ch.cyberduck.core.deepbox.io.swagger.client.ApiException;
import ch.cyberduck.core.deepbox.io.swagger.client.JSON;
import ch.cyberduck.core.deepbox.io.swagger.client.api.UserRestControllerApi;
import ch.cyberduck.core.deepbox.io.swagger.client.model.Me;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.features.AttributesFinder;
import ch.cyberduck.core.features.Copy;
import ch.cyberduck.core.features.Delete;
import ch.cyberduck.core.features.Directory;
import ch.cyberduck.core.features.FileIdProvider;
import ch.cyberduck.core.features.Find;
import ch.cyberduck.core.features.Move;
import ch.cyberduck.core.features.MultipartWrite;
import ch.cyberduck.core.features.Read;
import ch.cyberduck.core.features.Restore;
import ch.cyberduck.core.features.Share;
import ch.cyberduck.core.features.Touch;
import ch.cyberduck.core.features.Trash;
import ch.cyberduck.core.features.Write;
import ch.cyberduck.core.http.ChainedServiceUnavailableRetryStrategy;
import ch.cyberduck.core.http.CustomServiceUnavailableRetryStrategy;
import ch.cyberduck.core.http.ExecutionCountServiceUnavailableRetryStrategy;
import ch.cyberduck.core.http.HttpSession;
import ch.cyberduck.core.jersey.HttpComponentsProvider;
import ch.cyberduck.core.oauth.OAuth2AuthorizationService;
import ch.cyberduck.core.oauth.OAuth2ErrorResponseInterceptor;
import ch.cyberduck.core.oauth.OAuth2RequestInterceptor;
import ch.cyberduck.core.preferences.HostPreferences;
import ch.cyberduck.core.preferences.PreferencesFactory;
import ch.cyberduck.core.preferences.PreferencesReader;
import ch.cyberduck.core.proxy.ProxyFinder;
import ch.cyberduck.core.ssl.X509KeyManager;
import ch.cyberduck.core.ssl.X509TrustManager;
import ch.cyberduck.core.threading.CancelCallback;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpException;
import org.apache.http.HttpRequest;
import org.apache.http.HttpRequestInterceptor;
import org.apache.http.client.methods.HttpRequestWrapper;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.protocol.HttpContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.client.ClientProperties;
import org.glassfish.jersey.jackson.JacksonFeature;
import org.glassfish.jersey.message.internal.InputStreamProvider;

import javax.ws.rs.client.ClientBuilder;
import java.io.IOException;

public class DeepboxSession extends HttpSession<DeepboxApiClient> {
    private static final Logger log = LogManager.getLogger(DeepboxSession.class);

    private final PreferencesReader preferences = new HostPreferences(host);
    private final DeepboxIdProvider fileid = new DeepboxIdProvider(this);

    private OAuth2RequestInterceptor authorizationService;

    public DeepboxSession(final Host host, final X509TrustManager trust, final X509KeyManager key) {
        super(host, trust, key);
        this.pinLocalizations();
    }

    /**
     * Key to use in preferences to save the pinned locale.
     */
    private static String toPinnedLocalizationPropertyKey(final String name) {
        return String.format("deepbox.localization.%s", name);
    }

    @Override
    protected DeepboxApiClient connect(final ProxyFinder proxy, final HostKeyCallback key, final LoginCallback prompt, final CancelCallback cancel) throws BackgroundException {
        final HttpClientBuilder configuration = builder.build(proxy, this, prompt);
        authorizationService = new OAuth2RequestInterceptor(builder.build(proxy, this, prompt).build(), host, prompt) {
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
                .withFlowType(OAuth2AuthorizationService.FlowType.AuthorizationCode)
                .withRedirectUri(host.getProtocol().getOAuthRedirectUrl()
                );
        configuration.setServiceUnavailableRetryStrategy(new CustomServiceUnavailableRetryStrategy(host,
                new ExecutionCountServiceUnavailableRetryStrategy(new OAuth2ErrorResponseInterceptor(host, authorizationService))));
        new ChainedServiceUnavailableRetryStrategy(new ExecutionCountServiceUnavailableRetryStrategy(
                new ExecutionCountServiceUnavailableRetryStrategy(new OAuth2ErrorResponseInterceptor(host, authorizationService))));
        configuration.addInterceptorLast(authorizationService);
        configuration.addInterceptorLast((HttpRequestInterceptor) (request, context) -> request.addHeader("Accept-Language", this.getPinnedLocale()));

        final CloseableHttpClient apache = configuration.build();
        final DeepboxApiClient client = new DeepboxApiClient(apache);
        client.setBasePath(new HostUrlProvider().withUsername(false).withPath(true).get(host.getProtocol().getScheme(), host.getPort(),
                null, host.getHostname(), host.getProtocol().getContext()));
        client.setHttpClient(ClientBuilder.newClient(new ClientConfig()
                .property(ClientProperties.SUPPRESS_HTTP_COMPLIANCE_VALIDATION, true)
                .register(new InputStreamProvider())
                .register(new JSON())
                .register(JacksonFeature.class)
                .register(MultipartWrite.class)
                .connectorProvider(new HttpComponentsProvider(apache))));
        final int timeout = ConnectionTimeoutFactory.get(preferences).getTimeout() * 1000;
        client.setConnectTimeout(timeout);
        client.setReadTimeout(timeout);
        client.setUserAgent(new PreferencesUseragentProvider().get());
        return client;
    }

    @Override
    public void login(final LoginCallback prompt, final CancelCallback cancel) throws BackgroundException {
        final Credentials credentials = authorizationService.validate();
        try {
            final Me me = new UserRestControllerApi(client).usersMe(null, null);
            if(log.isDebugEnabled()) {
                log.debug(String.format("Authenticated for user %s", me));
            }
            credentials.setUsername(me.getEmail());
        }
        catch(ApiException e) {
            throw new DeepboxExceptionMappingService(fileid).map(e);
        }
    }

    private void pinLocalizations() {
        final String locale = preferences.getProperty("deepbox.locale");
        if(null == locale) {
            host.setProperty("deepbox.locale", PreferencesFactory.get().locale());
        }
        for(String name : DeepboxListService.VIRTUALFOLDERS) {
            final String localized = preferences.getProperty(toPinnedLocalizationPropertyKey(name));
            if(null == localized) {
                host.setProperty(toPinnedLocalizationPropertyKey(name), LocaleFactory.localizedString(name, "Deepbox"));
            }
        }
    }

    public String getPinnedLocale() {
        return preferences.getProperty("deepbox.locale");
    }

    public String getPinnedLocalization(final String name) {
        return DeepboxPathNormalizer.name(preferences.getProperty(toPinnedLocalizationPropertyKey(name)));
    }

    @Override
    protected void logout() {
        client.getHttpClient().close();
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T _getFeature(final Class<T> type) {
        if(type == FileIdProvider.class) {
            return (T) fileid;
        }
        if(type == ListService.class) {
            return (T) new DeepboxListService(this, fileid);
        }
        if(type == Directory.class) {
            return (T) new DeepboxDirectoryFeature(this, fileid);
        }
        if(type == Find.class) {
            return (T) new DeepboxFindFeature(this, fileid);
        }
        if(type == AttributesFinder.class) {
            return (T) new DeepboxAttributesFinderFeature(this, fileid);
        }
        if(type == Trash.class) {
            return (T) new DeepboxTrashFeature(this, fileid);
        }
        if(type == Delete.class) {
            return (T) new DeepboxDeleteFeature(this, fileid);
        }
        if(type == Read.class) {
            return (T) new DeepboxReadFeature(this, fileid);
        }
        if(type == Touch.class) {
            return (T) new DeepboxTouchFeature(this, fileid);
        }
        if(type == Write.class) {
            return (T) new DeepboxWriteFeature(this, fileid);
        }
        if(type == UrlProvider.class) {
            return (T) new DeepboxUrlProvider(this);
        }
        if(type == Share.class) {
            return (T) new DeepboxShareFeature(this, fileid);
        }
        if(type == Copy.class) {
            return (T) new DeepboxCopyFeature(this, fileid);
        }
        if(type == Move.class) {
            return (T) new DeepboxMoveFeature(this, fileid);
        }
        if(type == Restore.class) {
            return (T) new DeepboxRestoreFeature(this, fileid);
        }
        return super._getFeature(type);
    }

    public String getStage() {
        // For now, required for descriptive URL, API forthcoming
        // api.[<stage>.]deepbox.swiss
        String hostname = this.getHost().getHostname();
        return hostname.replaceAll("^api\\.", "").replaceAll("deepbox\\.swiss$", "");
    }
}
