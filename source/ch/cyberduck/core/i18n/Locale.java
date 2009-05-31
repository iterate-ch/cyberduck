package ch.cyberduck.core.i18n;

/*
 *  Copyright (c) 2008 David Kocher. All rights reserved.
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

import ch.cyberduck.ui.cocoa.foundation.NSBundle;

import org.apache.commons.lang.StringUtils;

/**
 * @version $Id:$
 */
public class Locale {

    /**
     * @param key
     * @return
     */
    public static String localizedString(final String key) {
        return localizedString(key, null);
    }

    /**
     * @param key
     * @param table
     * @return
     */
    public static String localizedString(final String key, final String table) {
        if(StringUtils.isEmpty(table)) {
            return NSBundle.localizedString(key);
        }
        return NSBundle.localizedString(key, table);
    }
}