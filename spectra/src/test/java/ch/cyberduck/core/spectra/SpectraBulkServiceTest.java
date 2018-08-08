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
import ch.cyberduck.core.Credentials;
import ch.cyberduck.core.DisabledCancelCallback;
import ch.cyberduck.core.DisabledConnectionCallback;
import ch.cyberduck.core.DisabledHostKeyCallback;
import ch.cyberduck.core.DisabledLoginCallback;
import ch.cyberduck.core.DisabledPasswordStore;
import ch.cyberduck.core.DisabledProgressListener;
import ch.cyberduck.core.Host;
import ch.cyberduck.core.LoginConnectionService;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathCache;
import ch.cyberduck.core.Scheme;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.exception.ConflictException;
import ch.cyberduck.core.exception.NotfoundException;
import ch.cyberduck.core.features.Delete;
import ch.cyberduck.core.ssl.DefaultX509KeyManager;
import ch.cyberduck.core.ssl.DisabledX509TrustManager;
import ch.cyberduck.core.transfer.Transfer;
import ch.cyberduck.core.transfer.TransferItem;
import ch.cyberduck.core.transfer.TransferStatus;
import ch.cyberduck.test.IntegrationTest;

import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import static org.junit.Assert.*;

@Category(IntegrationTest.class)
public class SpectraBulkServiceTest {

    @Test
    public void testPreUploadSingleFile() throws Exception {
        final Host host = new Host(new SpectraProtocol() {
            @Override
            public Scheme getScheme() {
                return Scheme.http;
            }
        }, System.getProperties().getProperty("spectra.hostname"), Integer.valueOf(System.getProperties().getProperty("spectra.port")), new Credentials(
            System.getProperties().getProperty("spectra.user"), System.getProperties().getProperty("spectra.key")
        ));
        final SpectraSession session = new SpectraSession(host, new DisabledX509TrustManager(),
            new DefaultX509KeyManager());
        final LoginConnectionService service = new LoginConnectionService(new DisabledLoginCallback() {
            @Override
            public void warn(final Host bookmark, final String title, final String message, final String continueButton, final String disconnectButton, final String preference) {
                //
            }
        }, new DisabledHostKeyCallback(),
            new DisabledPasswordStore(), new DisabledProgressListener());
        service.connect(session, PathCache.empty(), new DisabledCancelCallback());
        final Map<TransferItem, TransferStatus> files = new HashMap<>();
        final TransferStatus status = new TransferStatus();
        final Path file = new Path(String.format("/cyberduck/%s", new AlphanumericRandomStringService().random()), EnumSet.of(Path.Type.file));
        files.put(new TransferItem(file), status.length(1L));
        final SpectraBulkService bulk = new SpectraBulkService(session);
        bulk.pre(Transfer.Type.upload, files, new DisabledConnectionCallback());
        assertFalse(status.getParameters().isEmpty());
        assertNotNull(status.getParameters().get("job"));
        bulk.query(Transfer.Type.upload, file, status);
        new SpectraDeleteFeature(session).delete(Collections.singletonList(file), new DisabledLoginCallback(), new Delete.DisabledCallback());
        session.close();
    }

