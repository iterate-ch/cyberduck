package ch.cyberduck.core.preferences;

/*
 * Copyright (c) 2002-2026 iterate GmbH. All rights reserved.
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

import ch.cyberduck.core.Host;
import ch.cyberduck.core.Protocol;
import ch.cyberduck.core.TestProtocol;

import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.*;

public class HostPreferencesTest {

    @Test
    public void testRefreshPropertiesWhenProtocolChanges() {
        final MemoryPreferences defaults = new MemoryPreferences();
        defaults.load();
        defaults.setProperty("bookmark.weburl.configurable", true);
        defaults.setProperty("bookmark.password.placeholder", "");
        final Protocol original = new TestProtocol();
        final Protocol restricted = new TestProtocol() {
            @Override
            public Map<String, String> getProperties() {
                final Map<String, String> properties = new HashMap<>();
                properties.put("bookmark.weburl.configurable", "false");
                properties.put("bookmark.password.placeholder", "Optional key");
                return properties;
            }
        };
        final Host host = new Host(original);
        final HostPreferences preferences = new HostPreferences(host, defaults);
        preferences.setProperty("bookmark.test.custom", "saved override");
        assertTrue(preferences.getBoolean("bookmark.weburl.configurable"));
        assertEquals("", preferences.getProperty("bookmark.password.placeholder"));
        host.setProtocol(restricted);
        assertFalse(preferences.getBoolean("bookmark.weburl.configurable"));
        assertEquals("Optional key", preferences.getProperty("bookmark.password.placeholder"));
        assertEquals("saved override", preferences.getProperty("bookmark.test.custom"));
        host.setProtocol(original);
        assertTrue(preferences.getBoolean("bookmark.weburl.configurable"));
        assertEquals("", preferences.getProperty("bookmark.password.placeholder"));
        assertEquals("saved override", preferences.getProperty("bookmark.test.custom"));
    }
}
