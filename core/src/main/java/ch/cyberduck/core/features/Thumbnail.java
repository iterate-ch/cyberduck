package ch.cyberduck.core.features;

/*
 * Copyright (c) 2002-2026 iterate GmbH. All rights reserved.
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
import ch.cyberduck.core.exception.BackgroundException;

import java.io.InputStream;

/**
 * Retrieve thumbnail preview image generated on server for file
 */
@Optional
public interface Thumbnail {

    /**
     * Retrieve thumbnail image for file from server
     *
     * @param file File on server
     * @param size Requested dimension in pixels for the longest edge of the image. Implementations may return an image
     *             with different dimensions than requested
     * @return Stream to read encoded image data from. The image format is not specified but must be determined from
     * the content such as PNG or JPEG
     * @throws ch.cyberduck.core.exception.NotfoundException No thumbnail available for file
     * @throws BackgroundException                           Failure retrieving thumbnail from server
     */
    InputStream thumbnail(Path file, int size) throws BackgroundException;

    /**
     * @param file File on server
     * @return False when no thumbnail can be determined for file
     */
    default boolean isSupported(final Path file) {
        return file.isFile();
    }
}
