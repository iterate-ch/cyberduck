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
import ch.cyberduck.core.DisabledLoginCallback;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.features.Delete;
import ch.cyberduck.core.shared.DefaultHomeFinderService;
import ch.cyberduck.core.transfer.TransferStatus;

import org.junit.Test;

import java.util.Collections;
import java.util.EnumSet;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class SMBFindFeatureTest extends AbstractSMBTest {

    @Test
    public void testFindShareNotFound() throws Exception {
        assertFalse(new SMBFindFeature(session).find(new Path(new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory, Path.Type.volume))));
    }

    @Test
    public void testFindNotFound() throws Exception {
        assertFalse(new SMBFindFeature(session).find(new Path(new DefaultHomeFinderService(session).find(), new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file))));
    }

    @Test
    public void testFindDirectory() throws Exception {
        assertTrue(new SMBFindFeature(session).find(new DefaultHomeFinderService(session).find()));
    }

    @Test
    public void testFindFile() throws Exception {
        final Path file = new Path(new DefaultHomeFinderService(session).find(), new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file));
        new SMBTouchFeature(session).touch(file, new TransferStatus());
        assertTrue(new SMBFindFeature(session).find(file));
        assertFalse(new SMBFindFeature(session).find(new Path(file.getAbsolute(), EnumSet.of(Path.Type.directory))));
        new SMBDeleteFeature(session).delete(Collections.singletonList(file), new DisabledLoginCallback(), new Delete.DisabledCallback());
    }

    @Test
    public void testFindRoot() throws Exception {
        assertTrue(new SMBFindFeature(session).find(new Path("/", EnumSet.of(Path.Type.directory))));
    }
}