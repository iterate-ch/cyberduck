package ch.cyberduck.core;

/*
 *  Copyright (c) 2009 David Kocher. All rights reserved.
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

import ch.cyberduck.ui.cocoa.model.Local;

/**
 * @version $Id:$
 */
public class LocalFactory {

    public static ch.cyberduck.core.Local createLocal(ch.cyberduck.core.Local parent, String name) {
        return new ch.cyberduck.ui.cocoa.model.Local(parent, name);
    }

    public static ch.cyberduck.core.Local createLocal(String parent, String name) {
        return new Local(parent, name);
    }

    public static ch.cyberduck.core.Local createLocal(String path) {
        return new Local(path);
    }

    public static ch.cyberduck.core.Local createLocal(java.io.File path) {
        return new Local(path);
    }
}
