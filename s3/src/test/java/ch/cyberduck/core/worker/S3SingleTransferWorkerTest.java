package ch.cyberduck.core.worker;

/*
 * Copyright (c) 2002-2017 iterate GmbH. All rights reserved.
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

import ch.cyberduck.core.*;
import ch.cyberduck.core.features.Delete;
import ch.cyberduck.core.features.Upload;
import ch.cyberduck.core.io.DisabledStreamListener;
import ch.cyberduck.core.io.SHA256ChecksumCompute;
import ch.cyberduck.core.io.StatusOutputStream;
import ch.cyberduck.core.io.StreamCopier;
import ch.cyberduck.core.local.DefaultTemporaryFileService;
import ch.cyberduck.core.notification.DisabledNotificationService;
import ch.cyberduck.core.s3.AbstractS3Test;
import ch.cyberduck.core.s3.S3AccessControlListFeature;
import ch.cyberduck.core.s3.S3AttributesFinderFeature;
import ch.cyberduck.core.s3.S3DefaultDeleteFeature;
import ch.cyberduck.core.s3.S3MultipartUploadService;
import ch.cyberduck.core.s3.S3Protocol;
import ch.cyberduck.core.s3.S3Session;
import ch.cyberduck.core.s3.S3SingleUploadService;
import ch.cyberduck.core.s3.S3WriteFeature;
import ch.cyberduck.core.serializer.impl.dd.ProfilePlistReader;
import ch.cyberduck.core.shared.DefaultAttributesFinderFeature;
import ch.cyberduck.core.ssl.DefaultX509KeyManager;
import ch.cyberduck.core.ssl.DefaultX509TrustManager;
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
import org.apache.commons.io.input.CountingInputStream;
import org.apache.commons.lang3.RandomUtils;
import org.jets3t.service.model.StorageObject;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.SocketTimeoutException;
import java.security.MessageDigest;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.Random;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.Assert.*;

@Category(IntegrationTest.class)
public class S3SingleTransferWorkerTest extends AbstractS3Test {

    @Test
    public void testDownloadVersioned() throws Exception {
        final Path home = new Path("versioning-test-eu-central-1-cyberduck", EnumSet.of(Path.Type.volume, Path.Type.directory));
        final Path test = new Path(home, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file));
        final Local localFile = new DefaultTemporaryFileService().create(test.getName());
        {
            final byte[] content = RandomUtils.nextBytes(39864);
            final TransferStatus writeStatus = new TransferStatus().withLength(content.length).withChecksum(new SHA256ChecksumCompute().compute(new ByteArrayInputStream(content), new TransferStatus()));
            final StatusOutputStream<StorageObject> out = new S3WriteFeature(session, new S3AccessControlListFeature(session)).write(test, writeStatus, new DisabledConnectionCallback());
            assertNotNull(out);
            new StreamCopier(writeStatus, writeStatus).withLimit((long) content.length).transfer(new ByteArrayInputStream(content), out);
            out.close();
        }
        final byte[] content = RandomUtils.nextBytes(39864);
        final TransferStatus writeStatus = new TransferStatus().withLength(content.length).withChecksum(new SHA256ChecksumCompute().compute(new ByteArrayInputStream(content), new TransferStatus()));
        final StatusOutputStream<StorageObject> out = new S3WriteFeature(session, new S3AccessControlListFeature(session)).write(test, writeStatus, new DisabledConnectionCallback());
        assertNotNull(out);
        new StreamCopier(writeStatus, writeStatus).withLimit((long) content.length).transfer(new ByteArrayInputStream(content), out);
        out.close();
        assertEquals(writeStatus.getResponse().getVersionId(), new S3AttributesFinderFeature(session, new S3AccessControlListFeature(session)).find(test).getVersionId());
        assertEquals(writeStatus.getResponse().getVersionId(), new DefaultAttributesFinderFeature(session).find(test).getVersionId());
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
        new S3DefaultDeleteFeature(session).delete(Collections.singletonList(test), new DisabledLoginCallback(), new Delete.DisabledCallback());
        localFile.delete();
    }

    @Test
    public void testUploadMultipartTransferWithFailure() throws Exception {
        final Local local = new Local(System.getProperty("java.io.tmpdir"), new AlphanumericRandomStringService().random());
        final byte[] content = new byte[6 * 1024 * 1024]; // Minimum multipart upload size
        new Random().nextBytes(content);
        final OutputStream out = local.getOutputStream(false);
        IOUtils.write(content, out);
        out.close();
        final ProtocolFactory factory = new ProtocolFactory(new HashSet<>(Collections.singleton(new S3Protocol())));
        final Profile profile = new ProfilePlistReader(factory).read(
                this.getClass().getResourceAsStream("/S3 (HTTPS).cyberduckprofile"));
        final Host host = new Host(profile, profile.getDefaultHostname(), new Credentials(
                PROPERTIES.get("s3.key"), PROPERTIES.get("s3.secret")
        )) {
            @Override
            public String getProperty(final String key) {
                if("s3.upload.multipart".equals(key)) {
                    return String.valueOf(true);
                }
                if("s3.upload.multipart.partsize.minimum".equals(key)) {
                    return String.valueOf(content.length);
                }
                if("s3.upload.multipart.threshold".equals(key)) {
                    return String.valueOf(content.length);
                }
                return super.getProperty(key);
            }
        };
        final AtomicBoolean failed = new AtomicBoolean();
        final S3Session session = new S3Session(host, new DefaultX509TrustManager(), new DefaultX509KeyManager()) {
            @Override
            @SuppressWarnings("unchecked")
            public <T> T _getFeature(final Class<T> type) {
                if(type == Upload.class) {
                    return (T) new S3MultipartUploadService(this, new S3WriteFeature(this, new S3AccessControlListFeature(this)), new S3AccessControlListFeature(this), 5 * 1024L * 1024L, 5) {
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
                                    if(this.getByteCount() >= 1024L * 1024L) {
                                        failed.set(true);
                                        throw new SocketTimeoutException();
                                    }
                                }
                            };
                        }
                    };
                }
                return super._getFeature(type);
            }
        };
        new LoginConnectionService(new DisabledLoginCallback(),
                new DisabledHostKeyCallback(),
                new DisabledPasswordStore(),
                new DisabledProgressListener()).connect(session, new DisabledCancelCallback());
        final Path home = new Path("test-eu-central-1-cyberduck", EnumSet.of(Path.Type.volume, Path.Type.directory));
        final Path test = new Path(home, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file));
        final Transfer t = new UploadTransfer(session.getHost(), test, local);
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
        assertEquals(content.length, new S3AttributesFinderFeature(session, new S3AccessControlListFeature(session)).find(test).getSize());
        assertEquals(content.length, counter.getRecv(), 0L);
        assertEquals(content.length, counter.getSent(), 0L);
        assertTrue(failed.get());
        new S3DefaultDeleteFeature(session).delete(Collections.singletonList(test), new DisabledLoginCallback(), new Delete.DisabledCallback());
    }

    @Test
    public void testUploadTransferWithFailure() throws Exception {
        final Local local = new Local(System.getProperty("java.io.tmpdir"), new AlphanumericRandomStringService().random());
        final byte[] content = new byte[6144];
        new Random().nextBytes(content);
        final OutputStream out = local.getOutputStream(false);
        IOUtils.write(content, out);
        out.close();
        final ProtocolFactory factory = new ProtocolFactory(new HashSet<>(Collections.singleton(new S3Protocol())));
        final Profile profile = new ProfilePlistReader(factory).read(
                this.getClass().getResourceAsStream("/S3 (HTTPS).cyberduckprofile"));
        final Host host = new Host(profile, profile.getDefaultHostname(), new Credentials(
                PROPERTIES.get("s3.key"), PROPERTIES.get("s3.secret")
        )) {
            @Override
            public String getProperty(final String key) {
                if("s3.upload.multipart".equals(key)) {
                    return String.valueOf(false);
                }
                return super.getProperty(key);
            }
        };
        final AtomicBoolean failed = new AtomicBoolean();
        final S3Session session = new S3Session(host, new DefaultX509TrustManager(), new DefaultX509KeyManager()) {
            @Override
            @SuppressWarnings("unchecked")
            public <T> T _getFeature(final Class<T> type) {
                if(type == Upload.class) {
                    return (T) new S3SingleUploadService(this, new S3WriteFeature(this, new S3AccessControlListFeature(this))) {
                        @Override
                        protected InputStream decorate(final InputStream in, final MessageDigest digest) {
                            if(failed.get()) {
                                // Second attempt successful
                                return in;
                            }
                            return new CountingInputStream(in) {
                                @Override
                                protected void beforeRead(final int n) throws IOException {
                                    failed.set(true);
                                    throw new SocketTimeoutException();
                                }
                            };
                        }
                    };
                }
                return super._getFeature(type);
            }
        };
        new LoginConnectionService(new DisabledLoginCallback(),
                new DisabledHostKeyCallback(),
                new DisabledPasswordStore(),
                new DisabledProgressListener()).connect(session, new DisabledCancelCallback());
        final Path home = new Path("test-eu-central-1-cyberduck", EnumSet.of(Path.Type.volume, Path.Type.directory));
        final Path test = new Path(home, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file));
        final Transfer t = new UploadTransfer(session.getHost(), test, local);
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
        assertEquals(content.length, new S3AttributesFinderFeature(session, new S3AccessControlListFeature(session)).find(test).getSize());
        assertEquals(content.length, counter.getRecv(), 0L);
        assertEquals(content.length, counter.getSent(), 0L);
        assertTrue(failed.get());
        new S3DefaultDeleteFeature(session).delete(Collections.singletonList(test), new DisabledLoginCallback(), new Delete.DisabledCallback());
    }
}
