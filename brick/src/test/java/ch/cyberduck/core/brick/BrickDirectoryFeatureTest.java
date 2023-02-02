package ch.cyberduck.core.brick;

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
import ch.cyberduck.core.exception.ConflictException;
import ch.cyberduck.core.exception.LockedException;
import ch.cyberduck.core.features.Delete;
import ch.cyberduck.core.transfer.TransferStatus;
import ch.cyberduck.test.IntegrationTest;

import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.util.Collections;
import java.util.EnumSet;

import static org.junit.Assert.fail;

@Category(IntegrationTest.class)
public class BrickDirectoryFeatureTest extends AbstractBrickTest {

    @Test
    public void testMakeDirectory() throws Exception {
        final Path directory = new BrickDirectoryFeature(session).mkdir(
                new Path(new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory)), new TransferStatus());
        try {
            new BrickDirectoryFeature(session).mkdir(directory, new TransferStatus());
            fail();
        }
        catch(ConflictException | LockedException e) {
            // Expected
        }
        new BrickDeleteFeature(session).delete(Collections.singletonList(directory), new DisabledLoginCallback(), new Delete.DisabledCallback());
    }
}