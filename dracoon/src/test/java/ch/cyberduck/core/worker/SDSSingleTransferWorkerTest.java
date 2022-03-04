package ch.cyberduck.core.worker;

/*
 * Copyright (c) 2002-2018 iterate GmbH. All rights reserved.
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
import ch.cyberduck.core.Credentials;
import ch.cyberduck.core.DisabledCancelCallback;
import ch.cyberduck.core.DisabledConnectionCallback;
import ch.cyberduck.core.DisabledHostKeyCallback;
import ch.cyberduck.core.DisabledLoginCallback;
import ch.cyberduck.core.DisabledProgressListener;
import ch.cyberduck.core.Host;
import ch.cyberduck.core.Local;
import ch.cyberduck.core.NullFilter;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.TestProtocol;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.features.Delete;
import ch.cyberduck.core.features.Upload;
import ch.cyberduck.core.http.HttpResponseOutputStream;
import ch.cyberduck.core.io.DisabledStreamListener;
import ch.cyberduck.core.io.SHA256ChecksumCompute;
import ch.cyberduck.core.io.StatusOutputStream;
import ch.cyberduck.core.io.StreamCopier;
import ch.cyberduck.core.local.DefaultLocalDirectoryFeature;
import ch.cyberduck.core.local.DefaultTemporaryFileService;
import ch.cyberduck.core.notification.DisabledNotificationService;
import ch.cyberduck.core.proxy.Proxy;
import ch.cyberduck.core.sds.AbstractSDSTest;
import ch.cyberduck.core.sds.SDSAttributesFinderFeature;
import ch.cyberduck.core.sds.SDSDeleteFeature;
import ch.cyberduck.core.sds.SDSDirectoryFeature;
import ch.cyberduck.core.sds.SDSFindFeature;
import ch.cyberduck.core.sds.SDSMultipartWriteFeature;
import ch.cyberduck.core.sds.SDSNodeIdProvider;
import ch.cyberduck.core.sds.SDSSession;
import ch.cyberduck.core.sds.io.swagger.client.model.Node;
import ch.cyberduck.core.shared.DefaultAttributesFinderFeature;
import ch.cyberduck.core.shared.DefaultUploadFeature;
import ch.cyberduck.core.ssl.DefaultX509KeyManager;
import ch.cyberduck.core.ssl.DisabledX509TrustManager;
import ch.cyberduck.core.transfer.DisabledTransferErrorCallback;
import ch.cyberduck.core.transfer.DisabledTransferPrompt;
import ch.cyberduck.core.transfer.DownloadTransfer;
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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.SocketTimeoutException;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.Assert.*;

@Category(IntegrationTest.class)
public class SDSSingleTransferWorkerTest extends AbstractSDSTest {

    @Test
    public void testDownloadVersioned() throws Exception {
        final SDSNodeIdProvider fileid = new SDSNodeIdProvider(session);
        final Path room = new SDSDirectoryFeature(session, fileid).mkdir(new Path(
            new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory, Path.Type.volume)), new TransferStatus());
        final Path test = new Path(room, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file));
        final Local localFile = new DefaultTemporaryFileService().create(test.getName());
        {
            final byte[] content = RandomUtils.nextBytes(39864);
            final TransferStatus writeStatus = new TransferStatus().withLength(content.length).withChecksum(new SHA256ChecksumCompute().compute(new ByteArrayInputStream(content), new TransferStatus()));
            final StatusOutputStream<Node> out = new SDSMultipartWriteFeature(session, fileid).write(test, writeStatus, new DisabledConnectionCallback());
            assertNotNull(out);
            new StreamCopier(writeStatus, writeStatus).withLimit((long) content.length).transfer(new ByteArrayInputStream(content), out);
            out.close();
        }
        final byte[] content = RandomUtils.nextBytes(39864);
        final TransferStatus writeStatus = new TransferStatus().exists(true).withLength(content.length).withChecksum(new SHA256ChecksumCompute().compute(new ByteArrayInputStream(content), new TransferStatus()));
        final StatusOutputStream<Node> out = new SDSMultipartWriteFeature(session, fileid).write(test, writeStatus, new DisabledConnectionCallback());
        assertNotNull(out);
        new StreamCopier(writeStatus, writeStatus).withLimit((long) content.length).transfer(new ByteArrayInputStream(content), out);
        out.close();
        final String versionId = test.attributes().getVersionId();
        assertEquals(versionId, new SDSAttributesFinderFeature(session, fileid).find(test).getVersionId());
        assertEquals(versionId, new DefaultAttributesFinderFeature(session).find(test).getVersionId());
        final Transfer t = new DownloadTransfer(new Host(new TestProtocol()), Collections.singletonList(new TransferItem(test, localFile)), new NullFilter<>());
        assertTrue(new SingleTransferWorker(session, session, t, new TransferOptions(), new TransferSpeedometer(t), new DisabledTransferPrompt() {
            @Override
            public TransferAction prompt(final TransferItem file) {
                return TransferAction.overwrite;
            }
        }, new DisabledTransferErrorCallback(),
            new DisabledProgressListener(), new DisabledStreamListener(), new DisabledLoginCallback(), new DisabledNotificationService()) {

        }.run(session));
        byte[] compare = new byte[content.length];
        assertArrayEquals(content, IOUtils.toByteArray(localFile.getInputStream()));
        test.attributes().setVersionId(versionId);
        assertEquals(versionId, new DefaultAttributesFinderFeature(session).find(test).getVersionId());
        new SDSDeleteFeature(session, fileid).delete(Arrays.asList(test, room), new DisabledLoginCallback(), new Delete.DisabledCallback());
        localFile.delete();
        session.close();
    }

    @Test
    public void testTransferredSizeRepeat() throws Exception {
        final SDSNodeIdProvider fileid = new SDSNodeIdProvider(session);
        final Local local = new Local(System.getProperty("java.io.tmpdir"), UUID.randomUUID().toString());
        final byte[] content = new byte[98305];  // chunk size 32768
        new Random().nextBytes(content);
        final OutputStream out = local.getOutputStream(false);
        IOUtils.write(content, out);
        out.close();
        final AtomicBoolean failed = new AtomicBoolean();
        final BytecountStreamListener counter = new BytecountStreamListener();
        final SDSSession conn = new SDSSession(session.getHost().withCredentials(
            new Credentials(System.getProperties().getProperty("sds.user"), System.getProperties().getProperty("sds.key"))
        ), new DisabledX509TrustManager(), new DefaultX509KeyManager()) {
            @Override
            @SuppressWarnings("unchecked")
            public <T> T _getFeature(final Class<T> type) {
                if(type == Upload.class) {
                    return (T) new DefaultUploadFeature(
                        new SDSMultipartWriteFeature(this, fileid) {
                            @Override
                            public HttpResponseOutputStream<Node> write(final Path file, final TransferStatus status, final ConnectionCallback callback) throws BackgroundException {
                                final HttpResponseOutputStream<Node> proxy = super.write(file, status, callback);
                                if(failed.get()) {
                                    // Second attempt successful
                                    return proxy;
                                }
                                return new HttpResponseOutputStream<Node>(new CountingOutputStream(proxy) {
                                    @Override
                                    protected void afterWrite(final int n) throws IOException {
                                        super.afterWrite(n);
                                        if(this.getByteCount() >= 42768L) {
                                            // Buffer size
                                            assertEquals(32768L, counter.getSent());
                                            failed.set(true);
                                            throw new SocketTimeoutException();
                                        }
                                    }
                                }) {
                                    @Override
                                    public Node getStatus() throws BackgroundException {
                                        return proxy.getStatus();
                                    }
                                };
                            }
                        }
                    );
                }
                return super._getFeature(type);
            }
        };
        conn.open(Proxy.DIRECT, new DisabledHostKeyCallback(), new DisabledLoginCallback(), new DisabledCancelCallback());
        conn.login(Proxy.DIRECT, new DisabledLoginCallback(), new DisabledCancelCallback());
        final Path room = new SDSDirectoryFeature(conn, fileid).mkdir(new Path(
            new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory, Path.Type.volume)), new TransferStatus());
        final Path test = new Path(room, UUID.randomUUID().toString(), EnumSet.of(Path.Type.file));
        final Transfer t = new UploadTransfer(session.getHost(), test, local);
        assertTrue(new SingleTransferWorker(conn, conn, t, new TransferOptions(), new TransferSpeedometer(t), new DisabledTransferPrompt() {
            @Override
            public TransferAction prompt(final TransferItem file) {
                return TransferAction.overwrite;
            }
        }, new DisabledTransferErrorCallback(),
            new DisabledProgressListener(), counter, new DisabledLoginCallback(), new DisabledNotificationService()) {

        }.run(session));
        local.delete();
        assertTrue(t.isComplete());
        assertEquals(content.length, new SDSAttributesFinderFeature(conn, fileid).find(test).getSize());
        assertEquals(content.length, counter.getRecv(), 0L);
        assertEquals(content.length, counter.getSent(), 0L);
        assertTrue(failed.get());
        new SDSDeleteFeature(conn, fileid).delete(Arrays.asList(test, room), new DisabledLoginCallback(), new Delete.DisabledCallback());
    }

    @Test
    public void testTransferredSizeRepeatFailureOnClose() throws Exception {
        final SDSNodeIdProvider fileid = new SDSNodeIdProvider(session);
        final Local local = new Local(System.getProperty("java.io.tmpdir"), UUID.randomUUID().toString());
        final byte[] content = new byte[98305];  // chunk size 32768
        new Random().nextBytes(content);
        final OutputStream out = local.getOutputStream(false);
        IOUtils.write(content, out);
        out.close();
        final AtomicBoolean failed = new AtomicBoolean();
        final SDSSession conn = new SDSSession(session.getHost().withCredentials(
            new Credentials(System.getProperties().getProperty("sds.user"), System.getProperties().getProperty("sds.key"))
        ), new DisabledX509TrustManager(), new DefaultX509KeyManager()) {
            @Override
            @SuppressWarnings("unchecked")
            public <T> T _getFeature(final Class<T> type) {
                if(type == Upload.class) {
                    return (T) new DefaultUploadFeature(
                        new SDSMultipartWriteFeature(this, fileid) {
                            @Override
                            public HttpResponseOutputStream<Node> write(final Path file, final TransferStatus status, final ConnectionCallback callback) throws BackgroundException {
                                final HttpResponseOutputStream<Node> proxy = super.write(file, status, callback);
                                if(failed.get()) {
                                    // Second attempt successful
                                    return proxy;
                                }
                                return new HttpResponseOutputStream<Node>(new CountingOutputStream(proxy) {
                                    @Override
                                    public void close() throws IOException {
                                        if(!failed.get()) {
                                            failed.set(true);
                                            throw new SocketTimeoutException();
                                        }
                                        super.close();
                                    }
                                }) {
                                    @Override
                                    public Node getStatus() throws BackgroundException {
                                        return proxy.getStatus();
                                    }
                                };
                            }
                        }
                    );
                }
                return super._getFeature(type);
            }
        };
        conn.open(Proxy.DIRECT, new DisabledHostKeyCallback(), new DisabledLoginCallback(), new DisabledCancelCallback());
        conn.login(Proxy.DIRECT, new DisabledLoginCallback(), new DisabledCancelCallback());
        final Path room = new SDSDirectoryFeature(conn, fileid).mkdir(new Path(
            new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory, Path.Type.volume)), new TransferStatus());
        final Path test = new Path(room, UUID.randomUUID().toString(), EnumSet.of(Path.Type.file));
        final Transfer t = new UploadTransfer(session.getHost(), test, local);
        final BytecountStreamListener counter = new BytecountStreamListener();
        assertTrue(new SingleTransferWorker(conn, conn, t, new TransferOptions(), new TransferSpeedometer(t), new DisabledTransferPrompt() {
            @Override
            public TransferAction prompt(final TransferItem file) {
                return TransferAction.overwrite;
            }
        }, new DisabledTransferErrorCallback(),
                new DisabledProgressListener(), counter, new DisabledLoginCallback(), new DisabledNotificationService()) {

        }.run(conn));
        local.delete();
        assertTrue(t.isComplete());
        assertEquals(98305L, counter.getSent(), 0L);
        assertTrue(failed.get());
        assertEquals(98305L, new SDSAttributesFinderFeature(conn, fileid).find(test).getSize());
        new SDSDeleteFeature(conn, fileid).delete(Arrays.asList(test, room), new DisabledLoginCallback(), new Delete.DisabledCallback());
    }

    @Test
    public void testTransferExistingFolder() throws Exception {
        final SDSNodeIdProvider fileid = new SDSNodeIdProvider(session);
        final Local folder = new Local(System.getProperty("java.io.tmpdir"), UUID.randomUUID().toString());
        new DefaultLocalDirectoryFeature().mkdir(folder);
        final Path room = new SDSDirectoryFeature(session, fileid).mkdir(new Path(
                new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory, Path.Type.volume)), new TransferStatus());
        final Transfer t = new UploadTransfer(session.getHost(), room, folder);
        final BytecountStreamListener counter = new BytecountStreamListener();
        assertTrue(new SingleTransferWorker(session, session, t, new TransferOptions(), new TransferSpeedometer(t), new DisabledTransferPrompt() {
            @Override
            public TransferAction prompt(final TransferItem file) {
                return TransferAction.overwrite;
            }
        }, new DisabledTransferErrorCallback(),
                new DisabledProgressListener(), counter, new DisabledLoginCallback(), new DisabledNotificationService()) {

        }.run(session));
        assertTrue(t.isComplete());
        assertTrue(new SDSFindFeature(session, fileid).find(room));
        new SDSDeleteFeature(session, fileid).delete(Collections.singletonList(room), new DisabledLoginCallback(), new Delete.DisabledCallback());
    }
}
