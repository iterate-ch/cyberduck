package ch.cyberduck.core.serializer.impl.dd;

/*
 * Copyright (c) 2002-2014 David Kocher. All rights reserved.
 * http://cyberduck.io/
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
 *
 * Bug fixes, suggestions and comments should be sent to:
 * feedback@cyberduck.io
 */

import ch.cyberduck.core.Local;
import ch.cyberduck.core.Profile;
import ch.cyberduck.core.Protocol;
import ch.cyberduck.core.ProtocolFactory;
import ch.cyberduck.core.Scheme;
import ch.cyberduck.core.TestProtocol;
import ch.cyberduck.core.features.Location;

import org.junit.Test;

import java.util.Collections;
import java.util.Set;

import static org.junit.Assert.*;

public class ProfilePlistReaderTest {

    @Test
    public void testDeserializeDropbox() throws Exception {
        final Profile profile = new ProfilePlistReader(new ProtocolFactory(Collections.singleton(new TestProtocol() {
            @Override
            public Type getType() {
                return Type.dropbox;
            }

            @Override
            public boolean isEnabled() {
                return false;
            }
        }))).read(
            new Local("src/test/resources/Test Dropbox.cyberduckprofile")
        );
        assertNotNull(profile);
    }

    @Test
    public void testDeserializeUnknownProtocol() throws Exception {
        final Profile profile = new ProfilePlistReader(new ProtocolFactory(Collections.singleton(new TestProtocol() {
            @Override
            public Type getType() {
                return Type.dav;
            }

            @Override
            public boolean isEnabled() {
                return false;
            }
        }))).read(
            new Local("src/test/resources/Unknown.cyberduckprofile")
        );
        assertNull(profile);
    }

    @Test
    public void testRegions() throws Exception {
        final Profile profile = new ProfilePlistReader(new ProtocolFactory(Collections.singleton(new TestProtocol() {
            @Override
            public boolean isEnabled() {
                return false;
            }
        }))).read(
            new Local("src/test/resources/Custom Regions S3.cyberduckprofile")
        );
        assertNotNull(profile);
        final Set<Location.Name> regions = profile.getRegions();
        assertEquals(2, regions.size());
        assertTrue("custom", regions.contains(new Location.Name("custom")));
        assertTrue("custom2", regions.contains(new Location.Name("custom2")));
    }

    @Test
    public void testEquals() throws Exception {
        final ProfilePlistReader reader = new ProfilePlistReader(new ProtocolFactory(Collections.singleton(new TestProtocol() {
            @Override
            public Type getType() {
                return Type.s3;
            }

            @Override
            public boolean isEnabled() {
                return false;
            }
        })));
        final Profile profile = reader.read(
            new Local("src/test/resources/Eucalyptus Walrus S3.cyberduckprofile")
        );
        assertNotNull(profile);
        assertEquals(profile, reader.read(
            new Local("src/test/resources/Eucalyptus Walrus S3.cyberduckprofile")
        ));
        assertEquals(Protocol.Type.s3, profile.getType());
        assertEquals(new TestProtocol(), profile.getProtocol());
        assertNotSame(new TestProtocol().getDefaultHostname(), profile.getDefaultHostname());
        assertEquals(new TestProtocol().getScheme(), profile.getScheme());
        assertEquals("eucalyptus", profile.getProvider());
    }

    @Test
    public void testEqualsDifferentScheme() throws Exception {
        final ProfilePlistReader reader = new ProfilePlistReader(new ProtocolFactory(Collections.singleton(new TestProtocol() {
            @Override
            public Type getType() {
                return Type.swift;
            }

            @Override
            public boolean isEnabled() {
                return false;
            }
        })));
        final Profile https = reader.read(
            new Local("src/test/resources/Openstack Swift (Swauth).cyberduckprofile")
        );
        assertNotNull(https);
        final Profile http = reader.read(
            new Local("src/test/resources/Openstack Swift (Swauth HTTP).cyberduckprofile")
        );
        assertNotNull(http);
        assertNotEquals(https, http);
    }

    @Test
    public void testEqualsContexts() throws Exception {
        final ProfilePlistReader reader = new ProfilePlistReader(new ProtocolFactory(Collections.singleton(new TestProtocol() {
            @Override
            public Type getType() {
                return Type.swift;
            }

            @Override
            public boolean isEnabled() {
                return false;
            }
        })));
        final Profile keystone = reader.read(
            new Local("src/test/resources/Openstack Swift (Keystone).cyberduckprofile")
        );
        assertNotNull(keystone);
        final Profile swauth = reader.read(
            new Local("src/test/resources/Openstack Swift (Swauth).cyberduckprofile")
        );
        assertNotNull(swauth);
        assertNotEquals(keystone, swauth);
    }

    @Test
    public void testProviderProfileS3HTTP() throws Exception {
        final ProfilePlistReader reader = new ProfilePlistReader(new ProtocolFactory(Collections.singleton(new TestProtocol() {
            @Override
            public Type getType() {
                return Type.s3;
            }

            @Override
            public boolean isEnabled() {
                return false;
            }
        })));
        final Profile profile = reader.read(
            new Local("src/test/resources/Test S3 (HTTP).cyberduckprofile")
        );
        assertNotNull(profile);
        assertEquals(profile, reader.read(
            new Local("src/test/resources/Test S3 (HTTP).cyberduckprofile")
        ));
        assertEquals(Protocol.Type.s3, profile.getType());
        assertEquals(new TestProtocol(), profile.getProtocol());
        assertTrue(profile.isHostnameConfigurable());
        assertTrue(profile.isPortConfigurable());
        assertEquals(Scheme.http, profile.getScheme());
        assertNotNull(profile.disk());
        assertEquals(profile.disk(), profile.disk());
        assertEquals(profile.icon(), profile.disk());
        assertEquals(80, profile.getDefaultPort());
        assertEquals(new TestProtocol().getDefaultHostname(), profile.getDefaultHostname());
        assertEquals(profile.disk(), new TestProtocol().disk());
        assertNotNull(profile.getProvider());
    }

    @Test
    public void testProviderProfileS3HTTPS() throws Exception {
        final ProfilePlistReader reader = new ProfilePlistReader(new ProtocolFactory(Collections.singleton(new TestProtocol() {
            @Override
            public Type getType() {
                return Type.s3;
            }

            @Override
            public boolean isEnabled() {
                return false;
            }
        })));
        final Profile profile = reader.read(
            new Local("src/test/resources/Test S3 (HTTPS).cyberduckprofile")
        );
        assertNotNull(profile);
        assertEquals(profile, reader.read(
            new Local("src/test/resources/Test S3 (HTTPS).cyberduckprofile")
        ));
        assertEquals(Protocol.Type.s3, profile.getType());
        assertEquals(new TestProtocol(), profile.getProtocol());
        assertTrue(profile.isHostnameConfigurable());
        assertTrue(profile.isPortConfigurable());
        assertEquals(Scheme.https, profile.getScheme());
        assertNotNull(profile.disk());
        assertEquals(profile.disk(), profile.disk());
        assertEquals(profile.icon(), profile.disk());
        assertEquals(443, profile.getDefaultPort());
        assertEquals(new TestProtocol().getDefaultHostname(), profile.getDefaultHostname());
        assertEquals(profile.disk(), new TestProtocol().disk());
        assertNotNull(profile.getProvider());
    }
}
