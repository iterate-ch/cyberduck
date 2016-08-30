package ch.cyberduck.core.local;

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

import ch.cyberduck.core.LocaleFactory;
import ch.cyberduck.core.Path;

public abstract class AbstractFileDescriptor implements FileDescriptor {

    @Override
    public String getKind(final Path file) {
        if(file.isFile()) {
            return this.getKind(file.getName());
        }
        if(file.isDirectory()) {
            return LocaleFactory.localizedString("Folder");
        }
        return LocaleFactory.localizedString("Unknown");
    }

    @Override
    public String getKind(final String filename) {
        return LocaleFactory.localizedString("Unknown");
    }
}
