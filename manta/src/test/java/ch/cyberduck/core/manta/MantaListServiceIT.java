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
import ch.cyberduck.core.AttributedList;
import ch.cyberduck.core.DisabledListProgressListener;
import ch.cyberduck.core.DisabledLoginCallback;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.RandomStringService;
import ch.cyberduck.core.features.Delete;
import ch.cyberduck.test.IntegrationTest;

import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.util.Collections;
import java.util.EnumSet;

import static org.junit.Assert.*;

@Category(IntegrationTest.class)
public class MantaListServiceIT extends AbstractMantaTest {

    @Test
    public void testListDrives() throws Exception {
        final AttributedList<Path> list = new MantaListService(session)
                .list(session.pathMapper.getAccountRoot(), new DisabledListProgressListener());
        assertFalse(list.isEmpty());
        for(Path f : list) {
            assertEquals(session.pathMapper.getAccountRoot(), f.getParent());
        }
        // assertTrue(list.contains(new DefaultHomeFinderService(session).find()));
    }

    @Test
    public void testListDriveChildren() throws Exception {
        final Path drive = MantaPathMapper.Volume.PRIVATE.forAccount(session);
        final AttributedList<Path> list = new MantaListService(session)
                .list(drive, new DisabledListProgressListener());
        assertFalse(list.isEmpty());
        for(Path f : list) {
            assertEquals(drive.getName(), f.getParent().getName());
            assertNotNull(f.getName());
        }
    }

    @Test
    public void testWhitespacedChild() throws Exception {
        final RandomStringService randomStringService = new AlphanumericRandomStringService();
        final Path testDir = new Path(
                MantaPathMapper.Volume.PRIVATE.forAccount(session),
                String.format("%s %s", randomStringService.random(), randomStringService.random()),
                EnumSet.of(Path.Type.directory));
        final Path target = new MantaDirectoryFeature(session)
                .mkdir(testDir, null, null);
        final AttributedList<Path> list = new MantaListService(session)
                .list(target, new DisabledListProgressListener());

        new MantaDeleteFeature(session).delete(Collections.singletonList(target), new DisabledLoginCallback(), new Delete.DisabledCallback());
    }
}
