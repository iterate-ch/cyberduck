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
import ch.cyberduck.core.exception.ConnectionRefusedException;
import ch.cyberduck.core.exception.ConnectionTimeoutException;
import ch.cyberduck.core.exception.ResolveFailedException;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.http.NoHttpResponseException;
import org.apache.log4j.Logger;

import java.io.EOFException;
import java.io.InterruptedIOException;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.text.MessageFormat;
import java.util.concurrent.TimeoutException;

public abstract class AbstractExceptionMappingService<T extends Throwable> implements ExceptionMappingService<T> {
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
        exception.setFile(file);
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
        return this.wrap(failure, LocaleFactory.localizedString("Connection failed", "Error"), buffer);
    }

    protected BackgroundException wrap(final T failure, final String title, final StringBuilder buffer) {
        if(buffer.toString().isEmpty()) {
            log.warn(String.format("No message for failure %s", failure));
            this.append(buffer, LocaleFactory.localizedString("Interoperability failure", "Error"));
        }
        for(Throwable cause : ExceptionUtils.getThrowableList(failure)) {
            if(cause instanceof InterruptedIOException) {
                // Handling socket timeouts
                return new ConnectionTimeoutException(buffer.toString(), failure);
            }
            if(cause instanceof TimeoutException) {
                //
                return new ConnectionTimeoutException(buffer.toString(), failure);
            }
            if(cause instanceof SocketException) {
                return new DefaultSocketExceptionMappingService().map((SocketException) cause);
            }
            if(cause instanceof EOFException) {
                return new ConnectionRefusedException(buffer.toString(), failure);
            }
            if(cause instanceof UnknownHostException) {
                return new ResolveFailedException(buffer.toString(), failure);
            }
            if(cause instanceof NoHttpResponseException) {
                return new ConnectionRefusedException(buffer.toString(), failure);
            }
        }
        return new BackgroundException(title, buffer.toString(), failure);
    }
}
