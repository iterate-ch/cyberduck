package ch.cyberduck.core.azure.apache;

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

import org.apache.commons.lang3.StringUtils;
import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.protocol.HTTP;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;

import com.azure.core.http.HttpClient;
import com.azure.core.http.HttpHeader;
import com.azure.core.http.HttpHeaders;
import com.azure.core.http.HttpMethod;
import com.azure.core.http.HttpRequest;
import com.azure.core.http.HttpResponse;
import com.azure.core.util.FluxUtil;
import reactor.core.publisher.Mono;

public class ApacheHttpClient implements HttpClient {
    private final org.apache.http.client.HttpClient httpClient;

    public ApacheHttpClient(final HttpClientBuilder builder) {
        this.httpClient = builder.build();
    }

    public Mono<HttpResponse> send(final HttpRequest azureRequest) {
        try {
            ApacheHttpRequest apacheRequest = new ApacheHttpRequest(azureRequest.getHttpMethod(), azureRequest.getUrl(),
                azureRequest.getHeaders());

            Mono<byte[]> bodyMono = (azureRequest.getBody() != null)
                ? FluxUtil.collectBytesInByteBufferStream(azureRequest.getBody())
                : Mono.just(new byte[0]);

            return bodyMono.flatMap(bodyBytes -> {
                apacheRequest.setEntity(new ByteArrayEntity(bodyBytes));
                try {
                    return Mono.just(new ApacheHttpResponse(azureRequest, httpClient.execute(apacheRequest)));
                }
                catch(IOException ex) {
                    return Mono.error(ex);
                }
            });
        }
        catch(URISyntaxException e) {
            return Mono.error(e);
        }
    }

    private static final class ApacheHttpRequest extends HttpEntityEnclosingRequestBase {
        private final String method;

        private ApacheHttpRequest(HttpMethod method, URL url, HttpHeaders headers) throws URISyntaxException {
            this.method = method.name();
            setURI(url.toURI());
            headers.stream().forEach(this::accept);
        }

        @Override
        public String getMethod() {
            return method;
        }

        private void accept(final HttpHeader header) {
            if(!StringUtils.equalsIgnoreCase(header.getName(), HTTP.CONTENT_LEN)) {
                this.addHeader(header.getName(), header.getValue());
            }
        }
    }
}
