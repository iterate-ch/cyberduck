package ch.cyberduck.core.local;

/*
 * Copyright (c) 2002-2014 David Kocher. All rights reserved.
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

import ch.cyberduck.core.Local;
import ch.cyberduck.core.exception.LocalAccessDeniedException;
import ch.cyberduck.core.local.features.Trash;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class DefaultLocalTrashFeature implements Trash {

    @Override
    public void trash(Local file) throws LocalAccessDeniedException {
        try {
            Files.delete(Paths.get(file.getAbsolute()));
        }
        catch(IOException e) {
            throw new LocalAccessDeniedException(String.format("Failed to move %s to Trash", file.getName()), e);
        }
    }
}
