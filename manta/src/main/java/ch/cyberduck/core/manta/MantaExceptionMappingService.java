package ch.cyberduck.core.manta;

/*
 * Copyright (c) 2002-2017 iterate GmbH. All rights reserved.
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
import ch.cyberduck.core.DefaultIOExceptionMappingService;
import ch.cyberduck.core.LocaleFactory;
import ch.cyberduck.core.exception.AccessDeniedException;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.exception.InteroperabilityException;
import ch.cyberduck.core.exception.LoginFailureException;
import ch.cyberduck.core.exception.NotfoundException;
import ch.cyberduck.core.http.HttpResponseExceptionMappingService;

import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.http.client.HttpResponseException;

import java.io.IOException;
import java.security.KeyException;

import com.joyent.manta.exception.MantaClientHttpResponseException;
import com.joyent.manta.exception.MantaIOException;

public class MantaExceptionMappingService extends AbstractExceptionMappingService<Exception> {

    private final MantaSession session;

    public MantaExceptionMappingService(final MantaSession session) {
        this.session = session;
    }

    public MantaExceptionMappingService() {
        this.session = null;
    }

    @Override
    public BackgroundException map(final Exception failure) {
        // TODO: more fine-grained mapping

        if(failure instanceof KeyException) {
            return new LoginFailureException("Login failed", failure);
        }
        if(failure instanceof MantaClientHttpResponseException) {
            final MantaClientHttpResponseException httpFailure = (MantaClientHttpResponseException) failure;
            return new HttpResponseExceptionMappingService().map(
                    new HttpResponseException(httpFailure.getStatusCode(), httpFailure.getStatusMessage()));
        }
        if(failure instanceof MantaIOException) {
            return new DefaultIOExceptionMappingService().map((IOException) ExceptionUtils.getRootCause(failure));
        }
        return new InteroperabilityException(failure.getMessage(), failure);
    }

    BackgroundException mapLoginException(final Exception failure) {
        if(!(failure instanceof MantaClientHttpResponseException)) {
            return map(failure);
        }

        final MantaClientHttpResponseException httpFailure = (MantaClientHttpResponseException) failure;

        switch(httpFailure.getStatusCode()) {
            case 403:
                String msg = LocaleFactory.localizedString("Login failed", "Credentials");
                if(session != null && !session.userIsOwner()) {
                    msg += ". Subusers may need to explicitly set Path";
                }

                return new LoginFailureException(msg, httpFailure);
            default:
                return new LoginFailureException("Unexpected error occurred while logging in", httpFailure);
        }
    }
}
