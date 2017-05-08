package ch.cyberduck.core.onedrive;

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

import ch.cyberduck.core.Path;
import ch.cyberduck.test.IntegrationTest;

import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.util.EnumSet;
import java.util.UUID;

import static org.junit.Assert.assertFalse;

@Category(IntegrationTest.class)
public class OneDriveFindFeatureTest extends AbstractOneDriveTest {

    @Test
    public void testFindFileNotFound() throws Exception {
        final OneDriveFindFeature f = new OneDriveFindFeature(session);
        assertFalse(f.find(new Path(new OneDriveHomeFinderFeature(session).find(), UUID.randomUUID().toString(), EnumSet.of(Path.Type.file))));
    }

    @Test
    public void testFindDriveNotFound() throws Exception {
        final OneDriveFindFeature f = new OneDriveFindFeature(session);
        assertFalse(f.find(new Path(UUID.randomUUID().toString(), EnumSet.of(Path.Type.file))));
    }
}