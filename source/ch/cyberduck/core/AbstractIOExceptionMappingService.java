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

import org.apache.commons.lang3.StringUtils;

import java.text.MessageFormat;

/**
 * @version $Id$
 */
public abstract class AbstractIOExceptionMappingService<T extends Exception> implements IOExceptionMappingService<T> {

    public BackgroundException map(final String message, final T failure) {
        final BackgroundException exception = this.map(failure);
        exception.setMessage(StringUtils.chomp(LocaleFactory.localizedString(message, "Error")));
        return exception;
    }

    public BackgroundException map(final String message, final T failure, final Path file) {
        final BackgroundException exception = this.map(failure);
        exception.setMessage(String.format("%s (%s)",
                MessageFormat.format(StringUtils.chomp(LocaleFactory.localizedString(message, "Error")), file.getName()),
                file.getName()));
        return exception;
    }

    /**
     * @param exception Service error
     * @return Mapped exception
     */
    @Override
    public abstract BackgroundException map(T exception);

    protected StringBuilder append(final StringBuilder buffer, final String message) {
        if(StringUtils.isBlank(message)) {
            return buffer;
        }
        if(buffer.length() > 0) {
            buffer.append(" ");
        }
        buffer.append(StringUtils.trim(message));
        if(buffer.charAt(buffer.length() - 1) == '.') {
            return buffer;
        }
        return buffer.append(".");
    }

    protected BackgroundException wrap(final T e, final StringBuilder buffer) {
        return new BackgroundException(LocaleFactory.localizedString("Connection failed", "Error"), buffer.toString(), e);
    }
}
