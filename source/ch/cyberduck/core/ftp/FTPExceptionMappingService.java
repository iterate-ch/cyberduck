package ch.cyberduck.core.ftp;

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

import ch.cyberduck.core.AbstractExceptionMappingService;
import ch.cyberduck.core.DefaultIOExceptionMappingService;
import ch.cyberduck.core.exception.AccessDeniedException;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.exception.ConnectionCanceledException;
import ch.cyberduck.core.exception.InteroperabilityException;
import ch.cyberduck.core.exception.LoginFailureException;
import ch.cyberduck.core.exception.NotfoundException;
import ch.cyberduck.core.exception.QuotaException;

import org.apache.commons.net.MalformedServerReplyException;
import org.apache.commons.net.ftp.FTPConnectionClosedException;
import org.apache.commons.net.ftp.FTPReply;

import java.io.IOException;

/**
 * @version $Id$
 */
public class FTPExceptionMappingService extends AbstractExceptionMappingService<IOException> {

    @Override
    public BackgroundException map(final IOException e) {
        final StringBuilder buffer = new StringBuilder();
        this.append(buffer, e.getMessage());
        if(e instanceof FTPConnectionClosedException) {
            return new ConnectionCanceledException(e);
        }
        if(e instanceof FTPException) {
            return this.handle((FTPException) e, buffer);
        }
        if(e instanceof MalformedServerReplyException) {
            return new InteroperabilityException(buffer.toString(), e);
        }
        return new DefaultIOExceptionMappingService().map(e);
    }

    private BackgroundException handle(final FTPException e, final StringBuilder buffer) {
        final int status = e.getCode();
        if(status == FTPReply.INSUFFICIENT_STORAGE) {
            return new QuotaException(buffer.toString(), e);
        }
        if(status == FTPReply.STORAGE_ALLOCATION_EXCEEDED) {
            return new QuotaException(buffer.toString(), e);
        }
        if(status == FTPReply.NOT_LOGGED_IN) {
            return new LoginFailureException(buffer.toString(), e);
        }
        if(status == FTPReply.FAILED_SECURITY_CHECK) {
            return new AccessDeniedException(buffer.toString(), e);
        }
        if(status == FTPReply.DENIED_FOR_POLICY_REASONS) {
            return new AccessDeniedException(buffer.toString(), e);
        }
        if(status == FTPReply.NEED_ACCOUNT) {
            return new AccessDeniedException(buffer.toString(), e);
        }
        if(status == FTPReply.NEED_ACCOUNT_FOR_STORING_FILES) {
            return new AccessDeniedException(buffer.toString(), e);
        }
        if(status == FTPReply.FILE_NAME_NOT_ALLOWED) {
            return new AccessDeniedException(buffer.toString(), e);
        }
        if(status == FTPReply.FILE_UNAVAILABLE) {
            // Requested action not taken. File unavailable (e.g., file not found, no access)
            return new NotfoundException(buffer.toString(), e);
        }
        if(status == FTPReply.UNAVAILABLE_RESOURCE) {
            return new NotfoundException(buffer.toString(), e);
        }
        return new InteroperabilityException(buffer.toString(), e);
    }
}
