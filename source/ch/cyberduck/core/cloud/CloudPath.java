package ch.cyberduck.core.cloud;

/*
 *  Copyright (c) 2008 David Kocher. All rights reserved.
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
import ch.cyberduck.core.PathRelativizer;
import ch.cyberduck.core.Session;
import ch.cyberduck.core.http.HttpPath;
import ch.cyberduck.core.local.Local;

/**
 * @version $Id$
 */
public abstract class CloudPath extends HttpPath {

    public <T> CloudPath(final Session session, T dict) {
        super(session, dict);
    }

    protected CloudPath(Path parent, String name, int type) {
        super(parent, name, type);
    }

    protected CloudPath(Session session, String path, int type) {
        super(session, path, type);
    }

    protected CloudPath(Path parent, final Local local) {
        super(parent, local);
    }

    @Override
    public boolean isContainer() {
        if(this.isRoot()) {
            return false;
        }
        return this.getParent().isRoot();
    }

    /**
     * @return Absolute path without the container name
     */
    @Override
    public String getKey() {
        if(this.isContainer()) {
            return null;
        }
        return PathRelativizer.relativize(this.getContainer().getAbsolute() + String.valueOf(Path.DELIMITER),
                this.getAbsolute());
    }
}