package ch.cyberduck.core.storegate;

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

import ch.cyberduck.core.AlphanumericRandomStringService;
import ch.cyberduck.core.DisabledPasswordCallback;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.features.Delete;
import ch.cyberduck.core.transfer.TransferStatus;
import ch.cyberduck.test.IntegrationTest;

import org.junit.Ignore;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.util.Collections;
import java.util.EnumSet;

import static org.junit.Assert.assertNotNull;

@Category(IntegrationTest.class)
@Ignore
public class StoregateShareFeatureTest extends AbstractStoregateTest {

    @Test
    public void toDownloadUrl() throws Exception {
        final StoregateIdProvider nodeid = new StoregateIdProvider(session);
        final Path room = new StoregateDirectoryFeature(session, nodeid).mkdir(
            new Path(String.format("/My files/%s", new AlphanumericRandomStringService().random()),
                EnumSet.of(Path.Type.directory, Path.Type.volume)), new TransferStatus());
        final Path test = new StoregateTouchFeature(session, nodeid).touch(
            new Path(room, String.format("%s", new AlphanumericRandomStringService().random()), EnumSet.of(Path.Type.file)), new TransferStatus());
        assertNotNull(new StoregateShareFeature(session, nodeid).toDownloadUrl(test, null, new DisabledPasswordCallback()).getUrl());
        new StoregateDeleteFeature(session, nodeid).delete(Collections.singletonList(room), new DisabledPasswordCallback(), new Delete.DisabledCallback());
    }

    @Test
    public void toUploadUrl() throws Exception {
        final StoregateIdProvider nodeid = new StoregateIdProvider(session);
        final Path room = new StoregateDirectoryFeature(session, nodeid).mkdir(
            new Path(String.format("/My files/%s", new AlphanumericRandomStringService().random()),
                EnumSet.of(Path.Type.directory, Path.Type.volume)), new TransferStatus());
        assertNotNull(new StoregateShareFeature(session, nodeid).toUploadUrl(room, null, new DisabledPasswordCallback()).getUrl());
        new StoregateDeleteFeature(session, nodeid).delete(Collections.singletonList(room), new DisabledPasswordCallback(), new Delete.DisabledCallback());
    }
}
