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
import ch.cyberduck.core.transfer.TransferStatus;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class SizeComparisonService implements ComparisonService {
    private static final Logger log = LogManager.getLogger(SizeComparisonService.class);

    @Override
    public Comparison compare(final Path.Type type, final PathAttributes local, final PathAttributes remote) {
        if(TransferStatus.UNKNOWN_LENGTH != local.getSize() && TransferStatus.UNKNOWN_LENGTH != remote.getSize()) {
            if(local.getSize() == remote.getSize()) {
                if(log.isDebugEnabled()) {
                    log.debug("Equal size {}", remote.getSize());
                }
                return Comparison.equal;
            }
            if(remote.getSize() == 0) {
                return Comparison.local;
            }
            if(local.getSize() == 0) {
                return Comparison.remote;
            }
            if(log.isDebugEnabled()) {
                log.debug("Local size {} not equal remote {}", local.getSize(), remote.getSize());
            }
            // Different file size
            return Comparison.notequal;
        }
        return Comparison.unknown;
    }

    @Override
    public int hashCode(final Path.Type type, final PathAttributes attr) {
        if(TransferStatus.UNKNOWN_LENGTH == attr.getSize()) {
            return 0;
        }
        return Long.valueOf(attr.getSize()).hashCode();
    }
}
