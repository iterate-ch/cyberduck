package ch.cyberduck.core.i18n;

/*
 * Copyright (c) 2002-2015 David Kocher. All rights reserved.
 * http://cyberduck.io/
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
 * feedback@cyberduck.io
 */

import ch.cyberduck.core.Local;
import ch.cyberduck.core.cache.LRUCache;
import ch.cyberduck.core.preferences.ApplicationResourcesFinderFactory;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RegexLocale implements Locale {
    private static final Logger log = LogManager.getLogger(RegexLocale.class);

    /**
     * Loaded tables
     */
    private final Set<String> tables = new HashSet<>();
    private final LRUCache<Key, String> cache = LRUCache.build(1000);
    private final Local resources;

    private static final java.util.Locale defaultLocale = java.util.Locale.getDefault();

    private String language
            = defaultLocale.getLanguage();
    private String country
            = defaultLocale.getCountry();

    private final Pattern pattern
            = Pattern.compile("\"(.*)\"\\s*=\\s*\"(.*)\";");

    public RegexLocale() {
        this(ApplicationResourcesFinderFactory.get().find());
    }

    public RegexLocale(final Local resources) {
        this.resources = resources;
    }

    @Override
    public void setDefault(final String language) {
        this.language = language;
        cache.clear();
        tables.clear();
    }

    @Override
    public String localize(final String key, final String table) {
        final Key lookup = new Key(table, key);
        if(!cache.contains(lookup)) {
            if(!tables.contains(table)) {
                try {
                    this.load(table);
                }
                catch(IOException e) {
                    log.warn(String.format("Failure loading properties from %s.strings. %s", table, e.getMessage()));
                }
                finally {
                    tables.add(table);
                }
            }
        }
        if(cache.contains(lookup)) {
            return cache.get(lookup);
        }
        return key;
    }

    private void load(final String table) throws IOException {
        if(!this.load(table, new File(String.format("%s/%s.lproj/%s.strings.1", resources.getAbsolute(), String.format("%s_%s", language, country), table)))) {
            if(!this.load(table, new File(String.format("%s/%s.lproj/%s.strings.1", resources.getAbsolute(), language, table)))) {
                this.load(table, new File(String.format("%s/%s.lproj/%s.strings", resources.getAbsolute(), language, table)));
            }
        }
    }

    private boolean load(final String table, final File file) throws IOException {
        if(file.exists()) {
            try (final LineNumberReader reader = new LineNumberReader(new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_16))) {
                String line;
                while((line = reader.readLine()) != null) {
                    final Matcher matcher = pattern.matcher(line);
                    if(matcher.matches()) {
                        cache.put(new Key(table, matcher.group(1)), matcher.group(2));
                    }
                }
            }
            return true;
        }
        return false;
    }

    private static final class Key {
        private final String table;
        private final String key;

        public Key(final String table, final String key) {
            this.table = table;
            this.key = key;
        }

        @Override
        public boolean equals(final Object o) {
            if(this == o) {
                return true;
            }
            if(!(o instanceof Key)) {
                return false;
            }
            final Key key1 = (Key) o;
            if(!Objects.equals(key, key1.key)) {
                return false;
            }
            if(!Objects.equals(table, key1.table)) {
                return false;
            }
            return true;
        }

        @Override
        public int hashCode() {
            int result = table != null ? table.hashCode() : 0;
            result = 31 * result + (key != null ? key.hashCode() : 0);
            return result;
        }

        @Override
        public String toString() {
            final StringBuilder sb = new StringBuilder("Key{");
            sb.append("table='").append(table).append('\'');
            sb.append(", key='").append(key).append('\'');
            sb.append('}');
            return sb.toString();
        }
    }
}
