package ch.cyberduck.core.dropbox;

/*
 * Copyright (c) 2002-2016 iterate GmbH. All rights reserved.
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

import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.message.BasicHeader;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.dropbox.core.http.HttpRequestor;

public class CommonsHttpRequestExecutor extends HttpRequestor {

    private final CloseableHttpClient client;

    public CommonsHttpRequestExecutor(final CloseableHttpClient client) {
        this.client = client;
    }

    @Override
    public Response doGet(final String url, final Iterable<Header> headers) throws IOException {
        final HttpGet request = new HttpGet(url);
        for(Header header : headers) {
            request.addHeader(new BasicHeader(header.getKey(), header.getValue()));
        }
        final CloseableHttpResponse response = client.execute(request);
        final Map<String, List<String>> responseHeaders = new HashMap<>();
        for(org.apache.http.Header header : response.getAllHeaders()) {
            // Ignore multiple headers with the same name
            responseHeaders.put(header.getName(), Collections.singletonList(header.getValue()));
        }
        return new Response(response.getStatusLine().getStatusCode(), response.getEntity().getContent(), responseHeaders);
    }

    @Override
    public Uploader startPost(final String url, final Iterable<Header> headers) throws IOException {
        final HttpPost request = new HttpPost(url);
        for(Header header : headers) {
            request.addHeader(new BasicHeader(header.getKey(), header.getValue()));
        }
        final CloseableHttpResponse response = client.execute(request);
        return new Uploader() {
            @Override
            public OutputStream getBody() {
                throw new UnsupportedOperationException();
            }

            @Override
            public void close() {
                throw new UnsupportedOperationException();
            }

            @Override
            public void abort() {
                throw new UnsupportedOperationException();
            }

            @Override
            public Response finish() throws IOException {
                throw new UnsupportedOperationException();
            }
        };
    }

    @Override
    public Uploader startPut(final String url, final Iterable<Header> headers) throws IOException {
        final HttpPut request = new HttpPut(url);
        for(Header header : headers) {
            request.addHeader(new BasicHeader(header.getKey(), header.getValue()));
        }
        final CloseableHttpResponse response = client.execute(request);
        return new Uploader() {
            @Override
            public OutputStream getBody() {
                throw new UnsupportedOperationException();
            }

            @Override
            public void close() {
                throw new UnsupportedOperationException();
            }

            @Override
            public void abort() {
                throw new UnsupportedOperationException();
            }

            @Override
            public Response finish() throws IOException {
                throw new UnsupportedOperationException();
            }
        };
    }
}
