package ch.cyberduck.core.azure;

/*
 * Copyright (c) 2002-2014 David Kocher. All rights reserved.
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
 * Bug fixes, suggestions and comments should be sent to feedback@cyberduck.ch
 */

import ch.cyberduck.core.AbstractIOExceptionMappingService;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.exception.LoginFailureException;
import ch.cyberduck.core.exception.NotfoundException;

import org.apache.commons.lang3.exception.ExceptionUtils;

import java.net.UnknownHostException;

import com.microsoft.windowsazure.services.core.storage.StorageException;

/**
 * @version $Id:$
 */
public class AzureExceptionMappingService extends AbstractIOExceptionMappingService<StorageException> {

    @Override
    public BackgroundException map(final StorageException e) {
        final StringBuilder buffer = new StringBuilder();
        this.append(buffer, e.getMessage());
        if(ExceptionUtils.getRootCause(e) instanceof UnknownHostException) {
            return new NotfoundException(e.getMessage(), e);
        }
        if(403 == e.getHttpStatusCode()) {
            return new LoginFailureException(e.getMessage(), e);
        }
        if(404 == e.getHttpStatusCode()) {
            return new NotfoundException(e.getMessage(), e);
        }
        return this.wrap(e, buffer);
    }
}