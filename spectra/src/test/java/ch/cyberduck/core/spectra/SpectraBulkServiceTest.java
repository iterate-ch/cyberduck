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
import ch.cyberduck.core.exception.AccessDeniedException;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.features.Delete;
import ch.cyberduck.core.transfer.Transfer;
import ch.cyberduck.core.transfer.TransferItem;
import ch.cyberduck.core.transfer.TransferStatus;
import ch.cyberduck.test.IntegrationTest;

import org.junit.Ignore;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import static org.junit.Assert.*;

@Category(IntegrationTest.class)
public class SpectraBulkServiceTest extends AbstractSpectraTest {

    @Test
    public void testPreUploadSingleFile() throws Exception {
        final Map<TransferItem, TransferStatus> files = new HashMap<>();
        final TransferStatus status = new TransferStatus();
        final Path container = new SpectraDirectoryFeature(session, new SpectraWriteFeature(session)).mkdir(
                new Path(new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory, Path.Type.volume)), new TransferStatus());
        final Path file = new Path(container, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file));
        files.put(new TransferItem(file), status.withLength(1L));
        final SpectraBulkService bulk = new SpectraBulkService(session);
        bulk.pre(Transfer.Type.upload, files, new DisabledConnectionCallback());
        assertFalse(status.getParameters().isEmpty());
        assertNotNull(status.getParameters().get("job"));
        bulk.query(Transfer.Type.upload, file, status);
        new SpectraDeleteFeature(session).delete(Collections.singletonList(container), new DisabledLoginCallback(), new Delete.DisabledCallback());
    }

    @Test
    public void testPreUploadDirectoryFile() throws Exception {
        final Map<TransferItem, TransferStatus> files = new HashMap<>();
        final Path container = new SpectraDirectoryFeature(session, new SpectraWriteFeature(session)).mkdir(
                new Path(new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory, Path.Type.volume)), new TransferStatus());
        final Path directory = new Path(container, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory));
        final TransferStatus directoryStatus = new TransferStatus().withLength(0L);
        files.put(new TransferItem(directory), directoryStatus);
        final TransferStatus fileStatus = new TransferStatus().withLength(1L);
        files.put(new TransferItem(new Path(directory, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file))), fileStatus);
        final SpectraBulkService bulk = new SpectraBulkService(session);
        final Set<UUID> set = bulk.pre(Transfer.Type.upload, files, new DisabledConnectionCallback());
        assertEquals(1, set.size());
        assertEquals(1, bulk.query(Transfer.Type.upload, directory, directoryStatus).size());
        assertEquals(1, bulk.query(Transfer.Type.upload, directory, fileStatus).size());
        new SpectraDeleteFeature(session).delete(Collections.singletonList(container), new DisabledLoginCallback(), new Delete.DisabledCallback());
    }

    @Test(expected = AccessDeniedException.class)
    public void testPreDownloadNotFound() throws Exception {
        new SpectraBulkService(session).pre(Transfer.Type.download, Collections.singletonMap(
            new TransferItem(new Path(String.format("/cyberduck/%s", new AlphanumericRandomStringService().random()), EnumSet.of(Path.Type.file))), new TransferStatus().withLength(1L)
        ), new DisabledConnectionCallback());
    }

    @Test
    public void testPreDownloadFolderOnly() throws Exception {
        final Set<UUID> keys = new SpectraBulkService(session).pre(Transfer.Type.download, Collections.singletonMap(
            new TransferItem(new Path(String.format("/cyberduck/%s", new AlphanumericRandomStringService().random()), EnumSet.of(Path.Type.directory))), new TransferStatus()
        ), new DisabledConnectionCallback());
        assertTrue(keys.isEmpty());
    }

    @Test
    public void testPreUploadLargeFile() throws Exception {
        final Map<TransferItem, TransferStatus> files = new HashMap<>();
        final TransferStatus status = new TransferStatus();
        final Path container = new SpectraDirectoryFeature(session, new SpectraWriteFeature(session)).mkdir(
                new Path(new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory, Path.Type.volume)), new TransferStatus());
        final Path file = new Path(container, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file));
        files.put(new TransferItem(file),
                // 11GB
                status.withLength(112640000000L));
        final SpectraBulkService bulk = new SpectraBulkService(session);
        bulk.pre(Transfer.Type.upload, files, new DisabledConnectionCallback());
        assertFalse(status.getParameters().isEmpty());
        assertNotNull(status.getParameters().get("job"));
        final List<TransferStatus> list = bulk.query(Transfer.Type.upload, file, status);
        assertFalse(list.isEmpty());
        long offset = 0;
        for(TransferStatus s : list) {
            assertEquals(offset, s.getOffset());
            assertTrue(s.getLength() > 0);
            offset += s.getLength();
        }
        try {
            bulk.pre(Transfer.Type.download, files, new DisabledConnectionCallback());
            fail();
        }
        catch(BackgroundException e) {
            //
        }
        new SpectraDeleteFeature(session).delete(Arrays.asList(container), new DisabledLoginCallback(), new Delete.DisabledCallback());
    }

    @Test
    public void testPreUploadMultipleLargeFile() throws Exception {
        final Map<TransferItem, TransferStatus> files = new HashMap<>();
        final TransferStatus status = new TransferStatus();
        final Path container = new SpectraDirectoryFeature(session, new SpectraWriteFeature(session)).mkdir(
                new Path(new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory, Path.Type.volume)), new TransferStatus());
        files.put(new TransferItem(new Path(container, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file))),
            // 11GB
            status.withLength(118111600640L)
        );
        files.put(new TransferItem(new Path(container, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file))),
            // 11GB
            status.withLength(118111600640L)
        );
        final SpectraBulkService bulk = new SpectraBulkService(session);
        bulk.pre(Transfer.Type.upload, files, new DisabledConnectionCallback());
        assertFalse(status.getParameters().isEmpty());
        assertNotNull(status.getParameters().get("job"));
        for(TransferItem file : files.keySet()) {
            final List<TransferStatus> list = bulk.query(Transfer.Type.upload, file.remote, status);
            assertFalse(list.isEmpty());
            long offset = 0;
            for(TransferStatus s : list) {
                assertEquals(offset, s.getOffset());
                offset += s.getLength();
                assertTrue(s.getLength() > 0);
            }
        }
        new SpectraDeleteFeature(session).delete(Collections.singletonList(container), new DisabledLoginCallback(), new Delete.DisabledCallback());
    }

    @Ignore
    @Test
    public void testSPECTRA67() throws Exception {
        final Path container = new Path(new Path("cyberduck", EnumSet.of(Path.Type.directory, Path.Type.volume)), "SPECTRA-67", EnumSet.of(Path.Type.directory));
        final HashMap<TransferItem, TransferStatus> files = new HashMap<>();
        for(int i = 1; i < 100; i++) {
            files.put(new TransferItem(new Path(container, String.format("test-%d.f", i), EnumSet.of(Path.Type.file))), new TransferStatus());
        }
        final SpectraBulkService bulk = new SpectraBulkService(session);
        // Clear cache
        bulk.clear();
        final Set<UUID> uuid = bulk.pre(Transfer.Type.download, files, new DisabledConnectionCallback());
        assertNotNull(uuid);
        assertFalse(uuid.isEmpty());
        assertEquals(1, uuid.size());
    }
}
