package ch.cyberduck.core.collections;

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

import java.util.AbstractList;
import java.util.List;

public class Partition<T> extends AbstractList<List<T>> {
    final List<T> list;
    final int size;

    public Partition(List<T> list, int size) {
        this.list = list;
        this.size = size;
    }

    @Override
    public List<T> get(int index) {
        int start = index * size;
        int end = Math.min(start + size, list.size());
        return list.subList(start, end);
    }

    @Override
    public int size() {
        return (list.size() + size - 1) / size;
    }

    @Override
    public boolean isEmpty() {
        return list.isEmpty();
    }
}
