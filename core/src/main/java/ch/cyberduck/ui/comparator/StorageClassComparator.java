package ch.cyberduck.ui.comparator;

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

import ch.cyberduck.core.Path;

import org.apache.commons.lang3.StringUtils;

public class StorageClassComparator extends BrowserComparator {

    public StorageClassComparator(final boolean ascending) {
        super(ascending, new FilenameComparator(ascending));
    }

    @Override
    protected int compareFirst(final Path p1, final Path p2) {
        if(StringUtils.isBlank(p1.attributes().getStorageClass()) && StringUtils.isBlank(p2.attributes().getStorageClass())) {
            return 0;
        }
        if(StringUtils.isBlank(p1.attributes().getStorageClass())) {
            return -1;
        }
        if(StringUtils.isBlank(p2.attributes().getStorageClass())) {
            return 1;
        }
        if(ascending) {
            return p1.attributes().getStorageClass().compareToIgnoreCase(p2.attributes().getStorageClass());
        }
        return -p1.attributes().getStorageClass().compareToIgnoreCase(p2.attributes().getStorageClass());
    }
}
