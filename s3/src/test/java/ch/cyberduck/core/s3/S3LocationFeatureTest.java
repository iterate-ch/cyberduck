package ch.cyberduck.core.s3;

/*
 * Copyright (c) 2002-2013 David Kocher. All rights reserved.
 * http://cyberduck.ch/
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
 * Bug fixes, suggestions and comments should be sent to feedback@cyberduck.ch
 */

import ch.cyberduck.core.Credentials;
import ch.cyberduck.core.DisabledCancelCallback;
import ch.cyberduck.core.DisabledHostKeyCallback;
import ch.cyberduck.core.DisabledLoginCallback;
import ch.cyberduck.core.DisabledPasswordStore;
import ch.cyberduck.core.Host;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathCache;
import ch.cyberduck.core.preferences.PreferencesFactory;
import ch.cyberduck.test.IntegrationTest;

import org.jets3t.service.Jets3tProperties;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.util.EnumSet;

import static ch.cyberduck.core.features.Location.unknown;
import static org.junit.Assert.*;

@Category(IntegrationTest.class)
public class S3LocationFeatureTest {

    @Test
    public void testGetLocation() throws Exception {
        final S3Session session = new S3Session(
                new Host(new S3Protocol(), new S3Protocol().getDefaultHostname(),
                        new Credentials(
                                System.getProperties().getProperty("s3.key"), System.getProperties().getProperty("s3.secret")
                        )));
        assertNotNull(session.open(new DisabledHostKeyCallback()));
        session.login(new DisabledPasswordStore(), new DisabledLoginCallback(), new DisabledCancelCallback(), PathCache.empty());
        final S3LocationFeature feature = new S3LocationFeature(session);
        assertEquals(new S3LocationFeature.S3Region("eu-west-1"), feature.getLocation(
                new Path("test-eu-west-1-cyberduck", EnumSet.of(Path.Type.volume, Path.Type.directory))
        ));
        assertEquals(new S3LocationFeature.S3Region("eu-central-1"), feature.getLocation(
                new Path("test-eu-central-1-cyberduck", EnumSet.of(Path.Type.volume, Path.Type.directory))
        ));
        assertEquals(new S3LocationFeature.S3Region("us-east-1"), feature.getLocation(
                new Path("test-us-east-1-cyberduck", EnumSet.of(Path.Type.volume, Path.Type.directory))
        ));
        assertEquals(unknown, feature.getLocation(
                new Path("/", EnumSet.of(Path.Type.volume, Path.Type.directory))
        ));
        session.close();
    }

    @Test
    public void testForbidden() throws Exception {
        final Host host = new Host(new S3Protocol(), "dist.springframework.org.s3.amazonaws.com", new Credentials(
                PreferencesFactory.get().getProperty("connection.login.anon.name"), null
        ));
        final S3Session session = new S3Session(host);
        session.open(new DisabledHostKeyCallback());
        assertEquals(unknown,
                new S3LocationFeature(session).getLocation(new Path("/dist.springframework.org", EnumSet.of(Path.Type.directory))));
        session.close();
    }

    @Test
    public void testGetLocationAWS4SignatureFrankfurt() throws Exception {
        final S3Session session = new S3Session(
                new Host(new S3Protocol(), new S3Protocol().getDefaultHostname(),
                        new Credentials(
                                System.getProperties().getProperty("s3.key"), System.getProperties().getProperty("s3.secret")
                        ))) {
            @Override
            public S3Protocol.AuthenticationHeaderSignatureVersion getSignatureVersion() {
                return S3Protocol.AuthenticationHeaderSignatureVersion.AWS4HMACSHA256;
            }
        };
        assertNotNull(session.open(new DisabledHostKeyCallback()));
        session.login(new DisabledPasswordStore(), new DisabledLoginCallback(), new DisabledCancelCallback(), PathCache.empty());
        assertEquals(new S3LocationFeature.S3Region("eu-central-1"), new S3LocationFeature(session).getLocation(
                new Path("test-eu-central-1-cyberduck", EnumSet.of(Path.Type.directory))
        ));
        session.close();
    }

    @Test
    public void testAccessPathStyleBucketEuCentral() throws Exception {
        final S3Session session = new S3Session(
                new Host(new S3Protocol(), new S3Protocol().getDefaultHostname(),
                        new Credentials(
                                System.getProperties().getProperty("s3.key"), System.getProperties().getProperty("s3.secret")
                        ))) {
            @Override
            public S3Protocol.AuthenticationHeaderSignatureVersion getSignatureVersion() {
                return S3Protocol.AuthenticationHeaderSignatureVersion.AWS4HMACSHA256;
            }

            @Override
            protected Jets3tProperties configure() {
                final Jets3tProperties properties = super.configure();
                properties.setProperty("s3service.disable-dns-buckets", String.valueOf(true));
                return properties;
            }
        };
        assertNotNull(session.open(new DisabledHostKeyCallback()));
        session.login(new DisabledPasswordStore(), new DisabledLoginCallback(), new DisabledCancelCallback(), PathCache.empty());
        assertEquals(new S3LocationFeature.S3Region("eu-central-1"), new S3LocationFeature(session).getLocation(
                new Path("test-eu-central-1-cyberduck", EnumSet.of(Path.Type.directory))
        ));
        session.close();
    }

    @Test
    public void testEquals() throws Exception {
        assertEquals(unknown, new S3LocationFeature.S3Region(null));
    }

    @Test
    public void testEmptyThirdPartyProvider() throws Exception {
        final Host host = new Host(new S3Protocol(), "mys3", new Credentials(
                PreferencesFactory.get().getProperty("connection.login.anon.name"), null
        ));
        final S3Session session = new S3Session(host);
        assertTrue(new S3LocationFeature(session).getLocations().isEmpty());
    }
}
