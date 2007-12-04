package ch.cyberduck.core.io;

/*
 *  Copyright (c) 2007 David Kocher. All rights reserved.
 *  http://cyberduck.ch/
 *
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 2 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  Bug fixes, suggestions and comments should be sent to:
 *  dkocher@cyberduck.ch
 */

import ch.cyberduck.core.Local;

import org.apache.log4j.Logger;

/**
 * @version $Id:$
 */
public class AbstractFileWatcherListener implements FileWatcherListener {
    private static Logger log = Logger.getLogger(AbstractFileWatcherListener.class);

    public void fileWritten(Local file) {
        log.debug("fileWritten" + file);
    }

    public void fileRenamed(Local file) {
        log.debug("fileRenamed" + file);
    }

    public void fileDeleted(Local file) {
        log.debug("fileDeleted" + file);
    }
}