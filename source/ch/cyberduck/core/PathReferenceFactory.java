package ch.cyberduck.core;

/*
 *  Copyright (c) 2010 David Kocher. All rights reserved.
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

import org.apache.commons.lang3.reflect.ConstructorUtils;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

/**
 * @version $Id$
 */
public class PathReferenceFactory extends Factory<PathReference> {

    private static final Preferences preferences
            = Preferences.instance();

    public static <T> PathReference<T> get(final Path param) {
        return new PathReferenceFactory().create(param);
    }

    protected <T> PathReference<T> create(final Path path) {
        final String clazz = preferences.getProperty("factory.pathreference.class");
        try {
            final Class<PathReference> name = (Class<PathReference>) Class.forName(clazz);
            final Constructor<PathReference> constructor = ConstructorUtils.getMatchingAccessibleConstructor(name, path.getClass());
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
}
