package ch.cyberduck.core.dav;

/*
 * Copyright (c) 2013 David Kocher. All rights reserved.
 * http://cyberduck.ch/
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
 *
 * Bug fixes, suggestions and comments should be sent to:
 * feedback@cyberduck.ch
 */

import ch.cyberduck.core.preferences.PreferencesFactory;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.Credentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.auth.BasicScheme;
import org.apache.http.impl.client.BasicAuthCache;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import com.github.sardine.DavResource;
import com.github.sardine.impl.SardineImpl;
import com.github.sardine.impl.handler.MultiStatusResponseHandler;
import com.github.sardine.impl.methods.HttpPropFind;
import com.github.sardine.model.Multistatus;
import com.github.sardine.model.Propfind;
import com.github.sardine.model.Response;
import com.github.sardine.util.SardineUtil;

public class DAVClient extends SardineImpl {
    private static final Logger log = LogManager.getLogger(DAVClient.class);

    private final String uri;

    private final BasicAuthCache authCache = new BasicAuthCache();
    private final CredentialsProvider authProvider = new BasicCredentialsProvider();

    public DAVClient(final String uri, final HttpClientBuilder http) {
        super(http);
        this.uri = uri;
    }

    public void setCredentials(final AuthScope authScope, final Credentials credentials) {
        authProvider.setCredentials(authScope, credentials);
    }

    @Override
    public void enablePreemptiveAuthentication(final String hostname, final int httpPort, final int httpsPort, final Charset credentialsCharset) {
        final BasicScheme basicScheme = new BasicScheme(credentialsCharset);
        authCache.put(new HttpHost(hostname, httpPort, "http"), basicScheme);
        authCache.put(new HttpHost(hostname, httpsPort, "https"), basicScheme);
    }

    @Override
    public void disablePreemptiveAuthentication() {
        authCache.clear();
    }

    @Override
    public <T> T execute(final HttpRequestBase request, final ResponseHandler<T> responseHandler) throws IOException {
        if(StringUtils.isNotBlank(request.getURI().getRawQuery())) {
            request.setURI(URI.create(String.format("%s%s?%s", uri, request.getURI().getRawPath(), request.getURI().getRawQuery())));
        }
        else {
            request.setURI(URI.create(String.format("%s%s", uri, request.getURI().getRawPath())));
        }
        final HttpClientContext context = HttpClientContext.create();
        context.setCredentialsProvider(authProvider);
        context.setAuthCache(authCache);
        return this.execute(context, request, responseHandler);
    }

    @Override
    protected HttpResponse execute(final HttpRequestBase request) throws IOException {
        if(StringUtils.isNotBlank(request.getURI().getRawQuery())) {
            request.setURI(URI.create(String.format("%s%s?%s", uri, request.getURI().getRawPath(), request.getURI().getRawQuery())));
        }
        else {
            request.setURI(URI.create(String.format("%s%s", uri, request.getURI().getRawPath())));
        }
        final HttpClientContext context = HttpClientContext.create();
        context.setCredentialsProvider(authProvider);
        context.setAuthCache(authCache);
        return this.execute(context, request, null);
    }

    @Override
    public List<DavResource> propfind(final String url, final int depth, final Propfind body) throws IOException {
        HttpPropFind entity = new HttpPropFind(url);
        entity.setDepth(depth < 0 ? "infinity" : Integer.toString(depth));
        entity.setEntity(new StringEntity(SardineUtil.toXml(body), StandardCharsets.UTF_8));
        Multistatus multistatus = this.execute(entity, PreferencesFactory.get().getBoolean("webdav.list.handler.sax") ? new SaxPropFindResponseHandler() : new MultiStatusResponseHandler());
        List<Response> responses = multistatus.getResponse();
        List<DavResource> resources = new ArrayList<>(responses.size());
        for(Response response : responses) {
            try {
                resources.add(new DavResource(response));
            }
            catch(URISyntaxException e) {
                log.warn(String.format("Ignore resource with invalid URI %s", response.getHref().get(0)));
            }
        }
        return resources;
    }

    public HttpClientContext getContext() {
        return context;
    }

    public HttpClient getClient() {
        return client;
    }
}
