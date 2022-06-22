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

import ch.cyberduck.core.AsciiRandomStringService;
import ch.cyberduck.core.Credentials;
import ch.cyberduck.core.DisabledCancelCallback;
import ch.cyberduck.core.DisabledHostKeyCallback;
import ch.cyberduck.core.DisabledLoginCallback;
import ch.cyberduck.core.DisabledPasswordStore;
import ch.cyberduck.core.DisabledProgressListener;
import ch.cyberduck.core.Host;
import ch.cyberduck.core.Local;
import ch.cyberduck.core.LoginConnectionService;
import ch.cyberduck.core.LoginOptions;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.Profile;
import ch.cyberduck.core.ProtocolFactory;
import ch.cyberduck.core.exception.NotfoundException;
import ch.cyberduck.core.features.Location;
import ch.cyberduck.core.preferences.PreferencesFactory;
import ch.cyberduck.core.proxy.Proxy;
import ch.cyberduck.core.serializer.impl.dd.ProfilePlistReader;
import ch.cyberduck.test.IntegrationTest;

import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.util.Collections;
import java.util.EnumSet;
import java.util.HashSet;

import static ch.cyberduck.core.features.Location.unknown;
import static org.junit.Assert.*;

@Category(IntegrationTest.class)
public class S3LocationFeatureTest extends AbstractS3Test {

    @Test(expected = NotfoundException.class)
    public void testNotfound() throws Exception {
        final S3LocationFeature feature = new S3LocationFeature(session, session.getClient().getRegionEndpointCache());
        feature.getLocation(
            new Path(new AsciiRandomStringService().random(), EnumSet.of(Path.Type.volume, Path.Type.directory))
        );
    }

    @Test
    public void testGetLocation() throws Exception {
        final S3LocationFeature feature = new S3LocationFeature(session, session.getClient().getRegionEndpointCache());
        assertEquals(new S3LocationFeature.S3Region("eu-west-1"), feature.getLocation(
            new Path("test-eu-west-1-cyberduck", EnumSet.of(Path.Type.volume, Path.Type.directory))
        ));
        assertEquals(new S3LocationFeature.S3Region("eu-central-1"), feature.getLocation(
            new Path("test-eu-central-1-cyberduck", EnumSet.of(Path.Type.volume, Path.Type.directory))
        ));
        assertEquals(new S3LocationFeature.S3Region("us-east-1"), feature.getLocation(
            new Path("test-us-east-1-cyberduck", EnumSet.of(Path.Type.volume, Path.Type.directory))
        ));
        assertEquals(new S3LocationFeature.S3Region("us-east-1"), feature.getLocation(
                new Path("/", EnumSet.of(Path.Type.volume, Path.Type.directory))
        ));
    }

    @Test
    public void testForbidden() throws Exception {
        final Host host = new Host(new S3Protocol(), "dist.springframework.org.s3.amazonaws.com", new Credentials(
            PreferencesFactory.get().getProperty("connection.login.anon.name"), null
        ));
        final S3Session session = new S3Session(host);
        final LoginConnectionService login = new LoginConnectionService(new DisabledLoginCallback() {
            @Override
            public Credentials prompt(final Host bookmark, final String username, final String title, final String reason, final LoginOptions options) {
                fail(reason);
                return null;
            }
        }, new DisabledHostKeyCallback(),
            new DisabledPasswordStore(), new DisabledProgressListener());
        login.check(session, new DisabledCancelCallback());
        assertEquals(unknown, new S3LocationFeature(session, session.getClient().getRegionEndpointCache()).getLocation(new Path("/", EnumSet.of(Path.Type.directory))));
        session.close();
    }

    @Test
    public void testGetLocationAWS4SignatureFrankfurt() throws Exception {
        assertEquals(new S3LocationFeature.S3Region("eu-central-1"), new S3LocationFeature(session, session.getClient().getRegionEndpointCache()).getLocation(
                new Path("test-eu-central-1-cyberduck", EnumSet.of(Path.Type.directory))
        ));
    }

    @Test
    public void testAccessPathStyleBucketEuCentral() throws Exception {
        final Host host = new Host(new S3Protocol(), new S3Protocol().getDefaultHostname(), new Credentials(
                System.getProperties().getProperty("s3.key"), System.getProperties().getProperty("s3.secret")
        ));
        host.setProperty("s3.bucket.virtualhost.disable", String.valueOf(true));
        final S3Session session = new S3Session(host);
        assertNotNull(session.open(Proxy.DIRECT, new DisabledHostKeyCallback(), new DisabledLoginCallback(), new DisabledCancelCallback()));
        session.login(Proxy.DIRECT, new DisabledLoginCallback(), new DisabledCancelCallback());
        assertEquals(new S3LocationFeature.S3Region("eu-central-1"), new S3LocationFeature(session, session.getClient().getRegionEndpointCache()).getLocation(
                new Path("test-eu-central-1-cyberduck", EnumSet.of(Path.Type.directory))
        ));
        session.close();
    }

    @Test
    public void testAccessBucketNameInHostname() throws Exception {
        assertEquals(new S3LocationFeature.S3Region("eu-west-3"), new S3LocationFeature(session, session.getClient().getRegionEndpointCache()).getLocation(
                new Path("/", EnumSet.of(Path.Type.directory))
        ));
    }

    @Test
    public void testEquals() {
        assertEquals(unknown, new S3LocationFeature.S3Region(null));
    }

    @Test
    public void testEmptyThirdPartyProvider() {
        final Host host = new Host(new S3Protocol(), "mys3");
        final S3Session session = new S3Session(host);
        assertTrue(new S3LocationFeature(session, session.getClient().getRegionEndpointCache()).getLocations().isEmpty());
    }

    @Test
    public void testNonEmptyProfile() throws Exception {
        final ProtocolFactory factory = new ProtocolFactory(new HashSet<>(Collections.singleton(new S3Protocol())));
        final Profile profile = new ProfilePlistReader(factory).read(
                new Local("../profiles/Wasabi (us-central-1).cyberduckprofile"));
        final S3Session session = new S3Session(new Host(profile, profile.getDefaultHostname()));
        final S3LocationFeature feature = new S3LocationFeature(session, session.getClient().getRegionEndpointCache());
        assertFalse(feature.getLocations().isEmpty());
        assertTrue(feature.getLocations().contains(new Location.Name("us-central-1")));
    }

    @Test
    public void testVirtualhost() {
        assertTrue(new S3LocationFeature(virtualhost, virtualhost.getClient().getRegionEndpointCache()).getLocations().isEmpty());
    }
}
