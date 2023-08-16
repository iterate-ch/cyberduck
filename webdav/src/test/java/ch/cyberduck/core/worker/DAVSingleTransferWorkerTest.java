package ch.cyberduck.core.worker;

/*
 * Copyright (c) 2002-2023 iterate GmbH. All rights reserved.
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
import ch.cyberduck.core.ConnectionCallback;
import ch.cyberduck.core.DisabledCancelCallback;
import ch.cyberduck.core.DisabledConnectionCallback;
import ch.cyberduck.core.DisabledHostKeyCallback;
import ch.cyberduck.core.DisabledLoginCallback;
import ch.cyberduck.core.DisabledPasswordStore;
import ch.cyberduck.core.DisabledProgressListener;
import ch.cyberduck.core.Host;
import ch.cyberduck.core.Local;
import ch.cyberduck.core.LoginConnectionService;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.dav.AbstractDAVTest;
import ch.cyberduck.core.dav.DAVDeleteFeature;
import ch.cyberduck.core.dav.DAVReadFeature;
import ch.cyberduck.core.dav.DAVSession;
import ch.cyberduck.core.dav.DAVUploadFeature;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.features.Delete;
import ch.cyberduck.core.features.Read;
import ch.cyberduck.core.io.BandwidthThrottle;
import ch.cyberduck.core.io.DisabledStreamListener;
import ch.cyberduck.core.notification.DisabledNotificationService;
import ch.cyberduck.core.shared.DefaultAttributesFinderFeature;
import ch.cyberduck.core.shared.DefaultHomeFinderService;
import ch.cyberduck.core.ssl.DefaultX509KeyManager;
import ch.cyberduck.core.ssl.DisabledX509TrustManager;
import ch.cyberduck.core.transfer.DisabledTransferPrompt;
import ch.cyberduck.core.transfer.DownloadTransfer;
import ch.cyberduck.core.transfer.Transfer;
import ch.cyberduck.core.transfer.TransferAction;
import ch.cyberduck.core.transfer.TransferErrorCallback;
import ch.cyberduck.core.transfer.TransferItem;
import ch.cyberduck.core.transfer.TransferOptions;
import ch.cyberduck.core.transfer.TransferSpeedometer;
import ch.cyberduck.core.transfer.TransferStatus;
import ch.cyberduck.test.IntegrationTest;

import org.apache.commons.io.IOUtils;
import org.apache.commons.io.input.CountingInputStream;
import org.apache.commons.lang3.RandomUtils;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.SocketTimeoutException;
import java.util.Collections;
import java.util.EnumSet;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.Assert.*;

@Category(IntegrationTest.class)
public class DAVSingleTransferWorkerTest extends AbstractDAVTest {

    @Test
    public void testDownloadTransferWithFailure() throws Exception {
        final Path test = new Path(new DefaultHomeFinderService(session).find(), new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file));
        final Local local = new Local(System.getProperty("java.io.tmpdir"), new AlphanumericRandomStringService().random());
        final byte[] content = RandomUtils.nextBytes(65536); // 100MB
        final OutputStream out = local.getOutputStream(false);
        assertNotNull(out);
        IOUtils.write(content, out);
        out.close();
        new DAVUploadFeature(session).upload(test, local, new BandwidthThrottle(BandwidthThrottle.UNLIMITED), new DisabledStreamListener(), new TransferStatus().withLength(content.length), new DisabledConnectionCallback());
        final AtomicBoolean failed = new AtomicBoolean();
        final Host host = new Host(session.getHost()) {
            @Override
            public String getProperty(final String key) {
                if("queue.download.segments.threshold".equals(key)) {
                    return String.valueOf(0);
                }
                if("queue.download.segments.count".equals(key)) {
                    return String.valueOf(2);
                }
                if("connection.retry".equals(key)) {
                    return String.valueOf(1);
                }
                return super.getProperty(key);
            }
        };
        final Transfer t = new DownloadTransfer(host, test, local);
        final BytecountStreamListener counter = new BytecountStreamListener();
        final DAVSession session = new DAVSession(host, new DisabledX509TrustManager(), new DefaultX509KeyManager()) {
            final DAVReadFeature read = new DAVReadFeature(this) {
                @Override
                public InputStream read(final Path file, final TransferStatus status, final ConnectionCallback callback) throws BackgroundException {
                    final InputStream proxy = super.read(file, status, callback);
                    if(failed.get()) {
                        // Second attempt successful
                        return proxy;
                    }
                    return new CountingInputStream(proxy) {
                        @Override
                        protected synchronized void beforeRead(final int n) throws IOException {
                            failed.set(true);
                            throw new SocketTimeoutException();
                        }
                    };
                }
            };

            @Override
            @SuppressWarnings("unchecked")
            public <T> T _getFeature(final Class<T> type) {
                if(type == Read.class) {
                    return (T) read;
                }
                return super._getFeature(type);
            }
        };
        new LoginConnectionService(new DisabledLoginCallback(),
                new DisabledHostKeyCallback(),
                new DisabledPasswordStore(),
                new DisabledProgressListener()).connect(session, new DisabledCancelCallback());
        final AbstractTransferWorker worker = new SingleTransferWorker(
                session, null, t, new TransferOptions(),
                new TransferSpeedometer(t), new DisabledTransferPrompt() {
            @Override
            public TransferAction prompt(final TransferItem file) {
                return TransferAction.overwrite;
            }
        }, new TransferErrorCallback() {
            @Override
            public boolean prompt(final TransferItem item, final TransferStatus status, final BackgroundException failure, final int pending) {
                return true;
            }
        }, new DisabledProgressListener(), counter, new DisabledConnectionCallback(), new DisabledNotificationService());
        assertTrue(worker.run(session));
        local.delete();
        assertEquals(t.getTransferred(), counter.getRecv(), 0L);
        assertEquals(t.getTransferred(), counter.getSent(), 0L);
        assertEquals(content.length, t.getSize(), 0L);
        assertEquals(content.length, t.getTransferred(), 0L);
        assertEquals(content.length, new DefaultAttributesFinderFeature(session).find(test).getSize());
        assertEquals(content.length, counter.getRecv(), 0L);
        assertEquals(content.length, counter.getSent(), 0L);
        assertTrue(t.isComplete());
        assertTrue(failed.get());
        new DAVDeleteFeature(session).delete(Collections.singletonList(test), new DisabledLoginCallback(), new Delete.DisabledCallback());
    }
}
