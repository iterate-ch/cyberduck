package ch.cyberduck.core.googledrive;

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

import ch.cyberduck.core.LocaleFactory;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathAttributes;
import ch.cyberduck.core.PathNormalizer;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.shared.AbstractHomeFeature;

import java.util.EnumSet;

public class DriveHomeFinderService extends AbstractHomeFeature {

    public static final String ROOT_FOLDER_ID = "root";

    public static final Path MYDRIVE_FOLDER
        = new Path(PathNormalizer.normalize(LocaleFactory.localizedString("My Drive", "Google Drive")),
        EnumSet.of(Path.Type.directory, Path.Type.placeholder, Path.Type.volume), new PathAttributes().withFileId(ROOT_FOLDER_ID));

    public static final Path SHARED_FOLDER_NAME
        = new Path(PathNormalizer.normalize(LocaleFactory.localizedString("Shared with me", "Google Drive")),
        EnumSet.of(Path.Type.directory, Path.Type.placeholder, Path.Type.volume));

    public static final Path SHARED_DRIVES_NAME
        = new Path(PathNormalizer.normalize(LocaleFactory.localizedString("Shared Drives", "Google Drive")),
        EnumSet.of(Path.Type.directory, Path.Type.placeholder, Path.Type.volume));

    public static final Path TRASH_FOLDER
            = new Path(PathNormalizer.normalize(LocaleFactory.localizedString("Trash", "Google Drive")),
            EnumSet.of(Path.Type.directory, Path.Type.placeholder, Path.Type.volume));

    @Override
    public Path find() throws BackgroundException {
        return MYDRIVE_FOLDER;
    }
}
