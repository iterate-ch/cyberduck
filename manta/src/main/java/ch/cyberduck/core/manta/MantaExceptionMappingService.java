package ch.cyberduck.core.manta;

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
import ch.cyberduck.core.exception.InteroperabilityException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.joyent.manta.exception.MantaException;

public class MantaExceptionMappingService extends AbstractExceptionMappingService<MantaException> {
    private static final Logger log = LogManager.getLogger(MantaExceptionMappingService.class);

    @Override
    public BackgroundException map(final MantaException failure) {
        log.warn("Map failure {}", failure.toString());
        final StringBuilder buffer = new StringBuilder();
        this.append(buffer, failure.getMessage());
        return new InteroperabilityException(buffer.toString(), failure);
    }
}
