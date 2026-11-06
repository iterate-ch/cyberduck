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

import ch.cyberduck.core.exception.HostParserException;
import ch.cyberduck.core.preferences.PreferencesFactory;

import org.junit.Test;

import java.util.Collections;
import java.util.Map;
import java.util.Properties;

import com.google.common.collect.ImmutableMap;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class HostParserTest {
    @Test
    public void testDefaultUsernameUnchangedForOtherProtocols() throws Exception {
        final String previous = PreferencesFactory.get().getProperty("connection.login.name");
        PreferencesFactory.get().setProperty("connection.login.name", "os-user");
        try {
            for(boolean configurable : new boolean[]{false, true}) {
                final Protocol protocol = new TestProtocol(Scheme.https) {
                    @Override
                    public boolean isUsernameConfigurable() {
                        return configurable;
                    }
                };
                final HostParser parser = new HostParser(new ProtocolFactory(Collections.singleton(protocol)), protocol);
                for(String url : new String[]{"https://host/path", "host/path"}) {
                    assertEquals("os-user", parser.get(url).getCredentials().getUsername());
                }
                assertEquals("explicit-user", parser.get("https://explicit-user@host/path").getCredentials().getUsername());
            }
        }
        finally {
            PreferencesFactory.get().setProperty("connection.login.name", previous);
        }
    }

    @Test
    public void testFixedUsernameProtocolFindsExistingOAuthTokens() throws Exception {
        final String previous = PreferencesFactory.get().getProperty("connection.login.name");
        PreferencesFactory.get().setProperty("connection.login.name", "os-user");
        try {
            final Protocol protocol = new TestProtocol(Scheme.https) {
                @Override
                public boolean isUsernameConfigurable() {
                    return false;
                }

                @Override
                public String getOAuthClientId() {
                    return "clientid";
                }
            };
            final UnsecureHostPasswordStore store = new UnsecureHostPasswordStore(null) {
                private final Properties properties = new Properties();

                @Override
                protected Properties load() {
                    return properties;
                }

                @Override
                protected void save(final Properties properties) {
                    // Keep credentials in memory for this test.
                }
            };
            final Host previousConnection = new Host(protocol, "host", new Credentials("os-user"));
            previousConnection.getCredentials().setOauth(new OAuthTokens("access-token", "refresh-token", 3600L));
            store.save(previousConnection);
            final Host parsed = new HostParser(new ProtocolFactory(Collections.singleton(protocol)), protocol).get("https://host/path");
            assertEquals("access-token", store.findOAuthTokens(parsed).getAccessToken());
            assertEquals("refresh-token", store.findOAuthTokens(parsed).getRefreshToken());
        }
        finally {
            PreferencesFactory.get().setProperty("connection.login.name", previous);
        }
    }


    @Test
    public void parse() throws HostParserException {
        final Host host = new HostParser(new ProtocolFactory(Collections.singleton(new TestProtocol(Scheme.https))))
            .get("https://t%40u@host:443/key");
        assertEquals("host", host.getHostname());
        assertEquals(443, host.getPort());
        assertEquals("t@u", host.getCredentials().getUsername());
        assertEquals("/key", host.getDefaultPath());
    }

    @Test
    public void parseNonConfigurableEmptyURL() throws HostParserException {
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
    public void parseEmptyHost() throws HostParserException {
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
    public void parseEmptyHostWithPath() throws HostParserException {
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
        assertEquals("folder/file", host.getDefaultPath());
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
    public void testParseAuthoritySimpleDomain() throws HostParserException {
        final Host host = new Host(new TestProtocol());
        final String authority = "domain.tld";
        final HostParser.StringReader reader = new HostParser.StringReader(authority);

        HostParser.parseAuthority(reader, host, null);
        assertEquals(authority, host.getHostname());
    }

    @Test
    public void testParseAuthorityUserDomain() throws HostParserException {
        final Host host = new Host(new TestProtocol());
        final String authority = "user@domain.tld";
        final HostParser.StringReader reader = new HostParser.StringReader(authority);

        HostParser.parseAuthority(reader, host, null);
        assertEquals("user", host.getCredentials().getUsername());
        assertEquals("domain.tld", host.getHostname());
    }

    @Test
    public void testParseAuthorityUserPasswordDomain() throws HostParserException {
        final Host host = new Host(new TestProtocol());
        final String authority = "user:password@domain.tld";
        final HostParser.StringReader reader = new HostParser.StringReader(authority);

        HostParser.parseAuthority(reader, host, null);
        assertEquals("user", host.getCredentials().getUsername());
        assertEquals("password", host.getCredentials().getPassword());
        assertEquals("domain.tld", host.getHostname());
    }

    @Test
    public void testParseAuthorityUserPasswordDomainPort() throws HostParserException {
        final Host host = new Host(new TestProtocol());
        final String authority = "user:password@domain.tld:1337";
        final HostParser.StringReader reader = new HostParser.StringReader(authority);

        HostParser.parseAuthority(reader, host, null);
        assertEquals("user", host.getCredentials().getUsername());
        assertEquals("password", host.getCredentials().getPassword());
        assertEquals("domain.tld", host.getHostname());
        assertEquals(1337, host.getPort());
    }

    @Test
    public void testParseAuthorityUserDefaultDomain() throws HostParserException {
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

        HostParser.parseAuthority(reader, host, null);
        assertEquals(host.getProtocol().getDefaultHostname(), host.getHostname());
    }

    @Test
    public void testParseAbsolute() {
        final Host host = new Host(new TestProtocol());
        final String path = "/path/sub/directory";
        final HostParser.StringReader reader = new HostParser.StringReader(path);

        HostParser.parseAbsolute(reader, host, null);
        assertEquals(path, host.getDefaultPath());
    }

    @Test
    public void testParseRootless() throws HostParserException {
        final Host host = new Host(new TestProtocol() {
            @Override
            public boolean isHostnameConfigurable() {
                return false;
            }
        });
        final String path = "path/sub/directory";
        final HostParser.StringReader reader = new HostParser.StringReader(path);

        HostParser.parseRootless(reader, host, null);
        assertEquals(path, host.getDefaultPath());
    }

    @Test
    public void testParseRootlessWithUser() throws HostParserException {
        final Host host = new Host(new TestProtocol() {
            @Override
            public boolean isHostnameConfigurable() {
                return false;
            }
        });
        final String path = "user@path/sub/directory";
        final HostParser.StringReader reader = new HostParser.StringReader(path);

        HostParser.parseRootless(reader, host, null);
        assertEquals("user", host.getCredentials().getUsername());
        assertEquals("path/sub/directory", host.getDefaultPath());
    }
}
