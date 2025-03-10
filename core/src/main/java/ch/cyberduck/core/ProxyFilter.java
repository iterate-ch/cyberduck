package ch.cyberduck.core;

/*
 * Copyright (c) 2002-2025 iterate GmbH. All rights reserved.
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

public class ProxyFilter<E> implements Filter<E> {

    private final Filter<E>[] filters;

    @SafeVarargs
    public ProxyFilter(final Filter<E>... filters) {
        this.filters = filters;
    }

    @Override
    public boolean accept(final E file) {
        for(Filter<E> filter : filters) {
            if(!filter.accept(file)) {
                return false;
            }
        }
        return true;
    }
}
