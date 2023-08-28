package ch.cyberduck.core.oauth;

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
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.exception.ExpiredTokenException;
import ch.cyberduck.core.exception.LoginFailureException;
import ch.cyberduck.core.http.DefaultHttpResponseExceptionMappingService;

import org.apache.http.client.HttpResponseException;

import com.google.api.client.auth.oauth2.TokenErrorResponse;
import com.google.api.client.auth.oauth2.TokenResponseException;

public class OAuthExceptionMappingService extends AbstractExceptionMappingService<TokenResponseException> {

    @Override
    public BackgroundException map(final TokenResponseException failure) {
        final StringBuilder buffer = new StringBuilder();
        final TokenErrorResponse details = failure.getDetails();
        if(null != details) {
            this.append(buffer, details.getErrorDescription());
            switch(details.getError()) {
                // Error code "invalid_request", "invalid_client", "invalid_grant", "unauthorized_client", "unsupported_grant_type", "invalid_scope"
                case "invalid_client":
                case "unauthorized_client":
                case "unsupported_grant_type":
                case "invalid_scope":
                    return new LoginFailureException(buffer.toString(), failure);
                case "invalid_grant":
                    return new ExpiredTokenException(buffer.toString(), failure);
            }
        }
        return new DefaultHttpResponseExceptionMappingService().map(new HttpResponseException(failure.getStatusCode(), buffer.toString()));
    }
}
