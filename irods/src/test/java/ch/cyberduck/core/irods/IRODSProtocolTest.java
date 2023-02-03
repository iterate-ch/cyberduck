/*
 * Copyright (c) 2002-2016 iterate GmbH. All rights reserved.
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

package ch.cyberduck.core.irods;

import ch.cyberduck.core.Host;
import ch.cyberduck.core.Profile;
import ch.cyberduck.core.Protocol;
import ch.cyberduck.core.serializer.Deserializer;

import org.junit.Test;

import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;

public class IRODSProtocolTest {

    @Test
    public void testFeatures() {
        assertEquals(Protocol.Case.sensitive, new IRODSProtocol().getCaseSensitivity());
        assertEquals(Protocol.DirectoryTimestamp.explicit, new IRODSProtocol().getDirectoryTimestamp());
    }

    @Test
    public void testGetPrefix() {
        assertEquals("ch.cyberduck.core.irods.IRODS", new IRODSProtocol().getPrefix());
    }

    @Test
    public void testRegion() {
        assertEquals("bhctest", new Host(new Profile(new IRODSProtocol(), new Deserializer<String>() {
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
                return null;
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
        }) {
            @Override
            public String getRegion() {
                return "bhctest";
            }
        }).getRegion());
    }

    @Test
    public void testResource() {
        final Host bookmark = new Host(new Profile(new IRODSProtocol(), new Deserializer<String>() {
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
                return null;
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
        }) {
            @Override
            public String getRegion() {
                return "DowZone01:MidlandResc";
            }
        });
        assertEquals("DowZone01", new IRODSSession(bookmark).getRegion());
        assertEquals("MidlandResc", new IRODSSession(bookmark).getResource());
    }
}
