package ch.cyberduck.core.gmxcloud;

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

import ch.cyberduck.core.AbstractPath;
import ch.cyberduck.core.AlphanumericRandomStringService;
import ch.cyberduck.core.DisabledConnectionCallback;
import ch.cyberduck.core.DisabledListProgressListener;
import ch.cyberduck.core.DisabledLoginCallback;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathAttributes;
import ch.cyberduck.core.exception.NotfoundException;
import ch.cyberduck.core.features.Delete;
import ch.cyberduck.core.transfer.TransferStatus;
import ch.cyberduck.test.IntegrationTest;

import org.apache.commons.io.IOUtils;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.EnumSet;

import static org.junit.Assert.*;

@Category(IntegrationTest.class)
public class GmxcloudReadFeatureTest extends AbstractGmxcloudTest {

    @Test
    public void readExistingFile() throws Exception {
        GmxcloudIdProvider fileid = new GmxcloudIdProvider(session);
        final Path container = new GmxcloudDirectoryFeature(session, fileid).mkdir(new Path("/TestFolderToDelete", EnumSet.of(AbstractPath.Type.directory)), new TransferStatus());
        assertTrue(new GmxcloudFindFeature(session, fileid).find(container, new DisabledListProgressListener()));
        final Path file = new Path(container, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file));
        final byte[] content = "This is simple test data".getBytes(StandardCharsets.UTF_8);
        createFile(file, content);
        assertTrue(new GmxcloudFindFeature(session, fileid).find(file));
        final PathAttributes attributes = new GmxcloudAttributesFinderFeature(session, fileid).find(file);
        assertEquals(content.length, attributes.getSize());
        final byte[] compare = new byte[content.length];
        final InputStream stream = new GmxcloudReadFeature(session, fileid).read(file, new TransferStatus(), new DisabledConnectionCallback());
        IOUtils.readFully(stream, compare);
        stream.close();
        assertArrayEquals(content, compare);
        new GmxcloudDeleteFeature(session, fileid).delete(Collections.singletonList(container), new DisabledLoginCallback(), new Delete.DisabledCallback());
    }

    @Test(expected = NotfoundException.class)
    public void readNonExistingFile() throws Exception {
        GmxcloudIdProvider fileid = new GmxcloudIdProvider(session);
        final Path container = new Path("/TestFolderToDelete", EnumSet.of(AbstractPath.Type.directory));
        final Path file = new Path(container, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file));
        byte[] content = "testContent".getBytes(StandardCharsets.UTF_8);
        new GmxcloudReadFeature(session, fileid).read(file, new TransferStatus().withLength(content.length), new DisabledConnectionCallback());
    }

}
