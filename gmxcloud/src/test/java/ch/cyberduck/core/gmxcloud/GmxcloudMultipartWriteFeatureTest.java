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
import ch.cyberduck.core.DisabledConnectionCallback;
import ch.cyberduck.core.DisabledListProgressListener;
import ch.cyberduck.core.DisabledLoginCallback;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.features.Delete;
import ch.cyberduck.core.http.HttpResponseOutputStream;
import ch.cyberduck.core.io.StreamCopier;
import ch.cyberduck.core.shared.DefaultFindFeature;
import ch.cyberduck.core.transfer.TransferStatus;
import ch.cyberduck.test.IntegrationTest;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.RandomUtils;
import org.junit.After;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Collections;
import java.util.EnumSet;

import static org.junit.Assert.*;

@Category(IntegrationTest.class)
public class GmxcloudMultipartWriteFeatureTest extends AbstractGmxcloudTest {

    @Test
    public void testMultipartWriteRead() throws Exception {
        GmxcloudIdProvider fileid = new GmxcloudIdProvider(session);
        final Path container = new GmxcloudDirectoryFeature(session, fileid).mkdir(new Path("/TestFolderToDelete", EnumSet.of(AbstractPath.Type.directory)), new TransferStatus());
        assertTrue(new GmxcloudFindFeature(session, fileid).find(container, new DisabledListProgressListener()));
        final Path file = new Path(container, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file));
        final byte[] content = RandomUtils.nextBytes(8943045);
        final TransferStatus status = new TransferStatus();
        status.setLength(-1L);
        final GmxcloudMultipartWriteFeature writer = new GmxcloudMultipartWriteFeature(session, fileid);
        final HttpResponseOutputStream<GmxcloudUploadResponse> out = writer.write(file, status, new DisabledConnectionCallback());
        assertNotNull(out);
        new StreamCopier(status, status).transfer(new ByteArrayInputStream(content), out);
        final String cdash64 = out.getStatus().getCdash64();
        assertNotNull(cdash64);
        assertTrue(new DefaultFindFeature(session).find(file));
        final byte[] compare = new byte[content.length];
        final InputStream stream = new GmxcloudReadFeature(session, fileid).read(file, new TransferStatus().withLength(content.length), new DisabledConnectionCallback());
        IOUtils.readFully(stream, compare);
        stream.close();
        assertArrayEquals(content, compare);
        new GmxcloudDeleteFeature(session, fileid).delete(Collections.singletonList(container), new DisabledLoginCallback(), new Delete.DisabledCallback());
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
