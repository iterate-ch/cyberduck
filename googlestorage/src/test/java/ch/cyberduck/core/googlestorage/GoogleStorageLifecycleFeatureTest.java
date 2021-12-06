package ch.cyberduck.core.googlestorage;

/*
 * Copyright (c) 2002-2019 iterate GmbH. All rights reserved.
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

import ch.cyberduck.core.AsciiRandomStringService;
import ch.cyberduck.core.DisabledLoginCallback;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.features.Delete;
import ch.cyberduck.core.lifecycle.LifecycleConfiguration;
import ch.cyberduck.core.shared.DefaultHomeFinderService;
import ch.cyberduck.core.transfer.TransferStatus;
import ch.cyberduck.test.IntegrationTest;

import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.util.Collections;
import java.util.EnumSet;
import java.util.Locale;

import static org.junit.Assert.assertEquals;

@Category(IntegrationTest.class)
public class GoogleStorageLifecycleFeatureTest extends AbstractGoogleStorageTest {

    @Test
    public void testSetConfiguration() throws Exception {
        final Path test = new Path(new DefaultHomeFinderService(session).find(),
                new AsciiRandomStringService().random().toLowerCase(Locale.ROOT), EnumSet.of(Path.Type.directory, Path.Type.volume));
        new GoogleStorageDirectoryFeature(session).mkdir(test, new TransferStatus());
        final GoogleStorageLifecycleFeature feature = new GoogleStorageLifecycleFeature(session);
        assertEquals(LifecycleConfiguration.empty(), feature.getConfiguration(test));
        feature.setConfiguration(test, new LifecycleConfiguration(1, 2));
        final LifecycleConfiguration read = feature.getConfiguration(test);
        assertEquals(1, read.getTransition(), 0L);
        assertEquals(2, read.getExpiration(), 0L);
        feature.setConfiguration(test, LifecycleConfiguration.empty());
        assertEquals(LifecycleConfiguration.empty(), feature.getConfiguration(test));
        new GoogleStorageDeleteFeature(session).delete(Collections.singletonList(test), new DisabledLoginCallback(), new Delete.DisabledCallback());
    }

    @Test
    public void testGetConfigurationAccessDenied() throws Exception {
        assertEquals(LifecycleConfiguration.empty(), new GoogleStorageLifecycleFeature(session).getConfiguration(
                new Path("bucket", EnumSet.of(Path.Type.directory))
        ));
    }
}
