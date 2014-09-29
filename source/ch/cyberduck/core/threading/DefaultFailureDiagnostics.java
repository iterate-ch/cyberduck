package ch.cyberduck.core.threading;

/*
 * Copyright (c) 2002-2013 David Kocher. All rights reserved.
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

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.http.NoHttpResponseException;
import org.apache.log4j.Logger;

import javax.net.ssl.SSLException;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.concurrent.TimeoutException;

/**
 * @version $Id$
 */
public final class DefaultFailureDiagnostics implements FailureDiagnostics<Exception> {
    private static final Logger log = Logger.getLogger(DefaultFailureDiagnostics.class);

    @Override
    public Type determine(final Exception failure) {
        for(Throwable cause : ExceptionUtils.getThrowableList(failure)) {
            if(cause instanceof SSLException) {
                return Type.network;
            }
            if(cause instanceof NoHttpResponseException) {
                return Type.network;
            }
            if(cause instanceof SocketException) {
                if(StringUtils.equals(cause.getMessage(), "Software caused connection abort")) {
                    // Do not report as failed if socket opening interrupted
                    log.warn(String.format("Suppressed socket exception %s", failure.getMessage()));
                    return Type.dismiss;
                }
                if(StringUtils.equals(cause.getMessage(), "Socket closed")) {
                    // Do not report as failed if socket opening interrupted
                    log.warn(String.format("Suppressed socket exception %s", failure.getMessage()));
                    return Type.dismiss;
                }
            }
            if(cause instanceof SocketException
                    || cause instanceof TimeoutException // Used in Promise#retrieve
                    || cause instanceof SocketTimeoutException
                    || cause instanceof UnknownHostException) {
                return Type.network;
            }
        }
        return Type.application;
    }
}
