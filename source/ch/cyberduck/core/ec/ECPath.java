package ch.cyberduck.core.ec;

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

import ch.cyberduck.core.Local;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathFactory;
import ch.cyberduck.core.Protocol;
import ch.cyberduck.core.s3.S3Path;

/**
 * @version $Id$
 */
public class ECPath extends S3Path {

    static {
        PathFactory.addFactory(Protocol.EUCALYPTUS, new Factory());
    }

    private static class Factory extends PathFactory<ECSession> {
        @Override
        protected Path create(ECSession session, String path, int type) {
            return new ECPath(session, path, type);
        }

        @Override
        protected Path create(ECSession session, String parent, String name, int type) {
            return new ECPath(session, parent, name, type);
        }

        @Override
        protected Path create(ECSession session, Path path, Local file) {
            return new ECPath(session, path, file);
        }

        @Override
        protected <T> Path create(ECSession session, T dict) {
            return new ECPath(session, dict);
        }
    }

    protected ECPath(ECSession s, String parent, String name, int type) {
        super(s, parent, name, type);
    }

    protected ECPath(ECSession s, String path, int type) {
        super(s, path, type);
    }

    protected ECPath(ECSession s, Path parent, Local file) {
        super(s, parent, file);
    }

    protected <T> ECPath(ECSession s, T dict) {
        super(s, dict);
    }
}