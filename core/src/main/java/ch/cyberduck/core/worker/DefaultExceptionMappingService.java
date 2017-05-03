package ch.cyberduck.core.worker;

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
import ch.cyberduck.core.LocaleFactory;
import ch.cyberduck.core.exception.BackgroundException;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;

public class DefaultExceptionMappingService extends AbstractExceptionMappingService<Throwable> {

    @Override
    public BackgroundException map(final Throwable failure) {
        final StringBuilder buffer = new StringBuilder();
        if(failure instanceof RuntimeException) {
            this.append(buffer, "Unknown application error");
        }
        this.append(buffer, failure.getMessage());
        for(Throwable cause : ExceptionUtils.getThrowableList(failure)) {
            if(!StringUtils.contains(failure.getMessage(), cause.getMessage())) {
                this.append(buffer, cause.getMessage());
            }
        }
        return this.wrap(failure, LocaleFactory.localizedString("Error", "Error"), buffer);
    }
}
