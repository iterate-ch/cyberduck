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
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.exception.ConnectionRefusedException;
import ch.cyberduck.core.exception.UnsupportedException;
import ch.cyberduck.core.worker.DefaultExceptionMappingService;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.util.concurrent.ExecutionException;

import com.google.common.base.Throwables;
import com.hierynomus.mssmb.SMB1NotSupportedException;
import com.hierynomus.smbj.common.SMBRuntimeException;

public class SMBTransportExceptionMappingService extends AbstractExceptionMappingService<IOException> {
    private static final Logger log = LogManager.getLogger(SMBTransportExceptionMappingService.class);

    @Override
    public BackgroundException map(final IOException failure) {
        log.warn("Map failure {}", failure.toString());
        for(Throwable cause : ExceptionUtils.getThrowableList(failure)) {
            if(cause instanceof SMBRuntimeException) {
                return new SMBExceptionMappingService().map((SMBRuntimeException) cause);
            }
            if(cause instanceof ExecutionException) {
                return new DefaultExceptionMappingService().map(Throwables.getRootCause(cause));
            }
        }
        final StringBuilder buffer = new StringBuilder();
        this.append(buffer, failure.getMessage());
        if(failure instanceof SMB1NotSupportedException) {
            return new UnsupportedException(buffer.toString(), failure);
        }
        return new ConnectionRefusedException(buffer.toString(), failure);
    }
}
