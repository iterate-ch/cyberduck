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
import ch.cyberduck.core.exception.NotfoundException;
import ch.cyberduck.core.logging.LoggingConfiguration;
import ch.cyberduck.test.IntegrationTest;

import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.util.EnumSet;
import java.util.UUID;

import static org.junit.Assert.*;

@Category(IntegrationTest.class)
public class S3LoggingFeatureTest {

    @Test
    public void testGetConfiguration() throws Exception {
        final S3Session session = new S3Session(
                new Host(new S3Protocol(), new S3Protocol().getDefaultHostname(),
                        new Credentials(
                                System.getProperties().getProperty("s3.key"), System.getProperties().getProperty("s3.secret")
                        )));
        assertNotNull(session.open(new DisabledHostKeyCallback(), new DisabledTranscriptListener()));
        session.login(new DisabledPasswordStore(), new DisabledLoginCallback(), new DisabledCancelCallback(), PathCache.empty());
        final LoggingConfiguration configuration = new S3LoggingFeature(session).getConfiguration(
                new Path("test-us-east-1-cyberduck", EnumSet.of(Path.Type.directory))
        );
        assertNotNull(configuration);
        assertEquals("test-logging-us-east-1-cyberduck", configuration.getLoggingTarget());
        assertTrue(configuration.isEnabled());
        session.close();
    }

    @Test(expected = NotfoundException.class)
    public void testReadNotFound() throws Exception {
        final S3Session session = new S3Session(
                new Host(new S3Protocol(), new S3Protocol().getDefaultHostname(),
                        new Credentials(
                                System.getProperties().getProperty("s3.key"), System.getProperties().getProperty("s3.secret")
                        )));
        assertNotNull(session.open(new DisabledHostKeyCallback(), new DisabledTranscriptListener()));
        session.login(new DisabledPasswordStore(), new DisabledLoginCallback(), new DisabledCancelCallback(), PathCache.empty());
        new S3LoggingFeature(session).getConfiguration(
                new Path(UUID.randomUUID().toString(), EnumSet.of(Path.Type.directory))
        );
    }

    @Test(expected = NotfoundException.class)
    public void testWriteNotFound() throws Exception {
        final S3Session session = new S3Session(
                new Host(new S3Protocol(), new S3Protocol().getDefaultHostname(),
                        new Credentials(
                                System.getProperties().getProperty("s3.key"), System.getProperties().getProperty("s3.secret")
                        )));
        assertNotNull(session.open(new DisabledHostKeyCallback(), new DisabledTranscriptListener()));
        session.login(new DisabledPasswordStore(), new DisabledLoginCallback(), new DisabledCancelCallback(), PathCache.empty());
        new S3LoggingFeature(session).setConfiguration(
                new Path(UUID.randomUUID().toString(), EnumSet.of(Path.Type.directory)), new LoggingConfiguration(false)
        );
    }
}
