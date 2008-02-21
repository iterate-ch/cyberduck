package ch.cyberduck.core.ftps;

/*
 *  Copyright (c) 2005 David Kocher. All rights reserved.
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

import ch.cyberduck.core.Local;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathFactory;
import ch.cyberduck.core.Session;
import ch.cyberduck.core.ftp.FTPPath;

import com.apple.cocoa.foundation.NSDictionary;

/**
 * @version $Id$
 */
public class FTPSPath extends FTPPath {

    static {
        PathFactory.addFactory(Session.FTP_TLS, new Factory());
    }

    private static class Factory extends PathFactory {
        protected Path create(Session session, String parent, String name, int type) {
            return new FTPSPath((FTPSSession) session, parent, name, type);
        }

        protected Path create(Session session, String path, int type) {
            return new FTPSPath((FTPSSession) session, path, type);
        }

        protected Path create(Session session, String path, Local file) {
            return new FTPSPath((FTPSSession) session, path, file);
        }

        protected Path create(Session session, NSDictionary dict) {
            return new FTPSPath((FTPSSession) session, dict);
        }
    }

    protected FTPSPath(FTPSSession s, String parent, String name, int type) {
        super(s, parent, name, type);
    }

    protected FTPSPath(FTPSSession s, String path, int type) {
        super(s, path, type);
    }

    protected FTPSPath(FTPSSession s, String parent, Local file) {
        super(s, parent, file);
    }

    protected FTPSPath(FTPSSession s, NSDictionary dict) {
        super(s, dict);
    }
}