package ch.cyberduck.core.spectra;

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

import ch.cyberduck.core.AlphanumericRandomStringService;
import ch.cyberduck.core.ConnectionCallback;
import ch.cyberduck.core.Local;
import ch.cyberduck.core.LoginCallback;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.ProgressListener;
import ch.cyberduck.core.features.Delete;
import ch.cyberduck.core.io.BandwidthThrottle;
import ch.cyberduck.core.io.StreamListener;
import ch.cyberduck.core.transfer.Transfer;
import ch.cyberduck.core.transfer.TransferItem;
import ch.cyberduck.core.transfer.TransferStatus;
import ch.cyberduck.test.IntegrationTest;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.RandomUtils;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;

import static org.junit.Assert.assertArrayEquals;

@Category(IntegrationTest.class)
public class SpectraUploadFeatureTest extends AbstractSpectraTest {

    @Test
    public void testUpload() throws Exception {
        final Local local = new Local(System.getProperty("java.io.tmpdir"), new AlphanumericRandomStringService().random());
        final int length = 32770;
        final byte[] content = RandomUtils.nextBytes(length);
        final OutputStream out = local.getOutputStream(false);
        IOUtils.write(content, out);
        out.close();
        final Path container = new SpectraDirectoryFeature(session).mkdir(
                new SpectraWriteFeature(session), new Path(new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory, Path.Type.volume)), new TransferStatus());
        final Path test = new Path(container, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file));
        final TransferStatus writeStatus = new TransferStatus().setLength(content.length);
        final SpectraBulkService bulk = new SpectraBulkService(session);
        bulk.pre(Transfer.Type.upload, Collections.singletonMap(new TransferItem(test), writeStatus), ConnectionCallback.noop);
        final SpectraUploadFeature upload = new SpectraUploadFeature(new SpectraBulkService(session));
        upload.upload(new SpectraWriteFeature(session), test, local, new BandwidthThrottle(BandwidthThrottle.UNLIMITED), ProgressListener.noop, StreamListener.noop,
                writeStatus, ConnectionCallback.noop);
        final byte[] buffer = new byte[content.length];
        final TransferStatus readStatus = new TransferStatus().setLength(content.length);
        bulk.pre(Transfer.Type.download, Collections.singletonMap(new TransferItem(test), readStatus), ConnectionCallback.noop);
        final InputStream in = new SpectraReadFeature(session).read(test, readStatus, ConnectionCallback.noop);
        IOUtils.readFully(in, buffer);
        in.close();
        assertArrayEquals(content, buffer);
        new SpectraDeleteFeature(session).delete(Collections.singletonList(container), LoginCallback.noop, new Delete.DisabledCallback());
        local.delete();
    }

    @Test
    public void testUploadMultipleFiles() throws Exception {
        final Local local1 = new Local(System.getProperty("java.io.tmpdir"), new AlphanumericRandomStringService().random());
        final TransferStatus status1;
        {
            final int length = 32770;
            final byte[] content = RandomUtils.nextBytes(length);
            final OutputStream out = local1.getOutputStream(false);
            IOUtils.write(content, out);
            out.close();
            status1 = new TransferStatus().setLength(content.length);
        }
        final Local local2 = new Local(System.getProperty("java.io.tmpdir"), new AlphanumericRandomStringService().random());
        final TransferStatus status2;
        {
            final int length = 32770;
            final byte[] content = RandomUtils.nextBytes(length);
            final OutputStream out = local2.getOutputStream(false);
            IOUtils.write(content, out);
            out.close();
            status2 = new TransferStatus().setLength(content.length);
        }
        final Path container = new SpectraDirectoryFeature(session).mkdir(
                new SpectraWriteFeature(session), new Path(new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory, Path.Type.volume)), new TransferStatus());
        final Path test1 = new Path(container, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file));
        final Path test2 = new Path(container, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file));
        final SpectraBulkService bulk = new SpectraBulkService(session);
        final HashMap<TransferItem, TransferStatus> files = new HashMap<>();
        files.put(new TransferItem(test1), status1);
        files.put(new TransferItem(test2), status2);
        bulk.pre(Transfer.Type.upload, files, ConnectionCallback.noop);
        final SpectraUploadFeature upload = new SpectraUploadFeature(new SpectraBulkService(session));
        upload.upload(new SpectraWriteFeature(session), test1, local1, new BandwidthThrottle(BandwidthThrottle.UNLIMITED), ProgressListener.noop, StreamListener.noop,
                status1, ConnectionCallback.noop);
        upload.upload(new SpectraWriteFeature(session), test2, local2, new BandwidthThrottle(BandwidthThrottle.UNLIMITED), ProgressListener.noop, StreamListener.noop,
                status2, ConnectionCallback.noop);
        new SpectraDeleteFeature(session).delete(Arrays.asList(test1, test2), LoginCallback.noop, new Delete.DisabledCallback());
        local1.delete();
        local2.delete();
        new SpectraDeleteFeature(session).delete(Collections.<Path>singletonList(container), LoginCallback.noop, new Delete.DisabledCallback());
    }
}
