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
import ch.cyberduck.core.DisabledTranscriptListener;
import ch.cyberduck.core.Host;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathCache;
import ch.cyberduck.core.VersioningConfiguration;
import ch.cyberduck.core.features.Delete;
import ch.cyberduck.core.features.Versioning;
import ch.cyberduck.core.preferences.PreferencesFactory;
import ch.cyberduck.test.IntegrationTest;

import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.util.Collections;
import java.util.EnumSet;
import java.util.UUID;

import static org.junit.Assert.*;

@Category(IntegrationTest.class)
public class S3VersioningFeatureTest {

    @Test
    public void testGetConfigurationDisabled() throws Exception {
        final S3Session session = new S3Session(
                new Host(new S3Protocol(), new S3Protocol().getDefaultHostname(),
                        new Credentials(
                                System.getProperties().getProperty("s3.key"), System.getProperties().getProperty("s3.secret")
                        )));
        session.open(new DisabledHostKeyCallback(), new DisabledTranscriptListener());
        session.login(new DisabledPasswordStore(), new DisabledLoginCallback(), new DisabledCancelCallback(), PathCache.empty());
        final VersioningConfiguration configuration
                = new S3VersioningFeature(session).getConfiguration(new Path("test-us-east-1-cyberduck", EnumSet.of(Path.Type.directory, Path.Type.volume)));
        assertNotNull(configuration);
        assertFalse(configuration.isEnabled());
        assertFalse(configuration.isMultifactor());
        session.close();
    }

    @Test
    public void testGetConfigurationEnabled() throws Exception {
        final S3Session session = new S3Session(
                new Host(new S3Protocol(), new S3Protocol().getDefaultHostname(),
                        new Credentials(
                                System.getProperties().getProperty("s3.key"), System.getProperties().getProperty("s3.secret")
                        )));
        session.open(new DisabledHostKeyCallback(), new DisabledTranscriptListener());
        session.login(new DisabledPasswordStore(), new DisabledLoginCallback(), new DisabledCancelCallback(), PathCache.empty());
        final VersioningConfiguration configuration
                = new S3VersioningFeature(session).getConfiguration(new Path("versioning-test-us-east-1-cyberduck", EnumSet.of(Path.Type.directory, Path.Type.volume)));
        assertNotNull(configuration);
        assertTrue(configuration.isEnabled());
        session.close();
    }

    @Test
    public void testSetConfiguration() throws Exception {
        final S3Session session = new S3Session(
                new Host(new S3Protocol(), new S3Protocol().getDefaultHostname(),
                        new Credentials(
                                System.getProperties().getProperty("s3.key"), System.getProperties().getProperty("s3.secret")
                        )));
        session.open(new DisabledHostKeyCallback(), new DisabledTranscriptListener());
        session.login(new DisabledPasswordStore(), new DisabledLoginCallback(), new DisabledCancelCallback(), PathCache.empty());
        final Path container = new Path(UUID.randomUUID().toString(), EnumSet.of(Path.Type.directory, Path.Type.volume));
        new S3DirectoryFeature(session).mkdir(container);
        final Versioning feature = new S3VersioningFeature(session);
        feature.setConfiguration(container, new DisabledLoginCallback(), new VersioningConfiguration(true, false));
        assertTrue(feature.getConfiguration(container).isEnabled());
        new S3DefaultDeleteFeature(session).delete(Collections.<Path>singletonList(container), new DisabledLoginCallback(), new Delete.DisabledCallback());
        session.close();
    }

    @Test
    public void testForbidden() throws Exception {
        final Host host = new Host(new S3Protocol(), "dist.springframework.org.s3.amazonaws.com", new Credentials(
                PreferencesFactory.get().getProperty("connection.login.anon.name"), null
        ));
        final S3Session session = new S3Session(host);
        session.open(new DisabledHostKeyCallback(), new DisabledTranscriptListener());
        assertEquals(VersioningConfiguration.empty(),
                new S3VersioningFeature(session).getConfiguration(new Path("/dist.springframework.org", EnumSet.of(Path.Type.directory))));
        session.close();
    }
}
