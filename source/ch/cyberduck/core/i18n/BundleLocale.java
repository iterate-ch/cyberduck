package ch.cyberduck.core.i18n;

/*
 *  Copyright (c) 2009 David Kocher. All rights reserved.
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

import ch.cyberduck.core.Preferences;
import ch.cyberduck.ui.cocoa.foundation.NSBundle;

import org.apache.commons.collections.map.LRUMap;

import java.util.Collections;
import java.util.Map;

/**
 * @version $Id$
 */
public class BundleLocale implements Locale {

    private static Map<String, String> cache
            = Collections.<String, String>synchronizedMap(new LRUMap(1000));

    @Override
    public String localize(final String key, final String table) {
        final String identifier = String.format("%s.%s", table, key);
        if(!cache.containsKey(identifier)) {
            cache.put(identifier, NSBundle.localizedString(key, table));
        }
        return cache.get(identifier);
    }

    @Override
    public void setDefault(final String language) {
        final Preferences preferences = Preferences.instance();
        if(null == language) {
            // Revert to system default language
            preferences.deleteProperty("AppleLanguages");
        }
        else {
            preferences.setProperty("AppleLanguages", Collections.singletonList(language));
        }
    }
}