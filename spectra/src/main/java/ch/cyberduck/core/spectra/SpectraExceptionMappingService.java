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
import ch.cyberduck.core.exception.AccessDeniedException;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.exception.InteroperabilityException;
import ch.cyberduck.core.exception.LoginFailureException;
import ch.cyberduck.core.exception.NotfoundException;
import ch.cyberduck.core.exception.QuotaException;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpStatus;

import java.io.IOException;

import com.spectralogic.ds3client.networking.FailedRequestException;

public class SpectraExceptionMappingService extends AbstractExceptionMappingService<FailedRequestException> {

    @Override
    public BackgroundException map(final FailedRequestException e) {
        final StringBuilder buffer = new StringBuilder();
        final int code = e.getStatusCode();
        this.append(buffer, e.getError().getMessage());
        if(HttpStatus.SC_NOT_FOUND == code) {
            return new NotfoundException(buffer.toString(), e);
        }
        if(HttpStatus.SC_CONFLICT == code) {
            return new AccessDeniedException(buffer.toString(), e);
        }
        if(HttpStatus.SC_FORBIDDEN == code) {
            if(StringUtils.isNotBlank(e.getError().getCode())) {
                if(e.getError().getCode().equals("SignatureDoesNotMatch")) {
                    return new LoginFailureException(buffer.toString(), e);
                }
                if(e.getError().getCode().equals("InvalidAccessKeyId")) {
                    return new LoginFailureException(buffer.toString(), e);
                }
                if(e.getError().getCode().equals("InvalidClientTokenId")) {
                    return new LoginFailureException(buffer.toString(), e);
                }
                if(e.getError().getCode().equals("InvalidSecurity")) {
                    return new LoginFailureException(buffer.toString(), e);
                }
                if(e.getError().getCode().equals("MissingClientTokenId")) {
                    return new LoginFailureException(buffer.toString(), e);
                }
                if(e.getError().getCode().equals("MissingAuthenticationToken")) {
                    return new LoginFailureException(buffer.toString(), e);
                }
            }
            return new AccessDeniedException(buffer.toString(), e);
        }
        if(HttpStatus.SC_UNAUTHORIZED == code) {
            // Actually never returned by S3 but always 403
            return new LoginFailureException(buffer.toString(), e);
        }
        if(HttpStatus.SC_BAD_REQUEST == code) {
            return new InteroperabilityException(buffer.toString(), e);
        }
        if(HttpStatus.SC_NOT_IMPLEMENTED == code) {
            return new InteroperabilityException(buffer.toString(), e);
        }
        if(HttpStatus.SC_SERVICE_UNAVAILABLE == code) {
            return new InteroperabilityException(buffer.toString(), e);
        }
        if(HttpStatus.SC_METHOD_NOT_ALLOWED == code) {
            return new InteroperabilityException(buffer.toString(), e);
        }
        if(HttpStatus.SC_PAYMENT_REQUIRED == code) {
            return new QuotaException(buffer.toString(), e);
        }
        if(e.getCause() instanceof IOException) {
            return new DefaultIOExceptionMappingService().map((IOException) e.getCause());
        }
        return this.wrap(e, buffer);
    }
}
