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
 * Bug fixes, suggestions and comments should be sent to feedback@cyberduck.ch
 */

import ch.cyberduck.core.exception.BackgroundException;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.NoHttpResponseException;

import javax.net.ssl.SSLException;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;

/**
 * @version $Id$
 */
public class NetworkFailureDiagnostics {

    public boolean isNetworkFailure(final BackgroundException e) {
        final Throwable cause = e.getCause();
        if(cause instanceof SSLException) {
            return StringUtils.contains(cause.getMessage(), "Received close_notify during handshake");
        }
        if(cause instanceof NoHttpResponseException) {
            return true;
        }
        return cause instanceof SocketException
                || cause instanceof SocketTimeoutException
                || cause instanceof UnknownHostException;
    }
}
