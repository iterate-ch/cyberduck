package ch.cyberduck.core;

/*
 * Copyright (c) 2002-2018 iterate GmbH. All rights reserved.
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

import ch.cyberduck.core.openstack.SwiftProtocol;
import ch.cyberduck.core.serializer.impl.dd.ProfilePlistReader;

import org.junit.Test;

import java.util.Collections;
import java.util.LinkedHashSet;

import static org.junit.Assert.assertEquals;

public class HostParserTest {

    @Test
    public void testParseCustomHostname() throws Exception {
        final ProtocolFactory factory = new ProtocolFactory(new LinkedHashSet<>(Collections.singletonList(new SwiftProtocol())));
        factory.register(new ProfilePlistReader(factory).read(this.getClass().getResourceAsStream("/Swift.cyberduckprofile")));
        final Host host = new HostParser(factory).get("swift://auth.cloud.ovh.net/container/");
        assertEquals("auth.cloud.ovh.net", host.getHostname());
        assertEquals(Protocol.Type.swift, host.getProtocol().getType());
        assertEquals("/container/", host.getDefaultPath());
    }
}
