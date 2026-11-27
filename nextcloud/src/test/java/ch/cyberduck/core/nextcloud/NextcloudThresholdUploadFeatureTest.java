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
import ch.cyberduck.core.Local;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.ProgressListener;
import ch.cyberduck.core.features.Delete;
import ch.cyberduck.core.io.BandwidthThrottle;
import ch.cyberduck.core.io.DisabledStreamListener;
import ch.cyberduck.core.ocs.OcsCapabilities;
import ch.cyberduck.core.shared.DefaultHomeFinderService;
import ch.cyberduck.core.transfer.TransferStatus;

import org.apache.commons.lang3.RandomUtils;
import org.junit.Test;

import java.io.OutputStream;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Chunked upload is the only one reporting progress for assembling the file on the server.
 */
public class NextcloudThresholdUploadFeatureTest extends AbstractNextcloudTest {

    private static final long THRESHOLD = 4096L;

    @Test
    public void testBelowThresholdSingleRequest() throws Exception {
        assertFalse(this.upload(THRESHOLD - 1));
    }

    @Test
    public void testAtThresholdChunked() throws Exception {
        assertTrue(this.upload(THRESHOLD));
    }

    @Test
    public void testAboveThresholdChunked() throws Exception {
        assertTrue(this.upload(THRESHOLD * 3 + 1));
    }

    /**
     * @return True if chunked upload was used
     */
    private boolean upload(final long length) throws Exception {
        final byte[] content = RandomUtils.nextBytes((int) length);
        final Local local = new Local(Files.createTempFile("threshold", ".bin").toString());
        try (OutputStream out = local.getOutputStream(false)) {
            out.write(content);
        }
        final Path test = new Path(new DefaultHomeFinderService(session).find(), new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file));
        final TransferStatus status = new TransferStatus().setLength(content.length);
        final List<String> messages = new ArrayList<>();
        new NextcloudThresholdUploadFeature(session, new OcsCapabilities(), THRESHOLD, THRESHOLD).upload(new NextcloudWriteFeature(session), test, local,
                new BandwidthThrottle(BandwidthThrottle.UNLIMITED), new ProgressListener() {
                    @Override
                    public void message(final String message) {
                        messages.add(message);
                    }
                }, new DisabledStreamListener(), status, ConnectionCallback.noop);
        assertTrue(status.isComplete());
        assertEquals(length, new NextcloudAttributesFinderFeature(session).find(test).getSize());
        new NextcloudDeleteFeature(session).delete(Collections.singletonMap(test, new TransferStatus()), new DisabledLoginCallback(), new Delete.DisabledCallback());
        local.delete();
        return messages.stream().anyMatch(m -> m.contains("Finalize"));
    }
}
