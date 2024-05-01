package ch.cyberduck.core.dropbox;

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

import ch.cyberduck.core.AbstractDropboxTest;
import ch.cyberduck.core.AlphanumericRandomStringService;
import ch.cyberduck.core.AsciiRandomStringService;
import ch.cyberduck.core.DisabledConnectionCallback;
import ch.cyberduck.core.DisabledListProgressListener;
import ch.cyberduck.core.DisabledLoginCallback;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathAttributes;
import ch.cyberduck.core.exception.AccessDeniedException;
import ch.cyberduck.core.features.Delete;
import ch.cyberduck.core.http.HttpResponseOutputStream;
import ch.cyberduck.core.io.StreamCopier;
import ch.cyberduck.core.shared.DefaultHomeFinderService;
import ch.cyberduck.core.transfer.TransferStatus;
import ch.cyberduck.test.IntegrationTest;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.RandomUtils;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Collections;
import java.util.EnumSet;

import com.dropbox.core.v2.files.Metadata;

import static org.junit.Assert.*;

@Category(IntegrationTest.class)
public class DropboxWriteFeatureTest extends AbstractDropboxTest {

    @Test
    public void testReadWrite() throws Exception {
        final DropboxWriteFeature write = new DropboxWriteFeature(session);
        final TransferStatus status = new TransferStatus();
        status.setModified(1700638960509L);
        final byte[] content = RandomUtils.nextBytes(66800);
        status.setLength(content.length);
        final Path test = new Path(new DefaultHomeFinderService(session).find(), new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file));
        final HttpResponseOutputStream<Metadata> out = write.write(test, status, new DisabledConnectionCallback());
        assertNotNull(out);
        new StreamCopier(new TransferStatus(), new TransferStatus()).transfer(new ByteArrayInputStream(content), out);
        assertNotSame(PathAttributes.EMPTY, status.getResponse());
        test.withAttributes(status.getResponse());
        assertTrue(new DropboxFindFeature(session).find(test));
        final PathAttributes attributes = new DropboxListService(session).list(test.getParent(), new DisabledListProgressListener()).get(test).attributes();
        assertEquals(status.getResponse(), attributes);
        assertEquals(1700638960000L, attributes.getModificationDate());
        assertEquals(content.length, attributes.getSize());
        {
            final InputStream in = new DropboxReadFeature(session).read(test, new TransferStatus().withLength(content.length), new DisabledConnectionCallback());
            final ByteArrayOutputStream buffer = new ByteArrayOutputStream(content.length);
            new StreamCopier(status, status).transfer(in, buffer);
            in.close();
            assertArrayEquals(content, buffer.toByteArray());
        }
        {
            final byte[] buffer = new byte[content.length - 1];
            final InputStream in = new DropboxReadFeature(session).read(test, new TransferStatus().withLength(content.length).append(true).withOffset(1L), new DisabledConnectionCallback());
            IOUtils.readFully(in, buffer);
            in.close();
            final byte[] reference = new byte[content.length - 1];
            System.arraycopy(content, 1, reference, 0, content.length - 1);
            assertArrayEquals(reference, buffer);
        }
        new DropboxDeleteFeature(session).delete(Collections.singletonList(test), new DisabledLoginCallback(), new Delete.DisabledCallback());
    }

    @Test
    public void testWriteAppendChunks() throws Exception {
        final DropboxWriteFeature write = new DropboxWriteFeature(session, 44000L);
        final TransferStatus status = new TransferStatus();
        final byte[] content = RandomUtils.nextBytes(290000);
        status.setLength(content.length);
        final Path test = new Path(new DefaultHomeFinderService(session).find(), new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file));
        final OutputStream out = write.write(test, status, new DisabledConnectionCallback());
        assertNotNull(out);
        new StreamCopier(new TransferStatus(), new TransferStatus()).transfer(new ByteArrayInputStream(content), out);
        assertNotSame(PathAttributes.EMPTY, status.getResponse());
        test.withAttributes(status.getResponse());
        assertTrue(new DropboxFindFeature(session).find(test));
        final PathAttributes attributes = new DropboxListService(session).list(test.getParent(), new DisabledListProgressListener()).get(test).attributes();
        assertEquals(content.length, attributes.getSize());
        {
            final InputStream in = new DropboxReadFeature(session).read(test, new TransferStatus().withLength(content.length), new DisabledConnectionCallback());
            final ByteArrayOutputStream buffer = new ByteArrayOutputStream(content.length);
            new StreamCopier(status, status).transfer(in, buffer);
            in.close();
            assertArrayEquals(content, buffer.toByteArray());
        }
        {
            final byte[] buffer = new byte[content.length - 1];
            final InputStream in = new DropboxReadFeature(session).read(test, new TransferStatus().withLength(content.length).append(true).withOffset(1L), new DisabledConnectionCallback());
            IOUtils.readFully(in, buffer);
            in.close();
            final byte[] reference = new byte[content.length - 1];
            System.arraycopy(content, 1, reference, 0, content.length - 1);
            assertArrayEquals(reference, buffer);
        }
        new DropboxDeleteFeature(session).delete(Collections.singletonList(test), new DisabledLoginCallback(), new Delete.DisabledCallback());
    }

    @Test(expected = AccessDeniedException.class)
    public void testWriteLibreOfficeLock() throws Exception {
        final DropboxWriteFeature write = new DropboxWriteFeature(session);
        final TransferStatus status = new TransferStatus();
        final byte[] content = RandomUtils.nextBytes(0);
        status.setLength(content.length);
        final Path test = new Path(new DefaultHomeFinderService(session).find(), ".~lock." + new AsciiRandomStringService().random(), EnumSet.of(Path.Type.file));
        final OutputStream out = write.write(test, status, new DisabledConnectionCallback());
        new StreamCopier(new TransferStatus(), new TransferStatus()).transfer(new ByteArrayInputStream(content), out);
    }

    @Test(expected = AccessDeniedException.class)
    public void testWriteLibreOfficeLockHash() throws Exception {
        final DropboxWriteFeature write = new DropboxWriteFeature(session);
        final TransferStatus status = new TransferStatus();
        final byte[] content = RandomUtils.nextBytes(0);
        status.setLength(content.length);
        final Path test = new Path(new DefaultHomeFinderService(session).find(), ".~lock." + new AsciiRandomStringService().random() + "#", EnumSet.of(Path.Type.file));
        final OutputStream out = write.write(test, status, new DisabledConnectionCallback());
        new StreamCopier(new TransferStatus(), new TransferStatus()).transfer(new ByteArrayInputStream(content), out);
    }

    @Test(expected = AccessDeniedException.class)
    public void testWriteMSOfficeLock() throws Exception {
        final DropboxWriteFeature write = new DropboxWriteFeature(session);
        final TransferStatus status = new TransferStatus();
        final byte[] content = RandomUtils.nextBytes(0);
        status.setLength(content.length);
        final Path test = new Path(new DefaultHomeFinderService(session).find(), "~$" + new AsciiRandomStringService().random(), EnumSet.of(Path.Type.file));
        final OutputStream out = write.write(test, status, new DisabledConnectionCallback());
        new StreamCopier(new TransferStatus(), new TransferStatus()).transfer(new ByteArrayInputStream(content), out);
    }

    @Test(expected = AccessDeniedException.class)
    public void testWriteDS_Store() throws Exception {
        final DropboxWriteFeature write = new DropboxWriteFeature(session);
        final TransferStatus status = new TransferStatus();
        final byte[] content = RandomUtils.nextBytes(0);
        status.setLength(content.length);
        final Path test = new Path(new DefaultHomeFinderService(session).find(), ".DS_Store", EnumSet.of(Path.Type.file));
        final OutputStream out = write.write(test, status, new DisabledConnectionCallback());
        new StreamCopier(new TransferStatus(), new TransferStatus()).transfer(new ByteArrayInputStream(content), out);
    }

    @Test(expected = AccessDeniedException.class)
    public void testWriteDesktopIni() throws Exception {
        final DropboxWriteFeature write = new DropboxWriteFeature(session);
        final TransferStatus status = new TransferStatus();
        final byte[] content = RandomUtils.nextBytes(0);
        status.setLength(content.length);
        final Path test = new Path(new DefaultHomeFinderService(session).find(), "desktop.ini", EnumSet.of(Path.Type.file));
        final OutputStream out = write.write(test, status, new DisabledConnectionCallback());
        new StreamCopier(new TransferStatus(), new TransferStatus()).transfer(new ByteArrayInputStream(content), out);
    }
}
