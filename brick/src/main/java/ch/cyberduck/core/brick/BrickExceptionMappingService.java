package ch.cyberduck.core.brick;/*
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

import ch.cyberduck.core.AbstractExceptionMappingService;
import ch.cyberduck.core.DefaultIOExceptionMappingService;
import ch.cyberduck.core.DefaultSocketExceptionMappingService;
import ch.cyberduck.core.brick.io.swagger.client.ApiException;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.exception.ConnectionCanceledException;
import ch.cyberduck.core.exception.LockedException;
import ch.cyberduck.core.http.DefaultHttpResponseExceptionMappingService;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpResponseException;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.net.SocketException;

public class BrickExceptionMappingService extends AbstractExceptionMappingService<ApiException> {
    private static final Logger log = Logger.getLogger(BrickExceptionMappingService.class);

    @Override
    public BackgroundException map(final ApiException failure) {
        switch(failure.getCode()) {
            case HttpStatus.SC_UNPROCESSABLE_ENTITY:
                return new LockedException(StringUtils.EMPTY, failure);
        }
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
        return new DefaultHttpResponseExceptionMappingService().map(failure, new StringBuilder(), failure.getCode());
    }
}
