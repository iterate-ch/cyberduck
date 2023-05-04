package ch.cyberduck.core.smb;/*
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
import ch.cyberduck.core.exception.AccessDeniedException;
import ch.cyberduck.core.exception.BackgroundException;

import com.hierynomus.mssmb2.SMBApiException;
import com.hierynomus.smbj.common.SMBRuntimeException;

public class SmbExceptionMappingService extends AbstractExceptionMappingService<SMBRuntimeException> {
    @Override
    public BackgroundException map(final SMBRuntimeException exception) {
        if(exception instanceof SMBApiException) {
            switch(((SMBApiException) exception).getStatus()) {
                // TODO: map all errors
                case STATUS_ACCESS_DENIED:
                    return new AccessDeniedException(exception.getMessage(), exception.getCause());
                default:
                    return new BackgroundException(exception.getMessage(), exception.getCause());
            }
        } else {
            return new BackgroundException(exception.getMessage(), exception.getCause());
        }


    }
}
