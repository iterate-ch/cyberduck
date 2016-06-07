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

import ch.cyberduck.core.preferences.Preferences;
import ch.cyberduck.core.preferences.PreferencesFactory;
import ch.cyberduck.core.serializer.Deserializer;

import org.apache.commons.lang3.reflect.ConstructorUtils;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

public class DeserializerFactory<T> extends Factory<Deserializer> {

    private static final Preferences preferences
            = PreferencesFactory.get();

    private String clazz;

    public DeserializerFactory() {
        this.clazz = preferences.getProperty("factory.deserializer.class");
    }

    /**
     * @param clazz Implementation class name
     */
    public DeserializerFactory(final String clazz) {
        this.clazz = clazz;
    }

    public Deserializer create(final T dict) {
        if(null == clazz) {
            throw new FactoryException(String.format("No implementation given for factory %s", this.getClass().getSimpleName()));
        }
        try {
            final Class<Deserializer> name = (Class<Deserializer>) Class.forName(clazz);
            final Constructor<Deserializer> constructor = ConstructorUtils.getMatchingAccessibleConstructor(name, dict.getClass());
            return constructor.newInstance(dict);
        }
        catch(InstantiationException | IllegalAccessException | InvocationTargetException | ClassNotFoundException e) {
            throw new FactoryException(e.getMessage(), e);
        }
    }

    public static <T> Deserializer get(final T dict) {
        return new DeserializerFactory<T>().create(dict);
    }
}
