package ch.cyberduck.core;

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

import ch.cyberduck.core.b2.B2AttributesFeature;
import ch.cyberduck.core.b2.B2DeleteFeature;
import ch.cyberduck.core.b2.B2LargeUploadService;
import ch.cyberduck.core.b2.B2Protocol;
import ch.cyberduck.core.b2.B2Session;
import ch.cyberduck.core.features.Delete;
import ch.cyberduck.core.features.Upload;
import ch.cyberduck.core.io.DisabledStreamListener;
import ch.cyberduck.core.transfer.DisabledTransferErrorCallback;
import ch.cyberduck.core.transfer.DisabledTransferPrompt;
import ch.cyberduck.core.transfer.Transfer;
import ch.cyberduck.core.transfer.TransferAction;
import ch.cyberduck.core.transfer.TransferItem;
import ch.cyberduck.core.transfer.TransferOptions;
import ch.cyberduck.core.transfer.TransferSpeedometer;
import ch.cyberduck.core.transfer.UploadTransfer;
import ch.cyberduck.core.worker.SingleTransferWorker;
import ch.cyberduck.test.IntegrationTest;

import org.apache.commons.io.IOUtils;
import org.apache.commons.io.input.CountingInputStream;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.SocketTimeoutException;
import java.security.MessageDigest;
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
        final byte[] content = new byte[100 * 1024 * 1024 + 1];
        new Random().nextBytes(content);
        final OutputStream out = local.getOutputStream(false);
        IOUtils.write(content, out);
        out.close();
        final Host host = new Host(new B2Protocol(), new B2Protocol().getDefaultHostname(),
                new Credentials(
                        System.getProperties().getProperty("b2.user"), System.getProperties().getProperty("b2.key")
                ));
        final AtomicBoolean failed = new AtomicBoolean();
        final B2Session session = new B2Session(host) {
            @Override
            @SuppressWarnings("unchecked")
            public <T> T getFeature(final Class<T> type) {
                if(type == Upload.class) {
                    return (T) new B2LargeUploadService(this, 100L * 1024L * 1024L) {
                        @Override
                        protected InputStream decorate(final InputStream in, final MessageDigest digest) throws IOException {
                            if(failed.get()) {
                                // Second attempt successful
                                return in;
                            }
                            return new CountingInputStream(in) {
                                @Override
                                protected void beforeRead(final int n) throws IOException {
                                    super.beforeRead(n);
                                    if(this.getByteCount() >= 100L * 1024L * 1024L) {
                                        failed.set(true);
                                        throw new SocketTimeoutException();
                                    }
                                }
                            };
                        }
                    };
                }
                return super.getFeature(type);
            }
        };
        session.open(new DisabledHostKeyCallback(), new DisabledTranscriptListener());
        session.login(new DisabledPasswordStore(), new DisabledLoginCallback(), new DisabledCancelCallback());
        final Path bucket = new Path("test-cyberduck", EnumSet.of(Path.Type.directory, Path.Type.volume));
        final Path test = new Path(bucket, UUID.randomUUID().toString(), EnumSet.of(Path.Type.file));
        final Transfer t = new UploadTransfer(new Host(new TestProtocol()), test, local);
        final BytecountStreamListener counter = new BytecountStreamListener(new DisabledStreamListener());
        assertTrue(new SingleTransferWorker(session, t, new TransferOptions(), new TransferSpeedometer(t), new DisabledTransferPrompt() {
            @Override
            public TransferAction prompt(final TransferItem file) {
                return TransferAction.overwrite;
            }
        }, new DisabledTransferErrorCallback(),
                new DisabledProgressListener(), counter, new DisabledLoginCallback(), TransferItemCache.empty()) {

        }.run(session));
        local.delete();
        assertEquals(100 * 1024 * 1024 + 1, new B2AttributesFeature(session).find(test).getSize());
        assertEquals(100 * 1024 * 1024 + 1, counter.getSent(), 0L);
        assertTrue(failed.get());
        new B2DeleteFeature(session).delete(Collections.singletonList(test), new DisabledLoginCallback(), new Delete.DisabledCallback());
    }
}
