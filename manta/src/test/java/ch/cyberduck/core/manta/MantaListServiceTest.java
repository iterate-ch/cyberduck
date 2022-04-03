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

import ch.cyberduck.core.AttributedList;
import ch.cyberduck.core.DisabledListProgressListener;
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
public class MantaListServiceTest extends AbstractMantaTest {

    @Test(expected = NotfoundException.class)
    public void testListNotFoundFolder() throws Exception {
        new MantaListService(session).list(new Path(
            new MantaHomeFinderFeature(session.getHost()).find(), "notfound", EnumSet.of(Path.Type.directory)), new DisabledListProgressListener());
    }

    @Test
    public void testListEmptyFolder() throws Exception {
        final Path folder = new MantaDirectoryFeature(session).mkdir(new Path(
            testPathPrefix, UUID.randomUUID().toString(), EnumSet.of(Path.Type.directory)), new TransferStatus());
        assertTrue(new MantaListService(session).list(folder, new DisabledListProgressListener()).isEmpty());
        new MantaDeleteFeature(session).delete(Collections.singletonList(folder), new DisabledLoginCallback(), new Delete.DisabledCallback());
    }

    @Test(expected = NotfoundException.class)
    public void testListNotfoundTopLevelFolder() throws Exception {
        final Path directory = new Path(testPathPrefix, "notfound-cyberduck", EnumSet.of(Path.Type.directory, Path.Type.volume));
        new MantaListService(session).list(directory, new DisabledListProgressListener());
    }

    @Test
    public void testAccountRoot() throws Exception {
        final MantaAccountHomeInfo root = new MantaAccountHomeInfo(session.getHost().getCredentials().getUsername(), session.getHost().getDefaultPath());
        final Path directory = root.getAccountRoot();
        final AttributedList<Path> list = new MantaListService(session)
                .list(directory, new DisabledListProgressListener());
        assertFalse(list.isEmpty());
        for(Path f : list) {
            assertSame(directory, f.getParent());
            assertEquals(directory.getName(), f.getParent().getName());
        }
    }
}
