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
import ch.cyberduck.core.transfer.TransferStatus;
import ch.cyberduck.test.IntegrationTest;

import org.apache.commons.lang3.StringUtils;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.util.Collections;
import java.util.EnumSet;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

@Category(IntegrationTest.class)
public class SpectraTouchFeatureTest extends AbstractSpectraTest {

    @Test
    public void testFile() {
        assertFalse(new SpectraTouchFeature(session).isSupported(new Path("/", EnumSet.of(Path.Type.directory, Path.Type.volume)), StringUtils.EMPTY));
        assertTrue(new SpectraTouchFeature(session).isSupported(new Path(new Path("/", EnumSet.of(Path.Type.directory, Path.Type.volume)), "/container", EnumSet.of(Path.Type.directory, Path.Type.volume)), StringUtils.EMPTY));
    }

    @Test
    public void testTouch() throws Exception {
        final Path container = new SpectraDirectoryFeature(session, new SpectraWriteFeature(session)).mkdir(
                new Path(new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory, Path.Type.volume)), new TransferStatus());
        final Path test = new Path(container, new AlphanumericRandomStringService().random() + ".txt", EnumSet.of(Path.Type.file));
        new SpectraTouchFeature(session).touch(test, new TransferStatus());
        assertTrue(new SpectraFindFeature(session).find(test));
        new SpectraDeleteFeature(session).delete(Collections.singletonList(test), new DisabledLoginCallback(), new Delete.DisabledCallback());
        assertFalse(new SpectraFindFeature(session).find(test));
        new SpectraDeleteFeature(session).delete(Collections.singletonList(container), new DisabledLoginCallback(), new Delete.DisabledCallback());
    }
}
