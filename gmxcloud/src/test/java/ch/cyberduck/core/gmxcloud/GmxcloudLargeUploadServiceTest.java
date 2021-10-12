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
import ch.cyberduck.core.BytecountStreamListener;
import ch.cyberduck.core.DisabledConnectionCallback;
import ch.cyberduck.core.DisabledListProgressListener;
import ch.cyberduck.core.DisabledLoginCallback;
import ch.cyberduck.core.Local;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.features.Delete;
import ch.cyberduck.core.io.BandwidthThrottle;
import ch.cyberduck.core.transfer.TransferStatus;
import ch.cyberduck.test.IntegrationTest;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.RandomUtils;
import org.junit.After;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.util.Collections;
import java.util.EnumSet;

import static org.junit.Assert.*;

@Category(IntegrationTest.class)
public class GmxcloudLargeUploadServiceTest extends AbstractGmxcloudTest {


    @Test
    public void testUploadLargeFileInChunks() throws Exception {
        GmxcloudIdProvider fileid = new GmxcloudIdProvider(session);
        final GmxcloudLargeUploadService gmxcloudLargeUploadService = new GmxcloudLargeUploadService(session, fileid, new GmxcloudWriteFeature(session, fileid), 4 * 1024L * 1024L, 5);
        final Path container = new GmxcloudDirectoryFeature(session, fileid).mkdir(new Path("/TestFolderToDelete", EnumSet.of(AbstractPath.Type.directory)), new TransferStatus());
        assertTrue(new GmxcloudFindFeature(session, fileid).find(container, new DisabledListProgressListener()));
        final String name = new AlphanumericRandomStringService().random();
        final Path file = new Path(container, name, EnumSet.of(Path.Type.file));
        final Local local = new Local(System.getProperty("java.io.tmpdir"), name);
        final int length = 5242881;
        final byte[] content = RandomUtils.nextBytes(length);
        IOUtils.write(content, local.getOutputStream(false));
        final TransferStatus status = new TransferStatus();
        status.setLength(content.length);
        final BytecountStreamListener count = new BytecountStreamListener();
        final GmxcloudUploadResponse gmxcloudUploadResponse = gmxcloudLargeUploadService.upload(file, local, new BandwidthThrottle(BandwidthThrottle.UNLIMITED), count, status, new DisabledConnectionCallback());
        assertNotNull(gmxcloudUploadResponse.getCdash64());
        assertEquals(content.length, count.getSent());
        assertTrue(status.isComplete());
        assertTrue(new GmxcloudFindFeature(session, fileid).find(file));
        assertEquals(content.length, new GmxcloudAttributesFinderFeature(session, fileid).find(file).getSize());
        final byte[] compare = new byte[length];
        IOUtils.readFully(new GmxcloudReadFeature(session, fileid).read(file, new TransferStatus().withLength(length), new DisabledConnectionCallback()), compare);
        assertArrayEquals(content, compare);
        new GmxcloudDeleteFeature(session, fileid).delete(Collections.singletonList(container), new DisabledLoginCallback(), new Delete.DisabledCallback());
        local.delete();
    }

    @After
    public void deleteContainer() throws BackgroundException {
        GmxcloudIdProvider fileid = new GmxcloudIdProvider(session);
        final Path container = new Path("/TestFolderToDelete", EnumSet.of(AbstractPath.Type.directory));
        if(new GmxcloudFindFeature(session, fileid).find(container, new DisabledListProgressListener())) {
            new GmxcloudDeleteFeature(session, fileid).delete(Collections.singletonList(container), new DisabledLoginCallback(), new Delete.DisabledCallback());
        }
    }


}
