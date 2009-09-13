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

import ch.cyberduck.ui.cocoa.foundation.NSObject;

import org.apache.commons.collections.map.LRUMap;
import org.apache.log4j.Logger;

import java.util.HashMap;
import java.util.Map;

/**
 * @version $Id:$
 */
public class AttributeCache<K> {
    private static Logger log = Logger.getLogger(AttributeCache.class);

    private Map<K, Map<String, NSObject>> impl;

    public AttributeCache(int size) {
        impl = new LRUMap(size) {
            @Override
            protected boolean removeLRU(LinkEntry entry) {
                log.debug("Removing from cache:" + entry);
                return true;
            }
        };
    }

    public NSObject put(K key, String attribute, NSObject value) {
        Map<String, NSObject> attributes;
        if(impl.containsKey(key)) {
            attributes = impl.get(key);
        }
        else {
            attributes = new HashMap<String, NSObject>();
        }
        attributes.put(attribute, value);
        impl.put(key, attributes);
        return value;
    }

    public NSObject get(K key, String attribute) {
        if(!impl.containsKey(key)) {
            log.warn("No cached attributes for " + key);
            return null;
        }
        final Map<String, NSObject> attributes = impl.get(key);
        return attributes.get(attribute);
    }

    public void remove(K key) {
        impl.remove(key);
    }

    public void clear() {
        impl.clear();
    }
}
