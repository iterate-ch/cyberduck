package ch.cyberduck.ui.action;

/*
 *  ch.cyberduck.ActionMap.java
 *  Cyberduck
 *
 *  $Header$
 *  $Revision$
 *  $Date$
 *
 *  Copyright (c) 2003 David Kocher. All rights reserved.
 *  http://icu.unizh.ch/~dkocher/
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

import ch.cyberduck.Cyberduck;

/**
* Contains all <code>Action</code> objects availble application wide.
 */
public class ActionMap extends HashMap {

    private static ActionMap instance = null;

    private ActionMap() {
        super();
    }

    public static ActionMap instance() {
        if(instance == null) {
            instance = new ActionMap();
        }
        return instance;
    }

    /**
     * @param key the actions name
     * @throws IllegalArgumentException if no Action with this key
     */
    public Object get(Object key) throws IllegalArgumentException {
        Object value = super.get(key);
        if(value == null) {
            Cyberduck.DEBUG("WARNING! No action with key '" + key.toString() + "'");
            this.toString();
            throw new IllegalArgumentException("No action with key '" + key.toString() + "'");
        }
        return value;
    }

    /**
     * @param key The action's name
     * @param value The Action
     * @see javax.swing.Action
     * @throws IllegalArgumentException If suche an action already exists
     */
    public Object put(Object key, Object value) throws IllegalArgumentException {
        if(super.get(key) != null) {
            this.toString();
            throw new IllegalArgumentException("Action with key '" + key.toString() + "' already exists.");
        }
        return super.put(key, value);
    }

    public String toString() {
        java.util.Collection values = this.values();
        java.util.Iterator i = values.iterator();
        StringBuffer buffer = new StringBuffer();
        while(i.hasNext()) {
            buffer.append(i.next().toString()+"\n");
        }
        return buffer.toString();
    }
}
