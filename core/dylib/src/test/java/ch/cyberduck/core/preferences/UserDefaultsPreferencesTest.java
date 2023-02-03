package ch.cyberduck.core.preferences;

/*
 * Copyright (c) 2002-2016 iterate GmbH. All rights reserved.
 * https://cyberduck.io/
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
 */

import ch.cyberduck.core.AlphanumericRandomStringService;

import org.apache.commons.lang3.StringUtils;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;

public class UserDefaultsPreferencesTest {

    @Test
    public void testGetListEscapedWhitespace() {
        Preferences p = new UserDefaultsPreferences();
        p.load();
        p.setProperty("metadata",
                "Cache-Control=public,max-age=31536000 Expires=Fri,\\ 01\\ Feb\\ 2013\\ 00:00:00\\ GMT");

        final List<String> properties = p.getList("metadata");
        assertTrue(properties.contains("Cache-Control=public,max-age=31536000"));
        assertTrue(properties.contains("Expires=Fri,\\ 01\\ Feb\\ 2013\\ 00:00:00\\ GMT"));
    }

    @Test
    public void testGetMap() {
        Preferences p = new UserDefaultsPreferences();
        p.load();
        p.setProperty("metadata", "Content-Type=application/xml Cache-Control=public,max-age=86400");
        final Map<String, String> properties = p.getMap("metadata");
        assertTrue(properties.containsKey("Content-Type"));
        assertEquals("application/xml", properties.get("Content-Type"));
        assertTrue(properties.containsKey("Cache-Control"));
        assertEquals("public,max-age=86400", properties.get("Cache-Control"));
    }

    @Test
    public void testGetList() {
        Preferences p = new UserDefaultsPreferences();
        p.load();
        p.setProperty("metadata", "a b");
        final List<String> properties = p.getList("metadata");
        assertTrue(properties.contains("a"));
        assertTrue(properties.contains("b"));
    }

    @Test
    public void testSetList() {
        Preferences p = new UserDefaultsPreferences();
        p.load();
        p.setProperty("metadata", Arrays.asList("a", "b"));
        final List<String> properties = p.getList("metadata");
        assertTrue(properties.contains("a"));
        assertTrue(properties.contains("b"));
    }

    @Test
    public void testLong() {
        Preferences p = new UserDefaultsPreferences();
        p.load();
        p.setProperty("test.p", 2L);
        assertEquals("2", p.getProperty("test.p"));
    }

    @Test
    public void testDouble() {
        Preferences p = new UserDefaultsPreferences();
        p.load();
        p.setProperty("test.p", 0.983652);
        assertEquals("0.983652", p.getProperty("test.p"));
    }

    @Test
    public void testIntegerFallback() {
        Preferences p = new UserDefaultsPreferences();
        p.load();
        p.setProperty("t", 1.2d);
        assertEquals(1.2d, p.getDouble("t"), 0d);
        assertEquals(1, p.getInteger("t"));
    }

    @Test
    public void testLongFallback() {
        Preferences p = new UserDefaultsPreferences();
        p.load();
        p.setProperty("t", 1.2d);
        assertEquals(1L, p.getLong("t"));
    }

    @Test
    public void testDefault() {
        UserDefaultsPreferences p = new UserDefaultsPreferences();
        p.load();
        p.setDefaults();
        final String property = new AlphanumericRandomStringService().random();
        p.setDefault(property, "test.value");
        assertEquals("test.value", p.getProperty(property));
        p.setDefault(property, "test.value2");
        assertEquals("test.value2", p.getProperty(property));
    }

    @Test
    public void testOverrideDefault() {
        UserDefaultsPreferences p = new UserDefaultsPreferences();
        p.load();
        p.setDefaults();
        final String property = new AlphanumericRandomStringService().random();
        p.setDefault(property, "test.value");
        assertEquals("test.value", p.getProperty(property));
        p.setProperty(property, "test.value2");
        assertEquals("test.value2", p.getProperty(property));
    }

    @Test
    public void testTemporaryDirectory() throws Exception {
        UserDefaultsPreferences p = new UserDefaultsPreferences();
        p.load();
        p.setDefaults();
        assertTrue(StringUtils.startsWith(p.getProperty("tmp.dir"), System.getProperty("java.io.tmpdir")));
        assertNotEquals(System.getProperty("java.io.tmpdir"), p.getProperty("tmp.dir"));
    }
}
