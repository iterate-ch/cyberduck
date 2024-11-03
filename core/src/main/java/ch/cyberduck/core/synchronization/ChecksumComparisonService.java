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
import ch.cyberduck.core.io.Checksum;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ChecksumComparisonService implements ComparisonService {
    private static final Logger log = LogManager.getLogger(ChecksumComparisonService.class);

    @Override
    public Comparison compare(final Path.Type type, final PathAttributes local, final PathAttributes remote) {
        if(Checksum.NONE == remote.getChecksum()) {
            log.warn("No remote checksum available for comparison {}", remote);
            return Comparison.unknown;
        }
        if(Checksum.NONE == local.getChecksum()) {
            log.warn("No local checksum available for comparison {}", local);
            return Comparison.unknown;
        }
        if(remote.getChecksum().equals(local.getChecksum())) {
            if(log.isDebugEnabled()) {
                log.debug("Equal checksum {}", remote.getChecksum());
            }
            return Comparison.equal;
        }
        if(log.isDebugEnabled()) {
            log.debug("Local checksum {} not equal remote {}", local.getChecksum(), remote.getChecksum());
        }
        return Comparison.notequal;
    }

    @Override
    public int hashCode(final Path.Type type, final PathAttributes attr) {
        if(Checksum.NONE == attr.getChecksum()) {
            return 0;
        }
        return attr.getChecksum().hashCode();
    }
}
