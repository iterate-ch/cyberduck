package ch.cyberduck.core.googlestorage;

/*
 * Copyright (c) 2002-2016 iterate GmbH. All rights reserved.
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

import ch.cyberduck.core.AsciiRandomStringService;
import ch.cyberduck.core.DisabledLoginCallback;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.cdn.Distribution;
import ch.cyberduck.core.cdn.DistributionConfiguration;
import ch.cyberduck.core.cdn.features.Cname;
import ch.cyberduck.core.cdn.features.DistributionLogging;
import ch.cyberduck.core.cdn.features.Index;
import ch.cyberduck.core.features.Delete;
import ch.cyberduck.core.transfer.TransferStatus;
import ch.cyberduck.test.IntegrationTest;

import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.util.Collections;
import java.util.EnumSet;
import java.util.Locale;

import static org.junit.Assert.*;

@Category(IntegrationTest.class)
public class GoogleStorageWebsiteDistributionConfigurationTest extends AbstractGoogleStorageTest {

    @Test
    public void testGetMethods() {
        final DistributionConfiguration configuration
            = new GoogleStorageWebsiteDistributionConfiguration(session);
        assertEquals(Collections.singletonList(Distribution.WEBSITE), configuration.getMethods(new Path("cyberduck-test-eu", EnumSet.of(Path.Type.directory, Path.Type.volume))));
    }

    @Test
    public void testWrite() throws Exception {
        final DistributionConfiguration configuration = new GoogleStorageWebsiteDistributionConfiguration(session);
        final Path bucket = new Path(new AsciiRandomStringService().random().toLowerCase(Locale.ROOT), EnumSet.of(Path.Type.directory, Path.Type.volume));
        new GoogleStorageDirectoryFeature(session).mkdir(bucket, new TransferStatus());
        configuration.write(bucket, new Distribution(Distribution.WEBSITE, null, true), new DisabledLoginCallback());
        final Distribution distribution = configuration.read(bucket, Distribution.WEBSITE, new DisabledLoginCallback());
        assertTrue(distribution.isEnabled());
        assertEquals(configuration.getName(), distribution.getName());
        new GoogleStorageDeleteFeature(session).delete(Collections.<Path>singletonList(bucket), new DisabledLoginCallback(), new Delete.DisabledCallback());
    }

    @Test
    public void testFeatures() {
        final DistributionConfiguration d = new GoogleStorageWebsiteDistributionConfiguration(session);
        assertNotNull(d.getFeature(Index.class, Distribution.WEBSITE));
        assertNotNull(d.getFeature(DistributionLogging.class, Distribution.WEBSITE));
        assertNull(d.getFeature(Cname.class, Distribution.WEBSITE));
    }
}
