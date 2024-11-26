package ch.cyberduck.core.dropbox;

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

import ch.cyberduck.core.DefaultPathContainerService;
import ch.cyberduck.core.Path;

import org.apache.commons.lang3.StringUtils;

public class DropboxPathContainerService extends DefaultPathContainerService {

    @Override
    public String getKey(final Path file) {
        return file.isRoot() ? StringUtils.EMPTY : file.getAbsolute();
    }
}
