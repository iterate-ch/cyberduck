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

import org.junit.Test;

import java.util.Collections;

import static org.junit.Assert.assertEquals;

public class HostParserTest {

    @Test
    public void parse() throws Exception {
        final Host host = new HostParser(new ProtocolFactory(Collections.singleton(new TestProtocol(Scheme.https)))).get("https://t%40u@host/key");
        assertEquals("host", host.getHostname());
        assertEquals("t@u", host.getCredentials().getUsername());
    }
}
