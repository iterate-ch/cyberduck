package ch.cyberduck.core.smb;

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
import ch.cyberduck.core.DisabledListProgressListener;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.io.StreamCopier;
import ch.cyberduck.core.shared.DefaultHomeFinderService;
import ch.cyberduck.core.transfer.TransferStatus;
import ch.cyberduck.test.TestcontainerTest;

import org.apache.commons.lang3.RandomUtils;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.io.ByteArrayInputStream;
import java.io.OutputStream;
import java.util.EnumSet;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.Assert.*;

/**
 * Concurrent listing of and upload into the same directories on one share. With SMB3 directory leasing enabled in
 * smbj 0.15.0, listings fail with STATUS_FILE_CLOSED and cached listings go stale (MD-28532,
 * <a href="https://github.com/hierynomus/smbj/issues/914">smbj#914</a>).
 */
@Category(TestcontainerTest.class)
public class SMBConcurrentListUploadTest extends AbstractSMBTest {

    @Test
    public void testDirectoryLeasingDisabled() {
        assertFalse(session.getClient().getConnectionContext().supportsDirectoryLeasing());
    }

    /**
     * List a directory in a loop on one thread while uploading files into it on another thread
     */
    @Test
    public void testConcurrentListAndUpload() throws Exception {
        final Path home = new DefaultHomeFinderService(session).find();
        final Path directory = new SMBDirectoryFeature(session).mkdir(new SMBWriteFeature(session),
                new Path(home, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory)), new TransferStatus());
        final int files = 200;
        final AtomicBoolean done = new AtomicBoolean();
        final ExecutorService lister = Executors.newSingleThreadExecutor();
        try {
            final Future<Integer> listings = lister.submit(() -> {
                int count = 0;
                while(!done.get()) {
                    new SMBListService(session).list(directory, new DisabledListProgressListener());
                    count++;
                }
                return count;
            });
            for(int i = 0; i < files; i++) {
                upload(new Path(directory, String.format("f-%d", i), EnumSet.of(Path.Type.file)), 4096);
            }
            done.set(true);
            assertTrue(listings.get(60, TimeUnit.SECONDS) > 0);
        }
        finally {
            done.set(true);
            lister.shutdownNow();
        }
        assertEquals(files, new SMBListService(session).list(directory, new DisabledListProgressListener()).size());
    }

    private void upload(final Path file, final int length) throws Exception {
        final byte[] content = RandomUtils.nextBytes(length);
        final TransferStatus status = new TransferStatus().setLength(content.length);
        final OutputStream out = new SMBWriteFeature(session).write(file, status, ConnectionCallback.noop);
        new StreamCopier(status, status).transfer(new ByteArrayInputStream(content), out);
    }
}
