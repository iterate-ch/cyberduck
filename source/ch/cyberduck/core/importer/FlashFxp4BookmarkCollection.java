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

import ch.cyberduck.core.Local;
import ch.cyberduck.core.LocalFactory;
import ch.cyberduck.core.Preferences;

import org.apache.log4j.Logger;

/**
 * @version $Id: FlashFxpBookmarkCollection.java 7472 2010-11-03 13:08:09Z dkocher $
 */
public class FlashFxp4BookmarkCollection extends FlashFxpBookmarkCollection {
    private static Logger log = Logger.getLogger(FlashFxp4BookmarkCollection.class);

    @Override
    public String getBundleIdentifier() {
        return "com.flashfxp4";
    }

    @Override
    public String getName() {
        return "FlashFXP 4";
    }

    @Override
    public Local getFile() {
        return LocalFactory.createLocal(Preferences.instance().getProperty("bookmark.import.flashfxp4.location"));
    }

}