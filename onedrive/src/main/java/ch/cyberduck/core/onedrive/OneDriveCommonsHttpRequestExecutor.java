package ch.cyberduck.core.onedrive;

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

import ch.cyberduck.core.http.DelayedHttpEntity;
import ch.cyberduck.core.threading.DefaultThreadPool;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.message.BasicHeader;
import org.apache.http.protocol.HTTP;
import org.nuxeo.onedrive.client.RequestExecutor;
import org.nuxeo.onedrive.client.RequestHeader;

import java.io.IOException;
import java.io.OutputStream;
import java.net.URL;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

public class OneDriveCommonsHttpRequestExecutor implements RequestExecutor {

    private final CloseableHttpClient client;

    public OneDriveCommonsHttpRequestExecutor(final CloseableHttpClient client) {
        this.client = client;
    }

    @Override
    public Upload doPost(final URL url, final Set<RequestHeader> headers) throws IOException {
        final HttpEntityEnclosingRequestBase request = new HttpPost(url.toString());
        this.authenticate(request);
        for(RequestHeader header : headers) {
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
                for(RequestHeader header : headers) {
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
        return new Upload() {
            @Override
            public Response getResponse() throws IOException {
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
                return new CommonsHttpResponse(response);
            }

            @Override
            public OutputStream getOutputStream() {
                try {
                    // Await execution of HTTP request to make stream available
                    entry.await();
                }
                catch(InterruptedException e) {
                    throw new RuntimeException(e);
                }
                return entity.getStream();
            }
        };
    }

    @Override
    public Response doGet(final URL url, final Set<RequestHeader> headers) throws IOException {
        final HttpGet request = new HttpGet(url.toString());
        for(RequestHeader header : headers) {
            if(header.getKey().equals(HTTP.TRANSFER_ENCODING)) {
                continue;
            }
            if(header.getKey().equals(HTTP.CONTENT_LEN)) {
                continue;
            }
            request.addHeader(new BasicHeader(header.getKey(), header.getValue()));
        }
        this.authenticate(request);
        final CloseableHttpResponse response = client.execute(request);
        return new CommonsHttpResponse(response);
    }

    protected void authenticate(final HttpRequestBase request) {
        //
    }

    private final class CommonsHttpResponse extends Response {
        private HttpResponse response;

        public CommonsHttpResponse(final HttpResponse response) throws IOException {
            super(response.getEntity().getContent());
            this.response = response;
        }

        @Override
        public int getStatusCode() throws IOException {
            return response.getStatusLine().getStatusCode();
        }

        @Override
        public String getStatusMessage() throws IOException {
            return response.getStatusLine().getReasonPhrase();
        }
    }

    public void shutdown() throws IOException {
        client.close();
    }
}
