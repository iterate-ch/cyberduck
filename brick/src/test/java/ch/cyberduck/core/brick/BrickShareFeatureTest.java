package ch.cyberduck.core.brick;

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
import ch.cyberduck.core.DisabledPasswordCallback;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.features.Delete;
import ch.cyberduck.core.features.Share;
import ch.cyberduck.core.transfer.TransferStatus;
import ch.cyberduck.test.IntegrationTest;

import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.util.Collections;
import java.util.EnumSet;

import static org.junit.Assert.assertNotNull;

@Category(IntegrationTest.class)
public class BrickShareFeatureTest extends AbstractBrickTest {

    @Test
    public void toDownloadUrl() throws Exception {
        final Path directory = new BrickDirectoryFeature(session).mkdir(new Path(new AlphanumericRandomStringService().random(),
            EnumSet.of(Path.Type.directory, Path.Type.volume)), new TransferStatus());
        final Path test = new BrickTouchFeature(session).touch(
            new Path(directory, String.format("%s", new AlphanumericRandomStringService().random()), EnumSet.of(Path.Type.file)), new TransferStatus());
        assertNotNull(new BrickShareFeature(session).toDownloadUrl(test, Share.Sharee.world, null, new DisabledPasswordCallback()).getUrl());
        new BrickDeleteFeature(session).delete(Collections.singletonList(directory), new DisabledPasswordCallback(), new Delete.DisabledCallback());
    }
}
