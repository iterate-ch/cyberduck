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
import ch.cyberduck.core.DisabledListProgressListener;
import ch.cyberduck.core.DisabledPasswordCallback;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.features.Delete;
import ch.cyberduck.core.shared.DefaultFindFeature;
import ch.cyberduck.core.shared.DefaultHomeFinderService;
import ch.cyberduck.core.transfer.TransferStatus;
import ch.cyberduck.test.TestcontainerTest;

import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

@Category(TestcontainerTest.class)
public class SMBDeleteFeatureTest extends AbstractSMBTest {

    @Test
    public void testDeleteFile() throws Exception {
        final Path home = new DefaultHomeFinderService(session).find();
        final Path folder = new SMBDirectoryFeature(session).mkdir(
                new Path(home, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory)), new TransferStatus());
        final Path file = new SMBTouchFeature(session).touch(
                new Path(folder, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file)), new TransferStatus());
        assertTrue(new SMBListService(session).list(folder, new DisabledListProgressListener()).contains(file));
        new SMBDeleteFeature(session).delete(Collections.singletonList(file), new DisabledPasswordCallback(), new Delete.DisabledCallback());
        assertFalse(new DefaultFindFeature(session).find(file, new DisabledListProgressListener()));
        new SMBDeleteFeature(session).delete(Collections.singletonList(folder), new DisabledPasswordCallback(), new Delete.DisabledCallback());
    }

    @Test
    public void testDeleteFolder() throws Exception {
        final Path home = new DefaultHomeFinderService(session).find();
        final Path folder = new SMBDirectoryFeature(session).mkdir(
                new Path(home, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory)), new TransferStatus());
        assertTrue(new SMBListService(session).list(home, new DisabledListProgressListener()).contains(folder));
        new SMBDeleteFeature(session).delete(Collections.singletonList(folder), new DisabledPasswordCallback(), new Delete.DisabledCallback());
        assertFalse(new DefaultFindFeature(session).find(folder, new DisabledListProgressListener()));
    }

    @Test
    public void testDeleteFileAndFolder() throws Exception {
        final Path home = new DefaultHomeFinderService(session).find();
        final Path folder = new SMBDirectoryFeature(session).mkdir(
                new Path(home, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory)), new TransferStatus());
        final Path file = new SMBTouchFeature(session).touch(
                new Path(home, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file)), new TransferStatus());
        assertTrue(new SMBFindFeature(session).find(file));
        new SMBDeleteFeature(session).delete(Arrays.asList(file, folder), new DisabledPasswordCallback(), new Delete.DisabledCallback());
        assertFalse(new SMBFindFeature(session).find(file));
    }
}
