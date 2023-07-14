package ch.cyberduck.core.eue;

/*
 * Copyright (c) 2002-2021 iterate GmbH. All rights reserved.
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
import ch.cyberduck.core.ExpiringObjectHolder;
import ch.cyberduck.core.Host;
import ch.cyberduck.core.HostKeyCallback;
import ch.cyberduck.core.ListService;
import ch.cyberduck.core.LoginCallback;
import ch.cyberduck.core.OAuthTokens;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathNormalizer;
import ch.cyberduck.core.UrlProvider;
import ch.cyberduck.core.eue.io.swagger.client.ApiException;
import ch.cyberduck.core.eue.io.swagger.client.api.GetUserSharesApi;
import ch.cyberduck.core.eue.io.swagger.client.api.UserInfoApi;
import ch.cyberduck.core.eue.io.swagger.client.model.UserSharesModel;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.exception.InteroperabilityException;
import ch.cyberduck.core.exception.NotfoundException;
import ch.cyberduck.core.features.*;
import ch.cyberduck.core.http.DefaultHttpRateLimiter;
import ch.cyberduck.core.http.DefaultHttpResponseExceptionMappingService;
import ch.cyberduck.core.http.HttpSession;
import ch.cyberduck.core.http.RateLimitingHttpRequestInterceptor;
import ch.cyberduck.core.oauth.OAuth2ErrorResponseInterceptor;
import ch.cyberduck.core.oauth.OAuth2RequestInterceptor;
import ch.cyberduck.core.preferences.HostPreferences;
import ch.cyberduck.core.preferences.PreferencesFactory;
import ch.cyberduck.core.proxy.Proxy;
import ch.cyberduck.core.ssl.X509KeyManager;
import ch.cyberduck.core.ssl.X509TrustManager;
import ch.cyberduck.core.threading.BackgroundActionPauser;
import ch.cyberduck.core.threading.CancelCallback;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.Header;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpRequest;
import org.apache.http.HttpRequestInterceptor;
import org.apache.http.HttpResponse;
import org.apache.http.HttpResponseInterceptor;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpResponseException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicHeader;
import org.apache.http.protocol.HttpContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.text.MessageFormat;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.Optional;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.migcomponents.migbase64.Base64;

public class EueSession extends HttpSession<CloseableHttpClient> {
    private static final Logger log = LogManager.getLogger(EueSession.class);

    private OAuth2RequestInterceptor authorizationService;

    private String basePath;
    private String vaultResourceId;

    private final EueResourceIdProvider resourceid = new EueResourceIdProvider(this);

    private final ExpiringObjectHolder<UserSharesModel> userShares
            = new ExpiringObjectHolder<>(new HostPreferences(host).getLong("eue.shares.ttl"));

    public EueSession(final Host host, final X509TrustManager trust, final X509KeyManager key) {
        super(host, trust, key);
    }

    @Override
    protected CloseableHttpClient connect(final Proxy proxy, final HostKeyCallback key, final LoginCallback prompt, final CancelCallback cancel) throws BackgroundException {
        final HttpClientBuilder configuration = builder.build(proxy, this, prompt);
        authorizationService = new OAuth2RequestInterceptor(builder.build(proxy, this, prompt).addInterceptorLast(new HttpRequestInterceptor() {
            @Override
            public void process(final HttpRequest request, final HttpContext context) {
                request.addHeader(HttpHeaders.AUTHORIZATION,
                        String.format("Basic %s", Base64.encodeToString(String.format("%s:%s", host.getProtocol().getOAuthClientId(), host.getProtocol().getOAuthClientSecret()).getBytes(StandardCharsets.UTF_8), false)));
            }
        }).build(), host)
                .withRedirectUri(host.getProtocol().getOAuthRedirectUrl()
                );
        configuration.setServiceUnavailableRetryStrategy(new OAuth2ErrorResponseInterceptor(host, authorizationService, prompt));
        configuration.addInterceptorLast(authorizationService);
        configuration.addInterceptorLast(new HttpRequestInterceptor() {
            @Override
            public void process(final HttpRequest request, final HttpContext context) {
                final String identifier = new HostPreferences(host).getProperty("apikey");
                if(StringUtils.isNotBlank(identifier)) {
                    request.addHeader(new BasicHeader("X-UI-API-KEY", identifier));
                }
            }
        });
        configuration.addInterceptorLast(new HttpRequestInterceptor() {
            @Override
            public void process(final HttpRequest request, final HttpContext context) {
                final String identifier = new HostPreferences(host).getProperty("app");
                if(StringUtils.isNotBlank(identifier)) {
                    final String app = String.format("%s.%s",
                            PreferencesFactory.get().getProperty("application.version"),
                            PreferencesFactory.get().getProperty("application.revision"));
                    request.addHeader(new BasicHeader("X-UI-APP", MessageFormat.format(identifier, app)));
                    if(StringUtils.isNotBlank(vaultResourceId)) {
                        if(StringUtils.contains(request.getRequestLine().getUri(), vaultResourceId)) {
                            // Overwrite default
                            request.setHeader(new BasicHeader("X-UI-APP", MessageFormat.format(
                                    StringUtils.replace(identifier, "/", ".tresor/"), app)));
                        }
                    }
                }
            }
        });
        configuration.addInterceptorLast(new HttpResponseInterceptor() {
            @Override
            public void process(final HttpResponse response, final HttpContext context) {
                final Optional<Header> hint = Arrays.asList(response.getAllHeaders()).stream()
                        .filter(header -> "X-UI-TRAFFIC-HINT".equalsIgnoreCase(header.getName())).findFirst();
                if(hint.isPresent()) {
                    // Any response can contain this header. If this happens, a client should take measures to
                    // reduce its request rate. We advise to wait two seconds before sending the next request.
                    log.warn(String.format("Retrieved throttle warning %s", hint.get()));
                    final BackgroundActionPauser pause = new BackgroundActionPauser(new BackgroundActionPauser.Callback() {
                        @Override
                        public void validate() {
                        }

                        @Override
                        public void progress(final Integer seconds) {
                            log.warn(String.format("Pause for %d seconds because of traffic hint", seconds));
                        }
                    }, new HostPreferences(host).getInteger("eue.limit.hint.second"));
                    pause.await();
                }
            }
        });
        if(new HostPreferences(host).getBoolean("eue.limit.requests.enable")) {
            configuration.addInterceptorLast(new RateLimitingHttpRequestInterceptor(new DefaultHttpRateLimiter(
                    new HostPreferences(host).getInteger("eue.limit.requests.second")
            )));
        }
        return configuration.build();
    }

    @Override
    public void login(final Proxy proxy, final LoginCallback prompt, final CancelCallback cancel) throws BackgroundException {
        try {
            authorizationService.refresh(authorizationService.authorize(host, prompt, cancel));
        }
        catch(InteroperabilityException e) {
            // Perm.INVALID_GRANT
            log.warn(String.format("Failure %s refreshing OAuth tokens", e));
            // Reset OAuth Tokens
            host.getCredentials().setOauth(OAuthTokens.EMPTY);
            authorizationService.authorize(host, prompt, cancel
            );
        }
        try {
            final StringBuilder url = new StringBuilder();
            url.append(host.getProtocol().getScheme().toString()).append("://");
            url.append(host.getProtocol().getDefaultHostname());
            if(!(host.getProtocol().getScheme().getPort() == host.getPort())) {
                url.append(":").append(host.getPort());
            }
            final String context = PathNormalizer.normalize(host.getProtocol().getContext());
            // Custom authentication context
            url.append(context);
            // Determine RestFS URL from service discovery
            final HttpGet request = new HttpGet(url.toString());
            final CloseableHttpResponse response = client.execute(request);
            switch(response.getStatusLine().getStatusCode()) {
                case HttpStatus.SC_OK:
                    final JsonElement element = JsonParser.parseReader(new InputStreamReader(response.getEntity().getContent()));
                    if(element.isJsonObject()) {
                        final JsonObject json = element.getAsJsonObject();
                        final URI uri = URI.create(json.getAsJsonObject("serviceTarget").getAsJsonPrimitive("uri").getAsString());
                        if(log.isInfoEnabled()) {
                            log.info(String.format("Set base path to %s", url));
                        }
                        this.setBasePath(uri.toString());
                    }
                    break;
                default:
                    throw new DefaultHttpResponseExceptionMappingService().map(new HttpResponseException(
                            response.getStatusLine().getStatusCode(), response.getStatusLine().getReasonPhrase()));
            }
            final Credentials credentials = host.getCredentials();
            credentials.setUsername(new UserInfoApi(new EueApiClient(this))
                    .userinfoGet(null, null).getAccount().getOsServiceId());
            if(StringUtils.isNotBlank(host.getProperty("pacs.url"))) {
                try {
                    client.execute(new HttpPost(host.getProperty("pacs.url")));
                }
                catch(IOException e) {
                    log.warn(String.format("Ignore failure %s running Personal Agent Context Service (PACS) request", e));
                }
            }
            if(StringUtils.isNotBlank(new HostPreferences(host).getProperty("cryptomator.vault.name.default"))) {
                final Path vault = new Path(new HostPreferences(host).getProperty("cryptomator.vault.name.default"), EnumSet.of(Path.Type.directory));
                try {
                    vaultResourceId = new EueAttributesFinderFeature(this, resourceid).find(vault).getFileId();
                    host.setProperty("cryptomator.enable", String.valueOf(true));
                }
                catch(NotfoundException e) {
                    log.warn(String.format("Disable vault features with no existing vault found at %s", vault));
                    // Disable vault features
                    host.setProperty("cryptomator.enable", String.valueOf(false));
                }
            }
            userShares.set(this.userShares());
        }
        catch(ApiException e) {
            throw new EueExceptionMappingService().map(e);
        }
        catch(HttpResponseException e) {
            throw new DefaultHttpResponseExceptionMappingService().map(e);
        }
        catch(IOException e) {
            throw new DefaultIOExceptionMappingService().map(e);
        }
    }

    @Override
    protected void logout() throws BackgroundException {
        try {
            client.close();
        }
        catch(IOException e) {
            throw new DefaultIOExceptionMappingService().map(e);
        }
        finally {
            resourceid.clear();
        }
    }

    @Override
    public <T> T getFeature(final Class<T> type) {
        return super.getFeature(type);
    }

    public String getBasePath() {
        return basePath;
    }

    public void setBasePath(String basePath) {
        this.basePath = basePath;
    }

    public UserSharesModel userShares() throws BackgroundException {
        if(userShares.get() == null) {
            try {
                userShares.set(new GetUserSharesApi(new EueApiClient(this)).shareGet(null, null));
            }
            catch(ApiException e) {
                throw new EueExceptionMappingService().map(e);
            }
        }
        return userShares.get();
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T _getFeature(final Class<T> type) {
        if(type == FileIdProvider.class) {
            return (T) resourceid;
        }
        if(type == ListService.class) {
            return (T) new EueListService(this, resourceid);
        }
        if(type == Read.class) {
            return (T) new EueReadFeature(this, resourceid);
        }
        if(type == Write.class) {
            return (T) new EueThresholdWriteFeature(this, resourceid);
        }
        if(type == MultipartWrite.class) {
            return (T) new EueMultipartWriteFeature(this, resourceid);
        }
        if(type == Touch.class) {
            return (T) new EueTouchFeature(this, resourceid);
        }
        if(type == Move.class) {
            return (T) new EueMoveFeature(this, resourceid);
        }
        if(type == Copy.class) {
            return (T) new EueCopyFeature(this, resourceid);
        }
        if(type == Directory.class) {
            return (T) new EueDirectoryFeature(this, resourceid);
        }
        if(type == Delete.class) {
            return (T) new EueDeleteFeature(this, resourceid);
        }
        if(type == Trash.class) {
            return (T) new EueTrashFeature(this, resourceid);
        }
        if(type == Find.class) {
            return (T) new EueFindFeature(this, resourceid);
        }
        if(type == AttributesFinder.class) {
            return (T) new EueAttributesFinderFeature(this, resourceid);
        }
        if(type == Timestamp.class) {
            return (T) new EueTimestampFeature(this, resourceid);
        }
        if(type == Upload.class) {
            return (T) new EueThresholdUploadService(this, resourceid, registry);
        }
        if(type == UrlProvider.class) {
            return (T) new EueShareUrlProvider(host, userShares.get());
        }
        if(type == Share.class) {
            return (T) new EueShareFeature(this, resourceid);
        }
        if(type == Quota.class) {
            return (T) new EueQuotaFeature(this);
        }
        return super._getFeature(type);
    }
}
