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
import ch.cyberduck.core.DisabledHostKeyCallback;
import ch.cyberduck.core.DisabledLoginCallback;
import ch.cyberduck.core.DisabledTranscriptListener;
import ch.cyberduck.core.Host;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.features.Delete;
import ch.cyberduck.test.IntegrationTest;

import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.util.Collections;
import java.util.EnumSet;
import java.util.UUID;

import static org.junit.Assert.assertTrue;

@Category(IntegrationTest.class)
public class S3CopyFeatureTest {

    @Test
    public void testCopyZeroLength() throws Exception {
        final Host host = new Host(new S3Protocol(), new S3Protocol().getDefaultHostname(),
                new Credentials(System.getProperties().getProperty("s3.key"), System.getProperties().getProperty("s3.secret"))
        );
        final S3Session session = new S3Session(host);
        session.open(new DisabledHostKeyCallback(), new DisabledTranscriptListener());
        final Path container = new Path("test-us-east-1-cyberduck", EnumSet.of(Path.Type.directory, Path.Type.volume));
        final Path test = new Path(container, UUID.randomUUID().toString(), EnumSet.of(Path.Type.file));
        test.attributes().setSize(0L);
        new S3TouchFeature(session).touch(test);
        final Path copy = new Path(container, UUID.randomUUID().toString(), EnumSet.of(Path.Type.file));
        new S3CopyFeature(session).copy(test, copy);
        assertTrue(new S3FindFeature(session).find(test));
        new S3DefaultDeleteFeature(session).delete(Collections.singletonList(test), new DisabledLoginCallback(), new Delete.DisabledCallback());
        assertTrue(new S3FindFeature(session).find(copy));
        new S3DefaultDeleteFeature(session).delete(Collections.singletonList(copy), new DisabledLoginCallback(), new Delete.DisabledCallback());
        session.close();
    }

    @Test
    public void testCopy() throws Exception {
        final Host host = new Host(new S3Protocol(), new S3Protocol().getDefaultHostname(),
                new Credentials(System.getProperties().getProperty("s3.key"), System.getProperties().getProperty("s3.secret"))
        );
        final S3Session session = new S3Session(host);
        session.open(new DisabledHostKeyCallback(), new DisabledTranscriptListener());
        final Path container = new Path("test-us-east-1-cyberduck", EnumSet.of(Path.Type.directory, Path.Type.volume));
        final Path test = new Path(container, UUID.randomUUID().toString(), EnumSet.of(Path.Type.file));
        new S3TouchFeature(session).touch(test);
        final Path copy = new Path(container, UUID.randomUUID().toString(), EnumSet.of(Path.Type.file));
        new S3CopyFeature(session).copy(test, copy);
        assertTrue(new S3FindFeature(session).find(test));
        new S3DefaultDeleteFeature(session).delete(Collections.singletonList(test), new DisabledLoginCallback(), new Delete.DisabledCallback());
        assertTrue(new S3FindFeature(session).find(copy));
        new S3DefaultDeleteFeature(session).delete(Collections.singletonList(copy), new DisabledLoginCallback(), new Delete.DisabledCallback());
        session.close();
    }
}
