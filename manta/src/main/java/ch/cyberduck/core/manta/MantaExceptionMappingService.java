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
import ch.cyberduck.core.Path;
import ch.cyberduck.core.StringAppender;
import ch.cyberduck.core.exception.AccessDeniedException;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.exception.InteroperabilityException;
import ch.cyberduck.core.exception.LoginFailureException;
import ch.cyberduck.core.http.HttpResponseExceptionMappingService;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.http.client.HttpResponseException;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.security.KeyException;

import com.joyent.manta.exception.MantaClientException;
import com.joyent.manta.exception.MantaClientHttpResponseException;
import com.joyent.manta.exception.MantaException;
import com.joyent.manta.exception.MantaIOException;

public class MantaExceptionMappingService extends AbstractExceptionMappingService<Exception> {

    private static final Logger log = Logger.getLogger(MantaExceptionMappingService.class);
    private final MantaSession session;

    public MantaExceptionMappingService(final MantaSession session) {
        this.session = session;
    }

    public MantaExceptionMappingService() {
        this.session = null;
    }

    @Override
    public BackgroundException map(final Exception failure) {

        // StringWriter sw = new StringWriter();
        // PrintWriter pw = new PrintWriter(sw);
        // failure.printStackTrace(pw);
        // log.error(sw.toString());

        /**
         * TODO: more fine-grained mapping
         */

        if(failure instanceof KeyException) {
            return new LoginFailureException("Could not log in.", failure);
        }
        if(failure instanceof MantaClientHttpResponseException) {
            return map((MantaClientHttpResponseException) failure);
        }

        if(failure instanceof MantaIOException) {
            return new DefaultIOExceptionMappingService().map((IOException) ExceptionUtils.getRootCause(failure));
        }
        return new InteroperabilityException(failure.getMessage(), failure);
    }

    private BackgroundException map(final MantaClientHttpResponseException httpFailure) {
        switch(httpFailure.getStatusCode()) {
            case 403:
                return new AccessDeniedException(httpFailure.getStatusMessage());
            default:
                return new InteroperabilityException("Unexpected remote error", httpFailure);
        }
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
