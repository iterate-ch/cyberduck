package ch.cyberduck.core;

/*
 * Copyright (c) 2012 David Kocher. All rights reserved.
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

import ch.cyberduck.core.preferences.Preferences;
import ch.cyberduck.core.preferences.PreferencesFactory;

import org.apache.commons.lang3.reflect.ConstructorUtils;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

/**
 * @version $Id$
 */
public final class LocalFactory extends Factory<Local> {

    private static final Preferences preferences
            = PreferencesFactory.get();

    @Override
    protected Local create() {
        return this.create(preferences.getProperty("local.user.home"));
    }

    protected Local create(final String path) {
        final String clazz = preferences.getProperty("factory.local.class");
        if(null == clazz) {
            throw new FactoryException(String.format("No implementation given for factory %s", this.getClass().getSimpleName()));
        }
        try {
            final Class<Local> name = (Class<Local>) Class.forName(clazz);
            final Constructor<Local> constructor = ConstructorUtils.getMatchingAccessibleConstructor(name, path.getClass());
            return constructor.newInstance(path);
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

    public static Local get(final Local parent, final String name) {
        return get(parent.isRoot() ? String.format("%s%s", parent.getAbsolute(), name) : String.format("%s/%s", parent.getAbsolute(), name));
    }

    public static Local get(final String parent, final String name) {
        return get(new LocalFactory().create(parent), name);
    }

    public static Local get(final String path) {
        return new LocalFactory().create(path);
    }
}
