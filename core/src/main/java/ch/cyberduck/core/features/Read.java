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

import ch.cyberduck.core.Path;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.transfer.TransferStatus;

import java.io.InputStream;

/**
 * @version $Id$
 */
public interface Read {

    /**
     * @param status Transfer status
     * @return Stream to read from to download file
     */
    InputStream read(Path file, TransferStatus status) throws BackgroundException;

    /**
     * @param file File
     * @return True if read with offset is supported
     */
    boolean offset(final Path file) throws BackgroundException;
}
