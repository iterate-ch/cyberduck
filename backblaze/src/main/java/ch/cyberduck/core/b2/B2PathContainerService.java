package ch.cyberduck.core.b2;

/*
 * Copyright (c) 2002-2017 iterate GmbH. All rights reserved.
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

import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathContainerService;

public class B2PathContainerService extends PathContainerService {

    protected static final String PLACEHOLDER = ".bzEmpty";

    @Override
    public String getKey(final Path file) {
        final String key = super.getKey(file);
        if(!file.isRoot() && !this.isContainer(file) && file.isDirectory()) {
            return key.concat(String.valueOf(Path.DELIMITER)).concat(PLACEHOLDER);
        }
        return key;
    }
}
