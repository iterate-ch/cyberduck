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

import ch.cyberduck.binding.foundation.NSBundle;
import ch.cyberduck.core.preferences.BundleApplicationResourcesFinder;
import ch.cyberduck.core.preferences.Preferences;
import ch.cyberduck.core.preferences.PreferencesFactory;

import org.apache.commons.collections.map.LRUMap;

import java.util.Collections;
import java.util.Map;

public class BundleLocale implements Locale {

    private NSBundle bundle;

    @SuppressWarnings("unchecked")
    private Map<String, String> cache
            = Collections.<String, String>synchronizedMap(new LRUMap(1000));

    public BundleLocale() {
        this(new BundleApplicationResourcesFinder().bundle());
    }

    public BundleLocale(final NSBundle bundle) {
        this.bundle = bundle;
    }

    @Override
    public String localize(final String key, final String table) {
        final String identifier = String.format("%s.%s", table, key);
        if(!cache.containsKey(identifier)) {
            cache.put(identifier, bundle.localizedString(key, table));
        }
        return cache.get(identifier);
    }

    @Override
    public void setDefault(final String language) {
        final Preferences preferences = PreferencesFactory.get();
        if(null == language) {
            // Revert to system default language
            preferences.deleteProperty("AppleLanguages");
        }
        else {
            preferences.setProperty("AppleLanguages", Collections.singletonList(language));
        }
        cache.clear();
    }
}