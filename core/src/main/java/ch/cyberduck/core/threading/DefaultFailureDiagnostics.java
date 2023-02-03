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

import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.exception.ConnectionCanceledException;
import ch.cyberduck.core.exception.ConnectionRefusedException;
import ch.cyberduck.core.exception.ConnectionTimeoutException;
import ch.cyberduck.core.exception.LoginFailureException;
import ch.cyberduck.core.exception.QuotaException;
import ch.cyberduck.core.exception.ResolveFailedException;
import ch.cyberduck.core.exception.SSLNegotiateException;
import ch.cyberduck.core.exception.TransferCanceledException;
import ch.cyberduck.core.exception.UnsupportedException;
import ch.cyberduck.core.io.IOResumeException;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.http.NoHttpResponseException;
import org.apache.http.conn.ConnectTimeoutException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.net.ssl.SSLException;
import javax.net.ssl.SSLHandshakeException;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.concurrent.TimeoutException;

public final class DefaultFailureDiagnostics implements FailureDiagnostics<BackgroundException> {
    private static final Logger log = LogManager.getLogger(DefaultFailureDiagnostics.class);

    @Override
    public Type determine(final BackgroundException failure) {
        if(log.isDebugEnabled()) {
            log.debug(String.format("Determine cause for failure %s", failure));
        }
        for(Throwable cause : ExceptionUtils.getThrowableList(failure)) {
            if(failure instanceof UnsupportedException) {
                return Type.unsupported;
            }
            if(failure instanceof LoginFailureException) {
                return Type.login;
            }
            if(cause instanceof ResolveFailedException) {
                return Type.network;
            }
            if(failure instanceof TransferCanceledException) {
                return Type.skip;
            }
            if(failure instanceof ConnectionCanceledException) {
                return Type.cancel;
            }
            if(cause instanceof ConnectionTimeoutException) {
                return Type.network;
            }
            if(cause instanceof ConnectionRefusedException) {
                return Type.network;
            }
            if(cause instanceof SSLNegotiateException) {
                return Type.application;
            }
            if(cause instanceof SSLHandshakeException) {
                return Type.application;
            }
            if(cause instanceof SSLException) {
                return Type.network;
            }
            if(cause instanceof NoHttpResponseException) {
                return Type.network;
            }
            if(cause instanceof ConnectTimeoutException) {
                return Type.network;
            }
            if(cause instanceof SocketException
                || cause instanceof IOResumeException
                || cause instanceof TimeoutException // Used in Promise#retrieve
                || cause instanceof SocketTimeoutException
                || cause instanceof UnknownHostException) {
                return Type.network;
            }
            if(cause instanceof QuotaException) {
                return Type.quota;
            }
        }
        return Type.application;
    }
}
