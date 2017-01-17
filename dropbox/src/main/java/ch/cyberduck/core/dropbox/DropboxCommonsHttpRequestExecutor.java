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

import ch.cyberduck.core.http.DelayedHttpEntity;
import ch.cyberduck.core.threading.DefaultThreadPool;

import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.message.BasicHeader;
import org.apache.http.protocol.HTTP;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import com.dropbox.core.http.HttpRequestor;

public class DropboxCommonsHttpRequestExecutor extends HttpRequestor {
    private static final Logger log = Logger.getLogger(DropboxCommonsHttpRequestExecutor.class);

    private final CloseableHttpClient client;

    public DropboxCommonsHttpRequestExecutor(final DropboxSession session, final CloseableHttpClient client) {
        this.client = client;
    }

    @Override
    public Response doGet(final String url, final Iterable<Header> headers) throws IOException {
        final HttpGet request = new HttpGet(url);
        for(Header header : headers) {
            if(header.getKey().equals(HTTP.TRANSFER_ENCODING)) {
                continue;
            }
            if(header.getKey().equals(HTTP.CONTENT_LEN)) {
                continue;
            }
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
        final HttpEntityEnclosingRequestBase request = new HttpPost(url);
        return this.execute(url, headers, request);
    }

    @Override
    public Uploader startPut(final String url, final Iterable<Header> headers) throws IOException {
        final HttpEntityEnclosingRequestBase request = new HttpPut(url);
        return this.execute(url, headers, request);
    }

    private Uploader execute(final String url, final Iterable<Header> headers, final HttpEntityEnclosingRequestBase request) throws IOException {
        for(Header header : headers) {
            if(header.getKey().equals(HTTP.TRANSFER_ENCODING)) {
                continue;
            }
            if(header.getKey().equals(HTTP.CONTENT_LEN)) {
                continue;
            }
            request.addHeader(new BasicHeader(header.getKey(), header.getValue()));
        }
        final CountDownLatch entry = new CountDownLatch(1);
        final DelayedHttpEntity entity = new DelayedHttpEntity(entry) {
            @Override
            public long getContentLength() {
                for(Header header : headers) {
                    if(header.getKey().equals(HTTP.CONTENT_LEN)) {
                        return Long.valueOf(header.getValue());
                    }
                }
                // Content-Encoding: chunked
                return -1L;
            }
        };
        request.setEntity(entity);
        final DefaultThreadPool<CloseableHttpResponse> executor = new DefaultThreadPool<CloseableHttpResponse>(1, String.format("http-%s", url));
        final Future<CloseableHttpResponse> future = executor.execute(new Callable<CloseableHttpResponse>() {
            @Override
            public CloseableHttpResponse call() throws Exception {
                return client.execute(request);
            }
        });
        return new Uploader() {
            @Override
            public OutputStream getBody() {
                try {
                    // Await execution of HTTP request to make stream available
                    entry.await();
                }
                catch(InterruptedException e) {
                    throw new RuntimeException(e);
                }
                return entity.getStream();
            }

            @Override
            public void close() {
                try {
                    // Close stream and exit client execution
                    entity.getStream().close();
                }
                catch(IOException e) {
                    log.warn(String.format("Failure closing stream for %s. %s", url, e.getMessage()));
                }
            }

            @Override
            public void abort() {
                this.close();
            }

            @Override
            public Response finish() throws IOException {
                final CloseableHttpResponse response;
                try {
                    response = future.get();
                }
                catch(InterruptedException e) {
                    throw new IOException(e);
                }
                catch(ExecutionException e) {
                    throw new IOException(e.getCause());
                }
                finally {
                    executor.shutdown(false);
                }
                final Map<String, List<String>> responseHeaders = new HashMap<String, List<String>>();
                for(org.apache.http.Header header : response.getAllHeaders()) {
                    // Ignore multiple headers with the same name
                    responseHeaders.put(header.getName(), Collections.singletonList(header.getValue()));
                }
                return new Response(response.getStatusLine().getStatusCode(), response.getEntity().getContent(), responseHeaders);
            }
        };
    }

    public void shutdown() throws IOException {
        client.close();
    }
}
