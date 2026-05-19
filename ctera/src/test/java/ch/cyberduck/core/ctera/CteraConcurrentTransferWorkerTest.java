package ch.cyberduck.core.ctera;

/*
 * Copyright (c) 2002-2025 iterate GmbH. All rights reserved.
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
import ch.cyberduck.core.Host;
import ch.cyberduck.core.Local;
import ch.cyberduck.core.LoginCallback;
import ch.cyberduck.core.NullFilter;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.ProgressListener;
import ch.cyberduck.core.TestProtocol;
import ch.cyberduck.core.dav.DAVAttributesFinderFeature;
import ch.cyberduck.core.dav.DAVUploadFeature;
import ch.cyberduck.core.features.Delete;
import ch.cyberduck.core.io.BandwidthThrottle;
import ch.cyberduck.core.io.StreamListener;
import ch.cyberduck.core.local.DefaultTemporaryFileService;
import ch.cyberduck.core.notification.DisabledNotificationService;
import ch.cyberduck.core.pool.SessionPool;
import ch.cyberduck.core.shared.DefaultHomeFinderService;
import ch.cyberduck.core.transfer.DisabledTransferErrorCallback;
import ch.cyberduck.core.transfer.DisabledTransferPrompt;
import ch.cyberduck.core.transfer.DownloadTransfer;
import ch.cyberduck.core.transfer.Transfer;
import ch.cyberduck.core.transfer.TransferAction;
import ch.cyberduck.core.transfer.TransferItem;
import ch.cyberduck.core.transfer.TransferOptions;
import ch.cyberduck.core.transfer.TransferSpeedometer;
import ch.cyberduck.core.transfer.TransferStatus;
import ch.cyberduck.core.worker.ConcurrentTransferWorker;
import ch.cyberduck.test.IntegrationTest;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.RandomUtils;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.io.OutputStream;
import java.util.Collections;
import java.util.EnumSet;

import static org.junit.Assert.*;

@Category(IntegrationTest.class)
public class CteraConcurrentTransferWorkerTest extends AbstractCteraTest {

    @Test
    public void testBelowSegmentSizeUpAndDownload() throws Exception {
        final Path test = new Path(new DefaultHomeFinderService(session).find(), new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file));
        final Local local = new Local(System.getProperty("java.io.tmpdir"), new AlphanumericRandomStringService().random());
        final byte[] content = RandomUtils.nextBytes(500); // Below segment size
        final OutputStream out = local.getOutputStream(false);
        assertNotNull(out);
        IOUtils.write(content, out);
        out.close();
        new DAVUploadFeature(session).upload(new CteraWriteFeature(session), test, local, new BandwidthThrottle(BandwidthThrottle.UNLIMITED), ProgressListener.noop, StreamListener.noop, new TransferStatus().setLength(content.length), ConnectionCallback.noop);
        assertEquals(content.length, new DAVAttributesFinderFeature(session).find(test).getSize());
        final Local localFile = new DefaultTemporaryFileService().create(test.getName());
        final Transfer download = new DownloadTransfer(new Host(new TestProtocol()), Collections.singletonList(new TransferItem(test, localFile)), new NullFilter<>());
        final SessionPool pool = new SessionPool.SingleSessionPool(session);
        final BytecountStreamListener bytecount = new BytecountStreamListener();
        assertTrue(new ConcurrentTransferWorker(pool, pool, download, new TransferOptions(), new TransferSpeedometer(download), new DisabledTransferPrompt() {
            @Override
            public TransferAction prompt(final TransferItem file) {
                return TransferAction.overwrite;
            }
        }, new DisabledTransferErrorCallback(), ConnectionCallback.noop, ProgressListener.noop, bytecount, new DisabledNotificationService()).run(session));
        assertArrayEquals(content, IOUtils.toByteArray(localFile.getInputStream()));
        assertEquals(content.length, bytecount.getRecv());
        new CteraDeleteFeature(session).delete(Collections.singletonList(test), LoginCallback.noop, new Delete.DisabledCallback());
        local.delete();
        localFile.delete();
    }

    @Test
    public void testLargeUpAndDownload() throws Exception {
        final Path test = new Path(new DefaultHomeFinderService(session).find(), new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file));
        final Local local = new Local(System.getProperty("java.io.tmpdir"), new AlphanumericRandomStringService().random());
        final byte[] content = RandomUtils.nextBytes(104857600); // 100MB
        final OutputStream out = local.getOutputStream(false);
        assertNotNull(out);
        IOUtils.write(content, out);
        out.close();
        new DAVUploadFeature(session).upload(new CteraWriteFeature(session), test, local, new BandwidthThrottle(BandwidthThrottle.UNLIMITED), ProgressListener.noop, StreamListener.noop, new TransferStatus().setLength(content.length), ConnectionCallback.noop);
        assertEquals(content.length, new DAVAttributesFinderFeature(session).find(test).getSize());
        final Local localFile = new DefaultTemporaryFileService().create(test.getName());
        final Transfer download = new DownloadTransfer(new Host(new TestProtocol()), Collections.singletonList(new TransferItem(test, localFile)), new NullFilter<>());
        final SessionPool pool = new SessionPool.SingleSessionPool(session);
        final BytecountStreamListener bytecount = new BytecountStreamListener();
        assertTrue(new ConcurrentTransferWorker(pool, pool, download, new TransferOptions(), new TransferSpeedometer(download), new DisabledTransferPrompt() {
            @Override
            public TransferAction prompt(final TransferItem file) {
                return TransferAction.overwrite;
            }
        }, new DisabledTransferErrorCallback(), ConnectionCallback.noop, ProgressListener.noop, bytecount, new DisabledNotificationService()).run(session));
        assertArrayEquals(content, IOUtils.toByteArray(localFile.getInputStream()));
        assertEquals(content.length, bytecount.getRecv());
        new CteraDeleteFeature(session).delete(Collections.singletonList(test), LoginCallback.noop, new Delete.DisabledCallback());
        local.delete();
        localFile.delete();
    }
}
