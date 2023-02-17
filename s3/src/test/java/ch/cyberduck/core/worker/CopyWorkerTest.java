package ch.cyberduck.core.worker;

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

import ch.cyberduck.core.Acl;
import ch.cyberduck.core.AlphanumericRandomStringService;
import ch.cyberduck.core.DisabledConnectionCallback;
import ch.cyberduck.core.DisabledLoginCallback;
import ch.cyberduck.core.DisabledProgressListener;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathCache;
import ch.cyberduck.core.pool.SessionPool;
import ch.cyberduck.core.s3.AbstractS3Test;
import ch.cyberduck.core.s3.S3AccessControlListFeature;
import ch.cyberduck.core.s3.S3DirectoryFeature;
import ch.cyberduck.core.s3.S3FindFeature;
import ch.cyberduck.core.s3.S3MetadataFeature;
import ch.cyberduck.core.s3.S3TouchFeature;
import ch.cyberduck.core.s3.S3WriteFeature;
import ch.cyberduck.core.transfer.TransferStatus;
import ch.cyberduck.test.IntegrationTest;

import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@Category(IntegrationTest.class)
public class CopyWorkerTest extends AbstractS3Test {

    @Test
    public void testCopyFile() throws Exception {
        final Path home = new Path("test-eu-central-1-cyberduck", EnumSet.of(Path.Type.directory, Path.Type.volume));
        final Path source = new Path(home, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file));
        final Path target = new Path(home, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file));
        new S3TouchFeature(session, new S3AccessControlListFeature(session)).touch(source, new TransferStatus().withLength(0L).withMime("application/cyberduck"));
        new S3AccessControlListFeature(session).setPermission(source, new Acl(
                new Acl.UserAndRole(
                        new Acl.Owner("80b9982b7b08045ee86680cc47f43c84bf439494a89ece22b5330f8a49477cf6"), new Acl.Role(Acl.Role.FULL)
                ),
                new Acl.UserAndRole(
                        new Acl.GroupUser("http://acs.amazonaws.com/groups/global/AllUsers"), new Acl.Role(Acl.Role.READ)
                )
        ));
        assertTrue(new S3FindFeature(session, new S3AccessControlListFeature(session)).find(source));
        final CopyWorker worker = new CopyWorker(Collections.singletonMap(source, target), new SessionPool.SingleSessionPool(session), PathCache.empty(), new DisabledProgressListener(), new DisabledConnectionCallback());
        worker.run(session);
        assertTrue(new S3FindFeature(session, new S3AccessControlListFeature(session)).find(source));
        assertTrue(new S3FindFeature(session, new S3AccessControlListFeature(session)).find(target));
        assertEquals("application/cyberduck",
                new S3MetadataFeature(session, new S3AccessControlListFeature(session)).getMetadata(target).get("Content-Type"));
        assertTrue(new S3AccessControlListFeature(session).getPermission(target).asList().contains(
                new Acl.UserAndRole(new Acl.Owner("80b9982b7b08045ee86680cc47f43c84bf439494a89ece22b5330f8a49477cf6"), new Acl.Role(Acl.Role.FULL))
        ));
        assertTrue(new S3AccessControlListFeature(session).getPermission(target).asList().contains(
                new Acl.UserAndRole(new Acl.GroupUser(Acl.GroupUser.EVERYONE), new Acl.Role(Acl.Role.READ))
        ));
        new DeleteWorker(new DisabledLoginCallback(), Arrays.asList(source, target), new DisabledProgressListener()).run(session);
    }

    @Test
    public void testCopyFileToDirectory() throws Exception {
        final Path home = new Path("test-eu-central-1-cyberduck", EnumSet.of(Path.Type.directory, Path.Type.volume));
        final Path sourceFile = new Path(home, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file));
        final S3AccessControlListFeature acl = new S3AccessControlListFeature(session);
        new S3TouchFeature(session, acl).touch(sourceFile, new TransferStatus().withLength(0L));
        assertTrue(new S3FindFeature(session, acl).find(sourceFile));
        final Path targetFolder = new Path(home, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory));
        final Path targetFile = new Path(targetFolder, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file));
        new S3DirectoryFeature(session, new S3WriteFeature(session, acl), acl).mkdir(targetFolder, new TransferStatus().withLength(0L));
        assertTrue(new S3FindFeature(session, acl).find(targetFolder));
        // copy file into vault
        final CopyWorker worker = new CopyWorker(Collections.singletonMap(sourceFile, targetFile), new SessionPool.SingleSessionPool(session), PathCache.empty(), new DisabledProgressListener(), new DisabledConnectionCallback());
        worker.run(session);
        assertTrue(new S3FindFeature(session, acl).find(sourceFile));
        assertTrue(new S3FindFeature(session, acl).find(targetFile));
        new DeleteWorker(new DisabledLoginCallback(), Arrays.asList(sourceFile, targetFolder), new DisabledProgressListener()).run(session);
    }

    @Test
    public void testCopyDirectory() throws Exception {
        final Path home = new Path("test-eu-central-1-cyberduck", EnumSet.of(Path.Type.directory, Path.Type.volume));
        final Path folder = new Path(home, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory));
        final Path sourceFile = new Path(folder, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file));
        final S3AccessControlListFeature acl = new S3AccessControlListFeature(session);
        new S3DirectoryFeature(session, new S3WriteFeature(session, acl), acl).mkdir(folder, new TransferStatus().withLength(0L));
        new S3TouchFeature(session, acl).touch(sourceFile, new TransferStatus().withLength(0L));
        assertTrue(new S3FindFeature(session, acl).find(folder));
        assertTrue(new S3FindFeature(session, acl).find(sourceFile));
        // move directory into vault
        final Path targetFolder = new Path(home, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory));
        final Path targetFile = new Path(targetFolder, sourceFile.getName(), EnumSet.of(Path.Type.file));
        final CopyWorker worker = new CopyWorker(Collections.singletonMap(folder, targetFolder), new SessionPool.SingleSessionPool(session), PathCache.empty(), new DisabledProgressListener(), new DisabledConnectionCallback());
        worker.run(session);
        assertTrue(new S3FindFeature(session, acl).find(targetFolder));
        assertTrue(new S3FindFeature(session, acl).find(targetFile));
        assertTrue(new S3FindFeature(session, acl).find(folder));
        assertTrue(new S3FindFeature(session, acl).find(sourceFile));
        new DeleteWorker(new DisabledLoginCallback(), Arrays.asList(folder, targetFolder), new DisabledProgressListener()).run(session);
    }
}
