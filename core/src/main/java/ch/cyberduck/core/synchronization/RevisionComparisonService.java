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

public class RevisionComparisonService implements ComparisonService {
    private static final Logger log = LogManager.getLogger(RevisionComparisonService.class.getName());

    public Comparison compare(final Path.Type type, final PathAttributes local, final PathAttributes remote) {
        if(null != local.getRevision() && null != remote.getRevision()) {
            if(local.getRevision().equals(remote.getRevision())) {
                // No conflict. Proceed with overwrite
                if(log.isDebugEnabled()) {
                    log.debug(String.format("Equal revision %s", remote.getRevision()));
                }
                return Comparison.equal;
            }
            if(log.isDebugEnabled()) {
                log.debug(String.format("Local revision %s not equal remote %s", local.getRevision(), remote.getRevision()));
            }
            return Comparison.notequal;
        }
        return Comparison.unknown;
    }

    @Override
    public int hashCode(final Path.Type type, final PathAttributes attr) {
        if(null == attr.getRevision()) {
            return 0;
        }
        return attr.getRevision().hashCode();
    }
}
