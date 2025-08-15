package ch.cyberduck.core.azure;

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

import ch.cyberduck.core.AbstractExceptionMappingService;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.exception.LoginFailureException;
import ch.cyberduck.core.http.DefaultHttpResponseExceptionMappingService;
import ch.cyberduck.core.ssl.SSLExceptionMappingService;

import org.apache.commons.lang3.exception.ExceptionUtils;

import javax.net.ssl.SSLException;
import java.util.Map;

import com.azure.core.exception.HttpResponseException;

public class AzureExceptionMappingService extends AbstractExceptionMappingService<HttpResponseException> {

    @Override
    public BackgroundException map(final HttpResponseException failure) {
        final StringBuilder buffer = new StringBuilder();
        switch(failure.getResponse().getStatusCode()) {
            case 403:
                if(failure.getValue() instanceof Map) {
                    final Map messages = (Map) failure.getValue();
                    if(messages.containsKey("Message")) {
                        this.append(buffer, messages.get("Message").toString());
                    }
                }
                return new LoginFailureException(buffer.toString(), failure);
        }
        this.append(buffer, failure.getMessage());
        for(Throwable cause : ExceptionUtils.getThrowableList(failure)) {
            if(cause instanceof SSLException) {
                return new SSLExceptionMappingService().map(buffer.toString(), (SSLException) cause);
            }
        }
        return new DefaultHttpResponseExceptionMappingService().map(failure, buffer, failure.getResponse().getStatusCode());
    }
}
