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

    private Constructor<? extends Local> constructorCreateFromString;
    private Constructor<? extends Local> constructorCreateFromParentString;

    private LocalFactory() {
        super("factory.local.class");
    }

    @Override
    protected Local create() {
        return this.create(PreferencesFactory.get().getProperty("local.user.home"));
    }

    private Local create(final String path) {
        try {
            if(null == constructorCreateFromString) {
                constructorCreateFromString = ConstructorUtils.getMatchingAccessibleConstructor(clazz, String.class);
            }
            return constructorCreateFromString.newInstance(path);
        }
        catch(InstantiationException | InvocationTargetException | IllegalAccessException e) {
            throw new FactoryException(e.getMessage(), e);
        }
    }

    private Local create(final Local parent, final String path) {
        try {
            if(null == constructorCreateFromParentString) {
                constructorCreateFromParentString = ConstructorUtils.getMatchingAccessibleConstructor(clazz, parent.getClass(), path.getClass());
            }
            return constructorCreateFromParentString.newInstance(parent, path);
        }
        catch(InstantiationException | InvocationTargetException | IllegalAccessException e) {
            throw new FactoryException(e.getMessage(), e);
        }
    }

    private static final LocalFactory singleton = new LocalFactory();

    public static Local get(final Local parent, final String name) {
        return singleton.create(parent, name);
    }

    public static Local get(final String parent, final String name) {
        return singleton.create(singleton.create(parent), name);
    }

    public static Local get(final String path) {
        return singleton.create(path);
    }

    public static Local get() {
        return singleton.create();
    }
}
