package ch.cyberduck.core.dav;

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

import ch.cyberduck.core.http.RedirectCallback;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpEntityEnclosingRequest;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.ProtocolException;
import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.protocol.HttpContext;

import java.net.URI;

import com.github.sardine.impl.SardineRedirectStrategy;

public class DAVRedirectStrategy extends SardineRedirectStrategy {
    private final RedirectCallback callback;

    public DAVRedirectStrategy(final RedirectCallback callback) {
        this.callback = callback;
    }

    @Override
    protected boolean isRedirectable(final String method) {
        return callback.redirect(method);
    }

    @Override
    public HttpUriRequest getRedirect(HttpRequest request, HttpResponse response, HttpContext context) throws ProtocolException {
        final String method = request.getRequestLine().getMethod();
        if(method.equalsIgnoreCase(HttpPut.METHOD_NAME)) {
            return this.copyEntity(new HttpPut(this.getLocationURI(request, response, context)), request);
        }
        return super.getRedirect(request, response, context);
    }

    @Override
    protected URI createLocationURI(final String location) throws ProtocolException {
        return super.createLocationURI(StringUtils.replaceAll(location, " ", "%20"));
    }

    private HttpUriRequest copyEntity(final HttpEntityEnclosingRequestBase redirect, final HttpRequest original) {
        if(original instanceof HttpEntityEnclosingRequest) {
            redirect.setEntity(((HttpEntityEnclosingRequest) original).getEntity());
        }
        return redirect;
    }
}
