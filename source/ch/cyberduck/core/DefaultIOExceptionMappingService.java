package ch.cyberduck.core;

/*
 * Copyright (c) 2013 David Kocher. All rights reserved.
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
 * dkocher@cyberduck.ch
 */

import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.exception.ConnectionCanceledException;
import ch.cyberduck.core.exception.InteroperabilityException;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.log4j.Logger;

import javax.net.ssl.SSLHandshakeException;
import javax.net.ssl.SSLKeyException;
import javax.net.ssl.SSLProtocolException;
import java.io.IOException;
import java.net.SocketException;
import java.security.cert.CertificateException;

/**
 * @version $Id$
 */
public class DefaultIOExceptionMappingService extends AbstractExceptionMappingService<IOException> {
    private static final Logger log = Logger.getLogger(DefaultIOExceptionMappingService.class);

    public BackgroundException map(final IOException failure, final Path directory) {
        return this.map("Connection failed", failure, directory);
    }

    @Override
    public BackgroundException map(final IOException failure) {
        if(failure instanceof SocketException) {
            if(failure.getMessage().equals("Software caused connection abort")) {
                // Do not report as failed if socket opening interrupted
                log.warn("Suppressed socket exception:" + failure.getMessage());
                return new ConnectionCanceledException(failure);
            }
            if(failure.getMessage().equals("Socket closed")) {
                // Do not report as failed if socket opening interrupted
                log.warn("Suppressed socket exception:" + failure.getMessage());
                return new ConnectionCanceledException(failure);
            }
        }
        if(failure instanceof SSLHandshakeException) {
            if(failure.getCause() instanceof CertificateException) {
                log.warn(String.format("Ignore certificate failure %s and drop connection", failure.getMessage()));
                // Server certificate not accepted
                return new ConnectionCanceledException(failure);
            }
        }
        final StringBuilder buffer = new StringBuilder();
        this.append(buffer, failure.getMessage());
        final Throwable cause = ExceptionUtils.getRootCause(failure);
        if(null != cause) {
            this.append(buffer, cause.getMessage());
        }
        if(failure instanceof SSLProtocolException) {
            return new InteroperabilityException(buffer.toString(), failure);
        }
        if(failure instanceof SSLHandshakeException) {
            return new InteroperabilityException(buffer.toString(), failure);
        }
        if(failure instanceof SSLKeyException) {
            return new InteroperabilityException(buffer.toString(), failure);
        }
        return this.wrap(failure, buffer);
    }
}