package ch.cyberduck.core.box;

/*
 * Copyright (c) 2002-2021 iterate GmbH. All rights reserved.
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

import ch.cyberduck.core.AlphanumericRandomStringService;
import ch.cyberduck.core.DisabledConnectionCallback;
import ch.cyberduck.core.DisabledLoginCallback;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathAttributes;
import ch.cyberduck.core.exception.NotfoundException;
import ch.cyberduck.core.features.Delete;
import ch.cyberduck.core.shared.DefaultHomeFinderService;
import ch.cyberduck.core.synchronization.Comparison;
import ch.cyberduck.core.synchronization.ComparisonService;
import ch.cyberduck.core.transfer.TransferStatus;
import ch.cyberduck.test.IntegrationTest;

import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.util.Collections;
import java.util.EnumSet;

import static org.junit.Assert.*;

@Category(IntegrationTest.class)
public class BoxMoveFeatureTest extends AbstractBoxTest {

    @Test
    public void testMove() throws Exception {
        final BoxFileidProvider fileid = new BoxFileidProvider(session);
        final Path test = new BoxTouchFeature(session, fileid).touch(new Path(new DefaultHomeFinderService(session).find(), new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file)), new TransferStatus());
        assertNotNull(test.attributes().getFileId());
        assertEquals(0L, test.attributes().getSize());
        assertNotEquals(-1L, test.attributes().getModificationDate());
        final Path target = new BoxMoveFeature(session, fileid).move(test,
                new Path(new DefaultHomeFinderService(session).find(), new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file)), new TransferStatus(), new Delete.DisabledCallback(), new DisabledConnectionCallback());
        assertFalse(new BoxFindFeature(session, fileid).find(new Path(test).withAttributes(PathAttributes.EMPTY)));
        assertTrue(new BoxFindFeature(session, fileid).find(target));
        assertEquals(test.attributes().getModificationDate(), target.attributes().getModificationDate());
        assertEquals(test.attributes().getChecksum(), target.attributes().getChecksum());
        assertEquals(Comparison.equal, session.getHost().getProtocol().getFeature(ComparisonService.class).compare(Path.Type.file, target.attributes(), new BoxAttributesFinderFeature(session, fileid).find(target)));
        new BoxDeleteFeature(session, fileid).delete(Collections.singletonList(target), new DisabledLoginCallback(), new Delete.DisabledCallback());
    }

    @Test
    public void testMoveDirectory() throws Exception {
        final BoxFileidProvider fileid = new BoxFileidProvider(session);
        final Path test = new Path(new DefaultHomeFinderService(session).find(), new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory));
        new BoxDirectoryFeature(session, fileid).mkdir(test, new TransferStatus());
        final Path target = new Path(new DefaultHomeFinderService(session).find(), new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory));
        new BoxMoveFeature(session, fileid).move(test, target, new TransferStatus(), new Delete.DisabledCallback(), new DisabledConnectionCallback());
        assertFalse(new BoxFindFeature(session, fileid).find(test.withAttributes(PathAttributes.EMPTY)));
        assertTrue(new BoxFindFeature(session, fileid).find(target));
        new BoxDeleteFeature(session, fileid).delete(Collections.<Path>singletonList(target), new DisabledLoginCallback(), new Delete.DisabledCallback());
    }

    @Test
    public void testMoveOverride() throws Exception {
        final BoxFileidProvider fileid = new BoxFileidProvider(session);
        final Path test = new BoxTouchFeature(session, fileid).touch(
                new Path(new DefaultHomeFinderService(session).find(), new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file)), new TransferStatus());
        final Path target = new BoxTouchFeature(session, fileid).touch(
                new Path(new DefaultHomeFinderService(session).find(), new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file)), new TransferStatus());
        final Path override = new BoxMoveFeature(session, fileid).move(test, target,
                new TransferStatus().exists(true).withRemote(target.attributes()), new Delete.DisabledCallback(), new DisabledConnectionCallback());
        assertFalse(new BoxFindFeature(session, fileid).find(test));
        assertTrue(new BoxFindFeature(session, fileid).find(override));
        assertEquals(test.attributes().getModificationDate(), target.attributes().getModificationDate());
        assertEquals(test.attributes().getChecksum(), target.attributes().getChecksum());
        new BoxDeleteFeature(session, fileid).delete(Collections.<Path>singletonList(target), new DisabledLoginCallback(), new Delete.DisabledCallback());
    }

    @Test(expected = NotfoundException.class)
    public void testMoveNotFound() throws Exception {
        final BoxFileidProvider fileid = new BoxFileidProvider(session);
        final Path test = new Path(new DefaultHomeFinderService(session).find(), new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file));
        new BoxMoveFeature(session, fileid).move(test, new Path(new DefaultHomeFinderService(session).find(), new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file)), new TransferStatus(), new Delete.DisabledCallback(), new DisabledConnectionCallback());
    }
}