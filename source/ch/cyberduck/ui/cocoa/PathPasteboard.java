package ch.cyberduck.ui.cocoa;

/*
 * Copyright (c) 2002-2009 David Kocher. All rights reserved.
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

import ch.cyberduck.core.*;
import ch.cyberduck.ui.cocoa.foundation.NSDictionary;

import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @version $Id$
 */
public class PathPasteboard extends Collection<NSDictionary> implements Pasteboard<NSDictionary> {
    private static Logger log = Logger.getLogger(PathPasteboard.class);

    private static Map<Host, PathPasteboard> instances = new HashMap<Host, PathPasteboard>() {
        @Override
        public void clear() {
            for(PathPasteboard board : instances.values()) {
                board.clear();
            }
            super.clear();
        }

        @Override
        public boolean isEmpty() {
            for(PathPasteboard pasteboard : this.values()) {
                if(!pasteboard.isEmpty()) {
                    return false;
                }
            }
            return true;
        }
    };

    /**
     * @param host
     * @return Pasteboard for a given host
     */
    public static PathPasteboard getPasteboard(Host host) {
        if(!instances.containsKey(host)) {
            instances.put(host, new PathPasteboard(host));
        }
        return instances.get(host);
    }

    /**
     * @return
     */
    public static Map<Host, PathPasteboard> allPasteboards() {
        return instances;
    }

    private Host host;

    private PathPasteboard(Host host) {
        this.host = host;
    }

    /**
     * @return
     */
    public List<Path> getFiles() {
        return this.getFiles(SessionFactory.createSession(host));
    }

    /**
     * @param session
     * @return
     */
    public List<Path> getFiles(final Session session) {
        List<Path> content = new ArrayList<Path>();
        for(NSDictionary dict : this) {
            content.add(PathFactory.createPath(session, dict));
        }
        return content;
    }

    @Override
    public void clear() {
        instances.remove(host);
        super.clear();
    }
}
