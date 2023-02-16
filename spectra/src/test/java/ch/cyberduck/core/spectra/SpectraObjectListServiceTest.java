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
import ch.cyberduck.core.AttributedList;
import ch.cyberduck.core.DisabledConnectionCallback;
import ch.cyberduck.core.DisabledListProgressListener;
import ch.cyberduck.core.DisabledLoginCallback;
import ch.cyberduck.core.DisabledPasswordCallback;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.VersioningConfiguration;
import ch.cyberduck.core.exception.NotfoundException;
import ch.cyberduck.core.features.Delete;
import ch.cyberduck.core.io.CRC32ChecksumCompute;
import ch.cyberduck.core.io.StreamCopier;
import ch.cyberduck.core.transfer.Transfer;
import ch.cyberduck.core.transfer.TransferItem;
import ch.cyberduck.core.transfer.TransferStatus;
import ch.cyberduck.test.IntegrationTest;

import org.apache.commons.lang3.RandomUtils;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.io.ByteArrayInputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;

import static org.junit.Assert.*;

@Category(IntegrationTest.class)
public class SpectraObjectListServiceTest extends AbstractSpectraTest {

    @Test
    public void tetsEmptyPlaceholder() throws Exception {
        final Path container = new SpectraDirectoryFeature(session, new SpectraWriteFeature(session)).mkdir(
                new Path(new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory, Path.Type.volume)), new TransferStatus());
        try {
            new SpectraObjectListService(session).list(new Path(container, "empty", EnumSet.of(Path.Type.directory, Path.Type.placeholder)),
                    new DisabledListProgressListener());
            fail();
        }
        catch(NotfoundException e) {
            // Expected
        }
        new SpectraDeleteFeature(session).delete(Collections.singletonList(container), new DisabledLoginCallback(), new Delete.DisabledCallback());
    }

    @Test(expected = NotfoundException.class)
    public void testListNotfound() throws Exception {
        final Path container = new Path("notfound.cyberduck.ch", EnumSet.of(Path.Type.directory, Path.Type.volume));
        new SpectraObjectListService(session).list(container, new DisabledListProgressListener());
    }

    @Test
    public void testListPlaceholder() throws Exception {
        final Path container = new SpectraDirectoryFeature(session, new SpectraWriteFeature(session)).mkdir(
                new Path(new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory, Path.Type.volume)), new TransferStatus());
        final Path placeholder = new SpectraDirectoryFeature(session, new SpectraWriteFeature(session)).mkdir(
                new Path(container, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory)), new TransferStatus());
        final AttributedList<Path> list = new SpectraObjectListService(session).list(placeholder, new DisabledListProgressListener());
        assertTrue(list.isEmpty());
        new SpectraDeleteFeature(session).delete(Collections.singletonList(container), new DisabledLoginCallback(), new Delete.DisabledCallback());
    }

    @Test
    @Ignore
    public void testVersioning() throws Exception {
        final Path container = new SpectraDirectoryFeature(session, new SpectraWriteFeature(session)).mkdir(
                new Path(new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory, Path.Type.volume)), new TransferStatus());
        new SpectraVersioningFeature(session).setConfiguration(container, new DisabledPasswordCallback(), new VersioningConfiguration(true));
        final Path folder = new Path(container, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory));
        new SpectraDirectoryFeature(session, new SpectraWriteFeature(session)).mkdir(folder, new TransferStatus());
        final Path test = new Path(folder, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file));
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
        final AttributedList<Path> list = new SpectraObjectListService(session).list(folder, new DisabledListProgressListener());
        assertEquals(2, list.size());
        for(Path f : list) {
            assertTrue(f.attributes().getMetadata().isEmpty());
        }
        new SpectraDeleteFeature(session).delete(Arrays.asList(test, folder), new DisabledLoginCallback(), new Delete.DisabledCallback());
        for(Path f : new SpectraObjectListService(session).list(folder, new DisabledListProgressListener())) {
            assertTrue(f.attributes().isDuplicate());
            if(f.attributes().getSize() == 0L) {
                assertTrue(f.attributes().getMetadata().containsKey(SpectraVersioningFeature.KEY_REVERTABLE));
            }
            else {
                assertTrue(f.attributes().getMetadata().isEmpty());
            }
        }
        new SpectraDeleteFeature(session).delete(Collections.singletonList(container), new DisabledLoginCallback(), new Delete.DisabledCallback());
    }
}
