package ch.cyberduck.core.lifecycle;

/*
 * Copyright (c) 2002-2013 David Kocher. All rights reserved.
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

import java.util.Objects;

public class LifecycleConfiguration {

    public static LifecycleConfiguration empty() {
        return new LifecycleConfiguration();
    }

    /**
     * Number of days until to transition file to different storage class
     */
    private Integer transition;
    /**
     * Number of days until to permanently delete file
     */
    private Integer expiration;

    public LifecycleConfiguration() {
        //
    }

    public LifecycleConfiguration(final Integer transition, final Integer expiration) {
        this.transition = transition;
        this.expiration = expiration;
    }

    public Integer getTransition() {
        return transition;
    }

    public Integer getExpiration() {
        return expiration;
    }

    @Override
    public boolean equals(final Object o) {
        if(this == o) {
            return true;
        }
        if(o == null || getClass() != o.getClass()) {
            return false;
        }
        final LifecycleConfiguration that = (LifecycleConfiguration) o;
        if(!Objects.equals(expiration, that.expiration)) {
            return false;
        }
        if(!Objects.equals(transition, that.transition)) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int result = transition != null ? transition.hashCode() : 0;
        result = 31 * result + (expiration != null ? expiration.hashCode() : 0);
        return result;
    }
}
