package ch.cyberduck.core.sftp;

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

import ch.cyberduck.core.AlphanumericRandomStringService;
import ch.cyberduck.core.DisabledConnectionCallback;
import ch.cyberduck.core.DisabledLoginCallback;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.exception.NotfoundException;
import ch.cyberduck.core.features.Delete;
import ch.cyberduck.core.transfer.TransferStatus;
import ch.cyberduck.test.IntegrationTest;

import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.util.Collections;
import java.util.EnumSet;
import java.util.UUID;

import static org.junit.Assert.*;

@Category(IntegrationTest.class)
public class SFTPMoveFeatureTest extends AbstractSFTPTest {

    @Test
    public void testMove() throws Exception {
        final Path workdir = new SFTPHomeDirectoryService(session).find();
        final Path test = new SFTPTouchFeature(session).touch(new Path(workdir, UUID.randomUUID().toString(), EnumSet.of(Path.Type.file)), new TransferStatus());
        assertEquals(0L, test.attributes().getSize());
        final Path target = new SFTPMoveFeature(session).move(test,
            new Path(workdir, UUID.randomUUID().toString(), EnumSet.of(Path.Type.file)), new TransferStatus(), new Delete.DisabledCallback(), new DisabledConnectionCallback());
        assertFalse(new SFTPFindFeature(session).find(test));
        assertTrue(new SFTPFindFeature(session).find(target));
        assertEquals(test.attributes(), target.attributes());
        new SFTPDeleteFeature(session).delete(Collections.<Path>singletonList(target), new DisabledLoginCallback(), new Delete.DisabledCallback());
    }

    @Test
    public void testMoveOverride() throws Exception {
        final Path workdir = new SFTPHomeDirectoryService(session).find();
        final Path test = new Path(workdir, UUID.randomUUID().toString(), EnumSet.of(Path.Type.file));
        new SFTPTouchFeature(session).touch(test, new TransferStatus());
        final Path target = new Path(workdir, UUID.randomUUID().toString(), EnumSet.of(Path.Type.file));
        new SFTPTouchFeature(session).touch(target, new TransferStatus());
        new SFTPMoveFeature(session).move(test, target, new TransferStatus().exists(true), new Delete.DisabledCallback(), new DisabledConnectionCallback());
        assertFalse(new SFTPFindFeature(session).find(test));
        assertTrue(new SFTPFindFeature(session).find(target));
        new SFTPDeleteFeature(session).delete(Collections.<Path>singletonList(target), new DisabledLoginCallback(), new Delete.DisabledCallback());
    }

    @Test
    public void testMoveOverrideDirectory() throws Exception {
        final Path workdir = new SFTPHomeDirectoryService(session).find();
        final Path test = new SFTPDirectoryFeature(session).mkdir(new Path(workdir, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory)), new TransferStatus());
        final Path target = new SFTPDirectoryFeature(session).mkdir(new Path(workdir, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory)), new TransferStatus());
        new SFTPTouchFeature(session).touch(new Path(target, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file)), new TransferStatus());
        new SFTPMoveFeature(session).move(test, target, new TransferStatus().exists(true), new Delete.DisabledCallback(), new DisabledConnectionCallback());
        assertFalse(new SFTPFindFeature(session).find(test));
        assertTrue(new SFTPFindFeature(session).find(target));
        new SFTPDeleteFeature(session).delete(Collections.singletonList(target), new DisabledLoginCallback(), new Delete.DisabledCallback());
    }

    @Test(expected = NotfoundException.class)
    public void testMoveNotFound() throws Exception {
        final Path workdir = new SFTPHomeDirectoryService(session).find();
        final Path test = new Path(workdir, UUID.randomUUID().toString(), EnumSet.of(Path.Type.file));
        new SFTPMoveFeature(session).move(test, new Path(workdir, UUID.randomUUID().toString(), EnumSet.of(Path.Type.file)), new TransferStatus(), new Delete.DisabledCallback(), new DisabledConnectionCallback());
    }

}
