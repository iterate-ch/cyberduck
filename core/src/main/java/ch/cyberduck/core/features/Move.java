package ch.cyberduck.core.features;

/*
 * Copyright (c) 2002-2016 iterate GmbH. All rights reserved.
 * https://cyberduck.io/
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
 */

import ch.cyberduck.core.ListService;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.exception.BackgroundException;

public interface Move {

    /**
     * @param callback Progress
     * @param file     Origin
     * @param renamed  Target
     * @param exists   True if the target file exists
     */
    void move(Path file, Path renamed, boolean exists, Delete.Callback callback) throws BackgroundException;

    boolean isSupported(Path source, final Path target);

    Move withDelete(Delete delete);

    Move withList(ListService list);
}
