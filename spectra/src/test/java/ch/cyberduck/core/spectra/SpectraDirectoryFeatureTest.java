/*
 * Copyright (c) 2015-2016 Spectra Logic Corporation. All rights reserved.
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

package ch.cyberduck.core.spectra;

import ch.cyberduck.core.AlphanumericRandomStringService;
import ch.cyberduck.core.DisabledLoginCallback;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.features.Delete;
import ch.cyberduck.core.shared.DefaultFindFeature;
import ch.cyberduck.core.shared.DefaultHomeFinderService;
import ch.cyberduck.core.transfer.TransferStatus;
import ch.cyberduck.test.IntegrationTest;

import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.util.Collections;
import java.util.EnumSet;

import static org.junit.Assert.assertTrue;

@Category(IntegrationTest.class)
public class SpectraDirectoryFeatureTest extends AbstractSpectraTest {

    @Test
    public void testCreateBucket() throws Exception {
        final SpectraDirectoryFeature feature = new SpectraDirectoryFeature(session, new SpectraWriteFeature(session));
        final Path test = new Path(new DefaultHomeFinderService(session).find(), new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory, Path.Type.volume));
        feature.mkdir(test, new TransferStatus());
        assertTrue(new SpectraFindFeature(session).find(test));
        new SpectraDeleteFeature(session).delete(Collections.singletonList(test), new DisabledLoginCallback(), new Delete.DisabledCallback());
    }

    @Test
    public void testCreatePlaceholder() throws Exception {
        final String bucketname = new AlphanumericRandomStringService().random();
        final String name = new AlphanumericRandomStringService().random();
        final Path container = new SpectraDirectoryFeature(session, new SpectraWriteFeature(session)).mkdir(
                new Path(bucketname, EnumSet.of(Path.Type.directory, Path.Type.volume)), new TransferStatus());
        final Path test = new SpectraDirectoryFeature(session, new SpectraWriteFeature(session)).mkdir(
            new Path(container, name, EnumSet.of(Path.Type.directory)), new TransferStatus());
        assertTrue(new SpectraFindFeature(session).find(test));
        assertTrue(new DefaultFindFeature(session).find(test));
        new SpectraDeleteFeature(session).delete(Collections.singletonList(container), new DisabledLoginCallback(), new Delete.DisabledCallback());
    }
}
