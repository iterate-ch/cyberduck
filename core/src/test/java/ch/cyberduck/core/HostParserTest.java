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
        final Host host = new HostParser(new ProtocolFactory(Collections.singleton(new TestProtocol(Scheme.https))))
            .get("https://t%40u@host:443/key");
        assertEquals("host", host.getHostname());
        assertEquals(443, host.getPort());
        assertEquals("t@u", host.getCredentials().getUsername());
        assertEquals("/key", host.getDefaultPath());
    }

    @Test
    public void parseNonConfigurableEmptyURL() {
        final Host host = new HostParser(new ProtocolFactory(Collections.singleton(new TestProtocol(Scheme.https) {
            @Override
            public String getDefaultHostname() {
                return "host";
            }

            @Override
            public String getDefaultPath() {
                return "/default-path";
            }

            @Override
            public boolean isHostnameConfigurable() {
                return false;
            }

            @Override
            public boolean isPathConfigurable() {
                return false;
            }
        }))).get("https://");
        assertEquals("host", host.getHostname());
        assertEquals("/default-path", host.getDefaultPath());
    }

    @Test
    public void parseEmptyHost() {
        final Host host = new HostParser(new ProtocolFactory(Collections.singleton(new TestProtocol(Scheme.https) {
            @Override
            public String getDefaultHostname() {
                return "host";
            }

            @Override
            public String getDefaultPath() {
                return "/default-path";
            }

            @Override
            public boolean isPathConfigurable() {
                return false;
            }
        }))).get("https://");
        assertEquals("host", host.getHostname());
        assertEquals("/default-path", host.getDefaultPath());
    }

    @Test
    public void parseEmptyHostWithPath() {
        final Host host = new HostParser(new ProtocolFactory(Collections.singleton(new TestProtocol(Scheme.https) {
            @Override
            public String getDefaultHostname() {
                return "host";
            }

            @Override
            public String getDefaultPath() {
                return "/default-path";
            }
        }))).get("https:///changed-path");
        assertEquals("host", host.getHostname());
        assertEquals("/changed-path", host.getDefaultPath());
    }

    @Test
    public void parseDefaultHostname() throws Exception {
        final Host host = new HostParser(new ProtocolFactory(Collections.singleton(new TestProtocol(Scheme.https) {
            @Override
            public String getDefaultHostname() {
                return "defaultHostname";
            }

            @Override
            public boolean isHostnameConfigurable() {
                return false;
            }
        }))).get("https://user@folder/file");
        assertEquals("defaultHostname", host.getHostname());
        assertEquals("user", host.getCredentials().getUsername());
        assertEquals("/folder/file", host.getDefaultPath());
    }
}
