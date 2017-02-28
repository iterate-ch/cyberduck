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

import ch.cyberduck.core.preferences.PreferencesFactory;

import org.apache.commons.lang3.reflect.ConstructorUtils;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

public final class LocalFactory extends Factory<Local> {

    protected LocalFactory() {
        super("factory.local.class");
    }

    @Override
    protected Local create() {
        return this.create(PreferencesFactory.get().getProperty("local.user.home"));
    }

    protected Local create(final String path) {
        final String clazz = PreferencesFactory.get().getProperty("factory.local.class");
        if(null == clazz) {
            throw new FactoryException(String.format("No implementation given for factory %s", this.getClass().getSimpleName()));
        }
        try {
            final Class<Local> name = (Class<Local>) Class.forName(clazz);
            final Constructor<Local> constructor = ConstructorUtils.getMatchingAccessibleConstructor(name, path.getClass());
            return constructor.newInstance(path);
        }
        catch(InstantiationException | InvocationTargetException | ClassNotFoundException | IllegalAccessException e) {
            throw new FactoryException(e.getMessage(), e);
        }
    }

    protected Local create(final Local parent, final String path) {
        final String clazz = PreferencesFactory.get().getProperty("factory.local.class");
        if(null == clazz) {
            throw new FactoryException(String.format("No implementation given for factory %s", this.getClass().getSimpleName()));
        }
        try {
            final Class<Local> name = (Class<Local>) Class.forName(clazz);
            final Constructor<Local> constructor = ConstructorUtils.getMatchingAccessibleConstructor(name, parent.getClass(), path.getClass());
            return constructor.newInstance(parent, path);
        }
        catch(InstantiationException | InvocationTargetException | ClassNotFoundException | IllegalAccessException e) {
            throw new FactoryException(e.getMessage(), e);
        }
    }

    public static Local get(final Local parent, final String name) {
        return new LocalFactory().create(parent, name);
    }

    public static Local get(final String parent, final String name) {
        return new LocalFactory().create(new LocalFactory().create(parent), name);
    }

    public static Local get(final String path) {
        return new LocalFactory().create(path);
    }
}
