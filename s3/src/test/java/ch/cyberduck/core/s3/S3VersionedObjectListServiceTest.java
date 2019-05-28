package ch.cyberduck.core.s3;

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
import ch.cyberduck.core.AsciiRandomStringService;
import ch.cyberduck.core.AttributedList;
import ch.cyberduck.core.DisabledConnectionCallback;
import ch.cyberduck.core.DisabledListProgressListener;
import ch.cyberduck.core.DisabledLoginCallback;
import ch.cyberduck.core.DisabledPasswordCallback;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathAttributes;
import ch.cyberduck.core.VersioningConfiguration;
import ch.cyberduck.core.exception.NotfoundException;
import ch.cyberduck.core.features.Delete;
import ch.cyberduck.core.features.Versioning;
import ch.cyberduck.core.http.HttpResponseOutputStream;
import ch.cyberduck.core.io.SHA256ChecksumCompute;
import ch.cyberduck.core.io.StreamCopier;
import ch.cyberduck.core.shared.DefaultAttributesFinderFeature;
import ch.cyberduck.core.transfer.TransferStatus;
import ch.cyberduck.test.IntegrationTest;

import org.apache.commons.text.RandomStringGenerator;
import org.jets3t.service.model.StorageObject;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;

import static org.junit.Assert.*;

@Category(IntegrationTest.class)
public class S3VersionedObjectListServiceTest extends AbstractS3Test {

    @Test
    public void testDirectory() throws Exception {
        final Path bucket = new Path("versioning-test-us-east-1-cyberduck", EnumSet.of(Path.Type.directory, Path.Type.volume));
        final Path directory = new S3DirectoryFeature(session, new S3WriteFeature(session)).mkdir(new Path(bucket, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory)), null, new TransferStatus());
        assertTrue(new S3VersionedObjectListService(session).list(bucket, new DisabledListProgressListener()).contains(directory));
        new S3DefaultDeleteFeature(session).delete(Collections.singletonList(directory), new DisabledLoginCallback(), new Delete.DisabledCallback());
        assertFalse(new S3VersionedObjectListService(session).list(bucket, new DisabledListProgressListener()).contains(directory));
    }

    @Test
    public void testList() throws Exception {
        final Path container = new Path("test-us-east-1-cyberduck", EnumSet.of(Path.Type.directory, Path.Type.volume));
        final AttributedList<Path> list = new S3VersionedObjectListService(session).list(
            new Path("test-us-east-1-cyberduck", EnumSet.of(Path.Type.directory, Path.Type.volume)), new DisabledListProgressListener());
        for(Path p : list) {
            assertEquals(container, p.getParent());
            if(p.isFile()) {
                assertNotEquals(-1L, p.attributes().getModificationDate());
                assertNotEquals(-1L, p.attributes().getSize());
                assertNotNull(p.attributes().getETag());
                assertNotNull(p.attributes().getStorageClass());
                assertNull(p.attributes().getVersionId());
            }
        }
    }

    @Test(expected = NotfoundException.class)
    public void testListNotFoundFolder() throws Exception {
        final Path container = new Path("versioning-test-us-east-1-cyberduck", EnumSet.of(Path.Type.volume));
        new S3ObjectListService(session).list(new Path(container, "notfound", EnumSet.of(Path.Type.directory)), new DisabledListProgressListener());
    }

    @Test
    public void testEnableVersioningExistingFiles() throws Exception {
        final Path bucket = new S3DirectoryFeature(session, new S3WriteFeature(session))
            .mkdir(new Path(new AsciiRandomStringService().random(), EnumSet.of(Path.Type.directory, Path.Type.volume)), "eu-central-1", new TransferStatus());
        assertTrue(new S3FindFeature(session).find(bucket));
        final Path file = new S3TouchFeature(session).touch(new Path(bucket, new AsciiRandomStringService().random(), EnumSet.of(Path.Type.file)), new TransferStatus());
        final S3WriteFeature feature = new S3WriteFeature(session);
        {
            final byte[] content = new RandomStringGenerator.Builder().build().generate(1024).getBytes(StandardCharsets.UTF_8);
            final TransferStatus status = new TransferStatus();
            status.setLength(content.length);
            status.setChecksum(new SHA256ChecksumCompute().compute(new ByteArrayInputStream(content), status));
            final HttpResponseOutputStream<StorageObject> out = feature.write(file, status, new DisabledConnectionCallback());
            new StreamCopier(status, status).transfer(new ByteArrayInputStream(content), out);
            out.close();
            assertEquals(content.length, new DefaultAttributesFinderFeature(session).find(file).getSize());
            final PathAttributes attr = new DefaultAttributesFinderFeature(session).find(file);
            assertEquals(content.length, attr.getSize());
            assertNull(new DefaultAttributesFinderFeature(session).find(file).getVersionId());
        }
        session.getFeature(Versioning.class).setConfiguration(bucket, new DisabledPasswordCallback(),
            new VersioningConfiguration(true));
        assertEquals("null", new DefaultAttributesFinderFeature(session).find(file).getVersionId());
        {
            final byte[] content = new RandomStringGenerator.Builder().build().generate(1024).getBytes(StandardCharsets.UTF_8);
            final TransferStatus status = new TransferStatus();
            status.setLength(content.length);
            status.setChecksum(new SHA256ChecksumCompute().compute(new ByteArrayInputStream(content), status));
            final HttpResponseOutputStream<StorageObject> out = feature.write(file, status, new DisabledConnectionCallback());
            new StreamCopier(status, status).transfer(new ByteArrayInputStream(content), out);
            out.close();
            assertEquals(content.length, new DefaultAttributesFinderFeature(session).find(file).getSize());
            final PathAttributes attr = new DefaultAttributesFinderFeature(session).find(file);
            assertEquals(content.length, attr.getSize());
            assertNotNull(attr.getVersionId());

            new S3DefaultDeleteFeature(session).delete(Arrays.asList(
                new Path(file).withAttributes(new PathAttributes().withVersionId("null")),
                new Path(file).withAttributes(attr), bucket), new DisabledLoginCallback(), new Delete.DisabledCallback());
        }
    }
}
