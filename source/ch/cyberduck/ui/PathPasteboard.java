package ch.cyberduck.ui;

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

import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @version $Id$
 */
public class PathPasteboard extends Collection<Path> implements Pasteboard<Path> {
    private static Logger log = Logger.getLogger(PathPasteboard.class);

    private static Map<Session, PathPasteboard> instances = new HashMap<Session, PathPasteboard>() {
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
     * @param session
     * @return Pasteboard for a given session
     */
    public static PathPasteboard getPasteboard(Session session) {
        if(!instances.containsKey(session)) {
            instances.put(session, new PathPasteboard(session));
        }
        return instances.get(session);
    }

    /**
     * @return
     */
    public static Map<Session, PathPasteboard> allPasteboards() {
        return instances;
    }

    private Session session;

    private PathPasteboard(Session session) {
        this.session = session;
    }

    public List<Path> copy() {
        List<Path> content = new ArrayList<Path>();
        Session copy = SessionFactory.createSession(session.getHost());
        for(Path path : this) {
            content.add(PathFactory.createPath(copy, path.getAsDictionary()));
        }
        return content;
    }

    @Override
    public void clear() {
        instances.remove(session);
        super.clear();
    }
}
