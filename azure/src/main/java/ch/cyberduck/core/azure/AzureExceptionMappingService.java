package ch.cyberduck.core.azure;

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

import ch.cyberduck.core.AbstractExceptionMappingService;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.exception.ConnectionTimeoutException;
import ch.cyberduck.core.exception.InteroperabilityException;
import ch.cyberduck.core.exception.LoginFailureException;
import ch.cyberduck.core.exception.NotfoundException;
import ch.cyberduck.core.exception.RetriableAccessDeniedException;
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
        switch(failure.getResponse().getStatusCode()) {
            case 404:
                return new NotfoundException(buffer.toString(), failure);
            case 304:
            case 405:
            case 400:
            case 411:
            case 412:
                return new InteroperabilityException(buffer.toString(), failure);
            case 500:
                // InternalError
                // OperationTimedOut
                return new ConnectionTimeoutException(buffer.toString(), failure);
            case 503:
                // ServerBusy
                return new RetriableAccessDeniedException(buffer.toString(), failure);
        }
        for(Throwable cause : ExceptionUtils.getThrowableList(failure)) {
            if(cause instanceof SSLException) {
                return new SSLExceptionMappingService().map(buffer.toString(), (SSLException) cause);
            }
        }
        return this.wrap(failure, buffer);
    }
}
