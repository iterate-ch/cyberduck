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

import ch.cyberduck.core.serializer.Deserializer;

import org.apache.commons.lang3.StringUtils;
import org.junit.Test;

import java.util.Collections;
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
                return null;
            }

            @Override
            public boolean booleanForKey(final String key) {
                return false;
            }

            @Override
            public List<String> keys() {
                return null;
            }
        });
        assertTrue(profile.getProperties().containsKey("quota.notification.url"));
        assertEquals("https://www.gmx.net/produkte/cloud/speicher-erweitern/?mc=03962659",
            profile.getProperties().get("quota.notification.url"));
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
            public boolean booleanForKey(final String key) {
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
                return (List<L>) Collections.singletonList("prop=${application.identifier}");
            }

            @Override
            public Map<String, String> mapForKey(final String key) {
                return null;
            }

            @Override
            public boolean booleanForKey(final String key) {
                return false;
            }

            @Override
            public List<String> keys() {
                return null;
            }
        });
        assertEquals("io.cyberduck", profile.getProvider());
        assertEquals("io.cyberduck", profile.getProperties().get("prop"));
    }
}
