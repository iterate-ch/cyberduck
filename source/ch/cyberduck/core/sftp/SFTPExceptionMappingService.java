package ch.cyberduck.core.sftp;

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

import ch.cyberduck.core.AbstractIOExceptionMappingService;
import ch.cyberduck.core.exception.AccessDeniedException;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.exception.ConnectionCanceledException;
import ch.cyberduck.core.exception.InteroperabilityException;
import ch.cyberduck.core.exception.LoginFailureException;
import ch.cyberduck.core.exception.NotfoundException;

import java.io.IOException;

import net.schmizz.sshj.common.DisconnectReason;
import net.schmizz.sshj.sftp.Response;
import net.schmizz.sshj.sftp.SFTPException;
import net.schmizz.sshj.transport.TransportException;
import net.schmizz.sshj.userauth.UserAuthException;

/**
 * @version $Id$
 */
public class SFTPExceptionMappingService extends AbstractIOExceptionMappingService<IOException> {

    @Override
    public BackgroundException map(final IOException e) {
        final StringBuilder buffer = new StringBuilder();
        this.append(buffer, e.getMessage());
        if(e instanceof SFTPException) {
            final SFTPException failure = (SFTPException) e;
            final Response.StatusCode code = failure.getStatusCode();
            if(code == Response.StatusCode.OP_UNSUPPORTED) {
                return new InteroperabilityException(buffer.toString(), e);
            }
            if(code == Response.StatusCode.NO_SUCH_FILE) {
                return new NotfoundException(buffer.toString(), e);
            }
            if(code == Response.StatusCode.PERMISSION_DENIED) {
                return new AccessDeniedException(buffer.toString(), e);
            }
        }
        if(e instanceof UserAuthException) {
            return new LoginFailureException(e.getMessage(), e);
        }
        if(e instanceof TransportException) {
            final TransportException failure = (TransportException) e;
            if(DisconnectReason.HOST_KEY_NOT_VERIFIABLE.equals(failure.getDisconnectReason())) {
                return new ConnectionCanceledException(e);
            }
            if(DisconnectReason.UNKNOWN.equals(failure.getDisconnectReason())) {
                // Too many authentication failures
                return new LoginFailureException(e.getMessage(), e);
            }
        }
        return this.wrap(e, buffer);
    }
}
