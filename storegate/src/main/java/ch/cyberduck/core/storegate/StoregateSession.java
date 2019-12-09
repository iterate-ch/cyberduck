package ch.cyberduck.core.storegate;

/*
 * Copyright (c) 2002-2019 iterate GmbH. All rights reserved.
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
import ch.cyberduck.core.HostKeyCallback;
import ch.cyberduck.core.HostUrlProvider;
import ch.cyberduck.core.ListService;
import ch.cyberduck.core.LoginCallback;
import ch.cyberduck.core.PreferencesUseragentProvider;
import ch.cyberduck.core.Scheme;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.exception.LoginFailureException;
import ch.cyberduck.core.features.AttributesFinder;
import ch.cyberduck.core.features.Copy;
import ch.cyberduck.core.features.Delete;
import ch.cyberduck.core.features.Directory;
import ch.cyberduck.core.features.IdProvider;
import ch.cyberduck.core.features.Lock;
import ch.cyberduck.core.features.Move;
import ch.cyberduck.core.features.MultipartWrite;
import ch.cyberduck.core.features.PromptUrlProvider;
import ch.cyberduck.core.features.Read;
import ch.cyberduck.core.features.Timestamp;
import ch.cyberduck.core.features.Touch;
import ch.cyberduck.core.features.Write;
import ch.cyberduck.core.http.HttpSession;
import ch.cyberduck.core.oauth.OAuth2ErrorResponseInterceptor;
import ch.cyberduck.core.oauth.OAuth2RequestInterceptor;
import ch.cyberduck.core.preferences.PreferencesFactory;
import ch.cyberduck.core.proxy.Proxy;
import ch.cyberduck.core.ssl.X509KeyManager;
import ch.cyberduck.core.ssl.X509TrustManager;
import ch.cyberduck.core.storegate.io.swagger.client.ApiException;
import ch.cyberduck.core.storegate.io.swagger.client.JSON;
import ch.cyberduck.core.storegate.io.swagger.client.api.SettingsApi;
import ch.cyberduck.core.storegate.io.swagger.client.api.UsersApi;
import ch.cyberduck.core.storegate.io.swagger.client.model.ExtendedUser;
import ch.cyberduck.core.storegate.io.swagger.client.model.RootFolder;
import ch.cyberduck.core.storegate.provider.HttpComponentsProvider;
import ch.cyberduck.core.threading.CancelCallback;

import org.apache.http.HttpHeaders;
import org.apache.http.HttpRequest;
import org.apache.http.HttpRequestInterceptor;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.EntityUtils;
import org.apache.log4j.Logger;
import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.jackson.JacksonFeature;
import org.glassfish.jersey.media.multipart.MultiPartFeature;
import org.glassfish.jersey.message.internal.InputStreamProvider;

import javax.ws.rs.client.ClientBuilder;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import com.migcomponents.migbase64.Base64;

import static ch.cyberduck.core.oauth.OAuth2AuthorizationService.CYBERDUCK_REDIRECT_URI;
import static com.google.api.client.json.Json.MEDIA_TYPE;

public class StoregateSession extends HttpSession<StoregateApiClient> {
    private static final Logger log = Logger.getLogger(StoregateSession.class);

    private OAuth2RequestInterceptor authorizationService;
    private List<RootFolder> roots = Collections.emptyList();

    private final StoregateIdProvider fileid = new StoregateIdProvider(this);

    public StoregateSession(final Host host, final X509TrustManager trust, final X509KeyManager key) {
        super(host, trust, key);
    }

    @Override
    protected StoregateApiClient connect(final Proxy proxy, final HostKeyCallback key, final LoginCallback prompt) {
        final HttpClientBuilder configuration = builder.build(proxy, this, prompt);
        authorizationService = new OAuth2RequestInterceptor(builder.build(proxy, this, prompt).addInterceptorLast(new HttpRequestInterceptor() {
            @Override
            public void process(final HttpRequest request, final HttpContext context) {
                request.addHeader(HttpHeaders.AUTHORIZATION,
                    String.format("Basic %s", Base64.encodeToString(String.format("%s:%s", host.getProtocol().getOAuthClientId(), host.getProtocol().getOAuthClientSecret()).getBytes(StandardCharsets.UTF_8), false)));
            }
        }).build(),
            host).withRedirectUri(CYBERDUCK_REDIRECT_URI.equals(host.getProtocol().getOAuthRedirectUrl()) ? host.getProtocol().getOAuthRedirectUrl() :
            Scheme.isURL(host.getProtocol().getOAuthRedirectUrl()) ? host.getProtocol().getOAuthRedirectUrl() : new HostUrlProvider().withUsername(false).withPath(true).get(
                host.getProtocol().getScheme(), host.getPort(), null, host.getHostname(), host.getProtocol().getOAuthRedirectUrl())
        );
        // Force login even if browser session already exists
        authorizationService.withParameter("prompt", "login");
        configuration.setServiceUnavailableRetryStrategy(new OAuth2ErrorResponseInterceptor(host, authorizationService, prompt));
        configuration.addInterceptorLast(authorizationService);
        final CloseableHttpClient apache = configuration.build();
        final StoregateApiClient client = new StoregateApiClient(apache);
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
        authorizationService.setTokens(authorizationService.authorize(host, controller, cancel));
        try {
            final HttpRequestBase request = new HttpPost(
                new HostUrlProvider().withUsername(false).withPath(true).get(
                    host.getProtocol().getScheme(), host.getPort(), null, host.getHostname(), "/identity/core/connect/userinfo")
            );
            request.addHeader(HttpHeaders.CONTENT_TYPE, MEDIA_TYPE);
            final CloseableHttpResponse response = client.getClient().execute(request);
            try {
                switch(response.getStatusLine().getStatusCode()) {
                    case HttpStatus.SC_OK:
                        try {
                            final JsonElement element = JsonParser.parseReader(new InputStreamReader(response.getEntity().getContent()));
                            if(element.isJsonObject()) {
                                final JsonObject json = element.getAsJsonObject();
                                final URI url = URI.create(json.getAsJsonPrimitive("web_url_api").getAsString());
                                if(log.isInfoEnabled()) {
                                    log.info(String.format("Set base path to %s", url));
                                }
                                client.setBasePath(url.toString());
                            }
                        }
                        catch(JsonParseException | IllegalArgumentException e) {
                            log.warn(String.format("Ignore failure %s", e));
                        }
                        break;
                    case HttpStatus.SC_FORBIDDEN:
                        // Insufficient scope
                        final BackgroundException failure = new StoregateExceptionMappingService().map(new ApiException(response.getStatusLine().getStatusCode(), response.getStatusLine().getReasonPhrase(), Collections.emptyMap(),
                            EntityUtils.toString(response.getEntity())));
                        throw new LoginFailureException(failure.getDetail(), failure);
                    default:
                        throw new StoregateExceptionMappingService().map(new ApiException(response.getStatusLine().getStatusCode(), response.getStatusLine().getReasonPhrase(), Collections.emptyMap(),
                            EntityUtils.toString(response.getEntity())));
                }
            }
            finally {
                EntityUtils.consume(response.getEntity());
            }
            // Get username
            final ExtendedUser me = new UsersApi(client).usersGetMe();
            if(log.isDebugEnabled()) {
                log.debug(String.format("Authenticated for user %s", me));
            }
            final Credentials credentials = host.getCredentials();
            credentials.setUsername(me.getUsername());
            // Get root folders
            roots = new SettingsApi(client).settingsGetRootfolders();
        }
        catch(ApiException e) {
            throw new StoregateExceptionMappingService().map(e);
        }
        catch(IOException e) {
            new DefaultIOExceptionMappingService().map(e);
        }
    }

    public List<RootFolder> roots() {
        return roots;
    }

    @Override
    protected void logout() {
        client.getHttpClient().close();
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T _getFeature(final Class<T> type) {
        if(type == IdProvider.class) {
            return (T) fileid;
        }
        if(type == ListService.class) {
            return (T) new StoregateListService(this, fileid);
        }
        if(type == Read.class) {
            return (T) new StoregateReadFeature(this, fileid);
        }
        if(type == Write.class) {
            return (T) new StoregateWriteFeature(this, fileid);
        }
        if(type == MultipartWrite.class) {
            return (T) new StoregateMultipartWriteFeature(this, fileid);
        }
        if(type == Touch.class) {
            return (T) new StoregateTouchFeature(this, fileid);
        }
        if(type == Move.class) {
            return (T) new StoregateMoveFeature(this, fileid);
        }
        if(type == Copy.class) {
            return (T) new StoregateCopyFeature(this, fileid);
        }
        if(type == Directory.class) {
            return (T) new StoregateDirectoryFeature(this, fileid);
        }
        if(type == Delete.class) {
            return (T) new StoregateDeleteFeature(this, fileid);
        }
        if(type == AttributesFinder.class) {
            return (T) new StoregateAttributesFinderFeature(this, fileid);
        }
        if(type == Lock.class) {
            return (T) new StoregateLockFeature(this, fileid);
        }
        if(type == PromptUrlProvider.class) {
            return (T) new StoregateShareFeature(this, fileid);
        }
        if(type == Timestamp.class) {
            return (T) new StoregateTimestampFeature(this, fileid);
        }
        return super._getFeature(type);
    }
}
