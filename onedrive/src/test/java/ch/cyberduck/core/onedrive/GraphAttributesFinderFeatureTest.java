package ch.cyberduck.core.onedrive;

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
import ch.cyberduck.core.DisabledLoginCallback;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathAttributes;
import ch.cyberduck.core.exception.NotfoundException;
import ch.cyberduck.core.features.Delete;
import ch.cyberduck.core.features.Home;
import ch.cyberduck.core.onedrive.features.GraphAttributesFinderFeature;
import ch.cyberduck.core.onedrive.features.GraphDeleteFeature;
import ch.cyberduck.core.onedrive.features.GraphDirectoryFeature;
import ch.cyberduck.core.onedrive.features.GraphFileIdProvider;
import ch.cyberduck.core.onedrive.features.GraphTouchFeature;
import ch.cyberduck.core.onedrive.features.GraphWriteFeature;
import ch.cyberduck.core.transfer.TransferStatus;
import ch.cyberduck.test.IntegrationTest;

import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.util.Collections;
import java.util.EnumSet;
import java.util.UUID;

import static org.junit.Assert.*;

@Category(IntegrationTest.class)
public class GraphAttributesFinderFeatureTest extends AbstractOneDriveTest {

    @Test
    public void testFindRoot() throws Exception {
        final GraphAttributesFinderFeature f = new GraphAttributesFinderFeature(session, fileid);
        assertEquals(PathAttributes.EMPTY, f.find(Home.root()));
    }

    @Test(expected = NotfoundException.class)
    public void testFindNotFound() throws Exception {
        new GraphAttributesFinderFeature(session, fileid).find(new Path(new OneDriveHomeFinderService().find(), UUID.randomUUID().toString(), EnumSet.of(Path.Type.file)));
    }

    @Test
    public void testFindFile() throws Exception {
        final Path file = new Path(new OneDriveHomeFinderService().find(), new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file));
        new GraphTouchFeature(session, fileid).touch(new GraphWriteFeature(session, fileid), file, new TransferStatus().setMime("x-application/cyberduck"));
        final PathAttributes attributes = new GraphAttributesFinderFeature(session, fileid).find(file);
        assertNotNull(attributes);
        assertNotEquals(-1L, attributes.getSize());
        assertNotEquals(-1L, attributes.getCreationDate());
        assertNotEquals(-1L, attributes.getModificationDate());
        assertNotNull(attributes.getETag());
        assertNotNull(attributes.getVersionId());
        assertNotNull(attributes.getLink());
        assertNotNull(attributes.getFileId());
        new GraphDeleteFeature(session, fileid).delete(Collections.singletonList(file), new DisabledLoginCallback(), new Delete.DisabledCallback());
    }

    @Test
    public void testFindDirectory() throws Exception {
        final Path file = new Path(new OneDriveHomeFinderService().find(), new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory));
        new GraphDirectoryFeature(session, fileid).mkdir(new GraphWriteFeature(session, fileid), file, new TransferStatus());
        final PathAttributes attributes = new GraphAttributesFinderFeature(session, fileid).find(file);
        assertNotNull(attributes);
        assertNotEquals(-1L, attributes.getSize());
        assertNotEquals(-1L, attributes.getCreationDate());
        assertNotEquals(-1L, attributes.getModificationDate());
        assertNotNull(attributes.getETag());
        assertNull(attributes.getVersionId());
        assertNotNull(attributes.getLink());
        assertNotNull(attributes.getFileId());
        new GraphDeleteFeature(session, fileid).delete(Collections.singletonList(file), new DisabledLoginCallback(), new Delete.DisabledCallback());
    }

    @Test
    public void testChangedFileId() throws Exception {
        final GraphFileIdProvider fileid = new GraphFileIdProvider(session);
        final Path drive = new OneDriveHomeFinderService().find();
        final Path test = new GraphTouchFeature(session, fileid).touch(new GraphWriteFeature(session, fileid), new Path(drive, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file)), new TransferStatus());
        final String previousnodeid = test.attributes().getFileId();
        new GraphDeleteFeature(session, fileid).delete(Collections.singletonList(test), new DisabledLoginCallback(), new Delete.DisabledCallback());
        final String latestnodeid = new GraphTouchFeature(session, fileid).touch(new GraphWriteFeature(session, fileid), new Path(drive, test.getName(), EnumSet.of(Path.Type.file)), new TransferStatus()).attributes().getFileId();
        assertNotNull(latestnodeid);
        // Assume previously seen but changed on server
        fileid.cache(test, previousnodeid);
        final GraphAttributesFinderFeature f = new GraphAttributesFinderFeature(session, fileid);
        assertEquals(latestnodeid, f.find(test).getFileId());
        new GraphDeleteFeature(session, fileid).delete(Collections.singletonList(test), new DisabledLoginCallback(), new Delete.DisabledCallback());
    }
}
