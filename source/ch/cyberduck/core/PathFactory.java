package ch.cyberduck.core;

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

import ch.cyberduck.core.cf.CFPath;
import ch.cyberduck.core.dav.DAVPath;
import ch.cyberduck.core.ftp.FTPPath;
import ch.cyberduck.core.gdocs.GDPath;
import ch.cyberduck.core.gstorage.GSPath;
import ch.cyberduck.core.s3.S3Path;
import ch.cyberduck.core.sftp.SFTPPath;

import org.apache.log4j.Logger;

import java.util.HashMap;
import java.util.Map;

public abstract class PathFactory<S extends Session> {
    private static Logger log = Logger.getLogger(PathFactory.class);

    /**
     * Registered factories
     */
    private static final Map<Protocol, PathFactory> factories = new HashMap<Protocol, PathFactory>() {
        @Override
        public PathFactory get(final Object key) {
            if(!(key instanceof Protocol)) {
                throw new FactoryException(String.format("No factory for key %s", key));
            }
            Protocol p = (Protocol) key;
            if(!factories.containsKey(p)) {
                if(log.isDebugEnabled()) {
                    log.debug(String.format("Register protocol %s", p.getIdentifier()));
                }
                switch(p.getType()) {
                    case ftp:
                        factories.put(p, FTPPath.factory());
                        break;
                    case sftp:
                        factories.put(p, SFTPPath.factory());
                        break;
                    case dav:
                        factories.put(p, DAVPath.factory());
                        break;
                    case s3:
                        factories.put(p, S3Path.factory());
                        break;
                    case googlestorage:
                        factories.put(p, GSPath.factory());
                        break;
                    case googledrive:
                        factories.put(p, GDPath.factory());
                        break;
                    case swift:
                        factories.put(p, CFPath.factory());
                        break;
                    default:
                        throw new FactoryException(String.format("No factory for protocol %s", p));
                }
            }
            return super.get(key);
        }
    };

    protected abstract Path create(S session, String path, int type);

    protected abstract Path create(S session, String parent, String name, int type);

    protected abstract Path create(S session, String parent, Local file);

    protected abstract <T> Path create(S session, T dict);

    public static <S extends Session> Path createPath(S session, String parent, String name, int type) {
        return factories.get(session.getHost().getProtocol()).create(session, parent, name, type);
    }

    public static <S extends Session> Path createPath(S session, String path, int type) {
        return factories.get(session.getHost().getProtocol()).create(session, path, type);
    }

    public static <S extends Session> Path createPath(S session, String parent, Local file) {
        return factories.get(session.getHost().getProtocol()).create(session, parent, file);
    }

    public static <S extends Session, T> Path createPath(S session, T dict) {
        return factories.get(session.getHost().getProtocol()).create(session, dict);
    }
}