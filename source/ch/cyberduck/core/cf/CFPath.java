package ch.cyberduck.core.cf;

/*
 * Copyright (c) 2002-2013 David Kocher. All rights reserved.
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

import ch.cyberduck.core.Path;
import ch.cyberduck.core.cloud.CloudPath;
import ch.cyberduck.core.local.Local;

/**
 * Rackspace Cloud Files Implementation
 *
 * @version $Id$
 */
public class CFPath extends CloudPath {

    public CFPath(Path parent, String name, int type) {
        super(parent, name, type);
    }

    public CFPath(String path, int type) {
        super(path, type);
    }

    public CFPath(Path parent, Local file) {
        super(parent, file);
    }

    public <T> CFPath(T dict) {
        super(dict);
    }
}
