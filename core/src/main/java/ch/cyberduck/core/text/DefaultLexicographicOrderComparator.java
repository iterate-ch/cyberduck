package ch.cyberduck.core.text;

/*
 * Copyright (c) 2002-2020 iterate GmbH. All rights reserved.
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

import java.util.Comparator;

/**
 * Compares two strings lexicographically. The comparison is based on the Unicode value of each character in the
 * strings.
 */
public class DefaultLexicographicOrderComparator implements Comparator<String> {

    @Override
    public int compare(final String o1, final String o2) {
        return o1.compareTo(o2);
    }
}
