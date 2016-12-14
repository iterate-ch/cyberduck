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

import org.apache.commons.lang3.exception.ExceptionUtils;

import java.net.UnknownHostException;

import com.microsoft.azure.storage.StorageException;

public class AzureExceptionMappingService extends AbstractExceptionMappingService<StorageException> {

    @Override
    public BackgroundException map(final StorageException e) {
        final StringBuilder buffer = new StringBuilder();
        this.append(buffer, e.getMessage());
        if(ExceptionUtils.getRootCause(e) instanceof UnknownHostException) {
            return new NotfoundException(buffer.toString(), e);
        }
        switch(e.getHttpStatusCode()) {
            case 403:
                return new LoginFailureException(buffer.toString(), e);
            case 404:
                return new NotfoundException(buffer.toString(), e);
            case 304:
            case 405:
            case 400:
            case 411:
            case 412:
                return new InteroperabilityException(buffer.toString(), e);
            case 500:
                // InternalError
                // OperationTimedOut
                return new ConnectionTimeoutException(buffer.toString(), e);
            case 503:
                // ServerBusy
                return new RetriableAccessDeniedException(buffer.toString(), e);
        }
        return this.wrap(e, buffer);
    }
}