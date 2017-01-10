package ch.cyberduck.core.synchronization;

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

import ch.cyberduck.core.LocalAttributes;
import ch.cyberduck.core.PathAttributes;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.io.Checksum;

import org.apache.log4j.Logger;

public class ChecksumComparisonService implements ComparisonService {
    private static final Logger log = Logger.getLogger(ChecksumComparisonService.class);

    @Override
    public Comparison compare(final PathAttributes remote, final LocalAttributes local) throws BackgroundException {
        if(Checksum.NONE == remote.getChecksum()) {
            log.warn(String.format("No remote checksum available for comparison %s", remote));
            return Comparison.notequal;
        }
        if(Checksum.NONE == local.getChecksum()) {
            log.warn(String.format("No local checksum available for comparison %s", local));
            return Comparison.notequal;
        }
        if(remote.getChecksum().equals(local.getChecksum())) {
            return Comparison.equal;
        }
        return Comparison.notequal;
    }
}
