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

import ch.cyberduck.core.Credentials;
import ch.cyberduck.core.DisabledHostKeyCallback;
import ch.cyberduck.core.DisabledTranscriptListener;
import ch.cyberduck.core.Host;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.Scheme;
import ch.cyberduck.core.exception.NotfoundException;
import ch.cyberduck.core.ssl.DefaultX509KeyManager;
import ch.cyberduck.core.ssl.DisabledX509TrustManager;
import ch.cyberduck.core.transfer.Transfer;
import ch.cyberduck.core.transfer.TransferStatus;
import ch.cyberduck.test.IntegrationTest;

import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
        session.open(new DisabledHostKeyCallback(), new DisabledTranscriptListener());
        final Map<Path, TransferStatus> files = new HashMap<>();
        final TransferStatus status = new TransferStatus();
        final Path file = new Path(String.format("/test.cyberduck.ch/%s", UUID.randomUUID().toString()), EnumSet.of(Path.Type.file));
        files.put(file,
                status.length(1L)
        );
        final SpectraBulkService service = new SpectraBulkService(session);
        service.pre(Transfer.Type.upload, files);
        assertFalse(status.getParameters().isEmpty());
        assertNotNull(status.getParameters().get("job"));
        service.query(Transfer.Type.upload, file, status);
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
        session.open(new DisabledHostKeyCallback(), new DisabledTranscriptListener());
        final Map<Path, TransferStatus> files = new HashMap<>();
        final Path directory = new Path(String.format("/test.cyberduck.ch/%s", UUID.randomUUID().toString()), EnumSet.of(Path.Type.directory));
        files.put(directory, new TransferStatus().length(0L));
        files.put(new Path(directory, UUID.randomUUID().toString(), EnumSet.of(Path.Type.file)),
                new TransferStatus().length(1L)
        );
        new SpectraBulkService(session).pre(Transfer.Type.upload, files);
        session.close();
    }

    @Test(expected = NotfoundException.class)
    public void testPreDownload() throws Exception {
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
        session.open(new DisabledHostKeyCallback(), new DisabledTranscriptListener());
        new SpectraBulkService(session).pre(Transfer.Type.download, Collections.singletonMap(
                new Path(String.format("/test.cyberduck.ch/%s", UUID.randomUUID().toString()), EnumSet.of(Path.Type.file)), new TransferStatus().length(1L)
        ));
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
        session.open(new DisabledHostKeyCallback(), new DisabledTranscriptListener());
        new SpectraBulkService(session).pre(Transfer.Type.download, Collections.singletonMap(
                new Path(String.format("/test.cyberduck.ch/%s", UUID.randomUUID().toString()), EnumSet.of(Path.Type.directory)), new TransferStatus()
        ));
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
        session.open(new DisabledHostKeyCallback(), new DisabledTranscriptListener());
        final Map<Path, TransferStatus> files = new HashMap<>();
        final TransferStatus status = new TransferStatus();
        final Path file = new Path(String.format("/test.cyberduck.ch/%s", UUID.randomUUID().toString()), EnumSet.of(Path.Type.file));
        files.put(file,
                // 11GB
                status.length(11264000000L)
        );
        final SpectraBulkService service = new SpectraBulkService(session);
        service.pre(Transfer.Type.upload, files);
        assertFalse(status.getParameters().isEmpty());
        assertNotNull(status.getParameters().get("job"));
        final List<TransferStatus> list = service.query(Transfer.Type.upload, file, status);
        assertEquals(1, list.size());
        session.close();
    }
}