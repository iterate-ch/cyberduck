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
import ch.cyberduck.core.DisabledLoginCallback;
import ch.cyberduck.core.ListService;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.features.Delete;
import ch.cyberduck.core.features.Touch;
import ch.cyberduck.core.shared.DefaultHomeFinderService;
import ch.cyberduck.core.transfer.TransferStatus;
import ch.cyberduck.test.IntegrationTest;

import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.util.Arrays;
import java.util.EnumSet;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@Category(IntegrationTest.class)
public class SMBListFeatureTest extends AbstractSMBTest {

    @Test
    public void testList() throws Exception {
        final Path home = new DefaultHomeFinderService(session).find();
        final Path folder = new Path(home, "empty_folder", EnumSet.of(Path.Type.directory));
        final Path test = new Path(folder, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file));
        assertTrue(session.getFeature(ListService.class).list(folder, new DisabledListProgressListener()).isEmpty());
        session.getFeature(Touch.class).touch(test, new TransferStatus());
        assertEquals(1, session.getFeature(ListService.class).list(folder, new DisabledListProgressListener()).size());
        assertEquals(test, session.getFeature(ListService.class).list(folder, new DisabledListProgressListener()).get(0));
        session.getFeature(Delete.class, new SMBDeleteFeature(session)).delete(Arrays.asList(test, folder), new DisabledLoginCallback(), new Delete.DisabledCallback());
    }

}