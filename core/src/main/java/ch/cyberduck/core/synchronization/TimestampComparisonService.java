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
import ch.cyberduck.core.features.Timestamp;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class TimestampComparisonService implements ComparisonService {
    private static final Logger log = LogManager.getLogger(TimestampComparisonService.class);

    @Override
    public Comparison compare(final Path.Type type, final PathAttributes local, final PathAttributes remote) {
        if(-1L != local.getModificationDate() && -1L != remote.getModificationDate()) {
            if(Timestamp.toSeconds(local.getModificationDate()) < Timestamp.toSeconds(remote.getModificationDate())) {
                if(log.isDebugEnabled()) {
                    log.debug(String.format("Local modification date %d is earlier than remote %d", local.getModificationDate(), remote.getModificationDate()));
                }
                return Comparison.remote;
            }
            if(Timestamp.toSeconds(local.getModificationDate()) > Timestamp.toSeconds(remote.getModificationDate())) {
                if(log.isDebugEnabled()) {
                    log.debug(String.format("Local modification date %d is newer than remote %d", local.getModificationDate(), remote.getModificationDate()));
                }
                return Comparison.local;
            }
            if(log.isDebugEnabled()) {
                log.debug(String.format("Equal modification date %s", remote.getModificationDate()));
            }
            return Comparison.equal;
        }
        return Comparison.unknown;
    }

    @Override
    public int hashCode(final Path.Type type, final PathAttributes attr) {
        if(-1L == attr.getModificationDate()) {
            return 0;
        }
        return Long.valueOf(attr.getModificationDate()).hashCode();
    }
}
