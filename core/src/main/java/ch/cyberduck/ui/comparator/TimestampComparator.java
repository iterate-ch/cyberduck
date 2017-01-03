package ch.cyberduck.ui.comparator;

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

import ch.cyberduck.core.Path;

public class TimestampComparator extends BrowserComparator {
    private static final long serialVersionUID = 2242337528465570314L;

    public TimestampComparator(final boolean ascending) {
        super(ascending, new FilenameComparator(ascending));
    }

    @Override
    protected int compareFirst(final Path p1, final Path p2) {
        final long d1 = p1.attributes().getModificationDate();
        final long d2 = p2.attributes().getModificationDate();
        if(d1 == d2) {
            return 0;
        }
        if(ascending) {
            return d1 > d2 ? 1 : -1;
        }
        return d1 > d2 ? -1 : 1;
    }
}
