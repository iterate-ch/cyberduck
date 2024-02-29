package ch.cyberduck.core.onedrive;

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
import ch.cyberduck.core.exception.NotfoundException;
import ch.cyberduck.core.exception.RetriableAccessDeniedException;
import ch.cyberduck.core.http.DefaultHttpResponseExceptionMappingService;
import ch.cyberduck.core.onedrive.features.GraphFileIdProvider;
import ch.cyberduck.core.preferences.PreferencesFactory;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpResponseException;
import org.nuxeo.onedrive.client.OneDriveAPIException;

import java.io.IOException;
import java.time.Duration;

public class GraphExceptionMappingService extends AbstractExceptionMappingService<OneDriveAPIException> {

    private final GraphFileIdProvider fileid;

    public GraphExceptionMappingService(final GraphFileIdProvider fileid) {
        this.fileid = fileid;
    }

    @Override
    public BackgroundException map(final String message, final OneDriveAPIException failure, final Path file) {
        if(failure.getResponseCode() > 0) {
            switch(failure.getResponseCode()) {
                case HttpStatus.SC_NOT_FOUND:
                    fileid.cache(file, null);
            }
        }
        return super.map(message, failure, file);
    }

    @Override
    public BackgroundException map(final OneDriveAPIException failure) {
        if(failure.getResponseCode() > 0) {
            final StringAppender buffer = new StringAppender();
            buffer.append(failure.getMessage());
            buffer.append(failure.getErrorMessage());
            switch(failure.getResponseCode()) {
                case HttpStatus.SC_TOO_MANY_REQUESTS:
                    // Rate limit
                    if(failure.getRetry() != null) {
                        return new RetriableAccessDeniedException(buffer.toString(), Duration.ofSeconds(failure.getRetry()));
                    }
                    return new RetriableAccessDeniedException(buffer.toString(), Duration.ofSeconds(PreferencesFactory.get().getInteger("connection.retry.delay")));
                case HttpStatus.SC_BAD_REQUEST:
                    if(StringUtils.equalsIgnoreCase("Must provide one of the following facets to create an item: Bundle, File, Folder, RemoteItem", failure.getErrorMessage())) {
                        return new NotfoundException(buffer.toString(), failure);
                    }
            }
            return new DefaultHttpResponseExceptionMappingService().map(new HttpResponseException(failure.getResponseCode(), buffer.toString()));
        }
        if(ExceptionUtils.getRootCause(failure) != failure && ExceptionUtils.getRootCause(failure) instanceof IOException) {
            return new DefaultIOExceptionMappingService().map((IOException) ExceptionUtils.getRootCause(failure));
        }
        return new InteroperabilityException(failure.getMessage(), failure);
    }
}
