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

import java.util.HashMap;
import java.util.Map;

public abstract class PathFactory<S extends Session> {

    /**
     * Registered factories
     */
    private static Map<Protocol, PathFactory> factories
            = new HashMap<Protocol, PathFactory>();

    protected abstract Path create(S session, String path, int type);

    protected abstract Path create(S session, String parent, String name, int type);

    protected abstract Path create(S session, Path parent, Local file);

    protected abstract <T> Path create(S session, T dict);

    /**
     * Register new factory
     *
     * @param protocol
     * @param f
     */
    public static void addFactory(Protocol protocol, PathFactory f) {
        factories.put(protocol, f);
    }

    /**
     * @param session
     * @param parent  The parent directory
     * @param name    The pathname relative the the parent directory
     * @param type
     * @return
     */
    public static Path createPath(Session session, String parent, String name, int type) {
        return (factories.get(session.getHost().getProtocol())).create(session, parent, name, type);
    }

    /**
     * @param session
     * @param path    The absolute pathname
     * @param type
     * @return
     */
    public static Path createPath(Session session, String path, int type) {
        return (factories.get(session.getHost().getProtocol())).create(session, path, type);
    }

    /**
     * @param session
     * @param parent  The parent directory
     * @param file    The local counterpart of this path
     * @return
     */
    public static Path createPath(Session session, Path parent, Local file) {
        return (factories.get(session.getHost().getProtocol())).create(session, parent, file);
    }

    /**
     * @param session
     * @param dict    Creates a path reading its properties from the dictionary
     * @param <T>
     * @return
     */
    public static <T> Path createPath(Session session, T dict) {
        return (factories.get(session.getHost().getProtocol())).create(session, dict);
    }
}