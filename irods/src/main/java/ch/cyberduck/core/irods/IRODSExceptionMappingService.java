package ch.cyberduck.core.irods;

/*
 * Copyright (c) 2002-2025 iterate GmbH. All rights reserved.
 * https://cyberduck.io/
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
 */

import ch.cyberduck.core.AbstractExceptionMappingService;
import ch.cyberduck.core.exception.AccessDeniedException;
import ch.cyberduck.core.exception.BackgroundException;

import ch.cyberduck.core.exception.ConnectionRefusedException;
import ch.cyberduck.core.exception.LockedException;
import ch.cyberduck.core.exception.LoginFailureException;

import ch.cyberduck.core.exception.NotfoundException;
import ch.cyberduck.core.exception.QuotaException;
import ch.cyberduck.core.exception.SSLNegotiateException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.irods.irods4j.low_level.api.IRODSErrorCodes;
import org.irods.irods4j.low_level.api.IRODSException;

public class IRODSExceptionMappingService extends AbstractExceptionMappingService<IRODSException> {

    private static final Logger log = LogManager.getLogger(IRODSExceptionMappingService.class);

    @Override
    public BackgroundException map(final IRODSException e) {
        log.warn("Map failure {}", e.toString());
        final StringBuilder buffer = new StringBuilder();
        this.append(buffer, e.getMessage());

        switch(e.getErrorCode()) {
            case IRODSErrorCodes.AUTHENTICATION_ERROR:
            case IRODSErrorCodes.CAT_INVALID_AUTHENTICATION:
            case IRODSErrorCodes.CAT_PASSWORD_EXPIRED:
            case IRODSErrorCodes.CAT_INVALID_USER:
                return new LoginFailureException(buffer.toString(), e);

            case IRODSErrorCodes.CAT_NO_ACCESS_PERMISSION:
            case IRODSErrorCodes.CAT_INSUFFICIENT_PRIVILEGE_LEVEL:
            case IRODSErrorCodes.SYS_NOT_ALLOWED:
                return new AccessDeniedException(buffer.toString(), e);

            case IRODSErrorCodes.INTERMEDIATE_REPLICA_ACCESS:
            case IRODSErrorCodes.SYS_REPLICA_INACCESSIBLE:
                return new LockedException(buffer.toString(), e);

            case IRODSErrorCodes.SSL_CERT_ERROR:
            case IRODSErrorCodes.SSL_HANDSHAKE_ERROR:
            case IRODSErrorCodes.SSL_INIT_ERROR:
            case IRODSErrorCodes.SSL_SHUTDOWN_ERROR:
            case IRODSErrorCodes.SSL_NOT_BUILT_INTO_CLIENT:
            case IRODSErrorCodes.SSL_NOT_BUILT_INTO_SERVER:
                return new SSLNegotiateException(buffer.toString(), e);

            case IRODSErrorCodes.SYS_RESC_QUOTA_EXCEEDED:
                return new QuotaException(buffer.toString(), e);

            case IRODSErrorCodes.CAT_NO_ROWS_FOUND:
            case IRODSErrorCodes.CAT_NOT_A_DATAOBJ_AND_NOT_A_COLLECTION:
                return new NotfoundException(buffer.toString(), e);

            case IRODSErrorCodes.CONNECTION_REFUSED:
                return new ConnectionRefusedException(buffer.toString(), e);
        }

        return this.wrap(e, buffer);
    }
}
