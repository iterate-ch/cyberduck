package ch.cyberduck.core.sftp;

/*
 *  Copyright (c) 2007 David Kocher. All rights reserved.
 *  http://cyberduck.ch/
 *
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 2 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  Bug fixes, suggestions and comments should be sent to:
 *  dkocher@cyberduck.ch
 */

import ch.cyberduck.core.Path;
import ch.cyberduck.core.local.Local;

/**
 * @version $Id$
 */
public class SFTPPath extends Path {

    public SFTPPath(final Path parent, final String name, final int type) {
        super(parent, name, type);
    }

    public SFTPPath(final String path, final int type) {
        super(path, type);
    }

    public SFTPPath(final Path parent, final Local file) {
        super(parent, file);
    }

    public <T> SFTPPath(final T dict) {
        super(dict);
    }
}
