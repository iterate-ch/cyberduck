package ch.cyberduck.core.ctera;

/*
 * Copyright (c) 2002-2022 iterate GmbH. All rights reserved.
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

import ch.cyberduck.core.Path;
import ch.cyberduck.core.dav.DAVTouchFeature;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.exception.InvalidFilenameException;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class CteraTouchFeature extends DAVTouchFeature {
    private static final Logger log = LogManager.getLogger(CteraTouchFeature.class);

    public CteraTouchFeature(final CteraSession session) {
        super(session);
    }

    @Override
    public void preflight(final Path workdir, final String filename) throws BackgroundException {
        if(!validate(filename)) {
            throw new InvalidFilenameException();
        }
    }

    public static boolean validate(final String filename) {
        if(StringUtils.containsAny(filename, '\\', '<', '>', ':', '"', '|', '?', '*', '/')) {
            log.warn(String.format("Validation failed for target name %s", filename));
            return false;
        }
        return true;
    }
}
