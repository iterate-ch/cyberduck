package ch.cyberduck.core.preferences;

/*
 * Copyright (c) 2002-2021 iterate GmbH. All rights reserved.
 * https://cyberduck.io/
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 */

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public interface PreferencesReader {
    Logger log = LogManager.getLogger(PreferencesReader.class);

    default String getProperty(final String... keys) {
        for(String key : keys) {
            final String value = this.getProperty(key);
            if(null == value) {
                continue;
            }
            return value;
        }
        return null;
    }

    /**
     * Give value in user settings or default value if not customized.
     *
     * @param key The property to query.
     * @return The user configured value or default.
     */
    String getProperty(String key);

    default List<String> getList(final String key) {
        return PreferencesReader.toList(this.getProperty(key));
    }

    default int getInteger(final String key) {
        return PreferencesReader.toInteger(this.getProperty(key));
    }

    default float getFloat(final String key) {
        return PreferencesReader.toFloat(this.getProperty(key));
    }

    default long getLong(final String key) {
        return PreferencesReader.toLong(this.getProperty(key));
    }

    default double getDouble(final String key) {
        return PreferencesReader.toDouble(this.getProperty(key));
    }

    default boolean getBoolean(final String key) {
        return PreferencesReader.toBoolean(this.getProperty(key));
    }


    default Map<String, String> getMap(final String property) {
        final List<String> list = this.getList(property);
        final Map<String, String> table = new HashMap<>();
        for(String m : list) {
            if(StringUtils.isBlank(m)) {
                continue;
            }
            if(!m.contains("=")) {
                log.warn("Invalid header {}", m);
                continue;
            }
            int split = m.indexOf('=');
            String key = m.substring(0, split);
            if(StringUtils.isBlank(key)) {
                log.warn("Missing key in {}", m);
                continue;
            }
            String value = m.substring(split + 1);
            if(StringUtils.isEmpty(value)) {
                log.warn("Missing value in {}", m);
                continue;
            }
            table.put(key, value);
        }
        return table;
    }

    static List<String> toList(final String value) {
        if(StringUtils.isBlank(value)) {
            return Collections.emptyList();
        }
        return Arrays.asList(value.split("(?<!\\\\)\\p{javaWhitespace}+"));
    }

    static int toInteger(final String v) {
        if(null == v) {
            return -1;
        }
        try {
            return Integer.parseInt(v);
        }
        catch(NumberFormatException e) {
            return (int) toDouble(v);
        }
    }

    static float toFloat(final String v) {
        if(null == v) {
            return -1;
        }
        try {
            return Float.parseFloat(v);
        }
        catch(NumberFormatException e) {
            return (float) toDouble(v);
        }
    }

    static long toLong(final String v) {
        if(null == v) {
            return -1;
        }
        try {
            return Long.parseLong(v);
        }
        catch(NumberFormatException e) {
            return (long) toDouble(v);
        }
    }

    static double toDouble(final String v) {
        if(null == v) {
            return -1;
        }
        try {
            return Double.parseDouble(v);
        }
        catch(NumberFormatException e) {
            return -1;
        }
    }

    static boolean toBoolean(final String v) {
        if(null == v) {
            return false;
        }
        if(v.equalsIgnoreCase(String.valueOf(true))) {
            return true;
        }
        if(v.equalsIgnoreCase(String.valueOf(false))) {
            return false;
        }
        if(v.equalsIgnoreCase(String.valueOf(1))) {
            return true;
        }
        if(v.equalsIgnoreCase(String.valueOf(0))) {
            return false;
        }
        return v.equalsIgnoreCase("yes");
    }
}
