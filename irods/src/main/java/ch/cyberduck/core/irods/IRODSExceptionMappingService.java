package ch.cyberduck.core.irods;

/*
 * Copyright (c) 2002-2015 David Kocher. All rights reserved.
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
import ch.cyberduck.core.exception.AccessDeniedException;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.exception.LoginFailureException;
import ch.cyberduck.core.exception.NotfoundException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.irods.jargon.core.exception.AuthenticationException;
import org.irods.jargon.core.exception.CatNoAccessException;
import org.irods.jargon.core.exception.DataNotFoundException;
import org.irods.jargon.core.exception.FileNotFoundException;
import org.irods.jargon.core.exception.InvalidGroupException;
import org.irods.jargon.core.exception.InvalidUserException;
import org.irods.jargon.core.exception.JargonException;

public class IRODSExceptionMappingService extends AbstractExceptionMappingService<JargonException> {
    private static final Logger log = LogManager.getLogger(IRODSExceptionMappingService.class);

    @Override
    public BackgroundException map(final JargonException e) {
        log.warn("Map failure {}", e.toString());
        final StringBuilder buffer = new StringBuilder();
        this.append(buffer, e.getMessage());
        if(e instanceof CatNoAccessException) {
            return new AccessDeniedException(buffer.toString(), e);
        }
        if(e instanceof FileNotFoundException) {
            return new NotfoundException(buffer.toString(), e);
        }
        if(e instanceof DataNotFoundException) {
            return new NotfoundException(buffer.toString(), e);
        }
        if(e instanceof AuthenticationException) {
            return new LoginFailureException(buffer.toString(), e);
        }
        if(e instanceof InvalidUserException) {
            return new LoginFailureException(buffer.toString(), e);
        }
        if(e instanceof InvalidGroupException) {
            return new LoginFailureException(buffer.toString(), e);
        }
        return this.wrap(e, buffer);
    }
}
