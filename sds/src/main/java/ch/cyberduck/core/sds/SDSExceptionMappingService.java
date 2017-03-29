package ch.cyberduck.core.sds;

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
import ch.cyberduck.core.http.HttpResponseExceptionMappingService;
import ch.cyberduck.core.sds.io.swagger.client.ApiException;

public class SDSExceptionMappingService extends AbstractExceptionMappingService<ApiException> {

    @Override
    public BackgroundException map(final ApiException failure) {
        final StringBuilder buffer = new StringBuilder();
        this.append(buffer, failure.getMessage());
        return new HttpResponseExceptionMappingService().map(failure, buffer, failure.getCode());
    }
}
