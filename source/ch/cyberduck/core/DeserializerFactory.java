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
 * @version $Id$
 */
public class DeserializerFactory<T> extends Factory<Deserializer> {

    private static final Preferences preferences
            = Preferences.instance();

    protected Deserializer create(final T dict) {
        try {
            final Class<Deserializer> name = (Class<Deserializer>) Class.forName(preferences.getProperty("factory.deserializer.class"));
            final Constructor<Deserializer> constructor = ConstructorUtils.getMatchingAccessibleConstructor(name, dict.getClass());
            return constructor.newInstance(dict);
        }
        catch(InstantiationException e) {
            throw new FactoryException(e.getMessage(), e);
        }
        catch(IllegalAccessException e) {
            throw new FactoryException(e.getMessage(), e);
        }
        catch(ClassNotFoundException e) {
            throw new FactoryException(e.getMessage(), e);
        }
        catch(InvocationTargetException e) {
            throw new FactoryException(e.getMessage(), e);
        }
    }

    public static <T> Deserializer get(final T dict) {
        return new DeserializerFactory<T>().create(dict);
    }
}
