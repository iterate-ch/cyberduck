package ch.cyberduck.core.signin;

/*
 * Copyright (c) 2002-2026 iterate GmbH. All rights reserved.
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

import ch.cyberduck.core.exception.AccessDeniedException;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.exception.ExpiredTokenException;
import ch.cyberduck.core.http.HttpResponseExceptionMappingService;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.client.HttpResponseException;

/**
 * Map failure from the token endpoint with the error code and message in the JSON response body
 * such as <code>{"error":"TOKEN_EXPIRED","message":"The refresh token has expired."}</code>
 */
public class AWSConsoleLoginExceptionMappingService extends HttpResponseExceptionMappingService<HttpResponseException> {

    @Override
    public BackgroundException map(final HttpResponseException failure) {
        if(failure instanceof ErrorResponseException) {
            final ErrorResponseException response = (ErrorResponseException) failure;
            final StringBuilder buffer = new StringBuilder();
            this.append(buffer, StringUtils.defaultIfBlank(response.getDetail(), response.getReasonPhrase()));
            switch(StringUtils.defaultString(response.getError())) {
                case "TOKEN_EXPIRED":
                case "AUTHCODE_EXPIRED":
                case "USER_CREDENTIALS_CHANGED":
                    // Continue with new authorization in browser
                    return new ExpiredTokenException(buffer.toString(), failure);
                case "INSUFFICIENT_PERMISSIONS":
                    return new AccessDeniedException(buffer.toString(), failure);
                default:
                    return this.map(failure, buffer, failure.getStatusCode());
            }
        }
        return super.map(failure);
    }

    /**
     * Error response with details from the JSON response body
     */
    public static final class ErrorResponseException extends HttpResponseException {
        private final String error;
        private final String detail;

        public ErrorResponseException(final int statusCode, final String reasonPhrase, final String error, final String detail) {
            super(statusCode, reasonPhrase);
            this.error = error;
            this.detail = detail;
        }

        public String getError() {
            return error;
        }

        public String getDetail() {
            return detail;
        }
    }
}
