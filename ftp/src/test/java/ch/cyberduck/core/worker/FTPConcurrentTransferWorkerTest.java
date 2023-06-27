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
import ch.cyberduck.core.ftp.AbstractFTPTest;
import ch.cyberduck.core.ftp.FTPAttributesFinderFeature;
import ch.cyberduck.core.ftp.FTPDeleteFeature;
import ch.cyberduck.core.ftp.FTPSession;
import ch.cyberduck.core.ftp.FTPWriteFeature;
import ch.cyberduck.core.io.DisabledStreamListener;
import ch.cyberduck.core.io.StatusOutputStream;
import ch.cyberduck.core.io.VoidStatusOutputStream;
import ch.cyberduck.core.notification.DisabledNotificationService;
import ch.cyberduck.core.pool.DefaultSessionPool;
import ch.cyberduck.core.pool.PooledSessionFactory;
import ch.cyberduck.core.pool.SessionPool;
import ch.cyberduck.core.preferences.HostPreferences;
import ch.cyberduck.core.shared.DefaultAttributesFinderFeature;
import ch.cyberduck.core.shared.DefaultHomeFinderService;
import ch.cyberduck.core.ssl.DefaultX509KeyManager;
import ch.cyberduck.core.ssl.DisabledX509TrustManager;
import ch.cyberduck.core.transfer.DisabledTransferErrorCallback;
import ch.cyberduck.core.transfer.DisabledTransferPrompt;
import ch.cyberduck.core.transfer.Transfer;
import ch.cyberduck.core.transfer.TransferAction;
import ch.cyberduck.core.transfer.TransferItem;
import ch.cyberduck.core.transfer.TransferOptions;
import ch.cyberduck.core.transfer.TransferSpeedometer;
import ch.cyberduck.core.transfer.TransferStatus;
import ch.cyberduck.core.transfer.UploadTransfer;
import ch.cyberduck.core.vault.DefaultVaultRegistry;

import org.apache.commons.io.IOUtils;
import org.apache.commons.io.output.CountingOutputStream;
import org.apache.commons.pool2.impl.GenericObjectPool;
import org.junit.Assume;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class FTPConcurrentTransferWorkerTest extends AbstractFTPTest {

    @Test
    public void testUploadTransferWithFailure() throws Exception {
        Assume.assumeTrue(Factory.Platform.getDefault() != Factory.Platform.Name.windows);
        final Local local = new Local(System.getProperty("java.io.tmpdir"), new AlphanumericRandomStringService().random());
        final byte[] content = new byte[98305];
        new Random().nextBytes(content);
        final OutputStream out = local.getOutputStream(false);
        IOUtils.write(content, out);
        out.close();
        final AtomicBoolean failed = new AtomicBoolean();
        final Path test = new Path(new DefaultHomeFinderService(session).find(), new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file));
        final Host host = new Host(session.getHost()) {
            @Override
            public String getProperty(final String key) {
                if("queue.connections.limit.ftp".equals(key)) {
                    return String.valueOf(2);
                }
                return super.getProperty(key);
            }
        };
        final Transfer t = new UploadTransfer(host, test, local);
        final BytecountStreamListener counter = new BytecountStreamListener();
        final LoginConnectionService connect = new LoginConnectionService(new DisabledLoginCallback(), new DisabledHostKeyCallback(), new DisabledPasswordStore(), new DisabledProgressListener());
        final DefaultSessionPool pool = new DefaultSessionPool(connect,
                new DefaultVaultRegistry(new DisabledPasswordCallback()), new DisabledTranscriptListener(), host,
                new GenericObjectPool<>(new PooledSessionFactory(connect, new DisabledX509TrustManager(), new DefaultX509KeyManager(),
                        host, new DefaultVaultRegistry(new DisabledPasswordCallback())) {
                    @Override
                    public Session create() {
                        return new FTPSession(session.getHost()) {
                            final FTPWriteFeature write = new FTPWriteFeature(this) {
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
                    }
                }));
        final ConcurrentTransferWorker worker = new ConcurrentTransferWorker(
                pool, SessionPool.DISCONNECTED, t, new TransferOptions(),
                new TransferSpeedometer(t), new DisabledTransferPrompt() {
            @Override
            public TransferAction prompt(final TransferItem file) {
                return TransferAction.overwrite;
            }
        }, new DisabledTransferErrorCallback(),
                new DisabledConnectionCallback(), new DisabledProgressListener(), counter, new DisabledNotificationService()
        );
        assertTrue(worker.run(session));
        local.delete();
        assertTrue(t.isComplete());
        assertEquals(content.length, t.getTransferred(), 0L);
        assertEquals(t.getTransferred(), counter.getRecv(), 0L);
        assertEquals(t.getTransferred(), counter.getSent(), 0L);
        assertEquals(content.length, new DefaultAttributesFinderFeature(session).find(test).getSize());
        assertEquals(content.length, new FTPAttributesFinderFeature(session).find(test).getSize());
        assertEquals(content.length, counter.getRecv(), 0L);
        assertEquals(content.length, counter.getSent(), 0L);
        assertTrue(failed.get());
        new FTPDeleteFeature(session).delete(Collections.singletonList(test), new DisabledLoginCallback(), new Delete.DisabledCallback());
    }

    @Test
    public void testUpload() throws Exception {
        final int files = 20;
        final int connections = 2;
        final List<TransferItem> list = new ArrayList<>();
        final Local file = new Local(File.createTempFile(new AlphanumericRandomStringService().random(), "t").getAbsolutePath());
        for(int i = 1; i <= files; i++) {
            list.add(new TransferItem(new Path(String.format("/t%d", i), EnumSet.of(Path.Type.file)), file));
        }
        final Host host = new Host(session.getHost()) {
            @Override
            public String getProperty(final String key) {
                if("queue.connections.limit.ftp".equals(key)) {
                    return String.valueOf(connections);
                }
                return super.getProperty(key);
            }
        };
        final Transfer transfer = new UploadTransfer(host, list);
        final DefaultSessionPool pool = new DefaultSessionPool(
                new LoginConnectionService(new DisabledLoginCallback(), new DisabledHostKeyCallback(), new DisabledPasswordStore(),
                        new DisabledProgressListener()),
                new DisabledX509TrustManager(), new DefaultX509KeyManager(),
                new DefaultVaultRegistry(new DisabledPasswordCallback()), new DisabledTranscriptListener(), host);
        final ConcurrentTransferWorker worker = new ConcurrentTransferWorker(
                pool.withMaxTotal(connections), SessionPool.DISCONNECTED,
                transfer, new TransferOptions(), new TransferSpeedometer(transfer), new DisabledTransferPrompt() {
            @Override
            public TransferAction prompt(final TransferItem file) {
                return TransferAction.overwrite;
            }
        }, new DisabledTransferErrorCallback(),
                new DisabledLoginCallback(), new DisabledProgressListener(), new DisabledStreamListener(), new DisabledNotificationService()
        );
        pool.withMaxTotal(connections);
        final Session<?> session = worker.borrow(ConcurrentTransferWorker.Connection.source);
        assertTrue(worker.run(session));
        worker.release(session, ConcurrentTransferWorker.Connection.source, null);
        assertEquals(0L, transfer.getTransferred(), 0L);
        worker.cleanup(true);
    }
}
