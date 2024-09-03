package ch.cyberduck.core.s3;

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

import ch.cyberduck.core.AsciiRandomStringService;
import ch.cyberduck.core.DisabledListProgressListener;
import ch.cyberduck.core.DisabledPasswordCallback;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.features.Delete;
import ch.cyberduck.core.transfer.TransferStatus;
import ch.cyberduck.test.IntegrationTest;

import org.jets3t.service.model.MultipartUpload;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.util.Collections;
import java.util.EnumSet;

import static org.junit.Assert.assertNotNull;

@Category(IntegrationTest.class)
public class S3ListServiceTest extends AbstractS3Test {

    @Test
    public void testListBucketNameInHostname() throws Exception {
        new S3ListService(virtualhost, new S3AccessControlListFeature(virtualhost)).list(
                new Path("/", EnumSet.of(Path.Type.directory, Path.Type.volume)), new DisabledListProgressListener());
    }

    @Test
    public void testListMultipartUploadDot() throws Exception {
        final Path bucket = new Path("test-eu-central-1-cyberduck", EnumSet.of(Path.Type.directory, Path.Type.volume));
        final S3AccessControlListFeature acl = new S3AccessControlListFeature(session);
        final Path placeholder = new S3DirectoryFeature(session, new S3WriteFeature(session, acl), acl).mkdir(
                new Path(bucket, new AsciiRandomStringService().random(), EnumSet.of(Path.Type.directory, Path.Type.placeholder)), new TransferStatus());
        final MultipartUpload multipart = session.getClient().multipartStartUpload(bucket.getName(), String.format("%s/.", new S3PathContainerService(session.getHost()).getKey(placeholder)), Collections.emptyMap());
        assertNotNull(new S3ListService(session, acl).list(placeholder, new DisabledListProgressListener()).find(path -> path.getName().equals(".")));
        new S3DefaultMultipartService(session).delete(multipart);
        new S3DefaultDeleteFeature(session, acl).delete(Collections.singletonList(placeholder), new DisabledPasswordCallback(), new Delete.DisabledCallback());
    }
}