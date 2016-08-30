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

import org.jets3t.service.model.S3Object;

public class LifecycleConfiguration {

    public static LifecycleConfiguration empty() {
        return new LifecycleConfiguration();
    }

    private Integer transition;
    private Integer expiration;
    private String storageClass;

    public LifecycleConfiguration() {
        //
    }

    public LifecycleConfiguration(final Integer transition, final Integer expiration) {
        this(transition, S3Object.STORAGE_CLASS_GLACIER, expiration);
    }

    public LifecycleConfiguration(final Integer transition, final String storageClass, final Integer expiration) {
        this.transition = transition;
        this.storageClass = storageClass;
        this.expiration = expiration;
    }

    public Integer getTransition() {
        return transition;
    }

    public Integer getExpiration() {
        return expiration;
    }

    public String getStorageClass() {
        return storageClass;
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
        if(expiration != null ? !expiration.equals(that.expiration) : that.expiration != null) {
            return false;
        }
        if(transition != null ? !transition.equals(that.transition) : that.transition != null) {
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
