package ch.cyberduck.core.local.features;

/*
 * Copyright (c) 2002-2015 David Kocher. All rights reserved.
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
import ch.cyberduck.core.LocaleFactory;
import ch.cyberduck.core.exception.AccessDeniedException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class DefaultSymlinkFeature implements Symlink {

    @Override
    public void symlink(final Local file, final String target) throws AccessDeniedException {
        try {
            Files.createSymbolicLink(Paths.get(file.getAbsolute()), Paths.get(target));
        }
        catch(IOException | UnsupportedOperationException e) {
            throw new AccessDeniedException(String.format("%s %s",
                    LocaleFactory.localizedString("Cannot create file", "Error"), file.getAbsolute()), e);
        }
    }
}
