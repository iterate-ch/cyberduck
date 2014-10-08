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

import org.apache.commons.lang3.StringUtils;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @version $Id$
 */
public class MemoryPreferences extends Preferences {

    private Map<String, String> store;

    @Override
    public void setProperty(final String property, final String v) {
        store.put(property, v);
    }

    @Override
    public void setProperty(final String property, final List<String> values) {
        store.put(property, StringUtils.join(values, ","));
    }

    @Override
    public void deleteProperty(final String property) {
        store.remove(property);
    }

    @Override
    public String getProperty(final String property) {
        if(store.containsKey(property)) {
            return store.get(property);
        }
        return defaults.get(property);
    }

    @Override
    public void save() {
        //
    }

    @Override
    protected void setDefaults() {
        defaults.put("logging.config", "log4j.xml");
        defaults.put("application.support.path", System.getProperty("java.io.tmpdir"));
        defaults.put("application.profiles.path", System.getProperty("java.io.tmpdir"));
        defaults.put("application.receipt.path", System.getProperty("java.io.tmpdir"));
        defaults.put("application.bookmarks.path", System.getProperty("java.io.tmpdir"));
        defaults.put("queue.download.folder", "~/Downloads");
        defaults.put("application.name", "Cyberduck");
        defaults.put("application.version", "0");
        defaults.put("connection.ssl.keystore.type", "KeychainStore");
        defaults.put("connection.ssl.keystore.provider", "Apple");
        super.setDefaults();
    }

    @Override
    protected void load() {
        store = new HashMap<String, String>();
    }

    @Override
    public List<String> applicationLocales() {
        return Collections.singletonList("en");
    }

    @Override
    public List<String> systemLocales() {
        return Collections.singletonList("en");
    }
}
