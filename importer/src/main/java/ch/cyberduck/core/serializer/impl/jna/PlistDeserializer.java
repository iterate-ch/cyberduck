package ch.cyberduck.core.serializer.impl.jna;

/*
 * Copyright (c) 2009 David Kocher. All rights reserved.
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

import ch.cyberduck.binding.foundation.NSArray;
import ch.cyberduck.binding.foundation.NSDictionary;
import ch.cyberduck.binding.foundation.NSEnumerator;
import ch.cyberduck.binding.foundation.NSObject;
import ch.cyberduck.binding.foundation.NSString;
import ch.cyberduck.core.serializer.Deserializer;

import org.apache.log4j.Logger;
import org.rococoa.Rococoa;

import java.util.ArrayList;
import java.util.List;

public class PlistDeserializer implements Deserializer<NSDictionary> {
    private static final Logger log = Logger.getLogger(PlistDeserializer.class);

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
        return false;
    }

    @Override
    public NSDictionary objectForKey(final String key) {
        final NSObject value = dict.objectForKey(key);
        if(null == value) {
            return null;
        }
        if(value.isKindOfClass(Rococoa.createClass("NSDictionary", NSDictionary._Class.class))) {
            return Rococoa.cast(value, NSDictionary.class);
        }
        log.warn(String.format("Unexpected value type for serialized key %s", key));
        return null;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> List<T> listForKey(final String key) {
        final NSObject value = dict.objectForKey(key);
        if(null == value) {
            return null;
        }
        if(value.isKindOfClass(Rococoa.createClass("NSArray", NSArray._Class.class))) {
            final NSArray array = Rococoa.cast(value, NSArray.class);
            final NSEnumerator enumerator = array.objectEnumerator();
            NSObject next;
            final List<T> list = new ArrayList<T>();
            while((next = enumerator.nextObject()) != null) {
                if(next.isKindOfClass(Rococoa.createClass("NSDictionary", NSDictionary._Class.class))) {
                    list.add((T) Rococoa.cast(next, NSDictionary.class));
                }
                if(next.isKindOfClass(Rococoa.createClass("NSString", NSString._Class.class))) {
                    list.add((T) Rococoa.cast(next, NSString.class).toString());
                }
            }
            return list;
        }
        log.warn(String.format("Unexpected value type for serialized key %s", key));
        return null;
    }
}