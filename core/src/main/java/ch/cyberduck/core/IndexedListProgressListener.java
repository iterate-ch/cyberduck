package ch.cyberduck.core;

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

import ch.cyberduck.core.exception.ConnectionCanceledException;
import ch.cyberduck.core.exception.ListCanceledException;

public abstract class IndexedListProgressListener implements ListProgressListener {

    private Integer index = 0;

    @Override
    public IndexedListProgressListener reset() throws ConnectionCanceledException {
        index = 0;
        return this;
    }

    @Override
    public void chunk(final Path directory, final AttributedList<Path> list) throws ConnectionCanceledException {
        int i = index;
        while(i < list.size()) {
            final int before = list.size();
            this.visit(list, i, list.get(i));
            if(list.size() == before) {
                i++;
            }
            // Else current item has been removed
        }
        index = list.size();
    }

    /**
     * @param list  List
     * @param index Index of file in list
     * @param file  New file in chunk
     * @throws ListCanceledException Interrupt list
     */
    public abstract void visit(AttributedList<Path> list, int index, Path file) throws ConnectionCanceledException;
}

