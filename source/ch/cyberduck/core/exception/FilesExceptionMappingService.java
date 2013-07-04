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

import org.apache.http.HttpStatus;
import org.apache.http.StatusLine;

import com.rackspacecloud.client.cloudfiles.FilesException;

/**
 * @version $Id$
 */
public class FilesExceptionMappingService extends AbstractIOExceptionMappingService<FilesException> {

    @Override
    public BackgroundException map(final FilesException e) {
        final StringBuilder buffer = new StringBuilder();
        this.append(buffer, e.getMessage());
        final StatusLine status = e.getHttpStatusLine();
        if(null != status) {
            this.append(buffer, String.format("%d %s", status.getStatusCode(), status.getReasonPhrase()));
        }
        if(e.getHttpStatusCode() == HttpStatus.SC_UNAUTHORIZED) {
            return new LoginFailureException(buffer.toString(), e);
        }
        if(e.getHttpStatusCode() == HttpStatus.SC_FORBIDDEN) {
            return new LoginFailureException(buffer.toString(), e);
        }
        if(e.getHttpStatusCode() == HttpStatus.SC_NOT_FOUND) {
            return new NotfoundException(buffer.toString(), e);
        }
        return this.wrap(e, buffer);
    }
}