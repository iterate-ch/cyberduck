package ch.cyberduck.core.googlestorage;

/*
 * Copyright (c) 2002-2019 iterate GmbH. All rights reserved.
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
import ch.cyberduck.core.BytecountStreamListener;
import ch.cyberduck.core.DisabledConnectionCallback;
import ch.cyberduck.core.DisabledLoginCallback;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathAttributes;
import ch.cyberduck.core.exception.NotfoundException;
import ch.cyberduck.core.features.Delete;
import ch.cyberduck.core.http.HttpResponseOutputStream;
import ch.cyberduck.core.io.SHA256ChecksumCompute;
import ch.cyberduck.core.io.StreamCopier;
import ch.cyberduck.core.transfer.TransferStatus;
import ch.cyberduck.test.IntegrationTest;

import org.apache.commons.io.input.CountingInputStream;
import org.apache.commons.io.output.NullOutputStream;
import org.apache.commons.lang3.RandomUtils;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;

import com.google.api.services.storage.model.StorageObject;

import static org.junit.Assert.*;

@Category(IntegrationTest.class)
public class GoogleStorageReadFeatureTest extends AbstractGoogleStorageTest {

    @Test(expected = NotfoundException.class)
    public void testReadNotFound() throws Exception {
        final TransferStatus status = new TransferStatus();
        final Path container = new Path("cyberduck-test-eu", EnumSet.of(Path.Type.directory, Path.Type.volume));
        new GoogleStorageReadFeature(session).read(new Path(container, "nosuchname", EnumSet.of(Path.Type.file)), status.withLength(2L), new DisabledConnectionCallback());
    }

    @Test
    @Ignore
    public void testReadRangeUnknownLength() throws Exception {
        final Path container = new Path("cyberduck-test-eu", EnumSet.of(Path.Type.directory, Path.Type.volume));
        final Path test = new Path(container, String.format("%s %s", new AlphanumericRandomStringService().random(), new AlphanumericRandomStringService().random()), EnumSet.of(Path.Type.file));
        new GoogleStorageTouchFeature(session).touch(test, new TransferStatus());
        final byte[] content = RandomUtils.nextBytes(1023);
        final TransferStatus status = new TransferStatus().withLength(content.length);
        status.setChecksum(new SHA256ChecksumCompute().compute(new ByteArrayInputStream(content), status));
        final HttpResponseOutputStream<StorageObject> out = new GoogleStorageWriteFeature(session).write(test, status, new DisabledConnectionCallback());
        assertNotNull(out);
        new StreamCopier(new TransferStatus(), new TransferStatus()).transfer(new ByteArrayInputStream(content), out);
        test.attributes().setVersionId(String.valueOf(out.getStatus().getGeneration()));
        status.setAppend(true);
        status.setOffset(100L);
        status.setLength(-1L);
        final InputStream in = new GoogleStorageReadFeature(session).read(test, status, new DisabledConnectionCallback());
        assertNotNull(in);
        final ByteArrayOutputStream buffer = new ByteArrayOutputStream(content.length - 100);
        new StreamCopier(status, status).transfer(in, buffer);
        final byte[] reference = new byte[content.length - 100];
        System.arraycopy(content, 100, reference, 0, content.length - 100);
        assertArrayEquals(reference, buffer.toByteArray());
        in.close();
        new GoogleStorageDeleteFeature(session).delete(Collections.singletonList(test), new DisabledLoginCallback(), new Delete.DisabledCallback());
    }

    @Test
    public void testDownloadGzip() throws Exception {
        final int length = 1457;
        final byte[] content = RandomUtils.nextBytes(length);
        final Path container = new Path("cyberduck-test-eu", EnumSet.of(Path.Type.directory, Path.Type.volume));
        final Path file = new Path(container, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file));
        final TransferStatus status = new TransferStatus().withLength(content.length);
        status.setChecksum(new SHA256ChecksumCompute().compute(new ByteArrayInputStream(content), status));
        final OutputStream out = new GoogleStorageWriteFeature(session).write(file, status, new DisabledConnectionCallback());
        new StreamCopier(new TransferStatus(), new TransferStatus()).transfer(new ByteArrayInputStream(content), out);
        final InputStream in = new GoogleStorageReadFeature(session).read(file, status, new DisabledConnectionCallback());
        assertNotNull(in);
        final BytecountStreamListener count = new BytecountStreamListener();
        new StreamCopier(status, status).withListener(count).transfer(in, NullOutputStream.NULL_OUTPUT_STREAM);
        assertEquals(content.length, count.getRecv());
        assertEquals(content.length, status.getLength());
        in.close();
        new GoogleStorageDeleteFeature(session).delete(Collections.singletonList(file), new DisabledLoginCallback(), new Delete.DisabledCallback());
    }

    @Test
    public void testReadCloseReleaseEntity() throws Exception {
        final Path container = new Path("cyberduck-test-eu", EnumSet.of(Path.Type.directory, Path.Type.volume));
        final Path file = new Path(container, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file));
        final int length = 2048;
        final byte[] content = RandomUtils.nextBytes(length);
        final TransferStatus status = new TransferStatus().withLength(content.length);
        status.setChecksum(new SHA256ChecksumCompute().compute(new ByteArrayInputStream(content), status));
        final OutputStream out = new GoogleStorageWriteFeature(session).write(file, status, new DisabledConnectionCallback());
        new StreamCopier(new TransferStatus(), new TransferStatus()).transfer(new ByteArrayInputStream(content), out);
        final CountingInputStream in = new CountingInputStream(new GoogleStorageReadFeature(session).read(file, status, new DisabledConnectionCallback()));
        in.close();
        assertEquals(0L, in.getByteCount(), 0L);
        new GoogleStorageDeleteFeature(session).delete(Collections.singletonList(file), new DisabledLoginCallback(), new Delete.DisabledCallback());
    }

    @Test
    public void testReadWhitespace() throws Exception {
        final int length = 47;
        final byte[] content = RandomUtils.nextBytes(length);
        final Path container = new Path("cyberduck-test-eu", EnumSet.of(Path.Type.directory, Path.Type.volume));
        final Path file = new Path(container, String.format("t %s", new AlphanumericRandomStringService().random()), EnumSet.of(Path.Type.file));
        final TransferStatus status = new TransferStatus().withLength(content.length);
        status.setChecksum(new SHA256ChecksumCompute().compute(new ByteArrayInputStream(content), status));
        final OutputStream out = new GoogleStorageWriteFeature(session).write(file, status, new DisabledConnectionCallback());
        new StreamCopier(new TransferStatus(), new TransferStatus()).transfer(new ByteArrayInputStream(content), out);
        assertEquals(length, new GoogleStorageAttributesFinderFeature(session).find(file).getSize());
        final CountingInputStream in = new CountingInputStream(new GoogleStorageReadFeature(session).read(file, new TransferStatus(), new DisabledConnectionCallback()));
        in.close();
        assertEquals(0L, in.getByteCount(), 0L);
        new GoogleStorageDeleteFeature(session).delete(Collections.singletonList(file), new DisabledLoginCallback(), new Delete.DisabledCallback());
    }

    @Test
    public void testReadPath() throws Exception {
        final int length = 47;
        final byte[] content = RandomUtils.nextBytes(length);
        final Path container = new Path("cyberduck-test-eu", EnumSet.of(Path.Type.directory, Path.Type.volume));
        final Path directory = new GoogleStorageDirectoryFeature(session).mkdir(new Path(container, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory)), new TransferStatus());
        final Path file = new Path(directory, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file));
        final TransferStatus status = new TransferStatus().withLength(content.length);
        status.setChecksum(new SHA256ChecksumCompute().compute(new ByteArrayInputStream(content), status));
        final OutputStream out = new GoogleStorageWriteFeature(session).write(file, status, new DisabledConnectionCallback());
        new StreamCopier(new TransferStatus(), new TransferStatus()).transfer(new ByteArrayInputStream(content), out);
        assertEquals(length, new GoogleStorageAttributesFinderFeature(session).find(file).getSize());
        final CountingInputStream in = new CountingInputStream(new GoogleStorageReadFeature(session).read(file, new TransferStatus(), new DisabledConnectionCallback()));
        in.close();
        assertEquals(0L, in.getByteCount(), 0L);
        new GoogleStorageDeleteFeature(session).delete(Arrays.asList(file, directory), new DisabledLoginCallback(), new Delete.DisabledCallback());
    }

    @Test
    public void testReadEmpty() throws Exception {
        final Path container = new Path("cyberduck-test-eu", EnumSet.of(Path.Type.directory, Path.Type.volume));
        final Path directory = new GoogleStorageDirectoryFeature(session).mkdir(new Path(container, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory)), new TransferStatus());
        final Path file = new GoogleStorageTouchFeature(session).touch(new Path(directory, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file)), new TransferStatus());
        assertEquals(0, new GoogleStorageAttributesFinderFeature(session).find(file).getSize());
        final CountingInputStream in = new CountingInputStream(new GoogleStorageReadFeature(session).read(file, new TransferStatus(), new DisabledConnectionCallback()));
        in.close();
        assertEquals(0L, in.getByteCount(), 0L);
        new GoogleStorageDeleteFeature(session).delete(Arrays.asList(file, directory), new DisabledLoginCallback(), new Delete.DisabledCallback());
    }

    @Test
    public void testReadPreviousVersion() throws Exception {
        final int length = 8;
        final byte[] content = RandomUtils.nextBytes(length);
        final Path container = new Path("cyberduck-test-eu", EnumSet.of(Path.Type.directory, Path.Type.volume));
        assertTrue(new GoogleStorageVersioningFeature(session).getConfiguration(container).isEnabled());
        final Path file = new GoogleStorageTouchFeature(session).touch(new Path(container, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file)), new TransferStatus());
        final String initialVersion = file.attributes().getVersionId();
        final TransferStatus status = new TransferStatus().withLength(content.length);
        status.setChecksum(new SHA256ChecksumCompute().compute(new ByteArrayInputStream(content), status));
        final HttpResponseOutputStream<StorageObject> out = new GoogleStorageWriteFeature(session).write(file, status, new DisabledConnectionCallback());
        new StreamCopier(new TransferStatus(), new TransferStatus()).transfer(new ByteArrayInputStream(content), out);
        assertEquals(0L, new GoogleStorageAttributesFinderFeature(session).find(file.withAttributes(new PathAttributes(file.attributes()).withVersionId(initialVersion))).getSize());
        // Read previous version
        status.setLength(0L);
        final InputStream in = new GoogleStorageReadFeature(session).read(file, status, new DisabledConnectionCallback());
        assertNotNull(in);
        final ByteArrayOutputStream buffer = new ByteArrayOutputStream(0);
        new StreamCopier(status, status).transfer(in, buffer);
        assertEquals(0, buffer.size());
        in.close();
        file.attributes().setVersionId(String.valueOf(out.getStatus().getGeneration()));
        assertEquals(length, new GoogleStorageAttributesFinderFeature(session).find(file).getSize());
        new GoogleStorageDeleteFeature(session).delete(Collections.singletonList(file), new DisabledLoginCallback(), new Delete.DisabledCallback());
    }
}
