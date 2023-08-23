package ch.cyberduck.core.smb;
/*
 * Copyright (c) 2002-2023 iterate GmbH. All rights reserved.
 * https://cyberduck.io/
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 */

import ch.cyberduck.core.AbstractExceptionMappingService;
import ch.cyberduck.core.LocaleFactory;
import ch.cyberduck.core.exception.AccessDeniedException;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.exception.ConflictException;
import ch.cyberduck.core.exception.ConnectionRefusedException;
import ch.cyberduck.core.exception.ConnectionTimeoutException;
import ch.cyberduck.core.exception.InteroperabilityException;
import ch.cyberduck.core.exception.LockedException;
import ch.cyberduck.core.exception.LoginFailureException;
import ch.cyberduck.core.exception.NotfoundException;
import ch.cyberduck.core.exception.QuotaException;
import ch.cyberduck.core.exception.UnsupportedException;

import org.apache.commons.lang3.exception.ExceptionUtils;

import com.hierynomus.mserref.NtStatus;
import com.hierynomus.mssmb2.SMBApiException;
import com.hierynomus.protocol.transport.TransportException;
import com.hierynomus.smbj.common.SMBRuntimeException;

public class SMBExceptionMappingService extends AbstractExceptionMappingService<SMBRuntimeException> {
    @Override
    public BackgroundException map(final SMBRuntimeException failure) {
        if(failure instanceof SMBApiException) {
            final StringBuilder buffer = new StringBuilder();
            final SMBApiException e = (SMBApiException) failure;
            // NTSTATUS
            final NtStatus status = e.getStatus();
            this.append(buffer, String.format("%s (0x%08x)",
                    LocaleFactory.localizedString(status.name(), "SMB"), e.getStatusCode()));
            switch(status) {
                case STATUS_BAD_NETWORK_NAME:
                case STATUS_NOT_FOUND:
                case STATUS_OBJECT_NAME_NOT_FOUND:
                case STATUS_OBJECT_PATH_NOT_FOUND:
                case STATUS_PATH_NOT_COVERED:
                    return new NotfoundException(buffer.toString(), failure.getCause());
                case STATUS_NOT_IMPLEMENTED:
                case STATUS_NOT_SUPPORTED:
                    return new UnsupportedException(buffer.toString(), failure.getCause());
                case STATUS_ACCESS_DENIED:
                    return new AccessDeniedException(buffer.toString(), failure.getCause());
                case STATUS_OBJECT_NAME_COLLISION:
                    return new ConflictException(buffer.toString(), failure.getCause());
                case STATUS_FILE_LOCK_CONFLICT:
                case STATUS_LOCK_NOT_GRANTED:
                case STATUS_SHARING_VIOLATION:
                    return new LockedException(buffer.toString(), failure.getCause());
                case STATUS_LOGON_FAILURE:
                case STATUS_PASSWORD_EXPIRED:
                case STATUS_ACCOUNT_DISABLED:
                case STATUS_LOGON_TYPE_NOT_GRANTED:
                    return new LoginFailureException(buffer.toString(), failure.getCause());
                case STATUS_DISK_FULL:
                    return new QuotaException(buffer.toString(), failure.getCause());
                case STATUS_IO_TIMEOUT:
                    return new ConnectionTimeoutException(buffer.toString(), failure.getCause());
                case STATUS_CONNECTION_DISCONNECTED:
                case STATUS_CONNECTION_RESET:
                    return new ConnectionRefusedException(buffer.toString(), failure.getCause());
                default:
                    return new InteroperabilityException(buffer.toString(), failure.getCause());
            }
        }
        for(Throwable cause : ExceptionUtils.getThrowableList(failure)) {
            if(cause instanceof TransportException) {
                final Throwable root = ExceptionUtils.getRootCause(failure);
                return new ConnectionRefusedException(root.getMessage(), cause);
            }
        }
        final Throwable root = ExceptionUtils.getRootCause(failure);
        return new BackgroundException(root.getMessage(), root);
    }
}
