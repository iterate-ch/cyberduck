package ch.cyberduck.core.googlestorage;

/*
 * Copyright (c) 2002-2013 David Kocher. All rights reserved.
 * http://cyberduck.ch/
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
 *
 * Bug fixes, suggestions and comments should be sent to feedback@cyberduck.ch
 */

import ch.cyberduck.core.AsciiRandomStringService;
import ch.cyberduck.core.DisabledLoginCallback;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.VersioningConfiguration;
import ch.cyberduck.core.features.Delete;
import ch.cyberduck.core.features.Versioning;
import ch.cyberduck.core.transfer.TransferStatus;
import ch.cyberduck.test.IntegrationTest;

import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.util.Collections;
import java.util.EnumSet;
import java.util.Locale;

import static org.junit.Assert.assertTrue;

@Category(IntegrationTest.class)
public class GoogleStorageVersioningFeatureTest extends AbstractGoogleStorageTest {

    @Test
    public void testSetConfiguration() throws Exception {
        final Path container = new Path(new AsciiRandomStringService().random().toLowerCase(Locale.ROOT), EnumSet.of(Path.Type.directory, Path.Type.volume));
        new GoogleStorageDirectoryFeature(session).mkdir(container, new TransferStatus());
        final Versioning feature = new GoogleStorageVersioningFeature(session);
        feature.setConfiguration(container, new DisabledLoginCallback(), new VersioningConfiguration(true, false));
        assertTrue(feature.getConfiguration(container).isEnabled());
        new GoogleStorageDeleteFeature(session).delete(Collections.<Path>singletonList(container), new DisabledLoginCallback(), new Delete.DisabledCallback());
    }
}
