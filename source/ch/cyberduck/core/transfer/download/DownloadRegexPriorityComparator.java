package ch.cyberduck.core.transfer.download;

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

import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathNormalizer;
import ch.cyberduck.core.preferences.PreferencesFactory;

import java.util.Comparator;

/**
 * @version $Id$
 */
public class DownloadRegexPriorityComparator implements Comparator<Path> {

    private final String pattern
            = PreferencesFactory.get().getProperty("queue.download.priority.regex");

    @Override
    public int compare(Path o1, Path o2) {
        if(PathNormalizer.name(o1.getAbsolute()).matches(pattern)) {
            return -1;
        }
        if(PathNormalizer.name(o2.getAbsolute()).matches(pattern)) {
            return 1;
        }
        return 0;
    }
}
