package ch.cyberduck.core.serializer;

/*
 * Copyright (c) 2009 David Kocher. All rights reserved.
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

import ch.cyberduck.core.Serializable;

import java.util.Collection;
import java.util.Map;

/**
 * @param <T> Serialized object type
 */
public interface Serializer<T> {

    /**
     * @param value Value for key
     * @param key   Identifier for value to serialize
     */
    void setStringForKey(String value, String key);

    /**
     * @param value Value for key
     * @param key   Identifier for value to serialize
     */
    void setObjectForKey(Serializable value, String key);

    /**
     * @param <O>   Type of serialized native object
     * @param value Value for key
     * @param key   Identifier for value to serialize
     */
    <O extends Serializable> void setListForKey(Collection<O> value, String key);

    void setStringListForKey(Collection<String> value, String key);

    void setMapForKey(Map<String, String> value, String key);

    void setBooleanForKey(boolean value, String key);

    /**
     * @return Native serialized format
     */
    T getSerialized();
}
