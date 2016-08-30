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

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;

public class DefaultPathKindDetector implements PathKindDetector {

    @Override
    public Path.Type detect(final String path) {
        if(StringUtils.isBlank(path)) {
            return Path.Type.directory;
        }
        if(path.endsWith(String.valueOf(Path.DELIMITER))) {
            return Path.Type.directory;
        }
        if(StringUtils.isBlank(FilenameUtils.getExtension(path))) {
            return Path.Type.directory;
        }
        return Path.Type.file;
    }
}
