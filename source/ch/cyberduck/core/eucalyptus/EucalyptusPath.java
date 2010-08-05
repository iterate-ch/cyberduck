package ch.cyberduck.core.eucalyptus;

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
import ch.cyberduck.core.s3.S3Path;

/**
 * @version $Id$
 */
public class EucalyptusPath extends S3Path {

    private static class Factory extends PathFactory<EucalyptusSession> {
        @Override
        protected Path create(EucalyptusSession session, String path, int type) {
            return new EucalyptusPath(session, path, type);
        }

        @Override
        protected Path create(EucalyptusSession session, String parent, String name, int type) {
            return new EucalyptusPath(session, parent, name, type);
        }

        @Override
        protected Path create(EucalyptusSession session, String parent, Local file) {
            return new EucalyptusPath(session, parent, file);
        }

        @Override
        protected <T> Path create(EucalyptusSession session, T dict) {
            return new EucalyptusPath(session, dict);
        }
    }

    public static PathFactory factory() {
        return new Factory();
    }

    protected EucalyptusPath(EucalyptusSession s, String parent, String name, int type) {
        super(s, parent, name, type);
    }

    protected EucalyptusPath(EucalyptusSession s, String path, int type) {
        super(s, path, type);
    }

    protected EucalyptusPath(EucalyptusSession s, String parent, Local file) {
        super(s, parent, file);
    }

    protected <T> EucalyptusPath(EucalyptusSession s, T dict) {
        super(s, dict);
    }
}