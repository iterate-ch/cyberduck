package ch.cyberduck.core;

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

import org.apache.commons.lang3.StringUtils;

public final class PathRelativizer {

    private PathRelativizer() {
        //
    }

    public static String relativize(String root, final String path) {
        if(StringUtils.isBlank(root)) {
            return path;
        }
        if(!StringUtils.equals(root, String.valueOf(Path.DELIMITER))) {
            root = root + String.valueOf(Path.DELIMITER);
        }
        if(StringUtils.contains(path, root)) {
            return path.substring(path.indexOf(root) + root.length());
        }
        return path;
    }
}
