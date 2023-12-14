package ch.cyberduck.core;

/*
 * Copyright (c) 2002-2017 iterate GmbH. All rights reserved.
 * https://cyberduck.io/
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

import ch.cyberduck.core.exception.AccessDeniedException;
import ch.cyberduck.core.serializer.impl.dd.ProfilePlistReader;

import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.Assert.*;

public class ProtocolFactoryTest {

    @Test
    public void testGetProtocols() {
        final TestProtocol defaultProtocol = new TestProtocol(Scheme.ftp);
        final TestProtocol providerProtocol = new TestProtocol(Scheme.ftp) {
            @Override
            public String getProvider() {
                return "c";
            }
        };
        final TestProtocol disabledProtocol = new TestProtocol(Scheme.sftp) {
            @Override
            public boolean isEnabled() {
                return false;
            }
        };
        final ProtocolFactory f = new ProtocolFactory(new HashSet<>(
                Arrays.asList(defaultProtocol, providerProtocol, disabledProtocol)));
        final List<Protocol> protocols = f.find();
        assertTrue(protocols.contains(defaultProtocol));
        assertTrue(protocols.contains(providerProtocol));
        assertFalse(protocols.contains(disabledProtocol));
    }

    @Test
    public void testFindUnknownDefaultProtocol() {
        final TestProtocol dav = new TestProtocol(Scheme.dav);
        final TestProtocol davs = new TestProtocol(Scheme.davs);
        final ProtocolFactory f = new ProtocolFactory(new LinkedHashSet<>(Arrays.asList(dav, davs)));
        assertEquals(dav, f.forName("dav"));
        assertEquals(dav, f.forScheme(Scheme.http));
        assertEquals(davs, f.forName("davs"));
        assertEquals(davs, f.forScheme(Scheme.https));
        assertNull(f.forName("ftp"));
    }

    @Test
    public void testFindProtocolWithProviderInIdentifier() {
        final TestProtocol dav = new TestProtocol(Scheme.dav) {
            @Override
            public String getIdentifier() {
                return "dav";
            }

            @Override
            public String getProvider() {
                return "provider";
            }
        };
        final ProtocolFactory f = new ProtocolFactory(new LinkedHashSet<>(Collections.singletonList(dav)));
        assertEquals(dav, f.forName("dav"));
        assertEquals(dav, f.forName("dav-provider"));
    }

    @Test
    public void testFindProtocolProviderMismatch() {
        final TestProtocol dav_provider1 = new TestProtocol(Scheme.dav) {
            @Override
            public String getIdentifier() {
                return "dav";
            }

            @Override
            public String getProvider() {
                return "default";
            }

            @Override
            public boolean isBundled() {
                return true;
            }
        };
        final TestProtocol dav_provider2 = new TestProtocol(Scheme.dav) {
            @Override
            public String getIdentifier() {
                return "dav";
            }

            @Override
            public String getProvider() {
                return "provider_2";
            }
        };
        final ProtocolFactory f = new ProtocolFactory(new LinkedHashSet<>(Arrays.asList(dav_provider1, dav_provider2)));
        assertEquals(dav_provider1, f.forName("dav"));
        assertEquals(dav_provider1, f.forName("dav", "default"));
        assertEquals(dav_provider1, f.forName("dav", "g"));
        assertEquals(dav_provider2, f.forName("dav", "provider_2"));
    }

    @Test
    public void testSchemeFallbackType() {
        final TestProtocol dav = new TestProtocol(Scheme.dav);
        final TestProtocol swift = new TestProtocol(Scheme.dav) {
            @Override
            public String getIdentifier() {
                return "swift-p";
            }

            @Override
            public Type getType() {
                return Type.swift;
            }
        };
        final ProtocolFactory f = new ProtocolFactory(new LinkedHashSet<>(Arrays.asList(dav, swift)));
        assertEquals(swift, f.forName("swift"));
    }

    @Test(expected = AccessDeniedException.class)
    public void testRegisterUnknownProtocol() throws Exception {
        new ProfilePlistReader(new ProtocolFactory(Collections.singleton(new TestProtocol() {
            @Override
            public Type getType() {
                return Type.dav;
            }
        }))).read(
                new Local("src/test/resources/Unknown.cyberduckprofile")
        );
    }

    @Test
    public void testOverrideBundledProviderProtocols() {
        final TestProtocol baseProtocol = new TestProtocol(Scheme.http);
        final TestProtocol overrideProtocol = new TestProtocol(Scheme.http) {
            @Override
            public String getProvider() {
                return "test-provider";
            }

            @Override
            public boolean isBundled() {
                return false;
            }
        };
        final ProtocolFactory f = new ProtocolFactory(Stream.of(baseProtocol, overrideProtocol).collect(Collectors.toSet()));
        assertEquals(baseProtocol, f.forName("test"));
        assertEquals(overrideProtocol, f.forName("test", "test-provider"));
    }

    @Test
    public void testOverrideBundledProtocols() {
        final TestProtocol baseProtocol = new TestProtocol(Scheme.http) {
            @Override
            public String getProvider() {
                return "test-provider1";
            }

            @Override
            public boolean isBundled() {
                return false;
            }
        };
        final TestProtocol overrideProtocol = new TestProtocol(Scheme.http) {
            @Override
            public String getProvider() {
                return "test-provider2";
            }

            @Override
            public boolean isBundled() {
                return false;
            }
        };
        final ProtocolFactory f = new ProtocolFactory(Stream.of(baseProtocol, overrideProtocol).collect(Collectors.toSet()));
        assertEquals(overrideProtocol, f.forName("test", "test-provider2"));
        assertEquals(baseProtocol, f.forName("test", "test-provider1"));
    }

    @Test
    public void testPrioritizeNonDeprecatedWithTypeLookup() {
        final TestProtocol first = new TestProtocol(Scheme.http) {
            @Override
            public Type getType() {
                return Type.dracoon;
            }

            @Override
            public String getProvider() {
                return "test-provider1";
            }

            @Override
            public boolean isBundled() {
                return false;
            }

            @Override
            public boolean isDeprecated() {
                return true;
            }
        };
        final TestProtocol second = new TestProtocol(Scheme.http) {
            @Override
            public Type getType() {
                return Type.dracoon;
            }

            @Override
            public String getProvider() {
                return "test-provider2";
            }

            @Override
            public boolean isBundled() {
                return false;
            }
        };
        final ProtocolFactory f = new ProtocolFactory(new LinkedHashSet<>(Arrays.asList(first, second)));
        assertEquals(second, f.forName("test", "test-provider2"));
        assertEquals(first, f.forName("test", "test-provider1"));
        assertEquals(second, f.forName("dracoon"));
    }

    @Test
    public void testRegisterUnregisterIsEnabled() throws Exception {
        final ProtocolFactory factory = new ProtocolFactory(Stream.of(new TestProtocol() {
            @Override
            public Type getType() {
                return Type.s3;
            }

            @Override
            public boolean isEnabled() {
                return true;
            }

            @Override
            public boolean isBundled() {
                return true;
            }
        }).collect(Collectors.toSet()));
        final ProfilePlistReader reader = new ProfilePlistReader(factory);
        final Local file = new Local("src/test/resources/Test S3 (HTTP).cyberduckprofile");
        final Profile profile = reader.read(file);
        factory.register(file);
        assertTrue(profile.isEnabled());
        factory.unregister(profile);
        assertFalse(profile.isEnabled());
        assertTrue(file.exists());
    }
}
