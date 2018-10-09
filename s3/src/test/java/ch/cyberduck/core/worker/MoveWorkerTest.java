package ch.cyberduck.core.worker;

/*
 * Copyright (c) 2002-2017 iterate GmbH. All rights reserved.
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

import ch.cyberduck.core.*;
import ch.cyberduck.core.features.Delete;
import ch.cyberduck.core.pool.SessionPool;
import ch.cyberduck.core.s3.S3AccessControlListFeature;
import ch.cyberduck.core.s3.S3DefaultDeleteFeature;
import ch.cyberduck.core.s3.S3DirectoryFeature;
import ch.cyberduck.core.s3.S3FindFeature;
import ch.cyberduck.core.s3.S3MetadataFeature;
import ch.cyberduck.core.s3.S3Protocol;
import ch.cyberduck.core.s3.S3Session;
import ch.cyberduck.core.s3.S3TouchFeature;
import ch.cyberduck.core.s3.S3VersionedObjectListService;
import ch.cyberduck.core.s3.S3WriteFeature;
import ch.cyberduck.core.transfer.TransferStatus;
import ch.cyberduck.test.IntegrationTest;

import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.*;

@Category(IntegrationTest.class)
public class MoveWorkerTest {

    @Test
    public void testMoveFile() throws Exception {
        final Host host = new Host(new S3Protocol(), new S3Protocol().getDefaultHostname(), new Credentials(
            System.getProperties().getProperty("s3.key"), System.getProperties().getProperty("s3.secret")
        ));
        final S3Session session = new S3Session(host);
        session.open(new DisabledHostKeyCallback(), new DisabledLoginCallback());
        session.login(new DisabledPasswordStore(), new DisabledLoginCallback(), new DisabledCancelCallback());
        final Path home = new Path("test-us-east-1-cyberduck", EnumSet.of(Path.Type.directory, Path.Type.volume));
        final Path source = new Path(home, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file));
        final Path target = new Path(home, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file));
        new S3TouchFeature(session).touch(source, new TransferStatus().withMime("application/cyberduck"));
        new S3AccessControlListFeature(session).setPermission(source, new Acl(
            new Acl.UserAndRole(
                new Acl.CanonicalUser("80b9982b7b08045ee86680cc47f43c84bf439494a89ece22b5330f8a49477cf6"), new Acl.Role(Acl.Role.FULL)
            ),
            new Acl.UserAndRole(
                new Acl.GroupUser("http://acs.amazonaws.com/groups/global/AllUsers"), new Acl.Role(Acl.Role.READ)
            )
        ));
        assertTrue(new S3FindFeature(session).find(source, new DisabledListProgressListener()));
        final MoveWorker worker = new MoveWorker(Collections.singletonMap(source, target), new SessionPool.SingleSessionPool(session), PathCache.empty(), new DisabledProgressListener(), new DisabledLoginCallback());
        worker.run(session);
        assertFalse(new S3FindFeature(session).find(source, new DisabledListProgressListener()));
        assertTrue(new S3FindFeature(session).find(target, new DisabledListProgressListener()));
        assertEquals("application/cyberduck",
            new S3MetadataFeature(session, new S3AccessControlListFeature(session)).getMetadata(target).get("Content-Type"));
        assertTrue(new S3AccessControlListFeature(session).getPermission(target).asList().contains(
            new Acl.UserAndRole(
                new Acl.CanonicalUser("80b9982b7b08045ee86680cc47f43c84bf439494a89ece22b5330f8a49477cf6"), new Acl.Role(Acl.Role.FULL)
            )
        ));
        assertTrue(new S3AccessControlListFeature(session).getPermission(target).asList().contains(
            new Acl.UserAndRole(
                new Acl.GroupUser("http://acs.amazonaws.com/groups/global/AllUsers"), new Acl.Role(Acl.Role.READ)
            )
        ));
        new DeleteWorker(new DisabledLoginCallback(), Collections.singletonList(target), new DisabledProgressListener()).run(session);
        session.close();
    }

    @Test
    public void testMoveVersionedDirectory() throws Exception {
        final Host host = new Host(new S3Protocol(), new S3Protocol().getDefaultHostname(), new Credentials(
            System.getProperties().getProperty("s3.key"), System.getProperties().getProperty("s3.secret")
        ));
        final S3Session session = new S3Session(host);
        session.open(new DisabledHostKeyCallback(), new DisabledLoginCallback());
        session.login(new DisabledPasswordStore(), new DisabledLoginCallback(), new DisabledCancelCallback());
        final Path bucket = new Path("versioning-test-us-east-1-cyberduck", EnumSet.of(Path.Type.directory, Path.Type.volume));

        final Path sourceDirectory = new S3DirectoryFeature(session, new S3WriteFeature(session)).mkdir(new Path(bucket, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory)), null, new TransferStatus());
        final Path targetDirectory = new S3DirectoryFeature(session, new S3WriteFeature(session)).mkdir(new Path(bucket, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory)), null, new TransferStatus());

        Path test = new Path(sourceDirectory, new AsciiRandomStringService().random(), EnumSet.of(Path.Type.file));
        final S3TouchFeature touch = new S3TouchFeature(session);
        final S3DefaultDeleteFeature delete = new S3DefaultDeleteFeature(session);
        touch.touch(test, new TransferStatus());
        Thread.sleep(1000); // timestamp has second precision only - versions are sorted by timestamp
        assertTrue(new S3FindFeature(session).find(test, new DisabledListProgressListener()));
        delete.delete(Collections.singletonList(test), new DisabledPasswordCallback(), new Delete.DisabledCallback());
        Thread.sleep(1000);
        test.attributes().setVersionId(null);
        assertTrue(new S3FindFeature(session).find(test, new DisabledListProgressListener()));
        test = touch.touch(test, new TransferStatus());
        Thread.sleep(1000);
        assertTrue(new S3FindFeature(session).find(test, new DisabledListProgressListener()));

        final S3VersionedObjectListService list = new S3VersionedObjectListService(session);
        final AttributedList<Path> versioned = list.list(sourceDirectory, new DisabledListProgressListener());

        final Map<Path, Path> files = new HashMap<>();
        for(Path source : versioned) {
            files.put(source, new Path(targetDirectory, source.getName(), source.getType(), source.attributes()));
        }
        final Map<Path, Path> result = new MoveWorker(files, new SessionPool.SingleSessionPool(session), PathCache.empty(), new DisabledProgressListener(), new DisabledLoginCallback()
        ).run(session);
        assertEquals(3, result.size());
        for(Map.Entry<Path, Path> entry : result.entrySet()) {
            assertFalse(new S3FindFeature(session).find(entry.getKey(), new DisabledListProgressListener()));
            assertTrue(new S3FindFeature(session).find(entry.getValue(), new DisabledListProgressListener()));
        }
        new DeleteWorker(new DisabledLoginCallback(), Collections.singletonList(targetDirectory), new DisabledProgressListener()).run(session);
        session.close();
    }
}
