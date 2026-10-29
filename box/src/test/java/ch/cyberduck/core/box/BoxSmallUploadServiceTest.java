package ch.cyberduck.core.box;

/*
 * Copyright (c) 2002-2026 iterate GmbH. All rights reserved.
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
import ch.cyberduck.core.ConnectionCallback;
import ch.cyberduck.core.Local;
import ch.cyberduck.core.LoginCallback;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.ProgressListener;
import ch.cyberduck.core.box.io.swagger.client.model.File;
import ch.cyberduck.core.features.Delete;
import ch.cyberduck.core.features.Upload;
import ch.cyberduck.core.io.BandwidthThrottle;
import ch.cyberduck.core.io.Checksum;
import ch.cyberduck.core.io.HashAlgorithm;
import ch.cyberduck.core.io.StreamListener;
import ch.cyberduck.core.transfer.TransferStatus;
import ch.cyberduck.test.IntegrationTest;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.RandomUtils;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.util.Collections;
import java.util.EnumSet;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@Category(IntegrationTest.class)
public class BoxSmallUploadServiceTest extends AbstractBoxTest {

    @Test
    public void testUploadChecksumRequired() throws Exception {
        final BoxFileidProvider fileid = new BoxFileidProvider(session);
        final BoxSmallUploadService service = new BoxSmallUploadService();
        final Path container = new BoxDirectoryFeature(session, fileid).mkdir(new BoxWriteFeature(session, fileid), new Path(
                new AlphanumericRandomStringService().random(), EnumSet.of(AbstractPath.Type.directory)), new TransferStatus());
        final String name = new AlphanumericRandomStringService().random();
        final Path file = new Path(container, name, EnumSet.of(Path.Type.file));
        final Local local = new Local(System.getProperty("java.io.tmpdir"), name);
        final byte[] content = RandomUtils.nextBytes(1025);
        IOUtils.write(content, local.getOutputStream(false));
        final TransferStatus status = new TransferStatus().setLength(content.length);
        assertTrue(service.features(file, status).contains(Upload.Flags.checksumRequired));
        assertTrue(service.features(file, status).contains(Upload.Flags.checksumResponse));
        assertEquals(HashAlgorithm.sha1, service.checksum(file, status).getAlgorithm());
        // Calculated prior to the transfer and sent with the request
        final Checksum checksum = service.checksum(file, status).compute(local.getInputStream(), new TransferStatus());
        status.setChecksum(checksum);
        final File response = service.upload(new BoxWriteFeature(session, fileid), file, local, new BandwidthThrottle(BandwidthThrottle.UNLIMITED),
                ProgressListener.noop, StreamListener.noop, status, ConnectionCallback.noop);
        assertEquals(checksum.hash, response.getSha1());
        // Checksum returned by the server
        assertEquals(checksum, status.getResponse().getChecksum());
        assertEquals(checksum, new BoxAttributesFinderFeature(session, fileid).find(file).getChecksum());
        new BoxDeleteFeature(session, fileid).delete(Collections.singletonList(container), LoginCallback.noop, new Delete.DisabledCallback());
        local.delete();
    }
}
