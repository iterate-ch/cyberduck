package ch.cyberduck.core.gs;

/*
 * Copyright (c) 2002-2010 David Kocher. All rights reserved.
 *
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

import ch.cyberduck.core.Local;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathFactory;
import ch.cyberduck.core.Protocol;
import ch.cyberduck.core.s3.S3Path;
import ch.cyberduck.core.s3.S3Session;

/**
 * @version $Id$
 */
public class GSPath extends S3Path {

    static {
        PathFactory.addFactory(Protocol.GOOGLESTORAGE_SSL, new Factory());
    }

    public static class Factory extends PathFactory<GSSession> {
        @Override
        protected Path create(GSSession session, String path, int type) {
            return new GSPath(session, path, type);
        }

        @Override
        protected Path create(GSSession session, String parent, String name, int type) {
            return new GSPath(session, parent, name, type);
        }

        @Override
        protected Path create(GSSession session, String path, Local file) {
            return new GSPath(session, path, file);
        }

        @Override
        protected <T> Path create(GSSession session, T dict) {
            return new GSPath(session, dict);
        }
    }

    protected GSPath(S3Session s, String parent, String name, int type) {
        super(s, parent, name, type);
    }

    protected GSPath(S3Session s, String path, int type) {
        super(s, path, type);
    }

    protected GSPath(S3Session s, String parent, Local file) {
        super(s, parent, file);
    }

    protected <T> GSPath(S3Session s, T dict) {
        super(s, dict);
    }

    @Override
    public String toHttpURL() {
        return "https://sandbox.google.com/storage" + this.getAbsolute();
    }
}
