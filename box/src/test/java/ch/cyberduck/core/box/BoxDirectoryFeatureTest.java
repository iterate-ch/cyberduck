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
import ch.cyberduck.core.DisabledLoginCallback;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.exception.ConflictException;
import ch.cyberduck.core.features.Delete;
import ch.cyberduck.core.features.Home;
import ch.cyberduck.core.shared.DefaultFindFeature;
import ch.cyberduck.core.transfer.TransferStatus;
import ch.cyberduck.test.IntegrationTest;

import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.util.Collections;
import java.util.EnumSet;

import static org.junit.Assert.*;

@Category(IntegrationTest.class)
public class BoxDirectoryFeatureTest extends AbstractBoxTest {

    @Test
    public void testCreateDirectory() throws Exception {
        final BoxFileidProvider fileid = new BoxFileidProvider(session);
        final Path folder = new BoxDirectoryFeature(session, fileid).mkdir(
                new Path(new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory)), new TransferStatus());
        assertTrue(new DefaultFindFeature(session).find(folder));
        assertTrue(new BoxFindFeature(session, fileid).find(folder));
        assertEquals(0L, folder.attributes().getSize());
        assertNotEquals(-1L, folder.attributes().getModificationDate());
        assertThrows(ConflictException.class, () -> new BoxDirectoryFeature(session, fileid).mkdir(folder, new TransferStatus()));
        new BoxDeleteFeature(session, fileid).delete(Collections.singletonList(folder), new DisabledLoginCallback(), new Delete.DisabledCallback());
        assertFalse(new DefaultFindFeature(session).find(folder));
    }

    @Test
    public void isSupported() {
        final BoxFileidProvider fileid = new BoxFileidProvider(session);
        assertTrue(new BoxDirectoryFeature(session, fileid).isSupported(Home.ROOT, new AlphanumericRandomStringService().random()));
        assertFalse(new BoxDirectoryFeature(session, fileid).isSupported(Home.ROOT, String.format("%s ", new AlphanumericRandomStringService().random())));
        assertFalse(new BoxDirectoryFeature(session, fileid).isSupported(Home.ROOT, String.format("%s\\", new AlphanumericRandomStringService().random())));
        assertFalse(new BoxDirectoryFeature(session, fileid).isSupported(Home.ROOT, String.format("%s/", new AlphanumericRandomStringService().random())));
        assertFalse(new BoxDirectoryFeature(session, fileid).isSupported(Home.ROOT, "."));
        assertFalse(new BoxDirectoryFeature(session, fileid).isSupported(Home.ROOT, ".."));
    }
}