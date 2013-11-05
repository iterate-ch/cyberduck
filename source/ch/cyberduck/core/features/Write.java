package ch.cyberduck.core.features;

/*
 * Copyright (c) 2013 David Kocher. All rights reserved.
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
 * feedback@cyberduck.ch
 */

import ch.cyberduck.core.Cache;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.transfer.TransferStatus;

import java.io.OutputStream;

/**
 * @version $Id$
 */
public interface Write {

    /**
     * @param status Transfer status
     * @return Stream to write to for upload
     */
    OutputStream write(Path file, TransferStatus status) throws BackgroundException;

    /**
     * Determine if a file exists and we can append to it.
     *
     * @param file   File
     * @param length Transfer Status
     * @param cache  Cache
     * @return True if can append to existing file
     */
    Append append(Path file, Long length, Cache cache) throws BackgroundException;

    public static final class Append {
        /**
         * Append to file
         */
        public boolean append;

        /**
         * Remote file size
         */
        public Long size = 0L;

        public Append() {
            //
        }

        public Append(final Long size) {
            this.append = true;
            this.size = size;
        }
    }
}
