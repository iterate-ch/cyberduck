package ch.cyberduck.core.synchronization;

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
import ch.cyberduck.core.PathAttributes;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class TimestampComparisonService implements ComparisonService {
    private static final Logger log = LogManager.getLogger(TimestampComparisonService.class);

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
