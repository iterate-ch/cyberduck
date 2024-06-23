package ch.cyberduck.core.manta;

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
import ch.cyberduck.core.DisabledConnectionCallback;
import ch.cyberduck.core.DisabledLoginCallback;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.features.AttributesFinder;
import ch.cyberduck.core.features.Delete;
import ch.cyberduck.core.features.Directory;
import ch.cyberduck.core.features.Move;
import ch.cyberduck.core.features.Touch;
import ch.cyberduck.core.transfer.TransferStatus;
import ch.cyberduck.test.IntegrationTest;

import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.util.Collections;
import java.util.EnumSet;

import static org.junit.Assert.*;

@Category(IntegrationTest.class)
public class MantaMoveFeatureTest extends AbstractMantaTest {

    @Test
    public void testRename() throws BackgroundException {
        final Touch touch = new MantaTouchFeature(session);
        final Move move = new MantaMoveFeature(session);
        final Delete delete = new MantaDeleteFeature(session);
        final AttributesFinder attributesFinder = new MantaAttributesFinderFeature(session);
        final Path drive = new MantaDirectoryFeature(session).mkdir(randomDirectory(), new TransferStatus());
        final Path file = new Path(drive, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file));
        touch.touch(file, new TransferStatus().withMime("x-application/cyberduck"));
        assertNotNull(attributesFinder.find(file));
        Path rename = new Path(drive, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file));
        assertTrue(move.isSupported(file, rename.getParent(), rename.getName()));
        assertEquals(rename, move.move(file, rename, new TransferStatus(), new Delete.DisabledCallback(), new DisabledConnectionCallback()));
        assertFalse(new MantaFindFeature(session).find(file));
        assertTrue(new MantaFindFeature(session).find(rename));
        assertNotNull(attributesFinder.find(rename));
        delete.delete(Collections.singletonList(rename), new DisabledLoginCallback(), new Delete.DisabledCallback());
    }

    @Test
    public void testMove() throws BackgroundException {
        final Directory directory = new MantaDirectoryFeature(session);
        final Touch touch = new MantaTouchFeature(session);
        final Move move = new MantaMoveFeature(session);
        final Delete delete = new MantaDeleteFeature(session);
        final AttributesFinder attributesFinder = new MantaAttributesFinderFeature(session);
        final Path drive = new MantaDirectoryFeature(session).mkdir(randomDirectory(), new TransferStatus());
        Path targetDirectory = new Path(drive, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory));
        directory.mkdir(targetDirectory, null);
        assertNotNull(attributesFinder.find(targetDirectory));
        Path touchedFile = new Path(drive, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file));
        touch.touch(touchedFile, new TransferStatus().withMime("x-application/cyberduck"));
        assertNotNull(attributesFinder.find(touchedFile));
        Path rename = new Path(targetDirectory, touchedFile.getName(), EnumSet.of(Path.Type.file));
        assertTrue(move.isSupported(touchedFile, rename.getParent(), rename.getName()));
        assertEquals(rename, move.move(touchedFile, rename, new TransferStatus(), new Delete.DisabledCallback(), new DisabledConnectionCallback()));
        assertNotNull(attributesFinder.find(rename));
        assertFalse(new MantaFindFeature(session).find(touchedFile));
        assertTrue(new MantaFindFeature(session).find(rename));
        delete.delete(Collections.singletonList(targetDirectory), new DisabledLoginCallback(), new Delete.DisabledCallback());
    }

    @Test
    public void testMoveRename() throws BackgroundException {
        final Directory directory = new MantaDirectoryFeature(session);
        final Touch touch = new MantaTouchFeature(session);
        final Move move = new MantaMoveFeature(session);
        final Delete delete = new MantaDeleteFeature(session);
        final AttributesFinder attributesFinder = new MantaAttributesFinderFeature(session);
        final Path drive = new MantaDirectoryFeature(session).mkdir(randomDirectory(), new TransferStatus());
        Path targetDirectory = new Path(drive, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory));
        directory.mkdir(targetDirectory, null);
        assertNotNull(attributesFinder.find(targetDirectory));
        Path touchedFile = new Path(drive, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file));
        touch.touch(touchedFile, new TransferStatus().withMime("x-application/cyberduck"));
        assertNotNull(attributesFinder.find(touchedFile));
        Path rename = new Path(targetDirectory, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file));
        assertTrue(move.isSupported(touchedFile, rename.getParent(), rename.getName()));
        assertEquals(rename, move.move(touchedFile, rename, new TransferStatus(), new Delete.DisabledCallback(), new DisabledConnectionCallback()));
        assertNotNull(attributesFinder.find(rename));
        assertFalse(new MantaFindFeature(session).find(touchedFile));
        assertTrue(new MantaFindFeature(session).find(rename));
        delete.delete(Collections.singletonList(targetDirectory), new DisabledLoginCallback(), new Delete.DisabledCallback());
    }
}
