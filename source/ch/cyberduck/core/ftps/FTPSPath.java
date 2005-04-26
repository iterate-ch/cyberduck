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

import com.apple.cocoa.foundation.NSDictionary;

import java.util.List;
import java.io.IOException;

import ch.cyberduck.core.*;

/**
 * @version $Id$
 */
public class FTPSPath extends Path {
    public void reset() {
    }

    public List list(String encoding, boolean refresh, boolean showHidden, boolean notifyObservers) {
        return null;
    }

    public void delete() {
    }

    public void cwdir() throws IOException {
        throw new IOException("FTP-TLS not supported in this version. " +
                "Upgrade to Cyberduck 2.5 or later.");
    }

    public void mkdir(boolean recursive) {
    }

    public void rename(String newFilename) {
    }

    public void changePermissions(Permission perm, boolean recursive) {
    }

    public Session getSession() {
        return null;
    }

    public void download() {
    }

    public void upload() {
    }

    static {
        PathFactory.addFactory(Session.FTP_TLS, new Factory());
    }

    private static class Factory extends PathFactory {
        protected Path create(Session session, String parent, String name) {
            return new FTPSPath((FTPSSession) session, parent, name);
        }

        protected Path create(Session session) {
            return new FTPSPath((FTPSSession) session);
        }

        protected Path create(Session session, String path) {
            return new FTPSPath((FTPSSession) session, path);
        }

        protected Path create(Session session, String path, Local file) {
            return new FTPSPath((FTPSSession) session, path, file);
        }

        protected Path create(Session session, NSDictionary dict) {
            return new FTPSPath((FTPSSession) session, dict);
        }
    }

    protected FTPSPath(FTPSSession s, String parent, String name) {
        super(parent, name);
    }

    protected FTPSPath(FTPSSession s, String path) {
        super(path);
    }

    protected FTPSPath(FTPSSession s) {
        super();
    }

    protected FTPSPath(FTPSSession s, String parent, Local file) {
        super(parent, file);
    }

    protected FTPSPath(FTPSSession s, NSDictionary dict) {
        super(dict);
    }
}