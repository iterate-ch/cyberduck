/*
 * Copyright (c) 2015-2016 Spectra Logic Corporation. All rights reserved.
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

package ch.cyberduck.core.spectra;

import ch.cyberduck.core.AbstractExceptionMappingService;
import ch.cyberduck.core.DefaultIOExceptionMappingService;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.exception.LoginFailureException;
import ch.cyberduck.core.http.HttpResponseExceptionMappingService;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpResponseException;

import java.io.IOException;

import com.spectralogic.ds3client.networking.FailedRequestException;

public class SpectraExceptionMappingService extends AbstractExceptionMappingService<FailedRequestException> {

    @Override
    public BackgroundException map(final FailedRequestException e) {
        final StringBuilder buffer = new StringBuilder();
        this.append(buffer, e.getError().getMessage());
        switch(e.getStatusCode()) {
            case HttpStatus.SC_FORBIDDEN:
                if(StringUtils.isNotBlank(e.getError().getCode())) {
                    switch(e.getError().getCode()) {
                        case "SignatureDoesNotMatch":
                            return new LoginFailureException(buffer.toString(), e);
                        case "InvalidAccessKeyId":
                            return new LoginFailureException(buffer.toString(), e);
                        case "InvalidClientTokenId":
                            return new LoginFailureException(buffer.toString(), e);
                        case "InvalidSecurity":
                            return new LoginFailureException(buffer.toString(), e);
                        case "MissingClientTokenId":
                            return new LoginFailureException(buffer.toString(), e);
                        case "MissingAuthenticationToken":
                            return new LoginFailureException(buffer.toString(), e);
                    }
                }
        }
        if(e.getCause() instanceof IOException) {
            return new DefaultIOExceptionMappingService().map((IOException) e.getCause());
        }
        return new HttpResponseExceptionMappingService().map(new HttpResponseException(e.getStatusCode(), buffer.toString()));
    }
}
