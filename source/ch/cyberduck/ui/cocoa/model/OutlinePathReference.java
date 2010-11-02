package ch.cyberduck.ui.cocoa.model;

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

import ch.cyberduck.core.AbstractPath;
import ch.cyberduck.core.PathReference;
import ch.cyberduck.core.PathReferenceFactory;
import ch.cyberduck.core.Preferences;
import ch.cyberduck.ui.cocoa.foundation.NSObject;
import ch.cyberduck.ui.cocoa.foundation.NSString;

import org.apache.commons.collections.map.LRUMap;

import java.util.Map;

/**
 * Mapper between path references returned from the outline view model and its internal
 * string representation.
 *
 * @version $Id$
 */
public class OutlinePathReference extends PathReference<NSObject> {

    private NSObject reference;

    private int hashcode;

    private static Map<String, NSString> cache = new LRUMap(
            Preferences.instance().getInteger("browser.model.cache.size")
    );


    /**
     * @param path
     */
    private OutlinePathReference(AbstractPath path) {
        String unique = path.unique();
        if(!cache.containsKey(unique)) {
            cache.put(unique, NSString.stringWithString(unique));
        }
        this.reference = cache.get(unique);
        this.hashcode = unique.hashCode();
    }

    /**
     * @param reference
     */
    public OutlinePathReference(NSObject reference) {
        this.reference = reference;
        this.hashcode = reference.toString().hashCode();
    }

    @Override
    public NSObject unique() {
        return reference;
    }

    @Override
    public int hashCode() {
        return hashcode;
    }

    private static class Factory extends PathReferenceFactory {
        @Override
        protected PathReference create() {
            throw new UnsupportedOperationException("Please provide a parameter");
        }

        @Override
        protected <T> PathReference<T> create(AbstractPath param) {
            return (PathReference<T>) new OutlinePathReference(param);
        }
    }

    public static void register() {
        PathReferenceFactory.addFactory(Factory.NATIVE_PLATFORM, new Factory());
    }
}