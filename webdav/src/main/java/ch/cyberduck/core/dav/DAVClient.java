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

import ch.cyberduck.core.http.HttpMethodReleaseInputStream;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.impl.client.HttpClientBuilder;

import java.io.IOException;
import java.net.URI;
import java.util.List;

import com.github.sardine.impl.SardineImpl;
import com.github.sardine.impl.handler.VoidResponseHandler;
import com.github.sardine.impl.io.ContentLengthInputStream;

public class DAVClient extends SardineImpl {

    private final String uri;

    public DAVClient(final String uri, final HttpClientBuilder http) {
        super(http);
        this.uri = uri;
    }

    @Override
    public <T> T execute(final HttpRequestBase request, final ResponseHandler<T> responseHandler) throws IOException {
        if(StringUtils.isNotBlank(request.getURI().getRawQuery())) {
            request.setURI(URI.create(String.format("%s%s?%s", uri, request.getURI().getRawPath(), request.getURI().getRawQuery())));
        }
        else {
            request.setURI(URI.create(String.format("%s%s", uri, request.getURI().getRawPath())));
        }
        return super.execute(request, responseHandler);
    }

    @Override
    protected HttpResponse execute(final HttpRequestBase request) throws IOException {
        if(StringUtils.isNotBlank(request.getURI().getRawQuery())) {
            request.setURI(URI.create(String.format("%s%s?%s", uri, request.getURI().getRawPath(), request.getURI().getRawQuery())));
        }
        else {
            request.setURI(URI.create(String.format("%s%s", uri, request.getURI().getRawPath())));
        }
        return super.execute(request);
    }

    @Override
    public ContentLengthInputStream get(final String url, final List<Header> headers) throws IOException {
        HttpGet get = new HttpGet(url);
        for(Header header : headers) {
            get.addHeader(header);
        }
        // Must use #execute without handler, otherwise the entity is consumed
        // already after the handler exits.
        HttpResponse response = this.execute(get);
        VoidResponseHandler handler = new VoidResponseHandler();
        try {
            handler.handleResponse(response);
            // Will abort the read when closed before EOF.
            return new ContentLengthInputStream(new HttpMethodReleaseInputStream(response), response.getEntity().getContentLength());
        }
        catch(IOException ex) {
            get.abort();
            throw ex;
        }
    }

    public HttpClientContext context() {
        return context;
    }
}