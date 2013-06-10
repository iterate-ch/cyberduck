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

import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.impl.client.AbstractHttpClient;

import java.io.IOException;

import com.googlecode.sardine.impl.SardineImpl;

/**
 * @version $Id:$
 */
public class DAVClient extends SardineImpl {

    private AbstractHttpClient http;

    public DAVClient(final AbstractHttpClient http) {
        super(http);
        this.http = http;
    }

    @Override
    public <T> T execute(final HttpRequestBase request, final ResponseHandler<T> responseHandler) throws IOException {
        return super.execute(request, responseHandler);
    }

    public void disconnect() {
        http.getConnectionManager().shutdown();
    }
}