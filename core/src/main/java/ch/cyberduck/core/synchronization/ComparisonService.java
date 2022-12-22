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

public interface ComparisonService {

    /**
     * @param type   File or folder
     * @param local  Latest cached attributes
     * @param remote Latest remote attributes
     * @return Comparison result
     */
    Comparison compare(Path.Type type, PathAttributes local, PathAttributes remote);

    ComparisonService disabled = new ComparisonService() {
        @Override
        public Comparison compare(final Path.Type type, final PathAttributes local, final PathAttributes remote) {
            return Comparison.unknown;
        }
    };
}
