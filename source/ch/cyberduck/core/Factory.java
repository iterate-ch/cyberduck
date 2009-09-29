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

    protected abstract T create();

    public static abstract class Platform {
        public abstract String toString();

        /**
         *
         * @param regex
         * @return
         */
        public boolean matches(String regex) {
            return this.toString().matches(regex);
        }
    }

    public boolean equals(Object other) {
        if(null == other) {
            return false;
        }
        if(other instanceof Platform) {
            return other.toString().equals(this.toString());
        }
        return false;
    }

    public static Platform NATIVE_PLATFORM = new Platform() {
        @Override
        public String toString() {
            return System.getProperty("os.name");
        }
    };

    public static Platform VERSION_PLATFORM = new Platform() {
        @Override
        public String toString() {
            return System.getProperty("os.version");
        }
    };
}