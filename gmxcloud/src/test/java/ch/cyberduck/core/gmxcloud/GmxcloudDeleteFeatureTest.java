package ch.cyberduck.core.gmxcloud;

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

import ch.cyberduck.core.AbstractPath;
import ch.cyberduck.core.AlphanumericRandomStringService;
import ch.cyberduck.core.DisabledListProgressListener;
import ch.cyberduck.core.DisabledLoginCallback;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.features.Delete;
import ch.cyberduck.core.transfer.TransferStatus;
import ch.cyberduck.test.IntegrationTest;

import org.apache.commons.lang3.RandomUtils;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.util.Collections;
import java.util.EnumSet;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

@Category(IntegrationTest.class)
public class GmxcloudDeleteFeatureTest extends AbstractGmxcloudTest {

    @Test
    public void testDeleteFolder() throws Exception {
        final GmxcloudResourceIdProvider fileid = new GmxcloudResourceIdProvider(session);
        final Path directory = new GmxcloudDirectoryFeature(session, fileid).mkdir(new Path(
                new AlphanumericRandomStringService().random(), EnumSet.of(AbstractPath.Type.directory)), new TransferStatus());
        assertTrue(new GmxcloudFindFeature(session, fileid).find(directory, new DisabledListProgressListener()));
        new GmxcloudDeleteFeature(session, fileid).delete(Collections.singletonList(directory), new DisabledLoginCallback(), new Delete.DisabledCallback());
        assertFalse((new GmxcloudFindFeature(session, fileid).find(directory, new DisabledListProgressListener())));
    }

    @Test
    public void testDeleteFile() throws Exception {
        final GmxcloudResourceIdProvider fileid = new GmxcloudResourceIdProvider(session);
        final Path folder = new Path(new AlphanumericRandomStringService().random(), EnumSet.of(AbstractPath.Type.directory));
        final Path file = new Path(folder, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file));
        new GmxcloudDirectoryFeature(session, fileid).mkdir(folder, new TransferStatus());
        createFile(file, RandomUtils.nextBytes(511));
        assertTrue(new GmxcloudFindFeature(session, fileid).find(file));
        new GmxcloudDeleteFeature(session, fileid).delete(Collections.singletonList(file), new DisabledLoginCallback(), new Delete.DisabledCallback());
        assertFalse((new GmxcloudFindFeature(session, fileid).find(file, new DisabledListProgressListener())));
        assertTrue(new GmxcloudFindFeature(session, fileid).find(folder, new DisabledListProgressListener()));
        new GmxcloudDeleteFeature(session, fileid).delete(Collections.singletonList(folder), new DisabledLoginCallback(), new Delete.DisabledCallback());
        assertFalse((new GmxcloudFindFeature(session, fileid).find(folder, new DisabledListProgressListener())));
    }
}
