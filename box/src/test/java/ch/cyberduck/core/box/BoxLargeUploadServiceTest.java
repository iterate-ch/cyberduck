package ch.cyberduck.core.box;

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
import ch.cyberduck.core.DisabledLoginCallback;
import ch.cyberduck.core.Local;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.box.io.swagger.client.model.File;
import ch.cyberduck.core.features.Delete;
import ch.cyberduck.core.io.BandwidthThrottle;
import ch.cyberduck.core.transfer.TransferStatus;
import ch.cyberduck.test.IntegrationTest;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.RandomUtils;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.util.Collections;
import java.util.EnumSet;

import static org.junit.Assert.*;

@Category(IntegrationTest.class)
public class BoxLargeUploadServiceTest extends AbstractBoxTest {

    @Test
    public void testUploadLargeFileInChunks() throws Exception {
        final BoxFileidProvider fileid = new BoxFileidProvider(session);
        final BoxLargeUploadService s = new BoxLargeUploadService(session, fileid, new BoxChunkedWriteFeature(session, fileid));
        final Path container = new BoxDirectoryFeature(session, fileid).mkdir(new Path(
                new AlphanumericRandomStringService().random(), EnumSet.of(AbstractPath.Type.directory)), new TransferStatus());
        final String name = new AlphanumericRandomStringService().random();
        final Path file = new Path(container, name, EnumSet.of(Path.Type.file));
        final Local local = new Local(System.getProperty("java.io.tmpdir"), name);
        final byte[] content = RandomUtils.nextBytes(20 * 1024 * 1024);
        IOUtils.write(content, local.getOutputStream(false));
        final TransferStatus status = new TransferStatus();
        status.setChecksum(new BoxBase64SHA1ChecksumCompute().compute(local.getInputStream(), new TransferStatus()));
        status.setLength(content.length);
        final BytecountStreamListener count = new BytecountStreamListener();
        final File response = s.upload(file, local, new BandwidthThrottle(BandwidthThrottle.UNLIMITED), count, status, new DisabledConnectionCallback());
        assertTrue(status.isComplete());
        assertNotNull(response.getSha1());
        assertEquals(content.length, count.getSent());
        assertTrue(status.isComplete());
        assertEquals(content.length, status.getResponse().getSize());
        assertTrue(new BoxFindFeature(session, fileid).find(file));
        assertEquals(content.length, new BoxAttributesFinderFeature(session, fileid).find(file).getSize());
        final byte[] compare = new byte[content.length];
        IOUtils.readFully(new BoxReadFeature(session, fileid).read(file, new TransferStatus().withLength(content.length), new DisabledConnectionCallback()), compare);
        assertArrayEquals(content, compare);
        new BoxDeleteFeature(session, fileid).delete(Collections.singletonList(container), new DisabledLoginCallback(), new Delete.DisabledCallback());
        local.delete();
    }
}
