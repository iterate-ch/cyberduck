package ch.cyberduck.core;

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

import ch.cyberduck.core.preferences.HostPreferencesFactory;
import ch.cyberduck.core.serializer.Deserializer;

import org.apache.commons.lang3.StringUtils;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class ProfileTest {

    @Test
    public void testProperties() {
        final Profile profile = new Profile(new TestProtocol(), new Deserializer<String>() {
            @Override
            public String stringForKey(final String key) {
                return null;
            }

            @Override
            public String objectForKey(final String key) {
                return null;
            }

            @Override
            public <L> List<L> listForKey(final String key) {
                return (List<L>) Collections.singletonList("quota.notification.url=https://www.gmx.net/produkte/cloud/speicher-erweitern/?mc=03962659");
            }

            @Override
            public Map<String, String> mapForKey(final String key) {
                if("Properties".equals(key)) {
                    final HashMap<String, String> properties = new HashMap<>();
                    properties.put("customprop1", "value1");
                    properties.put("custombool1", "true");
                    properties.put("customint1", "4");
                    return properties;
                }
                return Collections.emptyMap();
            }

            @Override
            public Boolean booleanForKey(final String key) {
                return false;
            }

            @Override
            public List<String> keys() {
                return Collections.singletonList("Properties");
            }
        });
        assertTrue(profile.getProperties().containsKey("quota.notification.url"));
        assertEquals("https://www.gmx.net/produkte/cloud/speicher-erweitern/?mc=03962659",
            profile.getProperties().get("quota.notification.url"));
        assertEquals("value1",
                profile.getProperties().get("customprop1"));
        assertTrue(HostPreferencesFactory.get(new Host(profile)).getBoolean("custombool1"));
        assertEquals(4, HostPreferencesFactory.get(new Host(profile)).getInteger("customint1"));
    }

    @Test
    public void testEmptyProperty() {
        final Profile profile = new Profile(new TestProtocol(), new Deserializer<String>() {
            @Override
            public String stringForKey(final String key) {
                return null;
            }

            @Override
            public String objectForKey(final String key) {
                return null;
            }

            @Override
            public <L> List<L> listForKey(final String key) {
                return (List<L>) Collections.singletonList("empty.prop=");
            }

            @Override
            public Map<String, String> mapForKey(final String key) {
                return null;
            }

            @Override
            public Boolean booleanForKey(final String key) {
                return false;
            }

            @Override
            public List<String> keys() {
                return null;
            }
        });
        assertTrue(profile.getProperties().containsKey("empty.prop"));
        assertEquals(StringUtils.EMPTY, profile.getProperties().get("empty.prop"));
    }

    @Test
    public void testSubstitution() {
        final Profile profile = new Profile(new TestProtocol(), new Deserializer<String>() {
            @Override
            public String stringForKey(final String key) {
                return "${application.identifier}";
            }

            @Override
            public String objectForKey(final String key) {
                return null;
            }

            @Override
            public <L> List<L> listForKey(final String key) {
                return (List<L>) Arrays.asList(
                        "prop=${application.identifier}",
                        "unknown=${unknown}"
                        );
            }

            @Override
            public Map<String, String> mapForKey(final String key) {
                return Collections.singletonMap("propFromMap", "${application.identifier}");
            }

            @Override
            public Boolean booleanForKey(final String key) {
                return false;
            }

            @Override
            public List<String> keys() {
                return null;
            }
        }) {
            @Override
            public String getOAuthClientSecret() {
                return "${notfound}";
            }
        };
        assertEquals("io.cyberduck", profile.getProvider());
        assertEquals("${notfound}", profile.getOAuthClientSecret());
        assertEquals("io.cyberduck", profile.getProperties().get("prop"));
        assertEquals("${unknown}", profile.getProperties().get("unknown"));
        assertEquals("io.cyberduck", profile.getProperties().get("propFromMap"));
    }
}
