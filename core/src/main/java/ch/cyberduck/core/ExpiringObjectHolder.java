package ch.cyberduck.core;

/*
 * Copyright (c) 2002-2017 iterate GmbH. All rights reserved.
 * https://cyberduck.io/
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 */

public class ExpiringObjectHolder<T> {

    private final Long timeToLiveMillis;

    private T object;
    private Long updated = Long.MIN_VALUE;

    public ExpiringObjectHolder(final Long timeToLiveMillis) {
        this.timeToLiveMillis = timeToLiveMillis;
    }

    public void set(final T object) {
        this.object = object;
        this.updated = System.currentTimeMillis();
    }

    public T get() {
        if(updated + timeToLiveMillis > System.currentTimeMillis()) {
            return object;
        }
        return object = null;
    }
}
