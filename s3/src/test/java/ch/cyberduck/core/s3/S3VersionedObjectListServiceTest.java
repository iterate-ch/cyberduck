package ch.cyberduck.core.s3;

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

import ch.cyberduck.core.AlphanumericRandomStringService;
import ch.cyberduck.core.AttributedList;
import ch.cyberduck.core.Credentials;
import ch.cyberduck.core.DisabledCancelCallback;
import ch.cyberduck.core.DisabledHostKeyCallback;
import ch.cyberduck.core.DisabledListProgressListener;
import ch.cyberduck.core.DisabledLoginCallback;
import ch.cyberduck.core.DisabledPasswordStore;
import ch.cyberduck.core.Host;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathAttributes;
import ch.cyberduck.core.features.Delete;
import ch.cyberduck.core.transfer.TransferStatus;
import ch.cyberduck.test.IntegrationTest;

import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.util.Collections;
import java.util.EnumSet;

import static org.junit.Assert.*;

@Category(IntegrationTest.class)
public class S3VersionedObjectListServiceTest {

    @Test
    public void testListVersioning() throws Exception {
        final S3Session session = new S3Session(
                new Host(new S3Protocol(), new S3Protocol().getDefaultHostname(),
                        new Credentials(
                                System.getProperties().getProperty("s3.key"), System.getProperties().getProperty("s3.secret")
                        )));
        session.open(new DisabledHostKeyCallback());
        session.login(new DisabledPasswordStore(), new DisabledLoginCallback(), new DisabledCancelCallback());
        final AttributedList<Path> list = new S3VersionedObjectListService(session).list(new Path("versioning-test-us-east-1-cyberduck",
                        EnumSet.of(Path.Type.directory, Path.Type.volume)),
                new DisabledListProgressListener());
        final PathAttributes att = new PathAttributes();
        assertFalse(list.contains(new Path("/versioning-test-us-east-1-cyberduck/test", EnumSet.of(Path.Type.file), att)));
        att.setVersionId("VLphaWnNt9MNseMuYVsLSmCFe6EuJJAq");
        assertTrue(list.contains(new Path("/versioning-test-us-east-1-cyberduck/test", EnumSet.of(Path.Type.file), att)));
        session.close();
    }

    @Test
    public void testDirectory() throws Exception {
        final S3Session session = new S3Session(
                new Host(new S3Protocol(), new S3Protocol().getDefaultHostname(),
                        new Credentials(
                                System.getProperties().getProperty("s3.key"), System.getProperties().getProperty("s3.secret")
                        )));
        session.open(new DisabledHostKeyCallback());
        session.login(new DisabledPasswordStore(), new DisabledLoginCallback(), new DisabledCancelCallback());
        final Path bucket = new Path("versioning-test-us-east-1-cyberduck", EnumSet.of(Path.Type.directory, Path.Type.volume));
        final Path directory = new S3DirectoryFeature(session, new S3WriteFeature(session)).mkdir(new Path(bucket, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory)), null, new TransferStatus());
        assertTrue(new S3VersionedObjectListService(session).list(bucket, new DisabledListProgressListener()).contains(directory));
        new S3DefaultDeleteFeature(session).delete(Collections.singletonList(directory), new DisabledLoginCallback(), new Delete.DisabledCallback());
        assertFalse(new S3VersionedObjectListService(session).list(bucket, new DisabledListProgressListener()).contains(directory));
        session.close();
    }

    @Test
    public void testList() throws Exception {
        final S3Session session = new S3Session(
                new Host(new S3Protocol(), new S3Protocol().getDefaultHostname(),
                        new Credentials(
                                System.getProperties().getProperty("s3.key"), System.getProperties().getProperty("s3.secret")
                        )));
        session.open(new DisabledHostKeyCallback());
        session.login(new DisabledPasswordStore(), new DisabledLoginCallback(), new DisabledCancelCallback());
        final Path container = new Path("test-us-east-1-cyberduck", EnumSet.of(Path.Type.directory, Path.Type.volume));
        final AttributedList<Path> list = new S3VersionedObjectListService(session).list(
                new Path("test-us-east-1-cyberduck", EnumSet.of(Path.Type.directory, Path.Type.volume)), new DisabledListProgressListener());
        for(Path p : list) {
            assertEquals(container, p.getParent());
            if(p.isFile()) {
                assertNotEquals(-1L, p.attributes().getModificationDate());
                assertNotEquals(-1L, p.attributes().getSize());
                assertNotNull(p.attributes().getETag());
                assertNotNull(p.attributes().getStorageClass());
                assertNull(p.attributes().getVersionId());
            }
        }
        session.close();
    }
}
