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
import ch.cyberduck.core.Path;
import ch.cyberduck.core.StringAppender;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.exception.InteroperabilityException;
import ch.cyberduck.core.http.HttpResponseExceptionMappingService;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.http.client.HttpResponseException;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;

import com.joyent.manta.exception.MantaClientException;
import com.joyent.manta.exception.MantaException;
import com.joyent.manta.exception.MantaIOException;

public class MantaExceptionMappingService extends AbstractExceptionMappingService<Exception> {

    private static final Logger log = Logger.getLogger(MantaExceptionMappingService.class);

    private static final boolean HTTP_FAILURE = false;
    private static final int RESPONSE_CODE = 0;

    @Override
    public BackgroundException map(final Exception failure) {

        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        failure.printStackTrace(pw);
        log.error(sw.toString());

        if (failure instanceof MantaClientException) {
            // do something with the client part
        }

        if(HTTP_FAILURE) {
            final StringAppender buffer = new StringAppender();
            buffer.append(failure.getMessage());
            return new HttpResponseExceptionMappingService().map(new HttpResponseException(RESPONSE_CODE, buffer.toString()));
        }
        if(failure instanceof MantaIOException) {
            return new DefaultIOExceptionMappingService().map((IOException) ExceptionUtils.getRootCause(failure));
        }
        return new InteroperabilityException(failure.getMessage(), failure);
    }
}
