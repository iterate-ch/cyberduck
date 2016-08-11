package ch.cyberduck.core.http;

/*
 * Copyright (c) 2002-2014 David Kocher. All rights reserved.
 * http://cyberduck.io/
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
 * feedback@cyberduck.io
 */

import ch.cyberduck.core.PreferencesUseragentProvider;
import ch.cyberduck.core.TranscriptListener;
import ch.cyberduck.core.UseragentProvider;

import org.apache.http.Header;
import org.apache.http.HttpClientConnection;
import org.apache.http.HttpException;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.message.BasicHeader;
import org.apache.http.protocol.HttpContext;
import org.apache.http.protocol.HttpRequestExecutor;

import java.io.IOException;

public class LoggingHttpRequestExecutor extends HttpRequestExecutor {

    private final UseragentProvider useragentProvider
            = new PreferencesUseragentProvider();

    private TranscriptListener listener;

    public LoggingHttpRequestExecutor(final TranscriptListener listener) {
        this.listener = listener;
    }

    @Override
    public HttpResponse execute(final HttpRequest request, final HttpClientConnection conn, final HttpContext context)
            throws IOException, HttpException {
        if(!request.containsHeader(HttpHeaders.USER_AGENT)) {
            request.addHeader(new BasicHeader(HttpHeaders.USER_AGENT, useragentProvider.get()));
        }
        return super.execute(request, conn, context);
    }

    @Override
    protected HttpResponse doSendRequest(final HttpRequest request, final HttpClientConnection conn, final HttpContext context) throws IOException, HttpException {
        listener.log(TranscriptListener.Type.request, request.getRequestLine().toString());
        for(Header header : request.getAllHeaders()) {
            listener.log(TranscriptListener.Type.request, header.toString());
        }
        return super.doSendRequest(request, conn, context);
    }

    @Override
    protected HttpResponse doReceiveResponse(final HttpRequest request, final HttpClientConnection conn, final HttpContext context) throws HttpException, IOException {
        final HttpResponse response = super.doReceiveResponse(request, conn, context);
        listener.log(TranscriptListener.Type.response, response.getStatusLine().toString());
        for(Header header : response.getAllHeaders()) {
            listener.log(TranscriptListener.Type.response, header.toString());
        }
        return response;
    }
}
