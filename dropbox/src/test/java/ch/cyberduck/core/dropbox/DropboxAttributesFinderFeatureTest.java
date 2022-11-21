package ch.cyberduck.core.dropbox;

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

import ch.cyberduck.core.AbstractDropboxTest;
import ch.cyberduck.core.AlphanumericRandomStringService;
import ch.cyberduck.core.DisabledLoginCallback;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathAttributes;
import ch.cyberduck.core.features.Delete;
import ch.cyberduck.core.shared.DefaultHomeFinderService;
import ch.cyberduck.core.transfer.TransferStatus;
import ch.cyberduck.test.IntegrationTest;

import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.util.Collections;
import java.util.EnumSet;

import static org.junit.Assert.*;

@Category(IntegrationTest.class)
public class DropboxAttributesFinderFeatureTest extends AbstractDropboxTest {

    @Test
    public void testFindFile() throws Exception {
        final Path root = new DefaultHomeFinderService(session).find();
        final Path folder = new DropboxDirectoryFeature(session).mkdir(new Path(root,
                new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory)), null);
        final DropboxAttributesFinderFeature f = new DropboxAttributesFinderFeature(session);
        assertEquals(-1L, f.find(folder).getModificationDate());
        final Path file = new Path(folder, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file));
        new DropboxTouchFeature(session).touch(file, new TransferStatus());
        final PathAttributes attr = f.find(file);
        assertEquals("e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855", attr.getChecksum().hash);
        assertNotEquals(-1L, attr.getModificationDate());
        assertNotNull(attr.getVersionId());
        new DropboxDeleteFeature(session).delete(Collections.singletonList(file), new DisabledLoginCallback(), new Delete.DisabledCallback());
    }
}