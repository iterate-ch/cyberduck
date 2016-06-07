package ch.cyberduck.core.transfer.upload;

/*
 * Copyright (c) 2002-2015 David Kocher. All rights reserved.
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

import ch.cyberduck.core.Local;

import java.util.Comparator;

public class LocalPriorityComparator implements Comparator<Local> {

    @Override
    public int compare(final Local o1, final Local o2) {
        if(o1.isDirectory() && o2.isDirectory()) {
            return 0;
        }
        if(o1.isFile() && o2.isFile()) {
            return 0;
        }
        if(o1.isDirectory()) {
            return -1;
        }
        if(o2.isDirectory()) {
            return 1;
        }
        return 0;
    }
}
