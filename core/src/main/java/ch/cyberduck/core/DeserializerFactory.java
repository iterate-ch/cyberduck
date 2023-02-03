package ch.cyberduck.core;

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

import ch.cyberduck.core.serializer.Deserializer;

import org.apache.commons.lang3.reflect.ConstructorUtils;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

/**
 * @param <T> Serialized object type
 */
public class DeserializerFactory<T> extends Factory<Deserializer<T>> {

    public DeserializerFactory() {
        super("factory.deserializer.class");
    }

    public Deserializer<T> create(final T dict) {
        try {
            final Constructor<? extends Deserializer<T>> constructor = ConstructorUtils.getMatchingAccessibleConstructor(clazz, dict.getClass());
            return constructor.newInstance(dict);
        }
        catch(InstantiationException | IllegalAccessException | InvocationTargetException e) {
            throw new FactoryException(e.getMessage(), e);
        }
    }

    public static <T> Deserializer<T> get() {
        return new DeserializerFactory<T>().create();
    }
}
