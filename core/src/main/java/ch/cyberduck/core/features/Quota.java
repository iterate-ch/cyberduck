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

import ch.cyberduck.core.exception.BackgroundException;

import java.util.Objects;

@Optional
public interface Quota {
    Space get() throws BackgroundException;

    final class Space {
        public Space(final Long used, final Long available) {
            this.used = used;
            this.available = available;
        }

        /**
         * Occupied space
         */
        public final Long used;
        /**
         * Remaining space
         */
        public final Long available;

        @Override
        public String toString() {
            final StringBuilder sb = new StringBuilder("Space{");
            sb.append("used=").append(used);
            sb.append(", available=").append(available);
            sb.append('}');
            return sb.toString();
        }

        @Override
        public boolean equals(final Object o) {
            if(this == o) {
                return true;
            }
            if(!(o instanceof Space)) {
                return false;
            }
            final Space space = (Space) o;
            return Objects.equals(used, space.used) && Objects.equals(available, space.available);
        }

        @Override
        public int hashCode() {
            return Objects.hash(used, available);
        }
    }

    Space unknown = new Space(0L, Long.MAX_VALUE);
}
