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

/**
 * @version $Id$
 */
public abstract class Factory<T> {

    private String clazz;

    protected Factory() {
        this.clazz = null;
    }

    /**
     * @param clazz Implementation class name
     */
    protected Factory(final String clazz) {
        this.clazz = Preferences.instance().getProperty(clazz);
    }

    /**
     * @return A new instance of the type of objects this
     * factory creates
     */
    protected T create() {
        if(null == clazz) {
            throw new FactoryException();
        }
        try {
            final Class<T> name = (Class<T>) Class.forName(clazz);
            return name.newInstance();
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
    }

    public static enum Platform {
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
    }
}