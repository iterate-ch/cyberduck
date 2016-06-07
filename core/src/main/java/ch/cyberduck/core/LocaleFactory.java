package ch.cyberduck.core;

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

import ch.cyberduck.core.i18n.Locale;

import org.apache.commons.lang3.StringUtils;

public class LocaleFactory extends Factory<Locale> {

    public LocaleFactory() {
        super("factory.locale.class");
    }

    private static Locale locale;

    /**
     * @return Locale instance for the current platform.
     */
    public static synchronized Locale get() {
        if(null == locale) {
            locale = new LocaleFactory().create();
        }
        return locale;
    }

    /**
     * @param key English variant
     * @return Localized from default table
     */
    public static String localizedString(final String key) {
        return localizedString(key, "Localizable");
    }

    /**
     * @param key   English variant
     * @param table The identifier of the table to lookup the string in. Could be a file.
     * @return Localized from table
     */
    public static String localizedString(final String key, final String table) {
        final String lookup = get().localize(key, table);
        if(StringUtils.contains(lookup, "{0}")) {
            return StringUtils.replace(lookup, "'", "''");
        }
        return lookup;
    }
}
