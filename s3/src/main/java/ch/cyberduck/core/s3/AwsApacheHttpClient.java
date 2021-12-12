package ch.cyberduck.core.s3;

/*
 * Copyright (c) 2002-2010 David Kocher. All rights reserved.
 *
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
 * dkocher@cyberduck.ch
 */

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpHead;
import org.apache.http.client.methods.HttpOptions;
import org.apache.http.client.methods.HttpPatch;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.impl.client.CloseableHttpClient;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import software.amazon.awssdk.http.ExecutableHttpRequest;
import software.amazon.awssdk.http.HttpExecuteRequest;
import software.amazon.awssdk.http.HttpExecuteResponse;
import software.amazon.awssdk.http.SdkHttpClient;
import software.amazon.awssdk.http.SdkHttpResponse;

public class AwsApacheHttpClient implements SdkHttpClient{

    private final HttpClient httpClient;

    public AwsApacheHttpClient(HttpClient httpClient){
        this.httpClient = httpClient;
    }

    @Override
    public ExecutableHttpRequest prepareRequest(final HttpExecuteRequest httpExecuteRequest) {
        return new ExecutableHttpRequest() {
            final HttpRequestBase httpRequestBase = AwsApacheHttpClient.this.createApacheRequest(httpExecuteRequest,
                httpExecuteRequest.httpRequest().getUri().toString());
            @Override
            public HttpExecuteResponse call() throws IOException {
                final HttpResponse httpResponse = AwsApacheHttpClient.this.httpClient.execute(httpRequestBase);
                SdkHttpResponse response = SdkHttpResponse.builder()
                    .statusCode(httpResponse.getStatusLine().getStatusCode())
                    .statusText(httpResponse.getStatusLine().getReasonPhrase())
                    .headers(AwsApacheHttpClient.this.transformHeaders(httpResponse))
                    .build();
                return HttpExecuteResponse.builder().response(response).build();
            }

            @Override
            public void abort() {
                httpRequestBase.abort();
            }
        };
    }

    @Override
    public String clientName() {
        return "Apache";
    }

    @Override
    public void close() {
        if (this.httpClient instanceof CloseableHttpClient) {
            try {
                ((CloseableHttpClient)this.httpClient).close();
            }
            catch(IOException e) {
                throw new RuntimeException(e);
            }
        }
    }


    private HttpRequestBase createApacheRequest(HttpExecuteRequest request, String uri) {
        switch(request.httpRequest().method()) {
            case HEAD:
                return new HttpHead(uri);
            case GET:
                return new HttpGet(uri);
            case DELETE:
                return new HttpDelete(uri);
            case OPTIONS:
                return new HttpOptions(uri);
            case PATCH:
                return new HttpPatch(uri);
            case POST:
                return new HttpPost(uri);
            case PUT:
                return new HttpPut(uri);
            default:
                throw new RuntimeException("Unknown HTTP method name: " + request.httpRequest().method());
        }
    }

    private Map<String, List<String>> transformHeaders(HttpResponse apacheHttpResponse) {
        return Stream.of(apacheHttpResponse.getAllHeaders()).collect(Collectors.groupingBy(NameValuePair::getName, Collectors.mapping(NameValuePair::getValue, Collectors.toList())));
    }

}
