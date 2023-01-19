package ch.cyberduck.core;

/*
 *  Copyright (c) 2009 David Kocher. All rights reserved.
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

import ch.cyberduck.core.preferences.PreferencesFactory;

import org.apache.commons.lang3.reflect.ConstructorUtils;

import java.lang.reflect.InvocationTargetException;

public abstract class Factory<T> {

    protected final Class<? extends T> clazz;

    public Factory() {
        this.clazz = null;
    }

    public Factory(final Class<? extends T> clazz) {
        this.clazz = clazz;
    }

    /**
     * @param name Implementation class name
     */
    protected Factory(final String name) {
        try {
            final String c = PreferencesFactory.get().getProperty(name);
            if(null == c) {
                throw new FactoryException(String.format("No implementation given for factory %s", this.getClass().getSimpleName()));
            }
            this.clazz = (Class<? extends T>) Class.forName(c);
        }
        catch(ClassNotFoundException e) {
            throw new FactoryException(e.getMessage(), e);
        }
    }

    /**
     * @return A new instance of the type of objects this
     * factory creates
     */
    protected T create() {
        if(null == clazz) {
            throw new FactoryException(String.format("No implementation given for factory %s", this.getClass().getSimpleName()));
        }
        try {
            return ConstructorUtils.getMatchingAccessibleConstructor(clazz).newInstance();
        }
        catch(InstantiationException | IllegalAccessException | InvocationTargetException e) {
            throw new FactoryException(e.getMessage(), e);
        }
    }

    public boolean isAvailable() {
        return null != clazz;
    }

    public enum Platform {
        osname {
            @Override
            public String toString() {
                return System.getProperty("os.name");
            }
        },
        osversion {
            @Override
            public String toString() {
                return System.getProperty("os.version");
            }
        };

        /**
         * @param regex Identification string
         * @return True if platform identification matches regular expression
         */
        public boolean matches(final String regex) {
            return this.toString().matches(regex);
        }

        public enum Name {
            mac,
            windows,
            linux,
            unknown
        }

        public static Platform.Name getDefault() {
            if(Factory.Platform.osname.matches(".*Linux.*")) {
                return Name.linux;
            }
            if(Factory.Platform.osname.matches(".*Windows.*")) {
                return Name.windows;
            }
            if(Factory.Platform.osname.matches(".*Mac.*")) {
                return Name.mac;
            }
            return Name.unknown;
        }
    }
}
