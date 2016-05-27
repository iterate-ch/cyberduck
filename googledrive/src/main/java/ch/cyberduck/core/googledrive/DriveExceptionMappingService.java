package ch.cyberduck.core.googledrive;

/*
 * Copyright (c) 2002-2016 iterate GmbH. All rights reserved.
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

import ch.cyberduck.core.DefaultIOExceptionMappingService;
import ch.cyberduck.core.exception.AccessDeniedException;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.exception.LoginFailureException;
import ch.cyberduck.core.exception.NotfoundException;

import java.io.IOException;

import com.google.api.client.auth.oauth2.TokenErrorResponse;
import com.google.api.client.auth.oauth2.TokenResponseException;
import com.google.api.client.googleapis.json.GoogleJsonResponseException;
import com.google.api.client.http.HttpResponseException;

public class DriveExceptionMappingService extends DefaultIOExceptionMappingService {

    @Override
    public BackgroundException map(final IOException failure) {
        if(failure instanceof TokenResponseException) {
            final TokenErrorResponse details = ((TokenResponseException) failure).getDetails();
            final StringBuilder buffer = new StringBuilder();
            this.append(buffer, details.getError());
            return new LoginFailureException(buffer.toString(), failure);
        }
        final StringBuilder buffer = new StringBuilder();
        if(failure instanceof GoogleJsonResponseException) {
            final GoogleJsonResponseException error = (GoogleJsonResponseException) failure;
            this.append(buffer, error.getDetails().getMessage());
        }
        if(failure instanceof HttpResponseException) {
            final HttpResponseException response = (HttpResponseException) failure;
            this.append(buffer, response.getStatusMessage());
            if(response.getStatusCode() == 401) {
                // Invalid Credentials. Refresh the access token using the long-lived refresh token
                return new LoginFailureException(buffer.toString(), failure);
            }
            if(response.getStatusCode() == 403) {
                return new AccessDeniedException(buffer.toString(), failure);
            }
            if(response.getStatusCode() == 404) {
                return new NotfoundException(buffer.toString(), failure);
            }
        }
        return super.map(failure);
    }
}