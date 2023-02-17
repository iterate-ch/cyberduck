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
import ch.cyberduck.core.DisabledPasswordCallback;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.features.Delete;
import ch.cyberduck.core.features.Home;
import ch.cyberduck.core.features.PromptUrlProvider;
import ch.cyberduck.core.transfer.TransferStatus;
import ch.cyberduck.test.IntegrationTest;

import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.util.Collections;
import java.util.EnumSet;

import static org.junit.Assert.*;

@Category(IntegrationTest.class)
public class BoxShareFeatureTest extends AbstractBoxTest {

    @Test
    public void testFolder() throws Exception {
        final BoxFileidProvider fileid = new BoxFileidProvider(session);
        final Path directory = new BoxDirectoryFeature(session, fileid).mkdir(new Path(new AlphanumericRandomStringService().random(),
                EnumSet.of(Path.Type.directory)), new TransferStatus());
        final BoxShareFeature feature = new BoxShareFeature(session, fileid);
        assertTrue(feature.isSupported(directory, PromptUrlProvider.Type.download));
        assertFalse(feature.isSupported(directory, PromptUrlProvider.Type.upload));
        assertNotNull(feature.toDownloadUrl(directory, null, new DisabledPasswordCallback()).getUrl());
        new BoxDeleteFeature(session, fileid).delete(Collections.singletonList(directory), new DisabledPasswordCallback(), new Delete.DisabledCallback());
    }

    @Test
    public void testFile() throws Exception {
        final BoxFileidProvider fileid = new BoxFileidProvider(session);
        final Path test = new BoxTouchFeature(session, fileid).touch(
                new Path(Home.ROOT, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file)), new TransferStatus());
        final BoxShareFeature feature = new BoxShareFeature(session, fileid);
        assertTrue(feature.isSupported(test, PromptUrlProvider.Type.download));
        assertFalse(feature.isSupported(test, PromptUrlProvider.Type.upload));
        assertNotNull(feature.toDownloadUrl(test, null, new DisabledPasswordCallback()).getUrl());
        new BoxDeleteFeature(session, fileid).delete(Collections.singletonList(test), new DisabledPasswordCallback(), new Delete.DisabledCallback());
    }
}