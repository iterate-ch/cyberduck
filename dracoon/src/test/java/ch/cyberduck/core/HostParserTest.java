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

import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;

import static org.junit.Assert.assertEquals;

public class HostParserTest {

    @Test
    public void testParseDefaultPath() throws Exception {
        final Profile profile = new ProfilePlistReader(new ProtocolFactory(Collections.singleton(new SDSProtocol()))).read(
            this.getClass().getResourceAsStream("/DRACOON (OAuth).cyberduckprofile"));
        assertEquals("/room/key", new HostParser(new ProtocolFactory(new HashSet<>(Arrays.asList(new SDSProtocol(), profile)))).get(
            "dracoon://duck.dracoon.com/room/key").getDefaultPath());
    }

    @Test
    public void testParseDefaultPathUmlautPercentEncoding() throws Exception {
        final Profile profile = new ProfilePlistReader(new ProtocolFactory(Collections.singleton(new SDSProtocol()))).read(
            this.getClass().getResourceAsStream("/DRACOON (OAuth).cyberduckprofile"));
        assertEquals("/home/ä-test", new HostParser(new ProtocolFactory(new HashSet<>(Arrays.asList(new SDSProtocol(), profile)))).get(
            "dracoon://duck.dracoon.com/home%2F%C3%A4-test").getDefaultPath());
    }
}
