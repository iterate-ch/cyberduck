package ch.cyberduck.core.smb;

/*
 * Copyright (c) 2002-2023 iterate GmbH. All rights reserved.
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
import ch.cyberduck.core.AttributedList;
import ch.cyberduck.core.DisabledListProgressListener;
import ch.cyberduck.core.DisabledLoginCallback;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.features.Delete;
import ch.cyberduck.core.shared.DefaultHomeFinderService;
import ch.cyberduck.core.transfer.TransferStatus;
import ch.cyberduck.test.TestcontainerTest;

import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@Category(TestcontainerTest.class)
public class SMBListServiceTest extends AbstractSMBTest {

    @Test
    public void testList() throws Exception {
        final Path home = new DefaultHomeFinderService(session).find();
        final Path testFolder = new SMBDirectoryFeature(session).mkdir(
                new Path(home, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory)), new TransferStatus());
        final Path testFile = new SMBTouchFeature(session).touch(new Path(testFolder,
                new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file)), new TransferStatus());
        final Path innerFolder = new SMBDirectoryFeature(session).mkdir(
                new Path(testFolder, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory)), new TransferStatus());
        final AttributedList<Path> result = new SMBListService(session).list(testFolder, new DisabledListProgressListener());
        assertEquals(2, result.size());
        assertTrue(result.contains(testFile));
        assertTrue(result.contains(innerFolder));
        new SMBDeleteFeature(session).delete(Arrays.asList(innerFolder, testFile, testFolder), new DisabledLoginCallback(), new Delete.DisabledCallback());
    }

    @Test
    public void testListEmptyFolder() throws Exception {
        final Path home = new DefaultHomeFinderService(session).find();
        final Path emptyFolder = new SMBDirectoryFeature(session).mkdir(
                new Path(home, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory)), new TransferStatus());
        final AttributedList<Path> result = new SMBListService(session).list(emptyFolder, new DisabledListProgressListener());
        assertEquals(0, result.size());
        new SMBDeleteFeature(session).delete(Collections.singletonList(emptyFolder), new DisabledLoginCallback(), new Delete.DisabledCallback());
    }
}
