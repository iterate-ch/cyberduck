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

import ch.cyberduck.core.AbstractTestCase;
import ch.cyberduck.core.Credentials;
import ch.cyberduck.core.DisabledCancelCallback;
import ch.cyberduck.core.DisabledHostKeyCallback;
import ch.cyberduck.core.DisabledLoginCallback;
import ch.cyberduck.core.DisabledPasswordStore;
import ch.cyberduck.core.DisabledProgressListener;
import ch.cyberduck.core.DisabledTranscriptListener;
import ch.cyberduck.core.Host;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.Preferences;
import ch.cyberduck.core.Protocol;
import ch.cyberduck.core.ProtocolFactory;
import ch.cyberduck.core.features.Versioning;

import org.junit.Test;

import java.util.Collections;
import java.util.EnumSet;
import java.util.UUID;

import static org.junit.Assert.*;

/**
 * @version $Id$
 */
public class S3VersioningFeatureTest extends AbstractTestCase {

    @Test
    public void testGetConfigurationDisabled() throws Exception {
        final S3Session session = new S3Session(
                new Host(ProtocolFactory.forName(Protocol.Type.s3.name()), ProtocolFactory.forName(Protocol.Type.s3.name()).getDefaultHostname(),
                        new Credentials(
                                properties.getProperty("s3.key"), properties.getProperty("s3.secret")
                        )));
        session.open(new DisabledHostKeyCallback(), new DisabledTranscriptListener());
        session.login(new DisabledPasswordStore(), new DisabledLoginCallback(), new DisabledCancelCallback());
        final VersioningConfiguration configuration
                = new S3VersioningFeature(session).getConfiguration(new Path("test.cyberduck.ch", EnumSet.of(Path.Type.directory, Path.Type.volume)));
        assertNotNull(configuration);
        assertFalse(configuration.isEnabled());
        assertFalse(configuration.isMultifactor());
        session.close();
    }

    @Test
    public void testGetConfigurationEnabled() throws Exception {
        final S3Session session = new S3Session(
                new Host(ProtocolFactory.forName(Protocol.Type.s3.name()), ProtocolFactory.forName(Protocol.Type.s3.name()).getDefaultHostname(),
                        new Credentials(
                                properties.getProperty("s3.key"), properties.getProperty("s3.secret")
                        )));
        session.open(new DisabledHostKeyCallback(), new DisabledTranscriptListener());
        session.login(new DisabledPasswordStore(), new DisabledLoginCallback(), new DisabledCancelCallback());
        final VersioningConfiguration configuration
                = new S3VersioningFeature(session).getConfiguration(new Path("versioning.test.cyberduck.ch", EnumSet.of(Path.Type.directory, Path.Type.volume)));
        assertNotNull(configuration);
        assertTrue(configuration.isEnabled());
        session.close();
    }

    @Test
    public void testSetConfiguration() throws Exception {
        final S3Session session = new S3Session(
                new Host(ProtocolFactory.forName(Protocol.Type.s3.name()), ProtocolFactory.forName(Protocol.Type.s3.name()).getDefaultHostname(),
                        new Credentials(
                                properties.getProperty("s3.key"), properties.getProperty("s3.secret")
                        )));
        session.open(new DisabledHostKeyCallback(), new DisabledTranscriptListener());
        session.login(new DisabledPasswordStore(), new DisabledLoginCallback(), new DisabledCancelCallback());
        final Path container = new Path(UUID.randomUUID().toString(), EnumSet.of(Path.Type.directory, Path.Type.volume));
        new S3DirectoryFeature(session).mkdir(container, null);
        final Versioning feature = new S3VersioningFeature(session);
        feature.setConfiguration(container, new DisabledLoginCallback(), new VersioningConfiguration(true, false));
        assertTrue(feature.getConfiguration(container).isEnabled());
        new S3DefaultDeleteFeature(session).delete(Collections.<Path>singletonList(container), new DisabledLoginCallback(), new DisabledProgressListener());
        session.close();
    }

    @Test
    public void testForbidden() throws Exception {
        final Host host = new Host(new S3Protocol(), "dist.springframework.org.s3.amazonaws.com", new Credentials(
                Preferences.instance().getProperty("connection.login.anon.name"), null
        ));
        final S3Session session = new S3Session(host);
        session.open(new DisabledHostKeyCallback(), new DisabledTranscriptListener());
        assertEquals(VersioningConfiguration.empty(),
                new S3VersioningFeature(session).getConfiguration(new Path("/dist.springframework.org", EnumSet.of(Path.Type.directory))));
        session.close();
    }
}
