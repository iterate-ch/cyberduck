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
    private static Map<Protocol, PathFactory> factories = new HashMap<Protocol, PathFactory>();

    protected abstract Path create(S session, String path, int type);

    protected abstract Path create(S session, String parent, String name, int type);

    protected abstract Path create(S session, Path parent, Local file);

    protected abstract <T> Path create(S session, T dict);

    /**
     * Register new factory
     * @param protocol
     * @param f
     */
    public static void addFactory(Protocol protocol, PathFactory f) {
        factories.put(protocol, f);
    }

    /**
     * @param parent The parent directory
     * @param name   The pathname relative the the parent directory
     */
    public static Path createPath(Session session, String parent, String name, int type) {
        loadClass(session.getHost().getProtocol());
        return (factories.get(session.getHost().getProtocol())).create(session, parent, name, type);
    }

    /**
     * @param path The absolute pathname
     */
    public static Path createPath(Session session, String path, int type) {
        loadClass(session.getHost().getProtocol());
        return (factories.get(session.getHost().getProtocol())).create(session, path, type);
    }

    /**
     * @param parent The parent directory
     * @param file   The local counterpart of this path
     */
    public static Path createPath(Session session, Path parent, Local file) {
        loadClass(session.getHost().getProtocol());
        return (factories.get(session.getHost().getProtocol())).create(session, parent, file);
    }

    /**
     * @param dict Creates a path reading its properties from the dictionary
     */
    public static <T> Path createPath(Session session, T dict) {
        loadClass(session.getHost().getProtocol());
        return (factories.get(session.getHost().getProtocol())).create(session, dict);
    }

    private static void loadClass(Protocol protocol) {
        if(!factories.containsKey(protocol)) {
            try {
                // Load dynamically
                Class.forName("ch.cyberduck.core." + protocol.getIdentifier() + "."
                        + protocol.getIdentifier().toUpperCase() + "Path");
            }
            catch(ClassNotFoundException e) {
                throw new RuntimeException("No class for type: " + protocol);
            }
            // See if it was put in:
            if(!factories.containsKey(protocol)) {
                throw new RuntimeException("No class for type: " + protocol);
            }
        }
    }
}