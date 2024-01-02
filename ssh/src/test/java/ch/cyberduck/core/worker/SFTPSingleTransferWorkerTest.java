package ch.cyberduck.core.worker;

/*
 * Copyright (c) 2002-2016 iterate GmbH. All rights reserved.
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

import ch.cyberduck.core.*;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.features.Delete;
import ch.cyberduck.core.features.Write;
import ch.cyberduck.core.io.StatusOutputStream;
import ch.cyberduck.core.io.StreamCopier;
import ch.cyberduck.core.io.VoidStatusOutputStream;
import ch.cyberduck.core.notification.DisabledNotificationService;
import ch.cyberduck.core.preferences.HostPreferences;
import ch.cyberduck.core.sftp.AbstractSFTPTest;
import ch.cyberduck.core.sftp.SFTPAttributesFinderFeature;
import ch.cyberduck.core.sftp.SFTPDeleteFeature;
import ch.cyberduck.core.sftp.SFTPDirectoryFeature;
import ch.cyberduck.core.sftp.SFTPFindFeature;
import ch.cyberduck.core.sftp.SFTPHomeDirectoryService;
import ch.cyberduck.core.sftp.SFTPListService;
import ch.cyberduck.core.sftp.SFTPReadFeature;
import ch.cyberduck.core.sftp.SFTPSession;
import ch.cyberduck.core.sftp.SFTPWriteFeature;
import ch.cyberduck.core.ssl.DefaultX509KeyManager;
import ch.cyberduck.core.ssl.DisabledX509TrustManager;
import ch.cyberduck.core.transfer.DisabledTransferErrorCallback;
import ch.cyberduck.core.transfer.DisabledTransferPrompt;
import ch.cyberduck.core.transfer.SyncTransfer;
import ch.cyberduck.core.transfer.Transfer;
import ch.cyberduck.core.transfer.TransferAction;
import ch.cyberduck.core.transfer.TransferItem;
import ch.cyberduck.core.transfer.TransferOptions;
import ch.cyberduck.core.transfer.TransferSpeedometer;
import ch.cyberduck.core.transfer.TransferStatus;
import ch.cyberduck.core.transfer.UploadTransfer;
import ch.cyberduck.test.IntegrationTest;

import org.apache.commons.io.IOUtils;
import org.apache.commons.io.output.CountingOutputStream;
import org.apache.commons.lang3.RandomUtils;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.SocketTimeoutException;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.Assert.*;
import static org.junit.Assert.assertArrayEquals;

@Category(IntegrationTest.class)
public class SFTPSingleTransferWorkerTest extends AbstractSFTPTest {

    @Test
    public void testUploadTransferWithFailure() throws Exception {
        final Local local = new Local(System.getProperty("java.io.tmpdir"), UUID.randomUUID().toString());
        final byte[] content = new byte[98305];
        new Random().nextBytes(content);
        final OutputStream out = local.getOutputStream(false);
        IOUtils.write(content, out);
        out.close();
        final BytecountStreamListener counter = new BytecountStreamListener();
        final AtomicBoolean failed = new AtomicBoolean();
        final Host host = new Host(session.getHost()) {
            @Override
            public String getProperty(final String key) {
                if("connection.retry".equals(key)) {
                    return String.valueOf(1);
                }
                return super.getProperty(key);
            }
        };
        final SFTPSession session = new SFTPSession(host, new DisabledX509TrustManager(), new DefaultX509KeyManager()) {
            final SFTPWriteFeature write = new SFTPWriteFeature(this) {
                @Override
                public StatusOutputStream<Void> write(final Path file, final TransferStatus status, final ConnectionCallback callback) throws BackgroundException {
                    final StatusOutputStream<Void> proxy = super.write(file, status, callback);
                    if(failed.get()) {
                        // Second attempt successful
                        return proxy;
                    }
                    return new VoidStatusOutputStream(new CountingOutputStream(proxy) {
                        @Override
                        protected void afterWrite(final int n) throws IOException {
                            super.afterWrite(n);
                            if(this.getByteCount() >= 42768L) {
                                // Buffer size
                                assertEquals(new HostPreferences(host).getLong("connection.chunksize"), counter.getSent());
                                failed.set(true);
                                throw new SocketTimeoutException();
                            }
                        }
                    }) {
                        @Override
                        public Void getStatus() throws BackgroundException {
                            return proxy.getStatus();
                        }
                    };
                }
            };

            @Override
            @SuppressWarnings("unchecked")
            public <T> T _getFeature(final Class<T> type) {
                if(type == Write.class) {
                    return (T) write;
                }
                return super._getFeature(type);
            }
        };
        new LoginConnectionService(new DisabledLoginCallback(),
                new DisabledHostKeyCallback(),
                new DisabledPasswordStore(),
                new DisabledProgressListener()).connect(session, new DisabledCancelCallback());
        final Path test = new Path(new SFTPHomeDirectoryService(session).find(), new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file));
        final Transfer t = new UploadTransfer(host, test, local);
        assertTrue(new SingleTransferWorker(session, session, t, new TransferOptions(), new TransferSpeedometer(t), new DisabledTransferPrompt() {
            @Override
            public TransferAction prompt(final TransferItem file) {
                return TransferAction.overwrite;
            }
        }, new DisabledTransferErrorCallback(),
                new DisabledProgressListener(), counter, new DisabledLoginCallback(), new DisabledNotificationService()) {

        }.run(session));
        local.delete();
        assertTrue(t.isComplete());
        assertEquals(t.getTransferred(), counter.getRecv(), 0L);
        assertEquals(t.getTransferred(), counter.getSent(), 0L);
        assertEquals(content.length, new SFTPAttributesFinderFeature(session).find(test).getSize());
        assertEquals(content.length, counter.getRecv(), 0L);
        assertEquals(content.length, counter.getSent(), 0L);
        assertTrue(failed.get());
        new SFTPDeleteFeature(session).delete(Collections.singletonList(test), new DisabledLoginCallback(), new Delete.DisabledCallback());
        local.delete();
    }

    @Test
    public void testSynchronizeUpload() throws Exception {
        final Local directory = new Local(System.getProperty("java.io.tmpdir"), new AlphanumericRandomStringService().random());
        directory.mkdir();
        final Local local = new Local(directory, new AlphanumericRandomStringService().random());
        final byte[] content = new byte[2387];
        new Random().nextBytes(content);
        final OutputStream out = local.getOutputStream(false);
        IOUtils.write(content, out);
        out.close();
        final BytecountStreamListener counter = new BytecountStreamListener();
        final Path remotedirectory = new Path(new SFTPHomeDirectoryService(session).find(), new AlphanumericRandomStringService().random(),
                EnumSet.of(Path.Type.directory));
        final Path remotefile = new Path(remotedirectory, local.getName(), EnumSet.of(Path.Type.file));
        final Transfer t = new SyncTransfer(session.getHost(), new TransferItem(remotedirectory, directory), TransferAction.overwrite);
        assertTrue(new SingleTransferWorker(session, session, t, new TransferOptions(), new TransferSpeedometer(t), new DisabledTransferPrompt() {
            @Override
            public TransferAction prompt(final TransferItem file) {
                return TransferAction.overwrite;
            }
        }, new DisabledTransferErrorCallback(),
                new DisabledProgressListener(), counter, new DisabledLoginCallback(), new DisabledNotificationService()) {

        }.run(session));
        local.delete();
        directory.delete();
        assertTrue(t.isComplete());
        assertEquals(t.getTransferred(), counter.getRecv(), 0L);
        assertEquals(t.getTransferred(), counter.getSent(), 0L);
        assertEquals(content.length, counter.getRecv(), 0L);
        assertEquals(content.length, counter.getSent(), 0L);
        new SFTPDeleteFeature(session).delete(Arrays.asList(remotefile, remotedirectory), new DisabledLoginCallback(), new Delete.DisabledCallback());
    }

    @Test
    public void testSynchronizeDownload() throws Exception {
        final Local directory = new Local(System.getProperty("java.io.tmpdir"), new AlphanumericRandomStringService().random());
        directory.mkdir();
        final Local local = new Local(directory, new AlphanumericRandomStringService().random());
        final BytecountStreamListener counter = new BytecountStreamListener();
        final Path remotedirectory = new Path(new SFTPHomeDirectoryService(session).find(), new AlphanumericRandomStringService().random(),
                EnumSet.of(Path.Type.directory));
        new SFTPDirectoryFeature(session).mkdir(remotedirectory, new TransferStatus());
        final Path remotefile = new Path(remotedirectory, local.getName(), EnumSet.of(Path.Type.file));
        final TransferStatus status = new TransferStatus();
        final byte[] content = RandomUtils.nextBytes(8576);
        status.setLength(content.length);
        status.setExists(false);
        final OutputStream out = new SFTPWriteFeature(session).write(remotefile, status, new DisabledConnectionCallback());
        assertNotNull(out);
        out.write(content);
        out.close();
        final Transfer t = new SyncTransfer(session.getHost(), new TransferItem(remotedirectory, directory), TransferAction.overwrite);
        assertTrue(new SingleTransferWorker(session, session, t, new TransferOptions(), new TransferSpeedometer(t), new DisabledTransferPrompt() {
            @Override
            public TransferAction prompt(final TransferItem file) {
                return TransferAction.overwrite;
            }
        }, new DisabledTransferErrorCallback(),
                new DisabledProgressListener(), counter, new DisabledLoginCallback(), new DisabledNotificationService()) {

        }.run(session));
        local.delete();
        directory.delete();
        assertTrue(t.isComplete());
        assertEquals(t.getTransferred(), counter.getRecv(), 0L);
        assertEquals(t.getTransferred(), counter.getSent(), 0L);
        assertEquals(content.length, counter.getRecv(), 0L);
        assertEquals(content.length, counter.getSent(), 0L);
        new SFTPDeleteFeature(session).delete(Arrays.asList(remotefile, remotedirectory), new DisabledLoginCallback(), new Delete.DisabledCallback());
    }
}
