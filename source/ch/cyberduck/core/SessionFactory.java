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

public abstract class SessionFactory {

    private static Map<Protocol, SessionFactory> factories = new HashMap<Protocol, SessionFactory>();

    protected abstract Session create(Host h);

    public static void addFactory(Protocol protocol, SessionFactory f) {
        factories.put(protocol, f);
    }

    public static Session createSession(Host h) {
        final Protocol protocol = h.getProtocol();
        if (!factories.containsKey(protocol)) {
            try {
                // Load dynamically
                String identifier = org.apache.commons.lang.StringUtils.capitalize(protocol.getIdentifier());
                if(identifier.length() < 4) {
                    identifier = identifier.toUpperCase();
                }
                Class.forName("ch.cyberduck.core." + protocol.getIdentifier() + "." + identifier + "Session");
            }
            catch (ClassNotFoundException e) {
                throw new RuntimeException("No class for type: " + protocol);
            }
            // See if it was put in:
            if (!factories.containsKey(protocol)) {
                throw new RuntimeException("No class for type: " + protocol);
            }
        }
        return (factories.get(protocol)).create(h);
    }
}