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

import ch.cyberduck.core.BytecountStreamListener;
import ch.cyberduck.core.Credentials;
import ch.cyberduck.core.DisabledCancelCallback;
import ch.cyberduck.core.DisabledHostKeyCallback;
import ch.cyberduck.core.DisabledLoginCallback;
import ch.cyberduck.core.DisabledPasswordStore;
import ch.cyberduck.core.DisabledProgressListener;
import ch.cyberduck.core.Host;
import ch.cyberduck.core.Local;
import ch.cyberduck.core.LoginConnectionService;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathAttributes;
import ch.cyberduck.core.b2.B2AttributesFinderFeature;
import ch.cyberduck.core.b2.B2DeleteFeature;
import ch.cyberduck.core.b2.B2LargeUploadService;
import ch.cyberduck.core.b2.B2Protocol;
import ch.cyberduck.core.b2.B2Session;
import ch.cyberduck.core.b2.B2VersionIdProvider;
import ch.cyberduck.core.b2.B2WriteFeature;
import ch.cyberduck.core.features.Delete;
import ch.cyberduck.core.features.Upload;
import ch.cyberduck.core.io.Checksum;
import ch.cyberduck.core.notification.DisabledNotificationService;
import ch.cyberduck.core.ssl.DefaultX509KeyManager;
import ch.cyberduck.core.ssl.DefaultX509TrustManager;
import ch.cyberduck.core.transfer.DisabledTransferErrorCallback;
import ch.cyberduck.core.transfer.DisabledTransferPrompt;
import ch.cyberduck.core.transfer.Transfer;
import ch.cyberduck.core.transfer.TransferAction;
import ch.cyberduck.core.transfer.TransferItem;
import ch.cyberduck.core.transfer.TransferOptions;
import ch.cyberduck.core.transfer.TransferSpeedometer;
import ch.cyberduck.core.transfer.UploadTransfer;
import ch.cyberduck.test.IntegrationTest;
import ch.cyberduck.test.VaultTest;

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

import static org.junit.Assert.*;

@Category(IntegrationTest.class)
public class B2SingleTransferWorkerTest extends VaultTest {

    @Test
    public void testUploadTransferWithFailure() throws Exception {
        final Local local = new Local(System.getProperty("java.io.tmpdir"), UUID.randomUUID().toString());
        final byte[] content = new byte[5242880 + 1];
        new Random().nextBytes(content);
        final OutputStream out = local.getOutputStream(false);
        IOUtils.write(content, out);
        out.close();
        final AtomicBoolean failed = new AtomicBoolean();
        final Host host = new Host(new B2Protocol()) {
            @Override
            public String getProperty(final String key) {
                if("connection.retry".equals(key)) {
                    return String.valueOf(1);
                }
                if("b2.upload.largeobject.threshold".equals(key)) {
                    return String.valueOf(0);
                }
                if("b2.upload.largeobject.size".equals(key)) {
                    return String.valueOf(5000000);
                }
                if("b2.upload.largeobject.concurrency".equals(key)) {
                    return String.valueOf(5);
                }
                if("queue.upload.checksum.calculate".equals(key)) {
                    return String.valueOf(true);
                }
                return super.getProperty(key);
            }
        };
        final B2Session session = new B2Session(host.withCredentials(new Credentials(
                PROPERTIES.get("b2.user"), PROPERTIES.get("b2.password")
        )), new DefaultX509TrustManager(), new DefaultX509KeyManager()) {
            final B2LargeUploadService upload = new B2LargeUploadService(this, new B2VersionIdProvider(this),
                    new B2WriteFeature(this, new B2VersionIdProvider(this))) {
                @Override
                protected InputStream decorate(final InputStream in, final MessageDigest digest) {
                    if(failed.get()) {
                        // Second attempt successful
                        return in;
                    }
                    return new CountingInputStream(in) {
                        @Override
                        protected void beforeRead(final int n) throws IOException {
                            super.beforeRead(n);
                            if(this.getByteCount() >= 5000000L) {
                                failed.set(true);
                                throw new SocketTimeoutException();
                            }
                        }
                    };
                }
            };

            @Override
            @SuppressWarnings("unchecked")
            public <T> T _getFeature(final Class<T> type) {
                if(type == Upload.class) {
                    return (T) upload;
                }
                return super._getFeature(type);
            }
        };
        new LoginConnectionService(new DisabledLoginCallback(),
                new DisabledHostKeyCallback(),
                new DisabledPasswordStore(),
                new DisabledProgressListener()).connect(session, new DisabledCancelCallback());
        final Path bucket = new Path("test-cyberduck", EnumSet.of(Path.Type.directory, Path.Type.volume));
        final Path test = new Path(bucket, UUID.randomUUID().toString(), EnumSet.of(Path.Type.file));
        final Transfer t = new UploadTransfer(host, test, local);
        final BytecountStreamListener counter = new BytecountStreamListener();
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
        final B2VersionIdProvider fileid = new B2VersionIdProvider(session);
        final PathAttributes attr = new B2AttributesFinderFeature(session, fileid).find(test);
        assertNotEquals(Checksum.NONE, attr.getChecksum());
        assertEquals(content.length, attr.getSize());
        assertEquals(content.length, counter.getRecv(), 0L);
        assertEquals(content.length, counter.getSent(), 0L);
        assertTrue(failed.get());
        new B2DeleteFeature(session, fileid).delete(Collections.singletonList(test), new DisabledLoginCallback(), new Delete.DisabledCallback());
    }
}
