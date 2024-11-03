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
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

public final class LocalFactory extends Factory<Local> {
    private static final Logger log = LogManager.getLogger(LocalFactory.class);

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
                log.warn("Caching constructor {} for class {}", constructorCreateFromString, clazz);
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
                log.warn("Caching constructor {} for class {}", constructorCreateFromParentString, clazz);
            }
            return constructorCreateFromParentString.newInstance(parent, path);
        }
        catch(InstantiationException | InvocationTargetException | IllegalAccessException e) {
            throw new FactoryException(e.getMessage(), e);
        }
    }

    private static LocalFactory singleton;

    public static synchronized Local get(final Local parent, final String name) {
        if(null == singleton) {
            singleton = new LocalFactory();
            log.debug("Using implementation {}", singleton.getClass());

        }
        return singleton.create(parent, name);
    }

    public static synchronized Local get(final String parent, final String name) {
        if(null == singleton) {
            singleton = new LocalFactory();
            log.debug("Using implementation {}", singleton.getClass());
        }
        return singleton.create(singleton.create(parent), name);
    }

    public static synchronized Local get(final String path) {
        if(null == singleton) {
            singleton = new LocalFactory();
            log.debug("Using implementation {}", singleton.getClass());
        }
        return singleton.create(path);
    }

    public static synchronized Local get() {
        if(null == singleton) {
            singleton = new LocalFactory();
            log.debug("Using implementation {}", singleton.getClass());
        }
        return singleton.create();
    }
}
