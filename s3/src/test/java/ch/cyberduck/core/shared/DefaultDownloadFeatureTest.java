package ch.cyberduck.core.shared;

/*
 * Copyright (c) 2002-2017 iterate GmbH. All rights reserved.
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
import ch.cyberduck.core.DisabledConnectionCallback;
import ch.cyberduck.core.DisabledLoginCallback;
import ch.cyberduck.core.Local;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.features.Delete;
import ch.cyberduck.core.io.BandwidthThrottle;
import ch.cyberduck.core.io.DisabledStreamListener;
import ch.cyberduck.core.io.SHA256ChecksumCompute;
import ch.cyberduck.core.io.StatusOutputStream;
import ch.cyberduck.core.io.StreamCopier;
import ch.cyberduck.core.s3.AbstractS3Test;
import ch.cyberduck.core.s3.S3AccessControlListFeature;
import ch.cyberduck.core.s3.S3DefaultDeleteFeature;
import ch.cyberduck.core.s3.S3ReadFeature;
import ch.cyberduck.core.s3.S3TouchFeature;
import ch.cyberduck.core.s3.S3WriteFeature;
import ch.cyberduck.core.transfer.TransferStatus;
import ch.cyberduck.test.IntegrationTest;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.RandomUtils;
import org.jets3t.service.model.S3Object;
import org.jets3t.service.model.StorageObject;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Collections;
import java.util.EnumSet;
import java.util.UUID;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertNotNull;

@Category(IntegrationTest.class)
public class DefaultDownloadFeatureTest extends AbstractS3Test {

    @Test
    public void testTransferVersioning() throws Exception {
        final Path container = new Path("versioning-test-eu-central-1-cyberduck", EnumSet.of(Path.Type.volume, Path.Type.directory));
        final Path test = new S3TouchFeature(session, new S3AccessControlListFeature(session)).touch(
                new Path(container, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file)), new TransferStatus().withLength(0L));
        {
            final byte[] content = RandomUtils.nextBytes(39864);
            final TransferStatus writeStatus = new TransferStatus().withLength(content.length).withChecksum(new SHA256ChecksumCompute().compute(new ByteArrayInputStream(content), new TransferStatus()));
            final StatusOutputStream<StorageObject> out = new S3WriteFeature(session, new S3AccessControlListFeature(session)).write(test, writeStatus, new DisabledConnectionCallback());
            assertNotNull(out);
            new StreamCopier(writeStatus, writeStatus).withLimit((long) content.length).transfer(new ByteArrayInputStream(content), out);
            out.close();
        }
        final byte[] content = RandomUtils.nextBytes(39864);
        {
            final TransferStatus writeStatus = new TransferStatus().withLength(content.length).withChecksum(new SHA256ChecksumCompute().compute(new ByteArrayInputStream(content), new TransferStatus()));
            final StatusOutputStream<StorageObject> out = new S3WriteFeature(session, new S3AccessControlListFeature(session)).write(test, writeStatus, new DisabledConnectionCallback());
            assertNotNull(out);
            new StreamCopier(writeStatus, writeStatus).withLimit((long) content.length).transfer(new ByteArrayInputStream(content), out);
            out.close();
            test.attributes().setVersionId(((S3Object) out.getStatus()).getVersionId());
            final Local local = new Local(System.getProperty("java.io.tmpdir"), UUID.randomUUID().toString());
            final TransferStatus readStatus = new TransferStatus().withLength(content.length);
            new DefaultDownloadFeature(new S3ReadFeature(session)).download(
                test, local, new BandwidthThrottle(BandwidthThrottle.UNLIMITED), new DisabledStreamListener(),
                readStatus,
                new DisabledConnectionCallback());
            final byte[] buffer = new byte[content.length];
            final InputStream in = local.getInputStream();
            IOUtils.readFully(in, buffer);
            in.close();
            assertArrayEquals(content, buffer);
        }
        new S3DefaultDeleteFeature(session).delete(Collections.singletonList(test), new DisabledLoginCallback(), new Delete.DisabledCallback());
    }
}
