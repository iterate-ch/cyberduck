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

import ch.cyberduck.binding.foundation.NSDictionary;
import ch.cyberduck.binding.foundation.NSMutableArray;
import ch.cyberduck.binding.foundation.NSMutableDictionary;
import ch.cyberduck.binding.foundation.NSNumber;
import ch.cyberduck.core.Serializable;
import ch.cyberduck.core.serializer.Serializer;

import java.util.Collection;
import java.util.Map;

public class PlistSerializer implements Serializer<NSDictionary> {

    final NSMutableDictionary dict;

    public PlistSerializer() {
        this(NSMutableDictionary.dictionary());
    }

    public PlistSerializer(NSMutableDictionary dict) {
        this.dict = dict;
    }

    @Override
    public void setStringForKey(final String value, final String key) {
        dict.setObjectForKey(value, key);
    }

    @Override
    public void setObjectForKey(final Serializable value, final String key) {
        dict.setObjectForKey(value.serialize(new PlistSerializer()), key);
    }

    @Override
    public <O extends Serializable> void setListForKey(final Collection<O> value, final String key) {
        final NSMutableArray list = NSMutableArray.array();
        for(Serializable serializable : value) {
            list.addObject(serializable.serialize(new PlistSerializer()));
        }
        dict.setObjectForKey(list, key);
    }

    @Override
    public void setStringListForKey(final Collection<String> value, final String key) {
        final NSMutableArray list = NSMutableArray.array();
        for(String serializable : value) {
            list.addObject(serializable);
        }
        dict.setObjectForKey(list, key);
    }

    @Override
    public void setMapForKey(final Map<String, String> value, final String key) {
        final NSMutableDictionary dict = NSMutableDictionary.dictionary();
        for(Map.Entry<String, String> entry : value.entrySet()) {
            dict.setObjectForKey(entry.getValue(), entry.getKey());
        }
        dict.setObjectForKey(dict, key);
    }

    @Override
    public void setBooleanForKey(final boolean value, final String key) {
        dict.setObjectForKey(NSNumber.numberWithBoolean(value), key);
    }

    @Override
    public NSDictionary getSerialized() {
        return dict;
    }
}
