package ch.cyberduck.core.s3;

/*
 * Copyright (c) 2002-2014 David Kocher. All rights reserved.
 * http://cyberduck.io/
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
 *
 * Bug fixes, suggestions and comments should be sent to:
 * feedback@cyberduck.io
 */

import ch.cyberduck.core.AlphanumericRandomStringService;
import ch.cyberduck.core.DisabledListProgressListener;
import ch.cyberduck.core.DisabledLoginCallback;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathAttributes;
import ch.cyberduck.core.SimplePathPredicate;
import ch.cyberduck.core.features.Delete;
import ch.cyberduck.core.transfer.TransferStatus;
import ch.cyberduck.test.IntegrationTest;

import org.jets3t.service.model.MultipartUpload;
import org.jets3t.service.model.S3Object;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.UUID;

import static org.junit.Assert.*;

@Category(IntegrationTest.class)
public class S3DefaultMultipartServiceTest extends AbstractS3Test {

    @Test
    public void testFindNotFound() throws Exception {
        final Path container = new Path("test-eu-central-1-cyberduck", EnumSet.of(Path.Type.directory, Path.Type.volume));
        final Path test = new Path(container, UUID.randomUUID().toString(), EnumSet.of(Path.Type.file));
        final List<MultipartUpload> list = new S3DefaultMultipartService(session).find(test);
        assertTrue(list.isEmpty());
    }

    @Test
    public void testDelete() throws Exception {
        final Path container = new Path("test-eu-central-1-cyberduck", EnumSet.of(Path.Type.directory, Path.Type.volume));
        final S3DefaultMultipartService service = new S3DefaultMultipartService(session);
        for(MultipartUpload multipart : service.find(container)) {
            service.delete(multipart);
        }
    }

    @Test
    public void testFind() throws Exception {
        final Path container = new Path("test-eu-central-1-cyberduck", EnumSet.of(Path.Type.directory, Path.Type.volume));
        final S3AccessControlListFeature acl = new S3AccessControlListFeature(session);
        final Path directory = new S3DirectoryFeature(session, new S3WriteFeature(session, acl), acl).mkdir(
                new Path(container, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory)), new TransferStatus());
        final S3DefaultMultipartService service = new S3DefaultMultipartService(session);
        assertTrue(service.find(directory).isEmpty());
        final Path file = new Path(directory, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file));
        final S3Object object = new S3WriteFeature(session, acl).getDetails(file, new TransferStatus());
        final MultipartUpload first = session.getClient().multipartStartUpload(container.getName(), object);
        assertNotNull(first);
        assertFalse(service.find(directory).isEmpty());
        assertTrue(service.find(new Path(container, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory))).isEmpty());
        assertEquals(first.getUploadId(), service.find(directory).iterator().next().getUploadId());
        assertFalse(new S3FindFeature(session, acl).find(file));
        final Path upload = new S3ListService(session, acl).list(directory, new DisabledListProgressListener()).find(new SimplePathPredicate(file));
        assertNotNull(upload);
        assertTrue(new S3FindFeature(session, acl).find(upload));
        assertTrue(upload.getType().contains(Path.Type.upload));
        assertTrue(new S3FindFeature(session, acl).find(upload));
        assertNotEquals(PathAttributes.EMPTY, new S3AttributesFinderFeature(session, acl).find(upload));
        // Make sure timestamp is later.
        Thread.sleep(2000L);
        final MultipartUpload second = session.getClient().multipartStartUpload(container.getName(), object);
        assertNotNull(second);
        final MultipartUpload multipart = service.find(file).iterator().next();
        assertNotNull(multipart);
        assertEquals(second.getUploadId(), multipart.getUploadId());
        assertNotNull(multipart);
        service.delete(first);
        service.delete(second);
        new S3DefaultDeleteFeature(session).delete(Collections.singletonList(directory), new DisabledLoginCallback(), new Delete.DisabledCallback());
    }

    @Test
    public void testFindKeyWithSpace() throws Exception {
        final Path container = new Path("test-eu-central-1-cyberduck", EnumSet.of(Path.Type.directory, Path.Type.volume));
        final Path test = new Path(container, "t f", EnumSet.of(Path.Type.file));
        final List<MultipartUpload> list = new S3DefaultMultipartService(session).find(test);
        assertTrue(list.isEmpty());
    }
}
