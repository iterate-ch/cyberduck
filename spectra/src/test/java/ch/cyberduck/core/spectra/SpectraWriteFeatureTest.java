/*
 * Copyright (c) 2015-2016 Spectra Logic Corporation. All rights reserved.
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

package ch.cyberduck.core.spectra;

import ch.cyberduck.core.AlphanumericRandomStringService;
import ch.cyberduck.core.DisabledConnectionCallback;
import ch.cyberduck.core.DisabledLoginCallback;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.features.Delete;
import ch.cyberduck.core.io.CRC32ChecksumCompute;
import ch.cyberduck.core.io.StreamCopier;
import ch.cyberduck.core.transfer.Transfer;
import ch.cyberduck.core.transfer.TransferItem;
import ch.cyberduck.core.transfer.TransferStatus;
import ch.cyberduck.test.IntegrationTest;

import org.apache.commons.lang3.RandomUtils;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Collections;
import java.util.EnumSet;

import static org.junit.Assert.*;

@Category(IntegrationTest.class)
public class SpectraWriteFeatureTest extends AbstractSpectraTest {

    @Test
    public void testWriteOverwrite() throws Exception {
        final Path container = new SpectraDirectoryFeature(session, new SpectraWriteFeature(session)).mkdir(
                new Path(new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory, Path.Type.volume)), new TransferStatus());
        final Path test = new Path(container, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file));
        final byte[] content = RandomUtils.nextBytes(1000);
        final TransferStatus status = new TransferStatus().withLength(content.length);
        status.setChecksum(new CRC32ChecksumCompute().compute(new ByteArrayInputStream(content), status));
        // Allocate
        final SpectraBulkService bulk = new SpectraBulkService(session);
        bulk.pre(Transfer.Type.upload, Collections.singletonMap(new TransferItem(test), status), new DisabledConnectionCallback());
        {
            final OutputStream out = new SpectraWriteFeature(session).write(test, status, new DisabledConnectionCallback());
            assertNotNull(out);
            new StreamCopier(new TransferStatus(), new TransferStatus()).transfer(new ByteArrayInputStream(content), out);
            out.close();
        }
        assertEquals(content.length, new SpectraAttributesFinderFeature(session).find(test).getSize());
        // Overwrite
        bulk.pre(Transfer.Type.upload, Collections.singletonMap(new TransferItem(test), status.exists(true)), new DisabledConnectionCallback());
        {
            final OutputStream out = new SpectraWriteFeature(session).write(test, status.exists(true), new DisabledConnectionCallback());
            new StreamCopier(new TransferStatus(), new TransferStatus()).transfer(new ByteArrayInputStream(content), out);
            out.close();
        }
        assertEquals(content.length, new SpectraAttributesFinderFeature(session).find(test).getSize());
        new SpectraDeleteFeature(session).delete(Collections.singletonList(container), new DisabledLoginCallback(), new Delete.DisabledCallback());
    }

    @Test
    public void testOverwriteZeroSized() throws Exception {
        final Path container = new SpectraDirectoryFeature(session, new SpectraWriteFeature(session)).mkdir(
                new Path(new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory, Path.Type.volume)), new TransferStatus());
        final Path test = new Path(container, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file));
        // Make 0-byte file
        new SpectraTouchFeature(session).touch(test, new TransferStatus());
        // Replace content
        final byte[] content = RandomUtils.nextBytes(1023);
        final TransferStatus status = new TransferStatus().withLength(content.length);
        status.setChecksum(new CRC32ChecksumCompute().compute(new ByteArrayInputStream(content), status));
        final SpectraBulkService bulk = new SpectraBulkService(session);
        bulk.pre(Transfer.Type.upload, Collections.singletonMap(new TransferItem(test), status.exists(true)), new DisabledConnectionCallback());
        final OutputStream out = new SpectraWriteFeature(session).write(test, status, new DisabledConnectionCallback());
        assertNotNull(out);
        new StreamCopier(new TransferStatus(), new TransferStatus()).transfer(new ByteArrayInputStream(content), out);
        out.close();
        new SpectraDeleteFeature(session).delete(Collections.singletonList(container), new DisabledLoginCallback(), new Delete.DisabledCallback());
    }

    @Test
    public void testSPECTRA69() throws Exception {
        final Path container = new SpectraDirectoryFeature(session, new SpectraWriteFeature(session)).mkdir(
                new Path(new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory, Path.Type.volume)), new TransferStatus());
        final Path test = new Path(container, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file));
        // Allocate
        final SpectraBulkService bulk = new SpectraBulkService(session);
        {
            final byte[] content1 = RandomUtils.nextBytes(1000);
            final TransferStatus status = new TransferStatus().withLength(content1.length);
            status.setChecksum(new CRC32ChecksumCompute().compute(new ByteArrayInputStream(content1), status));
            bulk.pre(Transfer.Type.upload, Collections.singletonMap(new TransferItem(test), status), new DisabledConnectionCallback());
            final OutputStream out = new SpectraWriteFeature(session).write(test, status, new DisabledConnectionCallback());
            assertNotNull(out);
            new StreamCopier(new TransferStatus(), new TransferStatus()).transfer(new ByteArrayInputStream(content1), out);
            out.close();
            assertEquals(content1.length, new SpectraAttributesFinderFeature(session).find(test).getSize());
            bulk.pre(Transfer.Type.download, Collections.singletonMap(new TransferItem(test), status), new DisabledConnectionCallback());
            final InputStream in = new SpectraReadFeature(session).read(test, status, new DisabledConnectionCallback());
            assertNotNull(in);
            final ByteArrayOutputStream buffer = new ByteArrayOutputStream(content1.length);
            new StreamCopier(status, status).transfer(in, buffer);
            assertArrayEquals(content1, buffer.toByteArray());
        }
        {
            final byte[] content2 = RandomUtils.nextBytes(1000);
            final TransferStatus status = new TransferStatus().withLength(content2.length);
            // Overwrite
            bulk.pre(Transfer.Type.upload, Collections.singletonMap(new TransferItem(test), status.exists(true)), new DisabledConnectionCallback());
            final OutputStream out = new SpectraWriteFeature(session).write(test, status.exists(true), new DisabledConnectionCallback());
            new StreamCopier(new TransferStatus(), new TransferStatus()).transfer(new ByteArrayInputStream(content2), out);
            out.close();
            assertEquals(content2.length, new SpectraAttributesFinderFeature(session).find(test).getSize());
            bulk.pre(Transfer.Type.download, Collections.singletonMap(new TransferItem(test), status), new DisabledConnectionCallback());
            final InputStream in = new SpectraReadFeature(session).read(test, status, new DisabledConnectionCallback());
            assertNotNull(in);
            final ByteArrayOutputStream buffer = new ByteArrayOutputStream(content2.length);
            new StreamCopier(status, status).transfer(in, buffer);
            assertArrayEquals(content2, buffer.toByteArray());
        }
        new SpectraDeleteFeature(session).delete(Collections.singletonList(container), new DisabledLoginCallback(), new Delete.DisabledCallback());
    }
}
