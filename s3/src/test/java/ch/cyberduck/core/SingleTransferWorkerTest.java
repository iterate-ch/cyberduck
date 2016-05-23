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

import ch.cyberduck.core.features.Delete;
import ch.cyberduck.core.features.Upload;
import ch.cyberduck.core.io.DisabledStreamListener;
import ch.cyberduck.core.s3.S3AttributesFeature;
import ch.cyberduck.core.s3.S3DefaultDeleteFeature;
import ch.cyberduck.core.s3.S3MultipartUploadService;
import ch.cyberduck.core.s3.S3Protocol;
import ch.cyberduck.core.s3.S3Session;
import ch.cyberduck.core.transfer.DisabledTransferErrorCallback;
import ch.cyberduck.core.transfer.DisabledTransferItemCallback;
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

@IntegrationTest
public class SingleTransferWorkerTest {

    @Test
    public void testTransferredSizeRepeat() throws Exception {
        final Local local = new Local(System.getProperty("java.io.tmpdir"), UUID.randomUUID().toString());
        final byte[] content = new byte[6 * 1024 * 1024];
        new Random().nextBytes(content);
        final OutputStream out = local.getOutputStream(false);
        IOUtils.write(content, out);
        out.close();
        final Host host = new Host(new S3Protocol(), new S3Protocol().getDefaultHostname(), new Credentials(
                System.getProperties().getProperty("s3.key"), System.getProperties().getProperty("s3.secret")
        ));
        final AtomicBoolean failed = new AtomicBoolean();
        final S3Session session = new S3Session(host) {
            @Override
            @SuppressWarnings("unchecked")
            public <T> T getFeature(final Class<T> type) {
                if(type == Upload.class) {
                    return (T) new S3MultipartUploadService(this, 5L * 1024L * 1024L, 1) {
                        @Override
                        protected InputStream decorate(final InputStream in, final MessageDigest digest) throws IOException {
                            if(failed.get()) {
                                // Second attempt successful
                                return super.decorate(in, digest);
                            }
                            return new CountingInputStream(super.decorate(in, digest)) {
                                @Override
                                protected void beforeRead(final int n) throws IOException {
                                    super.beforeRead(n);
                                    if(this.getByteCount() >= 5L * 1024L * 1024L) {
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
        final Path container = new Path("test.cyberduck.ch", EnumSet.of(Path.Type.directory, Path.Type.volume));
        container.attributes().setRegion("DFW");
        final Path test = new Path(container, UUID.randomUUID().toString(), EnumSet.of(Path.Type.file));
        final Transfer t = new UploadTransfer(new Host(new TestProtocol()), test, local);
        final BytecountStreamListener counter = new BytecountStreamListener(new DisabledStreamListener());
        assertTrue(new SingleTransferWorker(session, t, new TransferOptions(), new TransferSpeedometer(t), new DisabledTransferPrompt() {
            @Override
            public TransferAction prompt(final TransferItem file) {
                return TransferAction.overwrite;
            }
        }, new DisabledTransferErrorCallback(), new DisabledTransferItemCallback(),
                new DisabledProgressListener(), counter, new DisabledLoginCallback(), TransferItemCache.empty()) {

        }.run(session));
        local.delete();
        assertEquals(6L * 1024L * 1024L, new S3AttributesFeature(session).find(test).getSize());
        assertEquals(6L * 1024L * 1024L, counter.getSent(), 0L);
        assertTrue(failed.get());
        new S3DefaultDeleteFeature(session).delete(Collections.singletonList(test), new DisabledLoginCallback(), new Delete.Callback() {
            @Override
            public void delete(final Path file) {
            }
        });
    }
}
