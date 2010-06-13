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
import ch.cyberduck.core.Protocol;
import ch.cyberduck.core.ftp.FTPPath;

/**
 * @version $Id$
 */
public class FTPSPath extends FTPPath {

    static {
        PathFactory.addFactory(Protocol.FTP_TLS, new Factory());
    }

    private static class Factory extends PathFactory<FTPSSession> {
        @Override
        protected Path create(FTPSSession session, String parent, String name, int type) {
            return new FTPSPath(session, parent, name, type);
        }

        @Override
        protected Path create(FTPSSession session, String path, int type) {
            return new FTPSPath(session, path, type);
        }

        @Override
        protected Path create(FTPSSession session, Path path, Local file) {
            return new FTPSPath(session, path, file);
        }

        @Override
        protected <T> Path create(FTPSSession session, T dict) {
            return new FTPSPath(session, dict);
        }
    }

    protected FTPSPath(FTPSSession s, String parent, String name, int type) {
        super(s, parent, name, type);
    }

    protected FTPSPath(FTPSSession s, String path, int type) {
        super(s, path, type);
    }

    protected FTPSPath(FTPSSession s, Path parent, Local file) {
        super(s, parent, file);
    }

    protected <T> FTPSPath(FTPSSession s, T dict) {
        super(s, dict);
    }
}