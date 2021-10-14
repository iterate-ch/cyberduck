package ch.cyberduck.core.gmxcloud;/*
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

import ch.cyberduck.core.DefaultIOExceptionMappingService;
import ch.cyberduck.core.Factory;
import ch.cyberduck.core.Host;
import ch.cyberduck.core.HostKeyCallback;
import ch.cyberduck.core.ListService;
import ch.cyberduck.core.LoginCallback;
import ch.cyberduck.core.OAuthTokens;
import ch.cyberduck.core.PathNormalizer;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.features.AttributesFinder;
import ch.cyberduck.core.features.Copy;
import ch.cyberduck.core.features.Delete;
import ch.cyberduck.core.features.Directory;
import ch.cyberduck.core.features.FileIdProvider;
import ch.cyberduck.core.features.Find;
import ch.cyberduck.core.features.Move;
import ch.cyberduck.core.features.MultipartWrite;
import ch.cyberduck.core.features.PromptUrlProvider;
import ch.cyberduck.core.features.Read;
import ch.cyberduck.core.features.Touch;
import ch.cyberduck.core.features.Upload;
import ch.cyberduck.core.features.Write;
import ch.cyberduck.core.http.DefaultHttpResponseExceptionMappingService;
import ch.cyberduck.core.http.HttpSession;
import ch.cyberduck.core.oauth.OAuth2AuthorizationService;
import ch.cyberduck.core.oauth.OAuth2ErrorResponseInterceptor;
import ch.cyberduck.core.oauth.OAuth2RequestInterceptor;
import ch.cyberduck.core.preferences.HostPreferences;
import ch.cyberduck.core.preferences.PreferencesFactory;
import ch.cyberduck.core.proxy.Proxy;
import ch.cyberduck.core.ssl.X509KeyManager;
import ch.cyberduck.core.ssl.X509TrustManager;
import ch.cyberduck.core.threading.CancelCallback;

import org.apache.http.HttpHeaders;
import org.apache.http.HttpRequest;
import org.apache.http.HttpRequestInterceptor;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpResponseException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicHeader;
import org.apache.http.protocol.HttpContext;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.text.MessageFormat;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.migcomponents.migbase64.Base64;

public class GmxcloudSession extends HttpSession<CloseableHttpClient> {
    private static final Logger log = Logger.getLogger(GmxcloudSession.class);

    private OAuth2RequestInterceptor authorizationService;

    private String basePath;

    private final GmxcloudResourceIdProvider resourceid = new GmxcloudResourceIdProvider(this);

    public GmxcloudSession(final Host host, final X509TrustManager trust, final X509KeyManager key) {
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
                request.addHeader(new BasicHeader("X-UI-API-KEY", new HostPreferences(host)
                        .getProperty(String.format("apikey.%s", Factory.Platform.getDefault().name()))));
            }
        });
        configuration.addInterceptorLast(new HttpRequestInterceptor() {
            @Override
            public void process(final HttpRequest request, final HttpContext context) {
                request.addHeader(new BasicHeader("X-UI-APP", MessageFormat.format(new HostPreferences(host)
                        .getProperty(String.format("app.%s", Factory.Platform.getDefault().name())), String.format("%s.%s",
                        PreferencesFactory.get().getProperty("application.version"),
                        PreferencesFactory.get().getProperty("application.revision"))))
                );
            }
        });
        return configuration.build();
    }

    @Override
    public void login(final Proxy proxy, final LoginCallback prompt, final CancelCallback cancel) throws BackgroundException {
        final OAuthTokens tokens = authorizationService.refresh(authorizationService.authorize(host, prompt, cancel, OAuth2AuthorizationService.FlowType.AuthorizationCode));
        authorizationService.setTokens(tokens);
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


    @Override
    @SuppressWarnings("unchecked")
    public <T> T _getFeature(final Class<T> type) {
        if(type == FileIdProvider.class) {
            return (T) resourceid;
        }
        if(type == ListService.class) {
            return (T) new GmxcloudListService(this, resourceid);
        }
        if(type == Read.class) {
            return (T) new GmxcloudReadFeature(this, resourceid);
        }
        if(type == Write.class) {
            return (T) new GmxcloudWriteFeature(this, resourceid);
        }
        if(type == MultipartWrite.class) {
            return (T) new GmxcloudMultipartWriteFeature(this, resourceid);
        }
        if(type == Touch.class) {
            return (T) new GmxcloudTouchFeature(this, resourceid);
        }
        if(type == Move.class) {
            return (T) new GmxcloudMoveFeature(this, resourceid);
        }
        if(type == Copy.class) {
            return (T) new GmxcloudCopyFeature(this, resourceid);
        }
        if(type == Directory.class) {
            return (T) new GmxcloudDirectoryFeature(this, resourceid);
        }
        if(type == Delete.class) {
            return (T) new GmxcloudDeleteFeature(this, resourceid);
        }
        if(type == Find.class) {
            return (T) new GmxcloudFindFeature(this, resourceid);
        }
        if(type == AttributesFinder.class) {
            return (T) new GmxcloudAttributesFinderFeature(this, resourceid);
        }
        if(type == Upload.class) {
            return (T) new GmxcloudThresholdUploadService(this, resourceid);
        }
        if(type == PromptUrlProvider.class) {
            return (T) new GmxcloudShareFeature(this, resourceid);
        }
        return super._getFeature(type);
    }
}
