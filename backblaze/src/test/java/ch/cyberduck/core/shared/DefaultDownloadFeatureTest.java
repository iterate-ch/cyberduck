package ch.cyberduck.core.shared;

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
import ch.cyberduck.core.DisabledConnectionCallback;
import ch.cyberduck.core.DisabledLoginCallback;
import ch.cyberduck.core.Local;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.b2.AbstractB2Test;
import ch.cyberduck.core.b2.B2DeleteFeature;
import ch.cyberduck.core.b2.B2ReadFeature;
import ch.cyberduck.core.b2.B2TouchFeature;
import ch.cyberduck.core.b2.B2VersionIdProvider;
import ch.cyberduck.core.b2.B2WriteFeature;
import ch.cyberduck.core.features.Delete;
import ch.cyberduck.core.http.HttpResponseOutputStream;
import ch.cyberduck.core.io.BandwidthThrottle;
import ch.cyberduck.core.io.DisabledStreamListener;
import ch.cyberduck.core.io.StreamCopier;
import ch.cyberduck.core.transfer.TransferStatus;
import ch.cyberduck.test.IntegrationTest;

import org.apache.commons.io.IOUtils;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Random;
import java.util.UUID;

import synapticloop.b2.response.B2FileResponse;
import synapticloop.b2.response.BaseB2Response;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertNotNull;

@Category(IntegrationTest.class)
public class DefaultDownloadFeatureTest extends AbstractB2Test {

    @Test
    public void testOverwrite() throws Exception {
        final Path bucket = new Path("test-cyberduck", EnumSet.of(Path.Type.directory, Path.Type.volume));
        final B2VersionIdProvider fileid = new B2VersionIdProvider(session);
        final Path test = new B2TouchFeature(session, fileid).touch(
                new Path(bucket, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file)), new TransferStatus().withLength(0L));
        final byte[] content = new byte[39864];
        new Random().nextBytes(content);
        {
            final TransferStatus status = new TransferStatus().withLength(content.length);
            final HttpResponseOutputStream<BaseB2Response> out = new B2WriteFeature(session, fileid).write(test, status, new DisabledConnectionCallback());
            assertNotNull(out);
            new StreamCopier(status, status).withLimit(new Long(content.length)).transfer(new ByteArrayInputStream(content), out);
            out.close();
            test.attributes().setVersionId(((B2FileResponse) out.getStatus()).getFileId());
        }
        final Local local = new Local(System.getProperty("java.io.tmpdir"), UUID.randomUUID().toString());
        final TransferStatus status = new TransferStatus().withLength(content.length);
        new DefaultDownloadFeature(new B2ReadFeature(session, fileid)).download(
                test, local, new BandwidthThrottle(BandwidthThrottle.UNLIMITED), new DisabledStreamListener(),
                status,
            new DisabledConnectionCallback());
        final byte[] buffer = new byte[content.length];
        final InputStream in = local.getInputStream();
        IOUtils.readFully(in, buffer);
        in.close();
        assertArrayEquals(content, buffer);
        new B2DeleteFeature(session, fileid).delete(Collections.singletonList(test), new DisabledLoginCallback(), new Delete.DisabledCallback());
    }

    @Test
    public void testTransferUnknownSize() throws Exception {
        final Path bucket = new Path("test-cyberduck", EnumSet.of(Path.Type.directory, Path.Type.volume));
        final Path test = new Path(bucket, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file));
        final B2VersionIdProvider fileid = new B2VersionIdProvider(session);
        new B2TouchFeature(session, fileid).touch(test, new TransferStatus().withLength(0L));
        final byte[] content = new byte[1];
        new Random().nextBytes(content);
        {
            final TransferStatus status = new TransferStatus().withLength(content.length);
            final OutputStream out = new B2WriteFeature(session, fileid).write(test, status, new DisabledConnectionCallback());
            assertNotNull(out);
            new StreamCopier(status, status).withLimit(new Long(content.length)).transfer(new ByteArrayInputStream(content), out);
            out.close();
        }
        final Local local = new Local(System.getProperty("java.io.tmpdir"), UUID.randomUUID().toString());
        {
            final TransferStatus status = new TransferStatus().withLength(-1L);
            new DefaultDownloadFeature(new B2ReadFeature(session, fileid)).download(
                    test, local, new BandwidthThrottle(BandwidthThrottle.UNLIMITED), new DisabledStreamListener(),
                    status,
                new DisabledConnectionCallback());
        }
        final byte[] buffer = new byte[content.length];
        final InputStream in = local.getInputStream();
        IOUtils.readFully(in, buffer);
        in.close();
        assertArrayEquals(content, buffer);
        new B2DeleteFeature(session, fileid).delete(Collections.singletonList(test), new DisabledLoginCallback(), new Delete.DisabledCallback());
    }
}
