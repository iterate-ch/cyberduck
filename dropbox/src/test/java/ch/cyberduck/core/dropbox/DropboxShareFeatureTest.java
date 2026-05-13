package ch.cyberduck.core.dropbox;

/*
 * Copyright (c) 2002-2026 iterate GmbH. All rights reserved.
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
import ch.cyberduck.core.DescriptiveUrl;
import ch.cyberduck.core.DisabledPasswordCallback;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.features.Delete;
import ch.cyberduck.core.features.Share;
import ch.cyberduck.core.shared.DefaultHomeFinderService;
import ch.cyberduck.core.transfer.TransferStatus;
import ch.cyberduck.test.IntegrationTest;

import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;

import static org.junit.Assert.*;

@Category(IntegrationTest.class)
public class DropboxShareFeatureTest extends AbstractDropboxTest {

    @Test
    public void toDownloadUrl() throws Exception {
        final Path root = new DefaultHomeFinderService(session).find();
        final Path folder = new DropboxDirectoryFeature(session).mkdir(new DropboxWriteFeature(session), new Path(root,
                new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory)), null);
        {
            final DescriptiveUrl url = new DropboxShareFeature(session).toDownloadUrl(folder, Share.Sharee.world, null, new DisabledPasswordCallback());
            assertNotNull(url.getUrl());
            assertEquals(url, new DropboxShareFeature(session).toDownloadUrl(folder, Share.Sharee.world, null, new DisabledPasswordCallback()));
        }
        final Path file = new Path(folder, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file));
        new DropboxTouchFeature(session).touch(new DropboxWriteFeature(session), file, new TransferStatus());
        {
            final DescriptiveUrl url = new DropboxShareFeature(session).toDownloadUrl(file, Share.Sharee.world, null, new DisabledPasswordCallback());
            assertNotNull(url.getUrl());
            assertEquals(url, new DropboxShareFeature(session).toDownloadUrl(file, Share.Sharee.world, null, new DisabledPasswordCallback()));
        }
        new DropboxDeleteFeature(session).delete(Arrays.asList(file, folder), new DisabledPasswordCallback(), new Delete.DisabledCallback());
    }

    @Test
    public void toUploadUrl() throws Exception {
        final Path root = new DefaultHomeFinderService(session).find();
        final Path folder = new DropboxDirectoryFeature(session).mkdir(new DropboxWriteFeature(session), new Path(root,
                new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory)), null);
        {
            final DescriptiveUrl url = new DropboxShareFeature(session).toUploadUrl(folder, Share.Sharee.world, null, new DisabledPasswordCallback());
            assertNotNull(url.getUrl());
            assertNotEquals(url, new DropboxShareFeature(session).toUploadUrl(folder, Share.Sharee.world, null, new DisabledPasswordCallback()));
        }
        new DropboxDeleteFeature(session).delete(Collections.singletonList(folder), new DisabledPasswordCallback(), new Delete.DisabledCallback());
    }
}