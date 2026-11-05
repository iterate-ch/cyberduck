package ch.cyberduck.core.nextcloud;

/*
 * Copyright (c) 2002-2026 iterate GmbH. All rights reserved.
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
import ch.cyberduck.core.ConnectionCallback;
import ch.cyberduck.core.DisabledLoginCallback;
import ch.cyberduck.core.DisabledProgressListener;
import ch.cyberduck.core.Local;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.features.Delete;
import ch.cyberduck.core.io.DisabledStreamListener;
import ch.cyberduck.core.io.BandwidthThrottle;
import ch.cyberduck.core.ocs.OcsCapabilities;
import ch.cyberduck.core.preferences.PreferencesFactory;
import ch.cyberduck.core.shared.DefaultHomeFinderService;
import ch.cyberduck.core.transfer.TransferStatus;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.RandomUtils;
import org.junit.Test;

import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.util.Collections;
import java.util.EnumSet;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertTrue;

public class NextcloudThresholdUploadFeatureTest extends AbstractNextcloudTest {

    @Test
    public void testUploadChunks() throws Exception {
        PreferencesFactory.get().setProperty("nextcloud.upload.multipart.threshold", 1024L);
        PreferencesFactory.get().setProperty("nextcloud.upload.multipart.size", 5L * 1024 * 1024);
        try {
            final byte[] content = RandomUtils.nextBytes(12 * 1024 * 1024 + 17);
            final Local local = new Local(Files.createTempFile("chunked", ".bin").toString());
            try (OutputStream out = local.getOutputStream(false)) {
                out.write(content);
            }
            final Path test = new Path(new DefaultHomeFinderService(session).find(), new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file));
            final TransferStatus status = new TransferStatus().setLength(content.length);
            new NextcloudThresholdUploadFeature(session, new OcsCapabilities()).upload(new NextcloudWriteFeature(session), test, local, new BandwidthThrottle(BandwidthThrottle.UNLIMITED),
                    new DisabledProgressListener(), new DisabledStreamListener(), status, ConnectionCallback.noop);
            assertTrue(status.isComplete());
            final byte[] read = new byte[content.length];
            try (InputStream in = new NextcloudReadFeature(session).read(test, new TransferStatus().setLength(content.length), ConnectionCallback.noop)) {
                IOUtils.readFully(in, read);
            }
            assertArrayEquals(content, read);
            new NextcloudDeleteFeature(session).delete(Collections.singletonMap(test, new TransferStatus()), new DisabledLoginCallback(), new Delete.DisabledCallback());
            local.delete();
        }
        finally {
            PreferencesFactory.get().deleteProperty("nextcloud.upload.multipart.threshold");
            PreferencesFactory.get().deleteProperty("nextcloud.upload.multipart.size");
        }
    }
}
