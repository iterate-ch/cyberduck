package ch.cyberduck.core.idisk;

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

import com.apple.cocoa.foundation.NSDictionary;

import ch.cyberduck.core.*;
import ch.cyberduck.core.davs.DAVSPath;

/**
 * @version $Id:$
 */
public class IDISKPath extends DAVSPath {

    static {
        PathFactory.addFactory(Protocol.IDISK, new Factory());
    }

    private static class Factory extends PathFactory {
        protected Path create(Session session, String path, int type) {
            return new IDISKPath((IDISKSession) session, path, type);
        }

        protected Path create(Session session, String parent, String name, int type) {
            return new IDISKPath((IDISKSession) session, parent, name, type);
        }

        protected Path create(Session session, String path, Local file) {
            return new IDISKPath((IDISKSession) session, path, file);
        }

        protected Path create(Session session, NSDictionary dict) {
            return new IDISKPath((IDISKSession) session, dict);
        }
    }

    protected IDISKPath(IDISKSession s, String parent, String name, int type) {
        super(s, parent, name, type);
    }

    protected IDISKPath(IDISKSession s, String path, int type) {
        super(s, path, type);
    }

    protected IDISKPath(IDISKSession s, String parent, Local file) {
        super(s, parent, file);
    }

    protected IDISKPath(IDISKSession s, NSDictionary dict) {
        super(s, dict);
    }
}
