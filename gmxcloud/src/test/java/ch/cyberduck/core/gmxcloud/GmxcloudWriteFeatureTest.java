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
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathAttributes;
import ch.cyberduck.core.features.Delete;
import ch.cyberduck.core.http.HttpResponseOutputStream;
import ch.cyberduck.core.io.Checksum;
import ch.cyberduck.core.io.StreamCopier;
import ch.cyberduck.core.transfer.TransferStatus;
import ch.cyberduck.test.IntegrationTest;

import org.apache.commons.io.IOUtils;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.EnumSet;

import static org.junit.Assert.*;

@Category(IntegrationTest.class)
public class GmxcloudWriteFeatureTest extends AbstractGmxcloudTest {

    @Test
    public void testWriteSinglePart() throws Exception {
        GmxcloudIdProvider fileid = new GmxcloudIdProvider(session);
        final GmxcloudWriteFeature feature = new GmxcloudWriteFeature(session, fileid);
        final Path container = new GmxcloudDirectoryFeature(session, fileid).mkdir(new Path("/TestFolderToDelete", EnumSet.of(AbstractPath.Type.directory)), new TransferStatus());
        final GmxcloudFindFeature gmxcloudFindFeature = new GmxcloudFindFeature(session, fileid);
        assertTrue(gmxcloudFindFeature.find(container, new DisabledListProgressListener()));
        final Path file = new Path(container, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file));
        final byte[] content = "This is simple test data".getBytes(StandardCharsets.UTF_8);
        final TransferStatus status = new TransferStatus().withLength(content.length);
        final Checksum expectedChecksum = new GmxcloudCdash64Compute().compute(new ByteArrayInputStream(content), status);
        status.withChecksum(expectedChecksum);
        final HttpResponseOutputStream<GmxcloudUploadResponse> out = feature.write(file, status, new DisabledConnectionCallback());
        final ByteArrayInputStream in = new ByteArrayInputStream(content);
        final TransferStatus progress = new TransferStatus();
        final BytecountStreamListener count = new BytecountStreamListener();
        new StreamCopier(new TransferStatus(), progress).withListener(count).transfer(in, out);
        assertEquals(content.length, count.getSent());
        in.close();
        out.close();
        assertEquals(expectedChecksum.hash, out.getStatus().getCdash64());
        assertTrue(gmxcloudFindFeature.find(file));
        final PathAttributes attributes = new GmxcloudAttributesFinderFeature(session, fileid).find(file);
        assertEquals(content.length, attributes.getSize());
        final byte[] compare = new byte[content.length];
        final InputStream stream = new GmxcloudReadFeature(session, fileid).read(file, new TransferStatus().withLength(content.length), new DisabledConnectionCallback());
        IOUtils.readFully(stream, compare);
        stream.close();
        assertArrayEquals(content, compare);
        new GmxcloudDeleteFeature(session, fileid).delete(Collections.singletonList(container), new DisabledLoginCallback(), new Delete.DisabledCallback());
    }
}
