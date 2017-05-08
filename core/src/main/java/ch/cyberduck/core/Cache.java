package ch.cyberduck.core;

/*
 * Copyright (c) 2002-2017 iterate GmbH. All rights reserved.
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

import java.util.Set;

public interface Cache<T extends Referenceable> {
    boolean isCached(T parent);

    boolean isEmpty();

    boolean isHidden(T item);

    boolean isValid(T item);

    AttributedList<T> put(T parent, AttributedList<T> children);

    AttributedList<T> get(T parent);

    AttributedList<T> remove(T parent);

    Set<T> keySet();

    void invalidate(T parent);

    void clear();

    T lookup(CacheReference<T> reference);
}
