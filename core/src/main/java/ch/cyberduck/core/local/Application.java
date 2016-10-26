package ch.cyberduck.core.local;

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

import ch.cyberduck.core.LocaleFactory;

import org.apache.commons.lang3.StringUtils;

public class Application {

    public static final Application notfound
            = new Application(null, LocaleFactory.localizedString("Unknown"));

    private final String identifier;
    private final String name;

    public Application(final String identifier) {
        this(identifier, null);
    }

    public Application(final String identifier, final String name) {
        this.identifier = StringUtils.lowerCase(identifier);
        this.name = name;
    }

    public String getIdentifier() {
        return identifier;
    }

    public String getName() {
        return name;
    }

    @Override
    public boolean equals(final Object o) {
        if(this == o) {
            return true;
        }
        if(!(o instanceof Application)) {
            return false;
        }
        final Application that = (Application) o;
        if(identifier != null ? !identifier.equals(that.identifier) : that.identifier != null) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        return identifier != null ? identifier.hashCode() : 0;
    }

    @Override
    public String toString() {
        return this.getIdentifier();
    }
}
