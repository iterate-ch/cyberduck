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
import ch.cyberduck.core.exception.ConnectionRefusedException;
import ch.cyberduck.core.exception.ConnectionTimeoutException;
import ch.cyberduck.core.ssl.SSLExceptionMappingService;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.log4j.Logger;

import javax.net.ssl.SSLException;
import java.io.InterruptedIOException;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.text.MessageFormat;

/**
 * @version $Id$
 */
public abstract class AbstractExceptionMappingService<T extends Exception> implements ExceptionMappingService<T> {
    private static final Logger log = Logger.getLogger(AbstractExceptionMappingService.class);

    public BackgroundException map(final String message, final T failure) {
        final BackgroundException exception = this.map(failure);
        final StringBuilder m = new StringBuilder();
        this.append(m, StringUtils.chomp(LocaleFactory.localizedString(message, "Error")));
        exception.setMessage(m.toString());
        return exception;
    }

    public BackgroundException map(final String message, final T failure, final Path file) {
        final BackgroundException exception = this.map(failure);
        final StringBuilder m = new StringBuilder();
        final String formatted = MessageFormat.format(StringUtils.chomp(
                LocaleFactory.localizedString(message, "Error")), file.getName());
        if(StringUtils.contains(formatted, String.format("%s ", file.getName()))
                || StringUtils.contains(formatted, String.format(" %s", file.getName()))) {
            this.append(m, formatted);
        }
        else {
            this.append(m, String.format("%s (%s)",
                    MessageFormat.format(StringUtils.chomp(LocaleFactory.localizedString(message, "Error")), file.getName()),
                    file.getAbsolute()));
        }
        exception.setMessage(m.toString());
        return exception;
    }

    /**
     * @param exception Service error
     * @return Mapped exception
     */
    @Override
    public abstract BackgroundException map(T exception);

    public StringBuilder append(final StringBuilder buffer, final String message) {
        final StringAppender appender = new StringAppender(buffer);
        appender.append(StringUtils.capitalize(message));
        return buffer;
    }

    protected BackgroundException wrap(final T failure, final StringBuilder buffer) {
        if(buffer.toString().isEmpty()) {
            log.warn(String.format("No message for failure %s", failure));
            this.append(buffer, LocaleFactory.localizedString("Interoperability failure", "Error"));
        }
        if(failure instanceof SSLException) {
            return new SSLExceptionMappingService().map((SSLException) failure);
        }
        for(Throwable cause : ExceptionUtils.getThrowableList(failure)) {
            if(cause instanceof InterruptedIOException) {
                // Handling socket timeouts
                return new ConnectionTimeoutException(buffer.toString(), failure);
            }
            if(cause instanceof SocketException) {
                if(StringUtils.equals(cause.getMessage(), "Software caused connection abort")) {
                    return new ConnectionCanceledException(failure);
                }
                if(StringUtils.equals(cause.getMessage(), "Socket closed")) {
                    return new ConnectionCanceledException(failure);
                }
                return new ConnectionRefusedException(buffer.toString(), failure);
            }
            if(cause instanceof UnknownHostException) {
                return new ConnectionRefusedException(buffer.toString(), failure);
            }
        }
        return new BackgroundException(
                LocaleFactory.localizedString("Connection failed", "Error"), buffer.toString(), failure);
    }
}
