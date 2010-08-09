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
import ch.cyberduck.ui.cocoa.odb.EditorFactory;

import org.apache.commons.lang.StringUtils;
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

    @Override
    public void load() {
        this.load(this.getFile());
    }

    public abstract Local getFile();

    protected abstract void parse(Local file);

    @Override
    public void save() {
        throw new UnsupportedOperationException("Should not attempt to write to thirdparty bookmark collection");
    }

    public String getName() {
        return EditorFactory.getApplicationName(this.getBundleIdentifier());
    }

    public boolean isInstalled() {
        return StringUtils.isNotBlank(this.getName());
    }

    public abstract String getBundleIdentifier();

    public String getConfiguration() {
        return "bookmark.import." + this.getBundleIdentifier();
    }
}
