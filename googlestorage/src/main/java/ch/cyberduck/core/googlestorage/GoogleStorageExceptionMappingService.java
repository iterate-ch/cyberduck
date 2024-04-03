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
import ch.cyberduck.core.preferences.PreferencesFactory;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.time.Duration;
import java.util.Optional;

import com.google.api.client.googleapis.json.GoogleJsonError;
import com.google.api.client.googleapis.json.GoogleJsonResponseException;
import com.google.api.client.http.HttpResponseException;
import com.google.api.client.json.JsonParser;
import com.google.api.client.json.JsonToken;
import com.google.api.client.json.gson.GsonFactory;

public class GoogleStorageExceptionMappingService extends DefaultIOExceptionMappingService {
    private static final Logger log = LogManager.getLogger(GoogleStorageExceptionMappingService.class);

    @Override
    public BackgroundException map(final IOException failure) {
        final StringBuilder buffer = new StringBuilder();
        if(failure instanceof GoogleJsonResponseException) {
            final GoogleJsonResponseException error = (GoogleJsonResponseException) failure;
            final GoogleJsonError details = error.getDetails();
            if(details != null) {
                this.append(buffer, error.getDetails().getMessage());
                final Optional<GoogleJsonError.ErrorInfo> optionalInfo = details.getErrors().stream().findFirst();
                if(optionalInfo.isPresent()) {
                    final GoogleJsonError.ErrorInfo info = optionalInfo.get();
                    this.append(buffer, "domain: " + info.getDomain());
                    this.append(buffer, "reason: " + info.getReason());
                    if("usageLimits".equals(info.getDomain()) && details.getCode() == HttpStatus.SC_FORBIDDEN) {
                        return new RetriableAccessDeniedException(buffer.toString(), Duration.ofSeconds(
                            PreferencesFactory.get().getInteger("connection.retry.delay")
                        ), failure);
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

    /**
     * Parse failure message from error response
     *
     * @param response Error response with JSON body
     * @return Error message parsed from error key
     */
    public static String parse(final HttpResponse response) {
        if(response.getEntity() != null) {
            try (JsonParser parser = new GsonFactory().createJsonParser(response.getEntity().getContent())) {
                JsonToken currentToken = parser.getCurrentToken();
                // token is null at start, so get next token
                if(currentToken == null) {
                    currentToken = parser.nextToken();
                }
                // check for empty content
                if(currentToken != null) {
                    // make sure there is an "error" key
                    parser.skipToKey("error");
                    // in some cases (i.e. oauth), "error" can be a string, in most cases it's a
                    // GoogleJsonError object
                    if(parser.getCurrentToken() == JsonToken.VALUE_STRING) {
                        return parser.getText();
                    }
                    else if(parser.getCurrentToken() == JsonToken.START_OBJECT) {
                        final StringBuilder details = new StringBuilder();
                        final GoogleJsonError error = parser.parseAndClose(GoogleJsonError.class);
                        for(GoogleJsonError.ErrorInfo info : error.getErrors()) {
                            details.append(info.getMessage());
                        }
                        return details.toString();
                    }
                }
                parser.close();
            }
            catch(IOException exception) {
                // it would be bad to throw an exception while throwing an exception
                log.warn(String.format("Ignore failure %s parsing error reply from %s", exception, response));
            }
        }
        return response.getStatusLine().getReasonPhrase();
    }
}
