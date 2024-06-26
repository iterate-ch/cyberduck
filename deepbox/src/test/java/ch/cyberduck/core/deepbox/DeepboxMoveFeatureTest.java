package ch.cyberduck.core.deepbox;

/*
 * Copyright (c) 2002-2024 iterate GmbH. All rights reserved.
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

import ch.cyberduck.core.AbstractPath;
import ch.cyberduck.core.AlphanumericRandomStringService;
import ch.cyberduck.core.DisabledConnectionCallback;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathAttributes;
import ch.cyberduck.core.exception.NotfoundException;
import ch.cyberduck.core.features.Delete;
import ch.cyberduck.core.features.FileIdProvider;
import ch.cyberduck.core.synchronization.Comparison;
import ch.cyberduck.core.synchronization.ComparisonService;
import ch.cyberduck.core.transfer.TransferStatus;
import ch.cyberduck.test.IntegrationTest;

import org.junit.Ignore;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.util.EnumSet;

import static org.junit.Assert.*;

@Category(IntegrationTest.class)
public class DeepboxMoveFeatureTest extends AbstractDeepboxTest {
    @Test
    public void testMove() throws Exception {

        final DeepboxIdProvider fileid = (DeepboxIdProvider) session.getFeature(FileIdProvider.class);
        final Path documents = new Path("/ORG 4 - DeepBox Desktop App/Box1/Documents/", EnumSet.of(Path.Type.directory, Path.Type.volume));
        final Path test = new DeepboxTouchFeature(session, fileid).touch(new Path(documents, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file)), new TransferStatus());
        assertNotNull(test.attributes().getFileId());
        assertEquals(0L, test.attributes().getSize());
        assertNotEquals(-1L, test.attributes().getModificationDate());
        final Path target = new DeepboxMoveFeature(session, fileid).move(test,
                new Path(documents, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file)), new TransferStatus(), new Delete.DisabledCallback(), new DisabledConnectionCallback());
        assertFalse(new DeepboxFindFeature(session, fileid).find(new Path(test).withAttributes(PathAttributes.EMPTY)));
        assertTrue(new DeepboxFindFeature(session, fileid).find(target));
        assertEquals(test.attributes().getModificationDate(), target.attributes().getModificationDate());
        assertEquals(test.attributes().getChecksum(), target.attributes().getChecksum());
        assertEquals(Comparison.equal, session.getHost().getProtocol().getFeature(ComparisonService.class).compare(Path.Type.file, target.attributes(), new DeepboxAttributesFinderFeature(session, fileid).find(target)));

        deleteAndPurge(target);
    }

    @Test
    public void testMoveDirectory() throws Exception {
        final DeepboxIdProvider fileid = (DeepboxIdProvider) session.getFeature(FileIdProvider.class);
        final Path documents = new Path("/ORG 4 - DeepBox Desktop App/Box1/Documents/", EnumSet.of(Path.Type.directory, Path.Type.volume));
        final Path test = new Path(documents, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory));
        new DeepboxDirectoryFeature(session, fileid).mkdir(test, new TransferStatus());
        new DeepboxTouchFeature(session, fileid).touch(new Path(test, new AlphanumericRandomStringService().random(), EnumSet.of(AbstractPath.Type.file)), new TransferStatus());
        final Path target = new Path(documents, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory));
        new DeepboxMoveFeature(session, fileid).move(test, target, new TransferStatus(), new Delete.DisabledCallback(), new DisabledConnectionCallback());
        assertFalse(new DeepboxFindFeature(session, fileid).find(test.withAttributes(PathAttributes.EMPTY)));
        assertTrue(new DeepboxFindFeature(session, fileid).find(target));

        deleteAndPurge(target);
    }

    @Test
    @Ignore
    public void testMoveOverride() throws Exception {
        // TODO duplicates
        final DeepboxIdProvider fileid = (DeepboxIdProvider) session.getFeature(FileIdProvider.class);
        final Path documents = new Path("/ORG 4 - DeepBox Desktop App/Box1/Documents/", EnumSet.of(Path.Type.directory, Path.Type.volume));
        final Path test = new DeepboxTouchFeature(session, fileid).touch(
                new Path(documents, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file)), new TransferStatus());
        final Path target = new DeepboxTouchFeature(session, fileid).touch(
                new Path(documents, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file)), new TransferStatus());
        // TODO?
        new DeepboxMoveFeature(session, fileid).move(test, target,
                new TransferStatus().exists(true).withRemote(target.attributes()), new Delete.DisabledCallback(), new DisabledConnectionCallback());
        assertFalse(new DeepboxFindFeature(session, fileid).find(test.withAttributes(new PathAttributes())));
        assertTrue(new DeepboxFindFeature(session, fileid).find(target.withAttributes(new PathAttributes())));
        assertEquals(test.attributes().getModificationDate(), target.attributes().getModificationDate());
        assertEquals(test.attributes().getChecksum(), target.attributes().getChecksum());

//        deleteAndPurge(override);
//        deleteAndPurge(target);
    }

    @Test(expected = NotfoundException.class)
    public void testMoveNotFound() throws Exception {
        final DeepboxIdProvider fileid = (DeepboxIdProvider) session.getFeature(FileIdProvider.class);
        final Path documents = new Path("/ORG 4 - DeepBox Desktop App/Box1/Documents/", EnumSet.of(Path.Type.directory, Path.Type.volume));
        final Path test = new Path(documents, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file));
        final Path renamed = new Path(documents, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file));
        new DeepboxMoveFeature(session, fileid).move(test, renamed, new TransferStatus(), new Delete.DisabledCallback(), new DisabledConnectionCallback());

        deleteAndPurge(renamed);
    }
}