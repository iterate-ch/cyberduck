package ch.cyberduck.core.exception;

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

import ch.cyberduck.core.Path;
import ch.cyberduck.core.i18n.Locale;

import org.apache.commons.lang.StringUtils;

import java.text.MessageFormat;

/**
 * @version $Id$
 */
public abstract class AbstractIOExceptionMappingService<T extends Exception> implements IOExceptionMappingService<T> {

    public BackgroundException map(final String message, final T failure) {
        final BackgroundException exception = this.map(failure);
        exception.setMessage(StringUtils.chomp(Locale.localizedString(message, "Error")));
        return exception;
    }

    public BackgroundException map(final String message, final T failure, final Path directory) {
        final BackgroundException exception = this.map(failure);
        exception.setPath(directory);
        exception.setMessage(MessageFormat.format(StringUtils.chomp(Locale.localizedString(message, "Error")), directory.getName()));
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
        final BackgroundException exception = new BackgroundException(buffer.toString(), e);
        exception.setMessage(Locale.localizedString("Connection failed"));
        return exception;
    }
}
