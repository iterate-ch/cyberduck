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
import ch.cyberduck.core.serializer.Deserializer;

import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertEquals;

public class IRODSProtocolTest {

    @Test
    public void testGetPrefix() throws Exception {
        assertEquals("ch.cyberduck.core.irods.IRODS", new IRODSProtocol().getPrefix());
    }

    @Test
    public void testRegion() throws Exception {
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
            public boolean booleanForKey(final String key) {
                return false;
            }
        }) {
            @Override
            public String getRegion() {
                return "bhctest";
            }
        }).getRegion());
    }
}