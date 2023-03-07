package ch.cyberduck.core.features;

/*
 * Copyright (c) 2002-2016 iterate GmbH. All rights reserved.
 * https://cyberduck.io/
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
 */

import ch.cyberduck.core.LocaleFactory;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.exception.BackgroundException;

import java.util.Objects;
import java.util.Set;

/**
 * Retrieve geographical location remote storage
 */
@Optional
public interface Location {

    /**
     * @return Default region for new containers
     */
    Name getDefault();

    /**
     * @return Available regions
     */
    Set<Name> getLocations();

    /**
     * @param file File or folder
     * @return Region of container
     * @throws BackgroundException Failure determining region
     */
    Name getLocation(Path file) throws BackgroundException;

    class Name {
        private final String identifier;

        public Name(String identifier) {
            this.identifier = identifier;
        }

        public String getIdentifier() {
            return identifier;
        }

        public String toString() {
            return identifier;
        }

        @Override
        public boolean equals(final Object o) {
            if(this == o) {
                return true;
            }
            if(!(o instanceof Name)) {
                return false;
            }
            final Name name = (Name) o;
            if(!Objects.equals(identifier, name.identifier)) {
                return false;
            }
            return true;
        }

        @Override
        public int hashCode() {
            return identifier != null ? identifier.hashCode() : 0;
        }
    }

    Name unknown = new Name(null) {
        @Override
        public String toString() {
            return LocaleFactory.localizedString("Unknown");
        }
    };
}
