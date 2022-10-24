package ch.cyberduck.core.features;

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
import ch.cyberduck.core.exception.BackgroundException;

import java.util.Set;

public interface CustomActions {

    /**
     * Run custom action
     *
     * @param type Type of action to run
     * @param file Selected file or folder
     */
    void run(Action type, Path file) throws BackgroundException;

    /**
     * List supported custom action types
     *
     * @param file Selected file or folder
     * @return Set of supported actions for file or folder
     */
    Set<Action> list(Path file);

    interface Action {
        /**
         * @return Enum identifier
         */
        String name();

        /**
         * @return Localized description
         */
        String getTitle();
    }
}
