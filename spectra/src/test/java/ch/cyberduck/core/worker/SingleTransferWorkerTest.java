package ch.cyberduck.core.worker;

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

import ch.cyberduck.core.*;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.features.Delete;
import ch.cyberduck.core.features.Upload;
import ch.cyberduck.core.features.Write;
import ch.cyberduck.core.http.HttpResponseOutputStream;
import ch.cyberduck.core.io.DisabledStreamListener;
import ch.cyberduck.core.s3.S3AttributesFinderFeature;
import ch.cyberduck.core.s3.S3DefaultDeleteFeature;
import ch.cyberduck.core.spectra.SpectraBulkService;
import ch.cyberduck.core.spectra.SpectraProtocol;
import ch.cyberduck.core.spectra.SpectraSession;
import ch.cyberduck.core.spectra.SpectraUploadFeature;
import ch.cyberduck.core.spectra.SpectraWriteFeature;
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
import ch.cyberduck.test.IntegrationTest;

import org.apache.commons.io.IOUtils;
import org.apache.commons.io.output.CountingOutputStream;
import org.jets3t.service.model.StorageObject;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.io.IOException;
import java.io.OutputStream;
import java.net.SocketTimeoutException;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@Category(IntegrationTest.class)
public class SingleTransferWorkerTest {

    @Test
    public void testTransferredSizeRepeat() throws Exception {
        final Local local = new Local(System.getProperty("java.io.tmpdir"), UUID.randomUUID().toString());
        final byte[] content = new byte[98305];
        new Random().nextBytes(content);
        final OutputStream out = local.getOutputStream(false);
        IOUtils.write(content, out);
        out.close();
        final Host host = new Host(new SpectraProtocol() {
            @Override
            public Scheme getScheme() {
                return Scheme.http;
            }
        }, System.getProperties().getProperty("spectra.hostname"), Integer.valueOf(System.getProperties().getProperty("spectra.port")), new Credentials(
                System.getProperties().getProperty("spectra.user"), System.getProperties().getProperty("spectra.key")
        ));
        final AtomicBoolean failed = new AtomicBoolean();
        final SpectraSession session = new SpectraSession(host, new DisabledX509TrustManager(),
                new DefaultX509KeyManager()) {
            final SpectraWriteFeature write = new SpectraWriteFeature(this) {
                @Override
                public HttpResponseOutputStream<StorageObject> write(final Path file, final TransferStatus status, final ConnectionCallback callback) throws BackgroundException {
                    final HttpResponseOutputStream<StorageObject> proxy = super.write(file, status, callback);
                    if(failed.get()) {
                        // Second attempt successful
                        return proxy;
                    }
                    return new HttpResponseOutputStream<StorageObject>(new CountingOutputStream(proxy) {
                        @Override
                        protected void afterWrite(final int n) throws IOException {
                            super.afterWrite(n);
                            if(this.getByteCount() >= 42768L) {
                                assertTrue(this.getByteCount() < content.length);
                                // Buffer size
                                assertEquals(32768L, status.getOffset());
                                failed.set(true);
                                throw new SocketTimeoutException();
                            }
                        }
                    }) {
                        @Override
                        public StorageObject getStatus() throws BackgroundException {
                            return proxy.getStatus();
                        }

                        @Override
                        public void close() throws IOException {
                            proxy.close();
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
                if(type == Upload.class) {
                    return (T) new SpectraUploadFeature(write, new SpectraBulkService(this));
                }
                return super._getFeature(type);
            }
        };
        session.open(new DisabledHostKeyCallback(), new DisabledLoginCallback());
        session.login(new DisabledPasswordStore(), new DisabledLoginCallback(), new DisabledCancelCallback());
        final Path container = new Path("cyberduck", EnumSet.of(Path.Type.directory, Path.Type.volume));
        final Path test = new Path(container, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file));
        final Transfer t = new UploadTransfer(new Host(new TestProtocol()), test, local);
        final BytecountStreamListener counter = new BytecountStreamListener(new DisabledStreamListener());
        assertTrue(new SingleTransferWorker(session, session, t, new TransferOptions(), new TransferSpeedometer(t), new DisabledTransferPrompt() {
            @Override
            public TransferAction prompt(final TransferItem file) {
                return TransferAction.overwrite;
            }
        }, new DisabledTransferErrorCallback(),
                new DisabledProgressListener(), counter, new DisabledLoginCallback(), new DisabledPasswordCallback(), TransferItemCache.empty()) {

        }.run(session, session));
        local.delete();
        assertEquals(content.length, counter.getSent(), 0L);
        assertTrue(failed.get());
        assertEquals(content.length, new S3AttributesFinderFeature(session).find(test).getSize());
        new S3DefaultDeleteFeature(session).delete(Collections.singletonList(test), new DisabledLoginCallback(), new Delete.DisabledCallback());
    }
}
