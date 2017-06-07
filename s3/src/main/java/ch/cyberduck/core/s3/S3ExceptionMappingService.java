package ch.cyberduck.core.s3;

/*
 * Copyright (c) 2013 David Kocher. All rights reserved.
 * http://cyberduck.ch/
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
 * dkocher@cyberduck.ch
 */

import ch.cyberduck.core.AbstractExceptionMappingService;
import ch.cyberduck.core.DefaultIOExceptionMappingService;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.exception.ConnectionTimeoutException;
import ch.cyberduck.core.exception.InteroperabilityException;
import ch.cyberduck.core.exception.LoginFailureException;
import ch.cyberduck.core.http.HttpResponseExceptionMappingService;

import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpResponseException;
import org.jets3t.service.ServiceException;
import org.xml.sax.SAXException;

import java.io.IOException;

public class S3ExceptionMappingService extends AbstractExceptionMappingService<ServiceException> {

    @Override
    public BackgroundException map(final ServiceException e) {
        if(e.getCause() instanceof ServiceException) {
            return this.map((ServiceException) e.getCause());
        }
        final StringBuilder buffer = new StringBuilder();
        if(StringUtils.isNotBlank(e.getErrorMessage())) {
            // S3 protocol message parsed from XML
            this.append(buffer, StringEscapeUtils.unescapeXml(e.getErrorMessage()));
        }
        else {
            this.append(buffer, e.getResponseStatus());
            this.append(buffer, e.getMessage());
        }
        switch(e.getResponseCode()) {
            case HttpStatus.SC_FORBIDDEN:
                if(StringUtils.isNotBlank(e.getErrorCode())) {
                    switch(e.getErrorCode()) {
                        case "SignatureDoesNotMatch":
                        case "InvalidAccessKeyId":
                        case "InvalidClientTokenId":
                        case "InvalidSecurity":
                        case "MissingClientTokenId":
                        case "MissingAuthenticationToken":
                            return new LoginFailureException(buffer.toString(), e);
                    }
                }
            case HttpStatus.SC_BAD_REQUEST:
                if(StringUtils.isNotBlank(e.getErrorCode())) {
                    switch(e.getErrorCode()) {
                        case "RequestTimeout":
                            return new ConnectionTimeoutException(buffer.toString(), e);
                    }
                }
        }
        if(e.getCause() instanceof IOException) {
            return new DefaultIOExceptionMappingService().map((IOException) e.getCause());
        }
        if(e.getCause() instanceof SAXException) {
            return new InteroperabilityException(buffer.toString(), e);
        }
        return new HttpResponseExceptionMappingService().map(new HttpResponseException(e.getResponseCode(), buffer.toString()));
    }
}