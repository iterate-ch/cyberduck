package ch.cyberduck.core.box;

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

import ch.cyberduck.core.StringAppender;
import ch.cyberduck.core.box.io.swagger.client.JSON;
import ch.cyberduck.core.box.io.swagger.client.model.ClientError;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpResponseException;
import org.apache.http.impl.client.AbstractResponseHandler;

import java.io.IOException;

public abstract class BoxClientErrorResponseHandler<T> extends AbstractResponseHandler<T> {

    @Override
    public T handleResponse(final HttpResponse response) throws IOException {
        final StatusLine statusLine = response.getStatusLine();
        final HttpEntity entity = response.getEntity();
        if(statusLine.getStatusCode() >= 300) {
            final StringAppender message = new StringAppender();
            message.append(statusLine.getReasonPhrase());
            final ClientError error = new JSON().getContext(null).readValue(entity.getContent(), ClientError.class);
            message.append(error.getMessage());
            throw new HttpResponseException(statusLine.getStatusCode(), message.toString());
        }
        return super.handleResponse(response);
    }
}
