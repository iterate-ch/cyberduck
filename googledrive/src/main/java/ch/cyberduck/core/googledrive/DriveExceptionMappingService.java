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
import ch.cyberduck.core.Path;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.exception.InteroperabilityException;
import ch.cyberduck.core.exception.RetriableAccessDeniedException;
import ch.cyberduck.core.http.DefaultHttpResponseExceptionMappingService;
import ch.cyberduck.core.preferences.PreferencesFactory;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.http.HttpStatus;
import org.apache.http.util.EntityUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Optional;

import com.google.api.client.googleapis.json.GoogleJsonError;
import com.google.api.client.googleapis.json.GoogleJsonErrorContainer;
import com.google.api.client.googleapis.json.GoogleJsonResponseException;
import com.google.api.client.http.HttpHeaders;
import com.google.api.client.http.HttpResponseException;
import com.google.api.client.json.gson.GsonFactory;

public class DriveExceptionMappingService extends DefaultIOExceptionMappingService {
    private static final Logger log = LogManager.getLogger(DriveExceptionMappingService.class);

    private final DriveFileIdProvider fileid;

    public DriveExceptionMappingService(final DriveFileIdProvider fileid) {
        this.fileid = fileid;
    }

    @Override
    public BackgroundException map(final String message, final IOException failure, final Path file) {
        final HttpResponseException response = toResponse(failure);
        if(null != response) {
            switch(response.getStatusCode()) {
                case HttpStatus.SC_NOT_FOUND:
                    fileid.cache(file, null);
            }
        }
        return super.map(message, failure, file);
    }

    @Override
    public BackgroundException map(final IOException failure) {
        log.warn("Map failure {}", failure.toString());
        final StringBuilder buffer = new StringBuilder();
        final HttpResponseException cause = toResponse(failure);
        if(null != cause) {
            final GoogleJsonError details = toDetails(cause);
            if(details != null) {
                this.append(buffer, details.getMessage());
                final Optional<GoogleJsonError.ErrorInfo> optionalInfo = details.getErrors().stream().findFirst();
                if(optionalInfo.isPresent()) {
                    final GoogleJsonError.ErrorInfo info = optionalInfo.get();
                    this.append(buffer, "domain: " + info.getDomain());
                    this.append(buffer, "reason: " + info.getReason());
                    if("propertyLengthLimitExceeded".equals(info.getReason())) {
                        // Properties limited to 124 bytes for key and value
                        return new InteroperabilityException(buffer.toString(), failure);
                    }
                    if("usageLimits".equals(info.getDomain()) && details.getCode() == HttpStatus.SC_FORBIDDEN) {
                        return new RetriableAccessDeniedException(buffer.toString(),
                            Duration.ofSeconds(PreferencesFactory.get().getInteger("connection.retry.delay")), failure);
                    }
                }
            }
        }
        if(null != cause) {
            this.append(buffer, cause.getStatusMessage());
            return new DefaultHttpResponseExceptionMappingService().map(new org.apache.http.client
                .HttpResponseException(cause.getStatusCode(), buffer.toString()));
        }
        return super.map(failure);
    }

    /**
     * @param failure Failure possibly wrapping error response such as by output stream
     * @return Error response from service found in cause chain or null
     */
    private static HttpResponseException toResponse(final IOException failure) {
        for(Throwable cause : ExceptionUtils.getThrowableList(failure)) {
            if(cause instanceof HttpResponseException) {
                return (HttpResponseException) cause;
            }
        }
        return null;
    }

    /**
     * @param failure Error response
     * @return Error details from response or parsed from JSON in response body. Null if not available
     */
    private static GoogleJsonError toDetails(final HttpResponseException failure) {
        if(failure instanceof GoogleJsonResponseException) {
            return ((GoogleJsonResponseException) failure).getDetails();
        }
        if(StringUtils.isBlank(failure.getContent())) {
            return null;
        }
        try {
            return GsonFactory.getDefaultInstance().fromString(failure.getContent(), GoogleJsonErrorContainer.class).getError();
        }
        catch(IOException | IllegalArgumentException e) {
            log.warn("Failure {} parsing error response {}", e, failure.getContent());
            return null;
        }
    }

    /**
     * @param response Error response
     * @return Failure with status and body of error response to map with JSON error details
     */
    public static HttpResponseException toFailure(final org.apache.http.HttpResponse response) throws IOException {
        return new HttpResponseException.Builder(response.getStatusLine().getStatusCode(),
                response.getStatusLine().getReasonPhrase(), new HttpHeaders())
                .setContent(null == response.getEntity() ? null : EntityUtils.toString(response.getEntity(), StandardCharsets.UTF_8))
                .build();
    }
}
