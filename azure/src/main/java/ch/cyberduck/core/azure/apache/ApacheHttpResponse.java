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

import org.apache.http.HttpEntity;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.Arrays;

import com.azure.core.http.HttpHeaders;
import com.azure.core.http.HttpRequest;
import com.azure.core.http.HttpResponse;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

final class ApacheHttpResponse extends HttpResponse {
    private final int statusCode;
    private final HttpHeaders headers;
    private final HttpEntity entity;

    protected ApacheHttpResponse(HttpRequest request, org.apache.http.HttpResponse apacheResponse) {
        super(request);
        this.statusCode = apacheResponse.getStatusLine().getStatusCode();
        this.headers = new HttpHeaders();
        Arrays.stream(apacheResponse.getAllHeaders())
            .forEach(header -> headers.put(header.getName(), header.getValue()));
        this.entity = apacheResponse.getEntity();
    }

    public int getStatusCode() {
        return statusCode;
    }

    public String getHeaderValue(String s) {
        return headers.getValue(s);
    }

    public HttpHeaders getHeaders() {
        return headers;
    }

    public Flux<ByteBuffer> getBody() {
        return getBodyAsByteArray().map(ByteBuffer::wrap).flux();
    }

    public Mono<byte[]> getBodyAsByteArray() {
        if(null == entity) {
            return Mono.empty();
        }
        try {
            return Mono.just(EntityUtils.toByteArray(entity));
        }
        catch(IOException e) {
            return Mono.error(e);
        }
    }

    public Mono<String> getBodyAsString() {
        return getBodyAsByteArray().map(String::new);
    }

    public Mono<String> getBodyAsString(Charset charset) {
        return getBodyAsByteArray().map(bytes -> new String(bytes, charset));
    }
}
