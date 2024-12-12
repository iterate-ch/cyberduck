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
import ch.cyberduck.core.exception.LoginFailureException;
import ch.cyberduck.core.exception.NotfoundException;
import ch.cyberduck.core.http.DefaultHttpResponseExceptionMappingService;
import ch.cyberduck.core.ssl.SSLExceptionMappingService;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.http.client.HttpResponseException;

import javax.net.ssl.SSLException;
import java.net.UnknownHostException;

import com.microsoft.azure.storage.StorageException;

public class AzureExceptionMappingService extends AbstractExceptionMappingService<StorageException> {

    @Override
    public BackgroundException map(final StorageException failure) {
        final StringBuilder buffer = new StringBuilder();
        this.append(buffer, failure.getMessage());
        if(failure.getExtendedErrorInformation() != null) {
            for(String[] details : failure.getExtendedErrorInformation().getAdditionalDetails().values()) {
                for(String detail : details) {
                    this.append(buffer, detail);
                }
            }
        }
        if(ExceptionUtils.getRootCause(failure) instanceof UnknownHostException) {
            return new NotfoundException(buffer.toString(), failure);
        }
        for(Throwable cause : ExceptionUtils.getThrowableList(failure)) {
            if(cause instanceof SSLException) {
                return new SSLExceptionMappingService().map(buffer.toString(), (SSLException) cause);
            }
        }
        switch(failure.getHttpStatusCode()) {
            case 403:
                return new LoginFailureException(buffer.toString(), failure);
            default:
                return new DefaultHttpResponseExceptionMappingService().map(new HttpResponseException(failure.getHttpStatusCode(), buffer.toString()));
        }
    }
}
