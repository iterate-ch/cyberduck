package ch.cyberduck.core.s3;

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
import ch.cyberduck.core.s3h.S3HPath;
import ch.cyberduck.core.ssl.SSLSession;

import org.jets3t.service.model.S3Bucket;

import java.io.IOException;

/**
 * @version $Id$
 */
public class S3Path extends S3HPath {

    static {
        PathFactory.addFactory(Protocol.S3_SSL, new Factory());
    }

    private static class Factory extends PathFactory<S3Session> {
        @Override
        protected Path create(S3Session session, String path, int type) {
            return new S3Path(session, path, type);
        }

        @Override
        protected Path create(S3Session session, String parent, String name, int type) {
            return new S3Path(session, parent, name, type);
        }

        @Override
        protected Path create(S3Session session, String path, Local file) {
            return new S3Path(session, path, file);
        }

        @Override
        protected <T> Path create(S3Session session, T dict) {
            return new S3Path(session, dict);
        }
    }

    protected S3Path(S3Session s, String parent, String name, int type) {
        super(s, parent, name, type);
    }

    protected S3Path(S3Session s, String path, int type) {
        super(s, path, type);
    }

    protected S3Path(S3Session s, String parent, Local file) {
        super(s, parent, file);
    }

    protected <T> S3Path(S3Session s, T dict) {
        super(s, dict);
    }
}
