package ch.cyberduck.core.dropbox;

/*
 * Copyright (c) 2002-2016 iterate GmbH. All rights reserved.
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

import ch.cyberduck.core.AbstractDropboxTest;
import ch.cyberduck.core.AlphanumericRandomStringService;
import ch.cyberduck.core.AttributedList;
import ch.cyberduck.core.DisabledConnectionCallback;
import ch.cyberduck.core.DisabledListProgressListener;
import ch.cyberduck.core.DisabledLoginCallback;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.exception.ConflictException;
import ch.cyberduck.core.features.Delete;
import ch.cyberduck.core.shared.DefaultFindFeature;
import ch.cyberduck.core.shared.DefaultHomeFinderService;
import ch.cyberduck.core.transfer.TransferStatus;
import ch.cyberduck.test.IntegrationTest;

import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.util.Collections;
import java.util.EnumSet;

import static org.junit.Assert.*;

@Category(IntegrationTest.class)
public class DropboxMoveFeatureTest extends AbstractDropboxTest {

    @Test
    public void testMoveFile() throws Exception {
        final Path home = new DefaultHomeFinderService(session).find();
        final Path file = new DropboxTouchFeature(session).touch(new Path(home, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file)), new TransferStatus());
        assertTrue(new DropboxFindFeature(session).find(file));
        assertTrue(new DefaultFindFeature(session).find(file));
        final Path target = new DropboxMoveFeature(session).move(file, new Path(home, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file)), new TransferStatus(), new Delete.DisabledCallback(), new DisabledConnectionCallback());
        assertFalse(new DropboxFindFeature(session).find(file));
        assertTrue(new DropboxFindFeature(session).find(target));
        assertTrue(new DefaultFindFeature(session).find(target));
        assertNotEquals(target.attributes().getVersionId(), file.attributes().getVersionId());
        assertEquals(target.attributes().getModificationDate(), file.attributes().getModificationDate());
        assertEquals(target.attributes(), new DropboxAttributesFinderFeature(session).find(target));
        new DropboxDeleteFeature(session).delete(Collections.singletonList(target), new DisabledLoginCallback(), new Delete.DisabledCallback());
    }

    @Test
    public void testMoveOverride() throws Exception {
        final Path home = new DefaultHomeFinderService(session).find();
        final Path test = new Path(home, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file));
        new DropboxTouchFeature(session).touch(test, new TransferStatus());
        final Path target = new Path(home, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file));
        new DropboxTouchFeature(session).touch(target, new TransferStatus());
        assertThrows(ConflictException.class, () -> new DropboxMoveFeature(session).move(test, target, new TransferStatus().exists(false), new Delete.DisabledCallback(), new DisabledConnectionCallback()));
        new DropboxMoveFeature(session).move(test, target, new TransferStatus().exists(true), new Delete.DisabledCallback(), new DisabledConnectionCallback());
        assertFalse(new DropboxFindFeature(session).find(test));
        assertTrue(new DropboxFindFeature(session).find(target));
        new DropboxDeleteFeature(session).delete(Collections.<Path>singletonList(target), new DisabledLoginCallback(), new Delete.DisabledCallback());
    }

    @Test
    public void testMoveDirectory() throws Exception {
        final Path home = new DefaultHomeFinderService(session).find();
        final Path directory = new DropboxDirectoryFeature(session).mkdir(new Path(home, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory)), new TransferStatus());
        assertTrue(new DropboxFindFeature(session).find(directory));
        assertTrue(new DefaultFindFeature(session).find(directory));
        final Path target = new DropboxMoveFeature(session).move(directory, new Path(home, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory)), new TransferStatus(), new Delete.DisabledCallback(), new DisabledConnectionCallback());
        assertFalse(new DropboxFindFeature(session).find(directory));
        assertTrue(new DropboxFindFeature(session).find(target));
        assertTrue(new DefaultFindFeature(session).find(target));
        assertEquals(target.attributes(), new DropboxAttributesFinderFeature(session).find(target));
        new DropboxDeleteFeature(session).delete(Collections.singletonList(target), new DisabledLoginCallback(), new Delete.DisabledCallback());
    }

    @Test
    public void testMoveToExistingFile() throws Exception {
        final Path home = new DefaultHomeFinderService(session).find();
        final Path folder = new DropboxDirectoryFeature(session).mkdir(
            new Path(home, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory)), new TransferStatus());
        assertTrue(new DefaultFindFeature(session).find(folder));
        final Path test = new DropboxTouchFeature(session).touch(
            new Path(folder, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file)), new TransferStatus());
        assertTrue(new DefaultFindFeature(session).find(test));
        final Path temp = new DropboxTouchFeature(session).touch(
            new Path(folder, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file)), new TransferStatus());
        assertTrue(new DefaultFindFeature(session).find(temp));
        new DropboxMoveFeature(session).move(temp, test, new TransferStatus().exists(true), new Delete.DisabledCallback(), new DisabledConnectionCallback());
        final AttributedList<Path> files = new DropboxListService(session).list(folder, new DisabledListProgressListener());
        assertEquals(1, files.size());
        assertFalse(new DropboxFindFeature(session).find(temp));
        assertTrue(new DropboxFindFeature(session).find(test));
        new DropboxDeleteFeature(session).delete(Collections.singletonList(folder), new DisabledLoginCallback(), new Delete.DisabledCallback());
    }
}
