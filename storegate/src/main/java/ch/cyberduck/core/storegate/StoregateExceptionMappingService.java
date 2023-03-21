package ch.cyberduck.core.storegate;

/*
 * Copyright (c) 2002-2019 iterate GmbH. All rights reserved.
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

import ch.cyberduck.core.AbstractExceptionMappingService;
import ch.cyberduck.core.DefaultIOExceptionMappingService;
import ch.cyberduck.core.DefaultSocketExceptionMappingService;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.exception.ConnectionCanceledException;
import ch.cyberduck.core.http.DefaultHttpResponseExceptionMappingService;
import ch.cyberduck.core.storegate.io.swagger.client.ApiException;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpResponseException;

import java.io.IOException;
import java.io.StringReader;
import java.net.SocketException;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;

public class StoregateExceptionMappingService extends AbstractExceptionMappingService<ApiException> {

    private final StoregateIdProvider fileid;

    public StoregateExceptionMappingService(final StoregateIdProvider fileid) {
        this.fileid = fileid;
    }

    @Override
    public BackgroundException map(final String message, final ApiException failure, final Path file) {
        switch(failure.getCode()) {
            case HttpStatus.SC_NOT_FOUND:
                fileid.cache(file, null);
        }
        return super.map(message, failure, file);
    }

    @Override
    public BackgroundException map(final ApiException failure) {
        for(Throwable cause : ExceptionUtils.getThrowableList(failure)) {
            if(cause instanceof SocketException) {
                // Map Connection has been shutdown: javax.net.ssl.SSLException: java.net.SocketException: Broken pipe
                return new DefaultSocketExceptionMappingService().map((SocketException) cause);
            }
            if(cause instanceof HttpResponseException) {
                return new DefaultHttpResponseExceptionMappingService().map((HttpResponseException) cause);
            }
            if(cause instanceof IOException) {
                return new DefaultIOExceptionMappingService().map((IOException) cause);
            }
            if(cause instanceof IllegalStateException) {
                // Caused by: ch.cyberduck.core.sds.io.swagger.client.ApiException: javax.ws.rs.ProcessingException: java.lang.IllegalStateException: Connection pool shut down
                return new ConnectionCanceledException(cause);
            }
        }
        final StringBuilder buffer = new StringBuilder();
        this.parse(buffer, failure.getResponseBody());
        return new DefaultHttpResponseExceptionMappingService().map(failure, buffer, failure.getCode());
    }

    private void parse(final StringBuilder buffer, final String message) {
        if(StringUtils.isBlank(message)) {
            return;
        }
        try {
            final JsonElement element = JsonParser.parseReader(new StringReader(message));
            if(element.isJsonObject()) {
                final JsonObject json = element.getAsJsonObject();
                final JsonPrimitive error = json.getAsJsonPrimitive("message");
                if(null == error) {
                    this.append(buffer, message);
                }
                else {
                    this.append(buffer, error.getAsString());
                }
            }
            if(element.isJsonPrimitive()) {
                this.append(buffer, element.getAsString());
            }
        }
        catch(JsonParseException e) {
            // Ignore
        }
    }
}
