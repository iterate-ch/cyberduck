package ch.cyberduck.core.serializer.impl.dd;

/*
 * Copyright (c) 2002-2014 David Kocher. All rights reserved.
 * http://cyberduck.io/
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
 * feedback@cyberduck.io
 */

import ch.cyberduck.core.serializer.Deserializer;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.dd.plist.NSArray;
import com.dd.plist.NSDictionary;
import com.dd.plist.NSObject;
import com.dd.plist.NSString;

public class PlistDeserializer implements Deserializer<NSDictionary> {
    private static final Logger log = LogManager.getLogger(PlistDeserializer.class);

    final NSDictionary dict;

    public PlistDeserializer(final NSDictionary dict) {
        this.dict = dict;
    }

    @Override
    public String stringForKey(final String key) {
        final NSObject value = dict.objectForKey(key);
        if(null == value) {
            return null;
        }
        return value.toString();
    }

    @Override
    public boolean booleanForKey(final String key) {
        final String value = this.stringForKey(key);
        if(null == value) {
            return false;
        }
        if(value.equalsIgnoreCase(String.valueOf(1))) {
            return true;
        }
        return Boolean.parseBoolean(value);
    }

    @Override
    public NSDictionary objectForKey(final String key) {
        final NSObject value = dict.objectForKey(key);
        if(null == value) {
            return null;
        }
        if(value instanceof NSDictionary) {
            return (NSDictionary) value;
        }
        log.warn("Unexpected value type for serialized key {}", key);
        return null;
    }

    @Override
    public <T> List<T> listForKey(final String key) {
        final NSObject value = dict.objectForKey(key);
        if(null == value) {
            return null;
        }
        if(value instanceof NSArray) {
            final NSArray array = (NSArray) value;
            final List<T> list = new ArrayList<>();
            for(int i = 0; i < array.count(); i++) {
                final NSObject next = array.objectAtIndex(i);
                if(next instanceof NSDictionary) {
                    list.add((T) next);
                }
                else if(next instanceof NSString) {
                    list.add((T) next.toString());
                }
                else {
                    log.warn("Ignore content of type {}", next);
                }

            }
            return list;
        }
        log.warn("Unexpected value type for serialized key {}", key);
        return null;
    }

    @Override
    public Map<String, String> mapForKey(final String key) {
        final NSObject value = dict.objectForKey(key);
        if(null == value) {
            return null;
        }
        if(value instanceof NSDictionary) {
            final NSDictionary dict = (NSDictionary) value;
            final Map<String, String> map = new HashMap<>();
            for(String k : dict.allKeys()) {
                map.put(k, dict.objectForKey(k).toString());
            }
            return map;
        }
        log.warn("Unexpected value type for serialized key {}", key);
        return null;
    }

    @Override
    public List<String> keys() {
        return new ArrayList<>(dict.keySet());
    }
}
