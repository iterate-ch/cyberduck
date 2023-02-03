package ch.cyberduck.core.b2;

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

import ch.cyberduck.core.AlphanumericRandomStringService;
import ch.cyberduck.core.DisabledLoginCallback;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.features.Delete;
import ch.cyberduck.core.lifecycle.LifecycleConfiguration;
import ch.cyberduck.core.transfer.TransferStatus;
import ch.cyberduck.test.IntegrationTest;

import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.util.Collections;
import java.util.EnumSet;

import static org.junit.Assert.assertEquals;

@Category(IntegrationTest.class)
public class B2LifecycleFeatureTest extends AbstractB2Test {

    @Test
    public void testSetConfiguration() throws Exception {
        final Path bucket = new Path(new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory, Path.Type.volume));
        final B2VersionIdProvider fileid = new B2VersionIdProvider(session);
        new B2DirectoryFeature(session, fileid).mkdir(bucket, new TransferStatus());
        assertEquals(LifecycleConfiguration.empty(), new B2LifecycleFeature(session, fileid).getConfiguration(bucket));
        new B2LifecycleFeature(session, fileid).setConfiguration(bucket, new LifecycleConfiguration(1, 30));
        final LifecycleConfiguration configuration = new B2LifecycleFeature(session, fileid).getConfiguration(bucket);
        assertEquals(30, configuration.getExpiration(), 0L);
        assertEquals(1, configuration.getTransition(), 0L);
        new B2LifecycleFeature(session, fileid).setConfiguration(bucket, LifecycleConfiguration.empty());
        assertEquals(LifecycleConfiguration.empty(), new B2LifecycleFeature(session, fileid).getConfiguration(bucket));
        new B2DeleteFeature(session, fileid).delete(Collections.singletonList(bucket), new DisabledLoginCallback(), new Delete.DisabledCallback());
    }
}
