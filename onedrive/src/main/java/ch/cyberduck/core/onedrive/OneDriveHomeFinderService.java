package ch.cyberduck.core.onedrive;

/*
 * Copyright (c) 2002-2020 iterate GmbH. All rights reserved.
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
import ch.cyberduck.core.shared.AbstractHomeFeature;

import java.util.EnumSet;

public class OneDriveHomeFinderService extends AbstractHomeFeature {

    public static final Path MYFILES_NAME = new Path("/My Files", EnumSet.of(Path.Type.volume, Path.Type.placeholder, Path.Type.directory));
    public static final Path SHARED_NAME = new Path("/Shared", EnumSet.of(Path.Type.volume, Path.Type.placeholder, Path.Type.directory));

    @Override
    public Path find() throws BackgroundException {
        return MYFILES_NAME;
    }
}
