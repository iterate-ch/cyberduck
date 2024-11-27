package ch.cyberduck.core.jersey;

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

import ch.cyberduck.core.PreferencesUseragentProvider;
import ch.cyberduck.core.http.HttpMethodReleaseInputStream;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.methods.RequestBuilder;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.entity.AbstractHttpEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.protocol.BasicHttpContext;
import org.glassfish.jersey.client.ClientRequest;
import org.glassfish.jersey.client.ClientResponse;
import org.glassfish.jersey.client.spi.AsyncConnectorCallback;
import org.glassfish.jersey.client.spi.Connector;
import org.glassfish.jersey.message.internal.HeaderUtils;
import org.glassfish.jersey.message.internal.OutboundMessageContext;
import org.glassfish.jersey.message.internal.Statuses;

import javax.ws.rs.ProcessingException;
import javax.ws.rs.core.Configuration;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Future;

import com.google.common.util.concurrent.MoreExecutors;

public class HttpComponentsConnector implements Connector {

    private final CloseableHttpClient client;
    private final HttpClientContext context;

    public HttpComponentsConnector(final CloseableHttpClient client, final Configuration runtimeConfig) {
        this.client = client;
        this.context = HttpClientContext.create();
    }

    @Override
    public ClientResponse apply(final ClientRequest clientRequest) throws ProcessingException {
        final HttpUriRequest request = this.toUriHttpRequest(clientRequest);
        final Map<String, String> clientHeadersSnapshot = writeOutBoundHeaders(clientRequest.getHeaders(), request);

        try {
            final CloseableHttpResponse response;
            response = client.execute(new HttpHost(request.getURI().getHost(), request.getURI().getPort(), request.getURI().getScheme()), request, new BasicHttpContext(context));
            HeaderUtils.checkHeaderChanges(clientHeadersSnapshot, clientRequest.getHeaders(), this.getClass().getName());

            final Response.StatusType status = response.getStatusLine().getReasonPhrase() == null
                ? Statuses.from(response.getStatusLine().getStatusCode())
                : Statuses.from(response.getStatusLine().getStatusCode(), response.getStatusLine().getReasonPhrase());

            final ClientResponse responseContext = new ClientResponse(status, clientRequest);
            final List<URI> redirectLocations = context.getRedirectLocations();
            if(redirectLocations != null && !redirectLocations.isEmpty()) {
                responseContext.setResolvedRequestUri(redirectLocations.get(redirectLocations.size() - 1));
            }

            final Header[] respHeaders = response.getAllHeaders();
            final MultivaluedMap<String, String> headers = responseContext.getHeaders();
            for(final Header header : respHeaders) {
                final String headerName = header.getName();
                List<String> list = headers.get(headerName);
                if(list == null) {
                    list = new ArrayList<>();
                }
                list.add(header.getValue());
                headers.put(headerName, list);
            }

            final HttpEntity entity = response.getEntity();

            if(entity != null) {
                if(headers.get(HttpHeaders.CONTENT_LENGTH) == null) {
                    headers.add(HttpHeaders.CONTENT_LENGTH, String.valueOf(entity.getContentLength()));
                }

                final Header contentEncoding = entity.getContentEncoding();
                if(headers.get(HttpHeaders.CONTENT_ENCODING) == null && contentEncoding != null) {
                    headers.add(HttpHeaders.CONTENT_ENCODING, contentEncoding.getValue());
                }
            }
            responseContext.setEntityStream(this.toInputStream(response));
            return responseContext;
        }
        catch(final Exception e) {
            throw new ProcessingException(e);
        }
    }

    private InputStream toInputStream(final CloseableHttpResponse response) throws IOException {
        return new HttpMethodReleaseInputStream(response);
    }

    private HttpUriRequest toUriHttpRequest(final ClientRequest request) {
        final HttpEntity entity = this.getHttpEntity(request);
        return RequestBuilder
            .create(request.getMethod())
            .setUri(request.getUri())
            .setEntity(entity)
            .build();
    }

    private static Map<String, String> writeOutBoundHeaders(final MultivaluedMap<String, Object> headers,
                                                            final HttpUriRequest request) {
        final Map<String, String> stringHeaders = HeaderUtils.asStringHeadersSingleValue(headers);
        for(final Map.Entry<String, String> e : stringHeaders.entrySet()) {
            request.addHeader(e.getKey(), e.getValue());
        }
        return stringHeaders;
    }


    private HttpEntity getHttpEntity(final ClientRequest clientRequest) {
        final Object entity = clientRequest.getEntity();
        if(entity == null) {
            return null;
        }
        return new AbstractHttpEntity() {
            @Override
            public boolean isRepeatable() {
                return false;
            }

            @Override
            public long getContentLength() {
                return -1;
            }

            @Override
            public InputStream getContent() throws IllegalStateException {
                return null;
            }

            @Override
            public void writeTo(final OutputStream outputStream) throws IOException {
                clientRequest.setStreamProvider(new OutboundMessageContext.StreamProvider() {
                    @Override
                    public OutputStream getOutputStream(final int contentLength) {
                        return outputStream;
                    }
                });
                clientRequest.writeEntity();
            }

            @Override
            public boolean isStreaming() {
                return false;
            }
        };
    }

    @Override
    public Future<?> apply(final ClientRequest request, final AsyncConnectorCallback callback) {
        return MoreExecutors.newDirectExecutorService().submit(new Runnable() {
            @Override
            public void run() {
                try {
                    callback.response(apply(request));
                }
                catch(final Throwable t) {
                    callback.failure(t);
                }
            }
        });
    }

    @Override
    public String getName() {
        return new PreferencesUseragentProvider().get();
    }

    @Override
    public void close() {

    }
}
