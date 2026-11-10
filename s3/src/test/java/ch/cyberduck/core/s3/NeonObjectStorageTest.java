package ch.cyberduck.core.s3;

/*
 * Copyright (c) 2002-2026 iterate GmbH. All rights reserved.
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

import ch.cyberduck.core.Host;
import ch.cyberduck.core.HostKeyCallback;
import ch.cyberduck.core.LoginCallback;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.Profile;
import ch.cyberduck.core.ProtocolFactory;
import ch.cyberduck.core.Scheme;
import ch.cyberduck.core.cdn.DistributionConfiguration;
import ch.cyberduck.core.features.AclPermission;
import ch.cyberduck.core.features.Copy;
import ch.cyberduck.core.features.Delete;
import ch.cyberduck.core.features.Directory;
import ch.cyberduck.core.features.Lifecycle;
import ch.cyberduck.core.features.Location;
import ch.cyberduck.core.features.Logging;
import ch.cyberduck.core.features.Move;
import ch.cyberduck.core.features.Read;
import ch.cyberduck.core.features.Redundancy;
import ch.cyberduck.core.features.Restore;
import ch.cyberduck.core.features.Scheduler;
import ch.cyberduck.core.features.Share;
import ch.cyberduck.core.features.TransferAcceleration;
import ch.cyberduck.core.features.Upload;
import ch.cyberduck.core.features.Versioning;
import ch.cyberduck.core.features.Write;
import ch.cyberduck.core.preferences.HostPreferencesFactory;
import ch.cyberduck.core.proxy.DisabledProxyFinder;
import ch.cyberduck.core.serializer.impl.dd.ProfilePlistReader;
import ch.cyberduck.core.ssl.DefaultX509KeyManager;
import ch.cyberduck.core.ssl.DisabledX509TrustManager;
import ch.cyberduck.core.threading.CancelCallback;

import org.junit.Test;

import java.util.Collections;
import java.util.EnumSet;
import java.util.HashSet;

import static org.junit.Assert.*;

public class NeonObjectStorageTest {

    private static final String ENDPOINT = "br-cool-darkness-a1b2c3d4.storage.c-1.us-east-2.aws.neon.tech";

    @Test
    public void testNeonHostnames() {
        assertTrue(S3Session.isNeonHostname(ENDPOINT));
        assertTrue(S3Session.isNeonHostname("br-winter-pond-aptw82ef.storage.c-2.us-east-2.aws.neon.tech"));
        assertTrue(S3Session.isNeonHostname("BR-Winter-Pond-aptw82ef.Storage.C-2.EU-Central-1.AWS.Neon.Tech"));
        assertTrue(S3Session.isNeonHostname("br-winter-pond-aptw82ef.storage.ap-southeast-1.aws.neon.tech"));
        assertFalse(S3Session.isNeonHostname(null));
        assertFalse(S3Session.isNeonHostname(""));
        assertFalse(S3Session.isNeonHostname("s3.amazonaws.com"));
        assertFalse(S3Session.isNeonHostname("ep-cool-darkness-a1b2c3d4.c-1.us-east-2.aws.neon.tech"));
        assertFalse(S3Session.isNeonHostname("console.neon.tech"));
        assertFalse(S3Session.isNeonHostname("br-cool-darkness-a1b2c3d4.storage.c-1.us-east-2.aws.neon.tech.evil.com"));
        assertFalse(S3Session.isAwsHostname(ENDPOINT));
    }

    @Test
    public void testFindRegionInNeonHostname() {
        assertEquals("us-east-2", S3Session.findRegionInNeonHostname(ENDPOINT));
        assertEquals("eu-central-1", S3Session.findRegionInNeonHostname("br-x.storage.c-3.EU-CENTRAL-1.aws.neon.tech"));
        assertEquals("ap-southeast-1", S3Session.findRegionInNeonHostname("br-x.storage.ap-southeast-1.aws.neon.tech"));
        assertNull(S3Session.findRegionInNeonHostname("s3.amazonaws.com"));
        assertNull(S3Session.findRegionInNeonHostname(null));
    }

    @Test
    public void testFeatures() {
        final S3Session session = new S3Session(new Host(new S3Protocol(), ENDPOINT));
        // Not implemented by Neon Object Storage
        assertNull(session.getFeature(AclPermission.class));
        assertNull(session.getFeature(Share.class));
        assertNull(session.getFeature(Logging.class));
        assertNull(session.getFeature(Lifecycle.class));
        assertNull(session.getFeature(Versioning.class));
        assertNull(session.getFeature(Redundancy.class));
        assertNull(session.getFeature(DistributionConfiguration.class));
        assertNull(session.getFeature(Scheduler.class));
        assertNull(session.getFeature(Restore.class));
        assertNull(session.getFeature(TransferAcceleration.class));
        // Core S3 operations
        assertNotNull(session.getFeature(Read.class));
        assertNotNull(session.getFeature(Write.class));
        assertNotNull(session.getFeature(Upload.class));
        assertNotNull(session.getFeature(Directory.class));
        assertNotNull(session.getFeature(Copy.class));
        assertNotNull(session.getFeature(Move.class));
        assertNotNull(session.getFeature(Delete.class));
        assertNotNull(session.getFeature(Location.class));
    }

    @Test
    public void testFeaturesOtherHostname() {
        final S3Session session = new S3Session(new Host(new S3Protocol(), "play.min.io"));
        assertNotNull(session.getFeature(AclPermission.class));
        assertNotNull(session.getFeature(Logging.class));
        assertNotNull(session.getFeature(Lifecycle.class));
    }

    @Test
    public void testLocation() throws Exception {
        final S3Session session = new S3Session(new Host(new S3Protocol(), ENDPOINT));
        final S3LocationFeature feature = new S3LocationFeature(session);
        final Path bucket = new Path("/b", EnumSet.of(Path.Type.directory, Path.Type.volume));
        assertEquals(new S3LocationFeature.S3Region("us-east-2"), feature.getDefault(bucket));
        assertEquals(Collections.singleton(new S3LocationFeature.S3Region("us-east-2")), feature.getLocations(bucket));
        // No request made to query bucket location
        assertEquals(new S3LocationFeature.S3Region("us-east-2"), feature.getLocation(bucket));
    }

    @Test
    public void testProfile() throws Exception {
        final ProtocolFactory factory = new ProtocolFactory(new HashSet<>(Collections.singleton(new S3Protocol())));
        final Profile profile = new ProfilePlistReader(factory).read(
                this.getClass().getResourceAsStream("/Neon Object Storage.cyberduckprofile"));
        assertNotNull(profile);
        assertTrue(profile.isBundled());
        assertEquals("neon", profile.getProvider());
        assertEquals(Scheme.https, profile.getScheme());
        assertTrue(profile.isHostnameConfigurable());
        assertFalse(profile.isPortConfigurable());
        assertFalse(profile.isAnonymousConfigurable());
        assertEquals("", profile.getDefaultHostname());
        final Host host = new Host(profile, ENDPOINT);
        assertTrue(HostPreferencesFactory.get(host).getBoolean("s3.bucket.virtualhost.disable"));
        assertFalse(HostPreferencesFactory.get(host).getBoolean("s3.versioning.enable"));
        assertEquals("STANDARD", HostPreferencesFactory.get(host).getProperty("s3.storage.class.options"));
        // Bucket name must not be parsed from branch endpoint
        assertNull(RequestEntityRestStorageService.findBucketInHostname(host));
    }

    @Test
    public void testForcePathStyleWithGenericProfile() throws Exception {
        final Host host = new Host(new S3Protocol(), ENDPOINT);
        assertFalse(HostPreferencesFactory.get(host).getBoolean("s3.bucket.virtualhost.disable"));
        final S3Session session = new S3Session(host, new DisabledX509TrustManager(), new DefaultX509KeyManager());
        session.connect(new DisabledProxyFinder(), HostKeyCallback.noop, LoginCallback.noop, CancelCallback.noop);
        assertTrue(HostPreferencesFactory.get(host).getBoolean("s3.bucket.virtualhost.disable"));
        session.close();
    }
}
