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

    private volatile Constructor<? extends Local> constructorCreateFromString;
    private volatile Constructor<? extends Local> constructorCreateFromParentString;

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

    private static volatile LocalFactory singleton;

    /**
     * Lazily create the singleton without holding a lock while the factory is constructed or while {@link Local}
     * instances are created. Both may call {@link PreferencesFactory#get()}, which would otherwise invert the lock
     * order of {@link PreferencesFactory#set(ch.cyberduck.core.preferences.Preferences)} and deadlock.
     */
    private static LocalFactory instance() {
        LocalFactory factory = singleton;
        if(null == factory) {
            final LocalFactory created = new LocalFactory();
            synchronized(LocalFactory.class) {
                if(null == singleton) {
                    singleton = created;
                    log.debug("Using implementation {}", created.getClass());
                }
                factory = singleton;
            }
        }
        return factory;
    }

    public static Local get(final Local parent, final String name) {
        return instance().create(parent, name);
    }

    public static Local get(final String parent, final String name) {
        final LocalFactory factory = instance();
        return factory.create(factory.create(parent), name);
    }

    public static Local get(final String path) {
        return instance().create(path);
    }

    public static Local get() {
        return instance().create();
    }
}
