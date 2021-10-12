package ch.cyberduck.core.gmxcloud;

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

import ch.cyberduck.core.AbstractPath;
import ch.cyberduck.core.DisabledConnectionCallback;
import ch.cyberduck.core.DisabledLoginCallback;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.features.Delete;
import ch.cyberduck.core.transfer.TransferStatus;
import ch.cyberduck.test.IntegrationTest;

import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.EnumSet;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

@Category(IntegrationTest.class)
public class GmxcloudMoveFeatureTest extends AbstractGmxcloudTest {

    @Test
    public void testMoveFile() throws Exception {
        GmxcloudIdProvider fileid = new GmxcloudIdProvider(session);
        final Path sourceFolder = new Path("/sourceFolder", EnumSet.of(AbstractPath.Type.directory));
        final Path file = new Path(sourceFolder, "testfile.txt", EnumSet.of(Path.Type.file));
        new GmxcloudDirectoryFeature(session, fileid).mkdir(sourceFolder, new TransferStatus());
        createFile(file, "This is simple test data".getBytes(StandardCharsets.UTF_8));
        assertTrue(new GmxcloudFindFeature(session, fileid).find(file));
        final Path targetFolder = new GmxcloudDirectoryFeature(session, fileid).mkdir(new Path("/targetFolder", EnumSet.of(AbstractPath.Type.directory)), new TransferStatus());
        new GmxcloudMoveFeature(session, fileid).move(file, targetFolder, new TransferStatus(), new Delete.DisabledCallback(), new DisabledConnectionCallback());
        assertFalse(new GmxcloudFindFeature(session, fileid).find(file));
        assertTrue(new GmxcloudFindFeature(session, fileid).find(new Path(targetFolder, file.getName(), EnumSet.of(AbstractPath.Type.file))));
        new GmxcloudDeleteFeature(session, fileid).delete(Collections.singletonList(sourceFolder), new DisabledLoginCallback(), new Delete.DisabledCallback());
        new GmxcloudDeleteFeature(session, fileid).delete(Collections.singletonList(targetFolder), new DisabledLoginCallback(), new Delete.DisabledCallback());
    }
}
