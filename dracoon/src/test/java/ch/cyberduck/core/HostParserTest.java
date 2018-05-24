package ch.cyberduck.core;

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

import ch.cyberduck.core.sds.SDSProtocol;
import ch.cyberduck.core.serializer.impl.dd.ProfilePlistReader;
import ch.cyberduck.test.IntegrationTest;

import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;

import static org.junit.Assert.assertEquals;

@Category(IntegrationTest.class)
public class HostParserTest {

    @Test
    public void testParseUsernameFromUrlEvent() throws Exception {
        final Profile profile = new ProfilePlistReader(new ProtocolFactory(Collections.singleton(new SDSProtocol()))).read(
            new Local("../profiles/default/DRACOON (Email Address).cyberduckprofile"));
        assertEquals(0, new Host(new SDSProtocol(), "duck.ssp-europe.eu", 443, "/cyberduck-test/key", new Credentials(
            System.getProperties().getProperty("sds.user")
        ))
            .compareTo(new HostParser(new ProtocolFactory(new HashSet<>(Arrays.asList(new SDSProtocol(), profile)))).get(
                "dracoon://post%40iterate.ch@duck.ssp-europe.eu/key")));
    }
}
