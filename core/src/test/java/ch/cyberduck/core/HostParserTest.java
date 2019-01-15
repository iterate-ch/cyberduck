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

import org.junit.Assert;
import org.junit.Test;

import java.util.Collections;
import java.util.Map;

import com.google.common.collect.ImmutableMap;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

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
        }))).get("https://folder/file");
        assertEquals("defaultHostname", host.getHostname());
        assertEquals("/folder/file", host.getDefaultPath());
    }

    @Test
    public void parseDefaultHostnameWithUserRelativePath() throws Exception {
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
        assertEquals("folder/file", host.getDefaultPath());
    }

    @Test
    public void parseDefaultHostnameWithUserAbsolutePath() throws Exception {
        final Host host = new HostParser(new ProtocolFactory(Collections.singleton(new TestProtocol(Scheme.https) {
            @Override
            public String getDefaultHostname() {
                return "defaultHostname";
            }

            @Override
            public boolean isHostnameConfigurable() {
                return false;
            }
        }))).get("https://user@/folder/file");
        assertEquals("defaultHostname", host.getHostname());
        assertEquals("user", host.getCredentials().getUsername());
        assertEquals("/folder/file", host.getDefaultPath());
    }

    @Test
    public void testFindUriType() {
        final Map<String, HostParser.URITypes> tests = ImmutableMap.<String, HostParser.URITypes>builder()
            .put("/path", HostParser.URITypes.Absolute)
            .put("user@domain/path", HostParser.URITypes.Rootless)
            .put("//user@domain.tld:port/path", HostParser.URITypes.Authority)
            .put("", HostParser.URITypes.Undefined).build();
        for(Map.Entry<String, HostParser.URITypes> entry : tests.entrySet()) {
            final HostParser.StringReader reader = new HostParser.StringReader(entry.getKey());
            assertEquals(HostParser.findURIType(reader), entry.getValue());
        }
    }

    @Test
    public void testParseScheme() {
        final HostParser.Value<String> value = new HostParser.Value<>();
        final String test = "https:";
        final HostParser.StringReader reader = new HostParser.StringReader(test);

        assertTrue(HostParser.parseScheme(reader, value));
        assertEquals("https", value.getValue());
    }

    @Test
    public void testParseAuthoritySimpleDomain() {
        final Host host = new Host(new TestProtocol());
        final String authority = "domain.tld";
        final HostParser.StringReader reader = new HostParser.StringReader(authority);

        assertTrue(HostParser.parseAuthority(reader, host));
        assertEquals(authority, host.getHostname());
    }

    @Test
    public void testParseAuthorityUserDomain() {
        final Host host = new Host(new TestProtocol());
        final String authority = "user@domain.tld";
        final HostParser.StringReader reader = new HostParser.StringReader(authority);

        assertTrue(HostParser.parseAuthority(reader, host));
        assertEquals("user", host.getCredentials().getUsername());
        assertEquals("domain.tld", host.getHostname());
    }

    @Test
    public void testParseAuthorityUserPasswordDomain() {
        final Host host = new Host(new TestProtocol());
        final String authority = "user:password@domain.tld";
        final HostParser.StringReader reader = new HostParser.StringReader(authority);

        assertTrue(HostParser.parseAuthority(reader, host));
        assertEquals("user", host.getCredentials().getUsername());
        assertEquals("password", host.getCredentials().getPassword());
        assertEquals("domain.tld", host.getHostname());
    }

    @Test
    public void testParseAuthorityUserPasswordDomainPort() {
        final Host host = new Host(new TestProtocol());
        final String authority = "user:password@domain.tld:1337";
        final HostParser.StringReader reader = new HostParser.StringReader(authority);

        assertTrue(HostParser.parseAuthority(reader, host));
        assertEquals("user", host.getCredentials().getUsername());
        assertEquals("password", host.getCredentials().getPassword());
        assertEquals("domain.tld", host.getHostname());
        assertEquals(1337, host.getPort());
    }

    @Test
    public void testParseAuthorityUserDefaultDomain() {
        final Host host = new Host(new TestProtocol() {
            @Override
            public boolean isHostnameConfigurable() {
                return false;
            }

            @Override
            public String getDefaultHostname() {
                return "test";
            }
        });
        final String authority = "user@/";
        final HostParser.StringReader reader = new HostParser.StringReader(authority);

        assertTrue(HostParser.parseAuthority(reader, host));
        assertEquals(host.getProtocol().getDefaultHostname(), host.getHostname());
    }

    @Test
    public void testParseAbsolute() {
        final Host host = new Host(new TestProtocol());
        final String path = "/path/sub/directory";
        final HostParser.StringReader reader = new HostParser.StringReader(path);

        assertTrue(HostParser.parseAbsolute(reader, host));
        assertEquals(path, host.getDefaultPath());
    }

    @Test
    public void testParseRootless() {
        final Host host = new Host(new TestProtocol() {
            @Override
            public boolean isHostnameConfigurable() {
                return false;
            }
        });
        final String path = "path/sub/directory";
        final HostParser.StringReader reader = new HostParser.StringReader(path);

        assertTrue(HostParser.parseRootless(reader, host));
        assertEquals(path, host.getDefaultPath());
    }

    @Test
    public void testParseRootlessWithUser() {
        final Host host = new Host(new TestProtocol() {
            @Override
            public boolean isHostnameConfigurable() {
                return false;
            }
        });
        final String path = "user@path/sub/directory";
        final HostParser.StringReader reader = new HostParser.StringReader(path);

        assertTrue(HostParser.parseRootless(reader, host));
        assertEquals("user", host.getCredentials().getUsername());
        assertEquals("path/sub/directory", host.getDefaultPath());
    }
}