    @Test
    public void testPreUploadDirectoryFile() throws Exception {
        final Host host = new Host(new SpectraProtocol() {
            @Override
            public Scheme getScheme() {
                return Scheme.http;
            }
        }, System.getProperties().getProperty("spectra.hostname"), Integer.valueOf(System.getProperties().getProperty("spectra.port")), new Credentials(
            System.getProperties().getProperty("spectra.user"), System.getProperties().getProperty("spectra.key")
        ));
        final SpectraSession session = new SpectraSession(host, new DisabledX509TrustManager(),
            new DefaultX509KeyManager());
        final LoginConnectionService service = new LoginConnectionService(new DisabledLoginCallback() {
            @Override
            public void warn(final Host bookmark, final String title, final String message, final String continueButton, final String disconnectButton, final String preference) {
                //
            }
        }, new DisabledHostKeyCallback(),
            new DisabledPasswordStore(), new DisabledProgressListener());
        service.connect(session, PathCache.empty(), new DisabledCancelCallback());
        final Map<TransferItem, TransferStatus> files = new HashMap<>();
        final Path directory = new Path(String.format("/cyberduck/%s", new AlphanumericRandomStringService().random()), EnumSet.of(Path.Type.directory));
        final TransferStatus directoryStatus = new TransferStatus().length(0L);
        files.put(new TransferItem(directory), directoryStatus);
        final TransferStatus fileStatus = new TransferStatus().length(1L);
        files.put(new TransferItem(new Path(directory, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file))), fileStatus);
        final SpectraBulkService bulk = new SpectraBulkService(session);
        final Set<UUID> set = bulk.pre(Transfer.Type.upload, files, new DisabledConnectionCallback());
        assertEquals(1, set.size());
        assertEquals(1, bulk.query(Transfer.Type.upload, directory, directoryStatus).size());
        assertEquals(1, bulk.query(Transfer.Type.upload, directory, fileStatus).size());
        for(TransferItem item : files.keySet()) {
            new SpectraDeleteFeature(session).delete(Collections.singletonList(item.remote), new DisabledLoginCallback(), new Delete.DisabledCallback());
        }
        session.close();
    }

    @Test(expected = NotfoundException.class)
    public void testPreDownloadNotFound() throws Exception {
        final Host host = new Host(new SpectraProtocol() {
            @Override
            public Scheme getScheme() {
                return Scheme.http;
            }
        }, System.getProperties().getProperty("spectra.hostname"), Integer.valueOf(System.getProperties().getProperty("spectra.port")), new Credentials(
            System.getProperties().getProperty("spectra.user"), System.getProperties().getProperty("spectra.key")
        ));
        final SpectraSession session = new SpectraSession(host, new DisabledX509TrustManager(),
            new DefaultX509KeyManager());
        final LoginConnectionService service = new LoginConnectionService(new DisabledLoginCallback() {
            @Override
            public void warn(final Host bookmark, final String title, final String message, final String continueButton, final String disconnectButton, final String preference) {
                //
            }
        }, new DisabledHostKeyCallback(),
            new DisabledPasswordStore(), new DisabledProgressListener());
        service.connect(session, PathCache.empty(), new DisabledCancelCallback());
        new SpectraBulkService(session).pre(Transfer.Type.download, Collections.singletonMap(
            new TransferItem(new Path(String.format("/cyberduck/%s", new AlphanumericRandomStringService().random()), EnumSet.of(Path.Type.file))), new TransferStatus().length(1L)
        ), new DisabledConnectionCallback());
        session.close();
    }

    @Test
    public void testPreDownloadFolderOnly() throws Exception {
        final Host host = new Host(new SpectraProtocol() {
            @Override
            public Scheme getScheme() {
                return Scheme.http;
            }
        }, System.getProperties().getProperty("spectra.hostname"), Integer.valueOf(System.getProperties().getProperty("spectra.port")), new Credentials(
            System.getProperties().getProperty("spectra.user"), System.getProperties().getProperty("spectra.key")
        ));
        final SpectraSession session = new SpectraSession(host, new DisabledX509TrustManager(),
            new DefaultX509KeyManager());
        final LoginConnectionService service = new LoginConnectionService(new DisabledLoginCallback() {
            @Override
            public void warn(final Host bookmark, final String title, final String message, final String continueButton, final String disconnectButton, final String preference) {
                //
            }
        }, new DisabledHostKeyCallback(),
            new DisabledPasswordStore(), new DisabledProgressListener());
        service.connect(session, PathCache.empty(), new DisabledCancelCallback());
        final Set<UUID> keys = new SpectraBulkService(session).pre(Transfer.Type.download, Collections.singletonMap(
            new TransferItem(new Path(String.format("/cyberduck/%s", new AlphanumericRandomStringService().random()), EnumSet.of(Path.Type.directory))), new TransferStatus()
        ), new DisabledConnectionCallback());
        assertTrue(keys.isEmpty());
        session.close();
    }

    @Test
    public void testPreUploadLargeFile() throws Exception {
        final Host host = new Host(new SpectraProtocol() {
            @Override
            public Scheme getScheme() {
                return Scheme.http;
            }
        }, System.getProperties().getProperty("spectra.hostname"), Integer.valueOf(System.getProperties().getProperty("spectra.port")), new Credentials(
            System.getProperties().getProperty("spectra.user"), System.getProperties().getProperty("spectra.key")
        ));
        final SpectraSession session = new SpectraSession(host, new DisabledX509TrustManager(),
            new DefaultX509KeyManager());
        final LoginConnectionService service = new LoginConnectionService(new DisabledLoginCallback() {
            @Override
            public void warn(final Host bookmark, final String title, final String message, final String continueButton, final String disconnectButton, final String preference) {
                //
            }
        }, new DisabledHostKeyCallback(),
            new DisabledPasswordStore(), new DisabledProgressListener());
        service.connect(session, PathCache.empty(), new DisabledCancelCallback());
        final Map<TransferItem, TransferStatus> files = new HashMap<>();
        final TransferStatus status = new TransferStatus();
        final Path file = new Path(String.format("/cyberduck/%s", new AlphanumericRandomStringService().random()), EnumSet.of(Path.Type.file));
        files.put(new TransferItem(file),
            // 11GB
            status.length(112640000000L));
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
        new SpectraDeleteFeature(session).delete(Collections.singletonList(file), new DisabledLoginCallback(), new Delete.DisabledCallback());
        session.close();
    }

    @Test
    public void testPreUploadMultipleLargeFile() throws Exception {
        final Host host = new Host(new SpectraProtocol() {
            @Override
            public Scheme getScheme() {
                return Scheme.http;
            }
        }, System.getProperties().getProperty("spectra.hostname"), Integer.valueOf(System.getProperties().getProperty("spectra.port")), new Credentials(
            System.getProperties().getProperty("spectra.user"), System.getProperties().getProperty("spectra.key")
        ));
        final SpectraSession session = new SpectraSession(host, new DisabledX509TrustManager(),
            new DefaultX509KeyManager());
        final LoginConnectionService service = new LoginConnectionService(new DisabledLoginCallback() {
            @Override
            public void warn(final Host bookmark, final String title, final String message, final String continueButton, final String disconnectButton, final String preference) {
                //
            }
        }, new DisabledHostKeyCallback(),
            new DisabledPasswordStore(), new DisabledProgressListener());
        service.connect(session, PathCache.empty(), new DisabledCancelCallback());
        final Map<TransferItem, TransferStatus> files = new HashMap<>();
        final TransferStatus status = new TransferStatus();
        files.put(new TransferItem(new Path(String.format("/cyberduck/%s", new AlphanumericRandomStringService().random()), EnumSet.of(Path.Type.file))),
            // 11GB
            status.length(118111600640L)
        );
        files.put(new TransferItem(new Path(String.format("/cyberduck/%s", new AlphanumericRandomStringService().random()), EnumSet.of(Path.Type.file))),
            // 11GB
            status.length(118111600640L)
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
        for(TransferItem item : files.keySet()) {
            new SpectraDeleteFeature(session).delete(Collections.singletonList(item.remote), new DisabledLoginCallback(), new Delete.DisabledCallback());
        }
        session.close();
    }

    @Test(expected = ConflictException.class)
    public void testSPECTRA67() throws Exception {
        final Host host = new Host(new SpectraProtocol() {
            @Override
            public Scheme getScheme() {
                return Scheme.http;
            }
        }, System.getProperties().getProperty("spectra.hostname"), Integer.valueOf(System.getProperties().getProperty("spectra.port")), new Credentials(
            System.getProperties().getProperty("spectra.user"), System.getProperties().getProperty("spectra.key")
        ));
        final SpectraSession session = new SpectraSession(host, new DisabledX509TrustManager(), new DefaultX509KeyManager());
        session.open(new DisabledHostKeyCallback(), new DisabledLoginCallback());
        session.login(new DisabledPasswordStore(), new DisabledLoginCallback(), new DisabledCancelCallback());
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
        session.close();
    }
}
