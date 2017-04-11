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
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.exception.InteroperabilityException;
import ch.cyberduck.core.http.HttpResponseExceptionMappingService;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.http.client.HttpResponseException;
import org.nuxeo.onedrive.client.OneDriveAPIException;

import java.io.IOException;

public class OneDriveExceptionMappingService extends AbstractExceptionMappingService<OneDriveAPIException> {

    @Override
    public BackgroundException map(final OneDriveAPIException failure) {
        if(failure.getResponseCode() > 0) {
            final StringBuilder buffer = new StringBuilder();
            buffer.append(failure.getMessage());
            buffer.append(failure.getErrorMessage());
            return new HttpResponseExceptionMappingService().map(new HttpResponseException(failure.getResponseCode(), buffer.toString()));
        }
        if(ExceptionUtils.getRootCause(failure) instanceof IOException) {
            return new DefaultIOExceptionMappingService().map((IOException) ExceptionUtils.getRootCause(failure));
        }
        return new InteroperabilityException(failure.getMessage(), failure);
    }
}
