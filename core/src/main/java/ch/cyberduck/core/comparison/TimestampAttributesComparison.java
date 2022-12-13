package ch.cyberduck.core.comparison;

/*
 * Copyright (c) 2012 David Kocher. All rights reserved.
 * http://cyberduck.ch/
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
 *
 * Bug fixes, suggestions and comments should be sent to:
 * dkocher@cyberduck.ch
 */

import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathAttributes;
import ch.cyberduck.core.features.AttributesComparison;
import ch.cyberduck.core.synchronization.Comparison;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class TimestampAttributesComparison implements AttributesComparison {
    private static final Logger log = LogManager.getLogger(TimestampAttributesComparison.class);

    @Override
    public Comparison compare(final Path.Type type, final PathAttributes local, final PathAttributes remote) {
        if(log.isDebugEnabled()) {
            log.debug(String.format("Compare timestamp for %s with %s", local, remote));
        }
        if(-1L != local.getModificationDate() && -1L != remote.getModificationDate()) {
            if(local.getModificationDate() < remote.getModificationDate()) {
                return Comparison.remote;
            }
            if(local.getModificationDate() > remote.getModificationDate()) {
                return Comparison.local;
            }
            return Comparison.equal;
        }
        return Comparison.unknown;
    }
}
