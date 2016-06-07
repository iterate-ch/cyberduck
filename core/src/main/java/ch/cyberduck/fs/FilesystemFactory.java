package ch.cyberduck.fs;

/*
 * Copyright (c) 2002-2015 David Kocher. All rights reserved.
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
 * Bug fixes, suggestions and comments should be sent to feedback@cyberduck.ch
 */

import ch.cyberduck.core.Controller;
import ch.cyberduck.core.Factory;
import ch.cyberduck.core.FactoryException;
import ch.cyberduck.core.Host;
import ch.cyberduck.core.PathCache;
import ch.cyberduck.core.preferences.Preferences;
import ch.cyberduck.core.preferences.PreferencesFactory;

import org.apache.commons.lang3.reflect.ConstructorUtils;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

public class FilesystemFactory extends Factory<Filesystem> {

    private static final Preferences preferences
            = PreferencesFactory.get();

    public Filesystem create(final Controller controller, final Host bookmark, final PathCache cache) {
        final String clazz = preferences.getProperty("factory.filesystem.class");
        if(null == clazz) {
            throw new FactoryException(String.format("No implementation given for factory %s", this.getClass().getSimpleName()));
        }
        try {
            final Class<Filesystem> name = (Class<Filesystem>) Class.forName(clazz);
            final Constructor<Filesystem> constructor = ConstructorUtils.getMatchingAccessibleConstructor(name,
                    controller.getClass(), bookmark.getClass(), cache.getClass());
            return constructor.newInstance(controller, bookmark, cache);
        }
        catch(InstantiationException | InvocationTargetException | ClassNotFoundException | IllegalAccessException e) {
            throw new FactoryException(e.getMessage(), e);
        }
    }

    public static Filesystem get(final Controller controller, final Host bookmark, final PathCache cache) {
        return new FilesystemFactory().create(controller, bookmark, cache);
    }
}
