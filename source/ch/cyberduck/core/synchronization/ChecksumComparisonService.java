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

import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathAttributes;
import ch.cyberduck.core.exception.BackgroundException;

import org.apache.log4j.Logger;

/**
 * @version $Id$
 */
public class ChecksumComparisonService implements ComparisonService {
    private static final Logger log = Logger.getLogger(ComparisonService.class);

    @Override
    public Comparison compare(final Path p) throws BackgroundException {
        if(log.isDebugEnabled()) {
            log.debug(String.format("Compare checksum for %s", p.getAbsolute()));
        }
        final PathAttributes attributes = p.attributes();
        if(attributes.isFile()) {
            if(null == attributes.getChecksum()) {
                log.warn("No checksum available for comparison:" + p);
                return Comparison.notequal;
            }
            //fist make sure both files are larger than 0 bytes
            if(attributes.getChecksum().equals(p.getLocal().attributes().getChecksum())) {
                return Comparison.equal;
            }
        }
        //different sum - further comparison check
        return Comparison.notequal;
    }
}
