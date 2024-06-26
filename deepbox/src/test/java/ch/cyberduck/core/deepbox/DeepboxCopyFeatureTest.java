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

import ch.cyberduck.core.AlphanumericRandomStringService;
import ch.cyberduck.core.DisabledConnectionCallback;
import ch.cyberduck.core.DisabledLoginCallback;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathAttributes;
import ch.cyberduck.core.exception.NotfoundException;
import ch.cyberduck.core.exception.UnsupportedException;
import ch.cyberduck.core.features.FileIdProvider;
import ch.cyberduck.core.features.Find;
import ch.cyberduck.core.io.DisabledStreamListener;
import ch.cyberduck.core.transfer.TransferStatus;
import ch.cyberduck.test.IntegrationTest;

import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.util.EnumSet;

import static org.junit.Assert.assertTrue;

@Category(IntegrationTest.class)
public class DeepboxCopyFeatureTest extends AbstractDeepboxTest {

    @Test
    public void testCopyFile() throws Exception {
        final DeepboxIdProvider fileid = (DeepboxIdProvider) session.getFeature(FileIdProvider.class);
        final Path documents = new Path("/ORG 4 - DeepBox Desktop App/Box1/Documents/", EnumSet.of(Path.Type.directory, Path.Type.volume));
        final Path test = new Path(documents, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file));
        new DeepboxTouchFeature(session, fileid).touch(test, new TransferStatus());
        final Path copy = new Path(documents, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file));
        new DeepboxCopyFeature(session, fileid).copy(test, copy, new TransferStatus(), new DisabledConnectionCallback(), new DisabledStreamListener());
        try {
            assertTrue(new DeepboxFindFeature(session, fileid).find(test.withAttributes(new PathAttributes())));
            assertTrue(new DeepboxFindFeature(session, fileid).find(copy.withAttributes(new PathAttributes())));
        }
        finally {
            deleteAndPurge(test.withAttributes(new PathAttributes()));
            deleteAndPurge(copy.withAttributes(new PathAttributes()));
        }
    }

    @Test
    public void testCopyOverride() throws Exception {
        final DeepboxIdProvider fileid = (DeepboxIdProvider) session.getFeature(FileIdProvider.class);
        final Path documents = new Path("/ORG 4 - DeepBox Desktop App/Box1/Documents/", EnumSet.of(Path.Type.directory, Path.Type.volume));
        final Path folder = new Path(documents, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory));
        new DeepboxDirectoryFeature(session, fileid).mkdir(folder, new TransferStatus());
        final Path test = new Path(folder, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file));
        new DeepboxTouchFeature(session, fileid).touch(test, new TransferStatus());
        final Path copy = new Path(folder, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file));
        final Path existing = new DeepboxTouchFeature(session, fileid).touch(copy, new TransferStatus());
        new DeepboxCopyFeature(session, fileid).copy(test, copy, new TransferStatus().exists(true).withRemote(existing.attributes()), new DisabledConnectionCallback(), new DisabledStreamListener());
        final Find find = new DeepboxFindFeature(session, fileid);
        assertTrue(find.find(test));
        assertTrue(find.find(copy));

        deleteAndPurge(test);
        deleteAndPurge(copy);
        deleteAndPurge(folder);
    }

    @Test(expected = UnsupportedException.class)
    public void testCopyDirectory() throws Exception {
        final DeepboxIdProvider fileid = (DeepboxIdProvider) session.getFeature(FileIdProvider.class);
        final Path documents = new Path("/ORG 4 - DeepBox Desktop App/Box1/Documents/", EnumSet.of(Path.Type.directory, Path.Type.volume));
        final Path directory = new DeepboxDirectoryFeature(session, fileid).mkdir(new Path(documents,
                new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory)), new TransferStatus());
        final Path copy = new Path(documents, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory));
        try {
            new DeepboxCopyFeature(session, fileid).preflight(directory, copy);
        }
        finally {
            deleteAndPurge(directory);
        }
    }

    @Test(expected = NotfoundException.class)
    public void testMoveNotFound() throws Exception {
        final DeepboxIdProvider fileid = (DeepboxIdProvider) session.getFeature(FileIdProvider.class);
        final Path documents = new Path("/ORG 4 - DeepBox Desktop App/Box1/Documents/", EnumSet.of(Path.Type.directory, Path.Type.volume));
        final Path test = new Path(documents, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file));
        try {
            new DeepboxCopyFeature(session, fileid).copy(test, new Path(documents, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file)), new TransferStatus(),
                    new DisabledLoginCallback(), new DisabledStreamListener());
        }
        finally {
            deleteAndPurge(test);
        }

    }
}