package ch.cyberduck.core.local;

/*
 * Copyright (c) 2002-2014 David Kocher. All rights reserved.
 * http://cyberduck.io/
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
 * Bug fixes, suggestions and comments should be sent to:
 * feedback@cyberduck.io
 */

import ch.cyberduck.core.Local;
import ch.cyberduck.core.Preferences;
import org.apache.commons.io.FilenameUtils;

/**
 * @version $Id$
 */
public class WorkdirPrefixer {

    private Local workdir;

    public WorkdirPrefixer() {
        this.workdir = null;
    }

    public WorkdirPrefixer(final Local workdir) {
        this.workdir = workdir;
    }

    private boolean isAbsolute(final String path) {
        return FilenameUtils.getPrefixLength(path) != 0;
    }

    public String normalize(final String name) {
        if(!this.isAbsolute(name)) {
            if(null == workdir) {
                return String.format("%s%s%s", WorkingDirectoryFinderFactory.get().find().getAbsolute(), Preferences.instance().getProperty("local.delimiter"), name);
            }
            return String.format("%s%s%s", workdir.getAbsolute(), Preferences.instance().getProperty("local.delimiter"), name);
        }
        return name;
    }
}
