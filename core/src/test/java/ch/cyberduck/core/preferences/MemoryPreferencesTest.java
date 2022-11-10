package ch.cyberduck.core.preferences;

/*
 * Copyright (c) 2002-2022 iterate GmbH. All rights reserved.
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

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class MemoryPreferencesTest {

    @Test
    public void getDisplayName() {
        assertEquals("English", new MemoryPreferences().getDisplayName("en"));
        assertEquals("Čeština", new MemoryPreferences().getDisplayName("cs"));
        assertEquals("Dansk", new MemoryPreferences().getDisplayName("da"));
        assertEquals("Deutsch", new MemoryPreferences().getDisplayName("de"));
        assertEquals("Español", new MemoryPreferences().getDisplayName("es"));
        assertEquals("Suomi", new MemoryPreferences().getDisplayName("fi"));
        assertEquals("Français", new MemoryPreferences().getDisplayName("fr"));
        assertEquals("Italiano", new MemoryPreferences().getDisplayName("it"));
        assertEquals("Polski", new MemoryPreferences().getDisplayName("pl"));
        assertEquals("Português (Brasil)", new MemoryPreferences().getDisplayName("pt_BR"));
        assertEquals("Русский", new MemoryPreferences().getDisplayName("ru"));
        assertEquals("中文 (中国)", new MemoryPreferences().getDisplayName("zh_CN"));
        assertEquals("中文 (台灣)", new MemoryPreferences().getDisplayName("zh_TW"));
        assertEquals("Svenska", new MemoryPreferences().getDisplayName("sv"));
        assertEquals("Magyar", new MemoryPreferences().getDisplayName("hu"));
        assertEquals("日本語", new MemoryPreferences().getDisplayName("ja"));
        assertEquals("한국어", new MemoryPreferences().getDisplayName("ko"));
        assertEquals("Türkçe", new MemoryPreferences().getDisplayName("tr"));
        assertEquals("Hrvatski", new MemoryPreferences().getDisplayName("hr"));
        assertEquals("Latviešu", new MemoryPreferences().getDisplayName("lv"));
        assertEquals("Eesti", new MemoryPreferences().getDisplayName("et"));
        assertEquals("Nederlands", new MemoryPreferences().getDisplayName("nl"));
    }
}