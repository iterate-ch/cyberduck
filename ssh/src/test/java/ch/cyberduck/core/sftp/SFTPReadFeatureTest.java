package ch.cyberduck.core.sftp;

/*
 * Copyright (c) 2013 David Kocher. All rights reserved.
 * http://cyberduck.ch/
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
 *
 * Bug fixes, suggestions and comments should be sent to:
 * feedback@cyberduck.ch
 */

import ch.cyberduck.core.DisabledConnectionCallback;
import ch.cyberduck.core.DisabledLoginCallback;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.exception.NotfoundException;
import ch.cyberduck.core.features.Delete;
import ch.cyberduck.core.io.StreamCopier;
import ch.cyberduck.core.transfer.TransferStatus;
import ch.cyberduck.test.IntegrationTest;

import org.apache.commons.lang3.RandomUtils;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Collections;
import java.util.EnumSet;
import java.util.UUID;

import static org.junit.Assert.*;

@Category(IntegrationTest.class)
public class SFTPReadFeatureTest extends AbstractSFTPTest {

    @Test(expected = NotfoundException.class)
    public void testReadNotFound() throws Exception {
        final TransferStatus status = new TransferStatus();
        new SFTPReadFeature(session).read(new Path(new SFTPHomeDirectoryService(session).find(), "nosuchname", EnumSet.of(Path.Type.file)), status, new DisabledConnectionCallback());
    }

    @Test
    public void testRead() throws Exception {
        final Path home = new SFTPHomeDirectoryService(session).find();
        final Path test = new Path(home, UUID.randomUUID().toString(), EnumSet.of(Path.Type.file));
        new SFTPTouchFeature(session).touch(test, new TransferStatus());
        final int length = 39865;
        final byte[] content = RandomUtils.nextBytes(length);
        {
            final TransferStatus status = new TransferStatus().length(content.length);
            final OutputStream out = new SFTPWriteFeature(session).write(test, status, new DisabledConnectionCallback());
            assertNotNull(out);
            new StreamCopier(status, status).withLimit(new Long(content.length)).transfer(new ByteArrayInputStream(content), out);
            out.close();
        }
        {
            final TransferStatus status = new TransferStatus();
            status.setLength(content.length);
            final InputStream in = new SFTPReadFeature(session).read(test, status, new DisabledConnectionCallback());
            assertNotNull(in);
            final ByteArrayOutputStream buffer = new ByteArrayOutputStream(content.length);
            new StreamCopier(status, status).withLimit(new Long(content.length)).transfer(in, buffer);
            in.close();
            assertArrayEquals(content, buffer.toByteArray());
        }
        new SFTPDeleteFeature(session).delete(Collections.singletonList(test), new DisabledLoginCallback(), new Delete.DisabledCallback());
    }

    @Test
    public void testReadRange() throws Exception {
        final Path home = new SFTPHomeDirectoryService(session).find();
        final Path test = new Path(home, UUID.randomUUID().toString(), EnumSet.of(Path.Type.file));
        new SFTPTouchFeature(session).touch(test, new TransferStatus());
        final int length = 1048576;
        final byte[] content = RandomUtils.nextBytes(length);
        {
            final TransferStatus status = new TransferStatus().length(content.length);
            final OutputStream out = new SFTPWriteFeature(session).write(test, status, new DisabledConnectionCallback());
            assertNotNull(out);
            new StreamCopier(status, status).withLimit(new Long(content.length)).transfer(new ByteArrayInputStream(content), out);
            out.close();
        }
        {
            final TransferStatus status = new TransferStatus();
            status.setLength(content.length);
            status.setAppend(true);
            status.setOffset(100L);
            final InputStream in = new SFTPReadFeature(session).read(test, status, new DisabledConnectionCallback());
            assertNotNull(in);
            final ByteArrayOutputStream buffer = new ByteArrayOutputStream(content.length - 100);
            new StreamCopier(status, status).withLimit(new Long(content.length - 100)).transfer(in, buffer);
            in.close();
            final byte[] reference = new byte[content.length - 100];
            System.arraycopy(content, 100, reference, 0, content.length - 100);
            assertArrayEquals(reference, buffer.toByteArray());
        }
        new SFTPDeleteFeature(session).delete(Collections.singletonList(test), new DisabledLoginCallback(), new Delete.DisabledCallback());

    }

    @Test
    public void testUnconfirmedReadsNumber() {
        final SFTPReadFeature feature = new SFTPReadFeature(session);
        assertEquals(33, feature.getMaxUnconfirmedReads(new TransferStatus().length(TransferStatus.MEGA * 1L)));
        assertEquals(64, feature.getMaxUnconfirmedReads(new TransferStatus().length((long) (TransferStatus.GIGA * 1.3))));
    }
}
