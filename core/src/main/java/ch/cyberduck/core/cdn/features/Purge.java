package ch.cyberduck.core.cdn.features;

/*
 * Copyright (c) 2002-2013 David Kocher. All rights reserved.
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
 * Bug fixes, suggestions and comments should be sent to feedback@cyberduck.ch
 */

import ch.cyberduck.core.LoginCallback;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.cdn.Distribution;
import ch.cyberduck.core.exception.BackgroundException;

import java.util.List;

public interface Purge {

    /**
     * Purge objects from edge locations
     *
     * @param container Container
     * @param method    Distribution method
     * @param files     Selected files or containers to purge
     * @param prompt    Callback
     */
    void invalidate(Path container, Distribution.Method method, List<Path> files,
                    LoginCallback prompt) throws BackgroundException;
}
