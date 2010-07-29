package ch.cyberduck.core.importer;

/*
 * Copyright (c) 2002-2010 David Kocher. All rights reserved.
 *
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
 * Bug fixes, suggestions and comments should be sent to:
 * dkocher@cyberduck.ch
 */

import ch.cyberduck.core.AbstractHostCollection;
import ch.cyberduck.core.Local;

import org.apache.log4j.Logger;

/**
 * @version $Id:$
 */
public abstract class ThirdpartyBookmarkCollection extends AbstractHostCollection {
    private static Logger log = Logger.getLogger(ThirdpartyBookmarkCollection.class);

    /**
     * Parse the bookmark file.
     *
     * @param file Local path.
     */
    protected void load(Local file) {
        if(file.exists()) {
            log.info("Found Bookmarks file: " + file.getAbsolute());
            this.parse(file);
        }
    }

    protected abstract void parse(Local file);

    @Override
    public void save() {
        throw new UnsupportedOperationException("Should not attempt to write to thirdparty bookmark collection");
    }
}
