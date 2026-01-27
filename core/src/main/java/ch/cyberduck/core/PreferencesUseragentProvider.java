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
 * Bug fixes, suggestions and comments should be sent to:
 * feedback@cyberduck.ch
 */

import ch.cyberduck.core.preferences.Preferences;
import ch.cyberduck.core.preferences.PreferencesFactory;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;

public class PreferencesUseragentProvider implements UseragentProvider {

    private static final String ua = buildUserAgent();

    private static String buildUserAgent() {
        final Preferences preferences = PreferencesFactory.get();
        return String.format("%s/%s.%s (%s/%s) (%s)",
                sanitizeApplicationName(preferences.getProperty("application.name")),
                preferences.getProperty("application.version"),
                preferences.getProperty("application.revision"),
                preferences.getProperty("os.name"),
                preferences.getProperty("os.version"),
                preferences.getProperty("os.arch"));
    }

    private static String sanitizeApplicationName(final String application) {
        final char[] httpHeaderTokenSeparators = new char[]{
                ' ', '\t', '"', '(', ')',
                ',', '/', ':', ';', '<',
                '=', '>', '?', '@', '[',
                '\\', ']', '{', '}'
        };
        if(StringUtils.indexOfAny(application, httpHeaderTokenSeparators) == -1) {
            return application;
        }
        final StringBuilder builder = new StringBuilder();
        for(int i = 0; i < application.length(); i++) {
            final char token = application.charAt(i);
            if(ArrayUtils.indexOf(httpHeaderTokenSeparators, token) != -1) {
                continue;
            }
            builder.append(token);
        }
        return builder.toString();
    }

    @Override
    public String get() {
        return ua;
    }
}
