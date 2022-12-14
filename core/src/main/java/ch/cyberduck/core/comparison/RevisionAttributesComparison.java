package ch.cyberduck.core.comparison;

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
import ch.cyberduck.core.features.AttributesComparison;
import ch.cyberduck.core.synchronization.Comparison;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class RevisionAttributesComparison implements AttributesComparison {
    private static final Logger log = LogManager.getLogger(RevisionAttributesComparison.class.getName());

    public Comparison compare(final Path.Type type, final PathAttributes local, final PathAttributes remote) {
        if(null != local.getRevision() && null != remote.getRevision()) {
            if(local.getRevision().equals(remote.getRevision())) {
                // No conflict. Proceed with overwrite
                if(log.isDebugEnabled()) {
                    log.debug(String.format("Equal revision %s", remote.getRevision()));
                }
                return Comparison.equal;
            }
            log.warn(String.format("Revision %s in cache differs from %s on server", remote.getRevision(), local.getRevision()));
            return Comparison.notequal;
        }
        return Comparison.unknown;
    }
}
