package ch.cyberduck.core.googlestorage;

/*
 * Copyright (c) 2002-2019 iterate GmbH. All rights reserved.
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

import ch.cyberduck.core.DefaultIOExceptionMappingService;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.exception.RetriableAccessDeniedException;
import ch.cyberduck.core.http.DefaultHttpResponseExceptionMappingService;

import org.apache.http.HttpStatus;

import java.io.IOException;
import java.time.Duration;
import java.util.List;
import java.util.Optional;

import com.google.api.client.googleapis.json.GoogleJsonError;
import com.google.api.client.googleapis.json.GoogleJsonResponseException;
import com.google.api.client.http.HttpResponseException;

public class GoogleStorageExceptionMappingService extends DefaultIOExceptionMappingService {

    @Override
    public BackgroundException map(final IOException failure) {
        final StringBuilder buffer = new StringBuilder();
        if(failure instanceof GoogleJsonResponseException) {
            final GoogleJsonResponseException error = (GoogleJsonResponseException) failure;
            final GoogleJsonError details = error.getDetails();
            if (details != null) {
                this.append(buffer, error.getDetails().getMessage());
                final Optional<GoogleJsonError.ErrorInfo> optionalInfo = error.getDetails().getErrors().stream().findFirst();
                if (optionalInfo.isPresent()) {
                    final GoogleJsonError.ErrorInfo info = optionalInfo.get();
                    this.append(buffer, "domain: " + info.getDomain());
                    this.append(buffer, "reason: " + info.getReason());
                    if("usageLimits".equals(info.getDomain()) && error.getDetails().getCode() == HttpStatus.SC_FORBIDDEN) {
                        return new RetriableAccessDeniedException(buffer.toString(), Duration.ofSeconds(5), failure);
                    }
                }
            }
        }
        if(failure instanceof HttpResponseException) {
            final HttpResponseException response = (HttpResponseException) failure;
            this.append(buffer, response.getStatusMessage());
            return new DefaultHttpResponseExceptionMappingService().map(new org.apache.http.client
                .HttpResponseException(response.getStatusCode(), buffer.toString()));
        }
        return super.map(failure);
    }
}
