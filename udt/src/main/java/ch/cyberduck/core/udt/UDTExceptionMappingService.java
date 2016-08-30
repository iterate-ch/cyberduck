package ch.cyberduck.core.udt;

/*
 * Copyright (c) 2002-2014 David Kocher. All rights reserved.
 * http://cyberduck.io/
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
 * feedback@cyberduck.io
 */

import ch.cyberduck.core.AbstractExceptionMappingService;
import ch.cyberduck.core.exception.AccessDeniedException;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.exception.ConnectionRefusedException;
import ch.cyberduck.core.exception.ConnectionTimeoutException;
import ch.cyberduck.core.exception.InteroperabilityException;

import com.barchart.udt.ExceptionUDT;

public class UDTExceptionMappingService extends AbstractExceptionMappingService<ExceptionUDT> {

    @Override
    public BackgroundException map(final ExceptionUDT e) {
        final StringBuilder buffer = new StringBuilder();
        this.append(buffer, e.getError().getDescription());
        switch(e.getError()) {
            case NOSERVER:
            case ECONNSETUP:
            case ECONNFAIL:
            case ECONNLOST:
            case ECONNREJ:
                return new ConnectionRefusedException(buffer.toString(), e);
            case ETIMEOUT:
            case EINVSOCK:
                return new ConnectionTimeoutException(buffer.toString(), e);
            case EWRPERM:
                return new AccessDeniedException(buffer.toString(), e);
            case EINVPARAM:
                return new InteroperabilityException(buffer.toString(), e);
            case USER_DEFINED_MESSAGE:
                // Handle UDT Error : -4 : user defined message : UDT send time out [id: 0x3223fa70]
            case WRAPPER_UNKNOWN:
            case WRAPPER_UNIMPLEMENTED:
            case WRAPPER_MESSAGE:
                return new InteroperabilityException(buffer.toString(), e);

        }
        return this.wrap(e, buffer);
    }
}
