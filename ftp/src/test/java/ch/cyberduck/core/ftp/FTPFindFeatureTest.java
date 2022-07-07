package ch.cyberduck.core.ftp;

/*
 * Copyright (c) 2002-2022 iterate GmbH. All rights reserved.
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
import ch.cyberduck.core.features.Delete;
import ch.cyberduck.core.shared.DefaultHomeFinderService;
import ch.cyberduck.core.transfer.TransferStatus;
import ch.cyberduck.test.IntegrationTest;

import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.util.Collections;
import java.util.EnumSet;
import java.util.UUID;

import static org.junit.Assert.*;

@Category(IntegrationTest.class)
public class FTPFindFeatureTest extends AbstractFTPTest {

    @Test
    public void testFindNotFound() throws Exception {
        assertFalse(new FTPFindFeature(session).find(new Path(UUID.randomUUID().toString(), EnumSet.of(Path.Type.file))));
    }

    @Test
    public void testFindDirectory() throws Exception {
        assertTrue(new FTPFindFeature(session).find(new DefaultHomeFinderService(session).find()));
    }

    @Test
    public void testFindFile() throws Exception {
        final Path file = new Path(new DefaultHomeFinderService(session).find(), new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file));
        new FTPTouchFeature(session).touch(file, new TransferStatus());
        assertTrue(new FTPFindFeature(session).find(file));
        assertFalse(new FTPFindFeature(session).find(new Path(file.getAbsolute(), EnumSet.of(Path.Type.directory))));
        new FTPDeleteFeature(session).delete(Collections.singletonList(file), new DisabledLoginCallback(), new Delete.DisabledCallback());
    }

    @Test
    public void testFindRoot() throws Exception {
        assertTrue(new FTPFindFeature(session).find(new Path("/", EnumSet.of(Path.Type.directory))));
    }
}