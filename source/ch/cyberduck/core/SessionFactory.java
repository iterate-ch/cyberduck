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

import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class SessionFactory {
    private static Logger log = Logger.getLogger(SessionFactory.class);

    private static Map<Protocol, SessionFactory> factories
            = new HashMap<Protocol, SessionFactory>();

    /**
     * Ordered list of supported protocols.
     */
    private static List<Protocol> protocols
            = new ArrayList<Protocol>();

    protected abstract Session create(Host h);

    public static void addFactory(Protocol protocol, SessionFactory f) {
        log.debug("Add factory for protocol " + protocol + ":" + f);
        protocols.add(protocol);
        factories.put(protocol, f);
    }

    /**
     * @param h
     * @return
     */
    public static Session createSession(Host h) {
        return factories.get(h.getProtocol()).create(h);
    }

    /**
     * @return Available protocols for the user to choose from.
     */
    public static List<Protocol> getRegisteredProtocols() {
        return protocols;
    }

}