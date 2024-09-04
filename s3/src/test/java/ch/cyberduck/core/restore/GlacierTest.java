package ch.cyberduck.core.restore;

/*
 * Copyright (c) 2002-2020 iterate GmbH. All rights reserved.
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
import ch.cyberduck.core.DisabledPasswordCallback;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.features.Delete;
import ch.cyberduck.core.http.HttpResponseOutputStream;
import ch.cyberduck.core.io.SHA256ChecksumCompute;
import ch.cyberduck.core.io.StreamCopier;
import ch.cyberduck.core.s3.AbstractS3Test;
import ch.cyberduck.core.s3.S3AccessControlListFeature;
import ch.cyberduck.core.s3.S3AttributesFinderFeature;
import ch.cyberduck.core.s3.S3DefaultDeleteFeature;
import ch.cyberduck.core.s3.S3LocationFeature;
import ch.cyberduck.core.s3.S3WriteFeature;
import ch.cyberduck.core.ssl.DefaultX509KeyManager;
import ch.cyberduck.core.ssl.DisabledX509TrustManager;
import ch.cyberduck.core.transfer.TransferStatus;
import ch.cyberduck.test.IntegrationTest;

import org.apache.commons.lang3.RandomUtils;
import org.jets3t.service.model.StorageObject;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.io.ByteArrayInputStream;
import java.util.Collections;
import java.util.EnumSet;

import static org.junit.Assert.assertEquals;

@Category(IntegrationTest.class)
public class GlacierTest extends AbstractS3Test {

    @Test
    public void testRestore() throws Exception {
        final Path bucket = new Path("test-eu-central-1-cyberduck", EnumSet.of(Path.Type.volume, Path.Type.directory));
        final Path test = new Path(bucket, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file));
        final byte[] content = RandomUtils.nextBytes(512);
        final TransferStatus status = new TransferStatus();
        status.setStorageClass("GLACIER");
        status.setLength(content.length);
        status.setChecksum(new SHA256ChecksumCompute().compute(new ByteArrayInputStream(content), status));
        final S3AccessControlListFeature acl = new S3AccessControlListFeature(session);
        final HttpResponseOutputStream<StorageObject> out = new S3WriteFeature(session, acl).write(test, status, new DisabledConnectionCallback());
        new StreamCopier(status, status).transfer(new ByteArrayInputStream(content), out);
        out.close();
        assertEquals("GLACIER", new S3AttributesFinderFeature(session, acl).find(test).getStorageClass());
        final Glacier feature = new Glacier(session, new S3LocationFeature(session), new DisabledX509TrustManager(), new DefaultX509KeyManager());
        feature.restore(test, new DisabledLoginCallback());
        new S3DefaultDeleteFeature(session, acl).delete(Collections.singletonList(test), new DisabledPasswordCallback(), new Delete.DisabledCallback());
    }
}
