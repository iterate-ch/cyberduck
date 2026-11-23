package ch.cyberduck.core.openstack;

/*
 * Copyright (c) 2002-2026 iterate GmbH. All rights reserved.
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

import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.exception.LoginFailureException;

import org.apache.http.HttpStatus;

import ch.iterate.openstack.swift.exception.GenericException;

/**
 * Map failures of authentication requests. A bad request response is returned for invalid credentials.
 */
public class SwiftAuthenticationExceptionMappingService extends SwiftExceptionMappingService {

    @Override
    public BackgroundException map(final GenericException e) {
        final BackgroundException failure = super.map(e);
        if(HttpStatus.SC_BAD_REQUEST == e.getHttpStatusCode()) {
            return new LoginFailureException(failure.getDetail(false), e);
        }
        return failure;
    }
}
