package ch.cyberduck.core.sds;

/*
 * Copyright (c) 2002-2017 iterate GmbH. All rights reserved.
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
import ch.cyberduck.core.HostParser;
import ch.cyberduck.core.Local;
import ch.cyberduck.core.Profile;
import ch.cyberduck.core.ProtocolFactory;
import ch.cyberduck.core.serializer.impl.dd.ProfilePlistReader;
import ch.cyberduck.test.IntegrationTest;

import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.util.Collections;
import java.util.HashSet;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

@Category(IntegrationTest.class)
public class SDSProtocolTest {

    @Test
    public void testParse() throws Exception {
        final SDSProtocol protocol = new SDSProtocol();
        final ProtocolFactory factory = new ProtocolFactory(new HashSet<>(Collections.singleton(protocol)));
        final Profile profile = new ProfilePlistReader(factory).read(
                new Local(this.getClass().getResource("/test.cyberduckprofile").getPath()));
        factory.register(profile);
        {
            final Host host = new HostParser(factory).get("sds://duck");
            assertNotNull(host);
            assertEquals(profile, host.getProtocol());
        }
        {
            final Host host = new HostParser(factory).get("ssp-oauth://duck");
            assertNotNull(host);
            assertEquals(profile, host.getProtocol());
        }
    }
}