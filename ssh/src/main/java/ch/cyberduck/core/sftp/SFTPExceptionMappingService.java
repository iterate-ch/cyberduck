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

import ch.cyberduck.core.AbstractExceptionMappingService;
import ch.cyberduck.core.exception.AccessDeniedException;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.exception.ConnectionCanceledException;
import ch.cyberduck.core.exception.ConnectionRefusedException;
import ch.cyberduck.core.exception.InteroperabilityException;
import ch.cyberduck.core.exception.LoginFailureException;
import ch.cyberduck.core.exception.NotfoundException;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.log4j.Logger;

import java.io.IOException;

import net.schmizz.sshj.common.Buffer;
import net.schmizz.sshj.common.DisconnectReason;
import net.schmizz.sshj.common.SSHException;
import net.schmizz.sshj.connection.ConnectionException;
import net.schmizz.sshj.sftp.Response;
import net.schmizz.sshj.sftp.SFTPException;
import net.schmizz.sshj.transport.TransportException;
import net.schmizz.sshj.userauth.UserAuthException;

public class SFTPExceptionMappingService extends AbstractExceptionMappingService<IOException> {
    private static final Logger log = Logger.getLogger(SFTPExceptionMappingService.class);

    @Override
    public BackgroundException map(final IOException e) {
        if(ExceptionUtils.getRootCause(e) instanceof SSHException) {
            return this.map((SSHException) ExceptionUtils.getRootCause(e));
        }
        final StringBuilder buffer = new StringBuilder();
        this.append(buffer, e.getMessage());
        if(ExceptionUtils.getRootCause(e) != null) {
            if(!StringUtils.equals(e.getMessage(), ExceptionUtils.getRootCause(e).getMessage())) {
                this.append(buffer, ExceptionUtils.getRootCause(e).getMessage());
            }
        }
        if(e instanceof SFTPException) {
            final SFTPException failure = (SFTPException) e;
            final Response.StatusCode code = failure.getStatusCode();
            switch(code) {
                case UNKNOWN:
                case BAD_MESSAGE:
                case FAILURE:
                case OP_UNSUPPORTED:
                    return new InteroperabilityException(buffer.toString(), e);
                case NO_SUCH_FILE:
                    return new NotfoundException(buffer.toString(), e);
                case PERMISSION_DENIED:
                    return new AccessDeniedException(buffer.toString(), e);
                case NO_CONNECTION:
                case CONNECITON_LOST:
                    return new ConnectionRefusedException(buffer.toString(), e);
            }
        }
        if(e instanceof UserAuthException) {
            return new LoginFailureException(buffer.toString(), e);
        }
        if(e instanceof ConnectionException) {
            return new ConnectionRefusedException(buffer.toString(), e);
        }
        if(e instanceof Buffer.BufferException) {
            return new InteroperabilityException(buffer.toString(), e);
        }
        if(e instanceof SSHException) {
            final SSHException failure = (SSHException) e;
            final DisconnectReason reason = failure.getDisconnectReason();
            return this.map(e, buffer, reason);
        }
        return this.wrap(e, buffer);
    }

    public BackgroundException map(final IOException e, final StringBuilder buffer, final DisconnectReason reason) {
        final String failure = buffer.toString();
        if(DisconnectReason.HOST_KEY_NOT_VERIFIABLE.equals(reason)) {
            log.warn(String.format("Failure verifying host key. %s", failure));
            // Host key dismissed by user
            return new ConnectionCanceledException(e);
        }
        if(DisconnectReason.PROTOCOL_ERROR.equals(reason)) {
            // Too many authentication failures
            return new InteroperabilityException(failure, e);
        }
        if(DisconnectReason.ILLEGAL_USER_NAME.equals(reason)) {
            return new LoginFailureException(failure, e);
        }
        if(DisconnectReason.NO_MORE_AUTH_METHODS_AVAILABLE.equals(reason)) {
            return new LoginFailureException(failure, e);
        }
        if(DisconnectReason.PROTOCOL_VERSION_NOT_SUPPORTED.equals(reason)) {
            return new InteroperabilityException(failure, e);
        }
        if(e instanceof TransportException) {
            return new ConnectionRefusedException(buffer.toString(), e);
        }
        return this.wrap(e, buffer);
    }
}
