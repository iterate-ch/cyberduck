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
import ch.cyberduck.core.DisabledListProgressListener;
import ch.cyberduck.core.DisabledLoginController;
import ch.cyberduck.core.DisabledPasswordStore;
import ch.cyberduck.core.DisabledProgressListener;
import ch.cyberduck.core.DisabledTranscriptListener;
import ch.cyberduck.core.Host;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.exception.NotfoundException;

import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.UUID;

import static org.junit.Assert.assertEquals;


/**
 * @version $Id$
 */
public class S3StorageClassFeatureTest extends AbstractTestCase {

    @Test
    public void testGetClasses() throws Exception {
        assertEquals(Arrays.asList("STANDARD", "REDUCED_REDUNDANCY", "GLACIER"),
                new S3StorageClassFeature(null).getClasses());
    }

    @Test(expected = NotfoundException.class)
    public void testNotFound() throws Exception {
        final S3Session session = new S3Session(
                new Host(new S3Protocol(), new S3Protocol().getDefaultHostname(),
                        new Credentials(
                                properties.getProperty("s3.key"), properties.getProperty("s3.secret")
                        )));
        session.open(new DisabledHostKeyCallback(), session);
        session.login(new DisabledPasswordStore(), new DisabledLoginController(), new DisabledCancelCallback(), new DisabledTranscriptListener());
        final Path container = new Path("test.cyberduck.ch", EnumSet.of(Path.Type.volume));
        final Path test = new Path(container, UUID.randomUUID().toString(), EnumSet.of(Path.Type.file));
        final S3StorageClassFeature feature = new S3StorageClassFeature(session);
        feature.getClass(test);
    }

    @Test
    public void testSetClass() throws Exception {
        final S3Session session = new S3Session(
                new Host(new S3Protocol(), new S3Protocol().getDefaultHostname(),
                        new Credentials(
                                properties.getProperty("s3.key"), properties.getProperty("s3.secret")
                        )));
        session.open(new DisabledHostKeyCallback(), session);
        session.login(new DisabledPasswordStore(), new DisabledLoginController(), new DisabledCancelCallback(), new DisabledTranscriptListener());
        final Path container = new Path("test.cyberduck.ch", EnumSet.of(Path.Type.volume));
        final Path test = new Path(container, UUID.randomUUID().toString(), EnumSet.of(Path.Type.file));
        new S3TouchFeature(session).touch(test);
        final S3StorageClassFeature feature = new S3StorageClassFeature(session);
        assertEquals("STANDARD", feature.getClass(test));
        feature.setClass(test, "REDUCED_REDUNDANCY");
        assertEquals("REDUCED_REDUNDANCY", feature.getClass(test));
        assertEquals("REDUCED_REDUNDANCY", session.list(container,
                new DisabledListProgressListener()).get(test.getReference()).attributes().getStorageClass());
        new S3DefaultDeleteFeature(session).delete(Collections.<Path>singletonList(test), new DisabledLoginController(), new DisabledProgressListener());
        session.close();
    }
}
