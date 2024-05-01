package ch.cyberduck.core.nio;

/*
 * Copyright (c) 2002-2017 iterate GmbH. All rights reserved.
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

import ch.cyberduck.core.AbstractPath;
import ch.cyberduck.core.AsciiRandomStringService;
import ch.cyberduck.core.AttributedList;
import ch.cyberduck.core.DisabledCancelCallback;
import ch.cyberduck.core.DisabledConnectionCallback;
import ch.cyberduck.core.DisabledHostKeyCallback;
import ch.cyberduck.core.DisabledListProgressListener;
import ch.cyberduck.core.DisabledLoginCallback;
import ch.cyberduck.core.Host;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.exception.NotfoundException;
import ch.cyberduck.core.features.Delete;
import ch.cyberduck.core.io.StreamCopier;
import ch.cyberduck.core.proxy.Proxy;
import ch.cyberduck.core.shared.DefaultAttributesFinderFeature;
import ch.cyberduck.core.shared.DefaultFindFeature;
import ch.cyberduck.core.transfer.TransferStatus;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.RandomUtils;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.UUID;

import static org.junit.Assert.*;

public class LocalWriteFeatureTest {

    @Test
    public void testWriteSymlink() throws Exception {
        final LocalSession session = new LocalSession(new Host(new LocalProtocol(), new LocalProtocol().getDefaultHostname()));
        if(session.isPosixFilesystem()) {
            session.open(Proxy.DIRECT, new DisabledHostKeyCallback(), new DisabledLoginCallback(), new DisabledCancelCallback());
            session.login(Proxy.DIRECT, new DisabledLoginCallback(), new DisabledCancelCallback());
            final Path workdir = new LocalHomeFinderFeature().find();
            final Path target = new Path(workdir, UUID.randomUUID().toString(), EnumSet.of(Path.Type.file));
            new LocalTouchFeature(session).touch(target, new TransferStatus());
            assertTrue(new LocalFindFeature(session).find(target));
            final String name = UUID.randomUUID().toString();
            final Path symlink = new Path(workdir, name, EnumSet.of(Path.Type.file, AbstractPath.Type.symboliclink));
            new LocalSymlinkFeature(session).symlink(symlink, target.getName());
            assertTrue(new LocalFindFeature(session).find(symlink));
            final TransferStatus status = new TransferStatus();
            final int length = 1048576;
            final byte[] content = RandomUtils.nextBytes(length);
            status.setLength(content.length);
            status.setExists(true);
            final OutputStream out = new LocalWriteFeature(session).write(symlink, status, new DisabledConnectionCallback());
            new StreamCopier(status, status).transfer(new ByteArrayInputStream(content), out);
            out.close();
            {
                final ByteArrayOutputStream buffer = new ByteArrayOutputStream(content.length);
                final InputStream in = new LocalReadFeature(session).read(symlink, new TransferStatus().withLength(content.length), new DisabledConnectionCallback());
                new StreamCopier(status, status).transfer(in, buffer);
                assertArrayEquals(content, buffer.toByteArray());
            }
            {
                final byte[] buffer = new byte[0];
                final InputStream in = new LocalReadFeature(session).read(target, new TransferStatus(), new DisabledConnectionCallback());
                IOUtils.readFully(in, buffer);
                in.close();
                assertArrayEquals(new byte[0], buffer);
            }
            final AttributedList<Path> list = new LocalListService(session).list(workdir, new DisabledListProgressListener());
            assertTrue(list.contains(new Path(workdir, name, EnumSet.of(Path.Type.file))));
            assertFalse(list.contains(symlink));
            new LocalDeleteFeature(session).delete(Arrays.asList(target, symlink), new DisabledLoginCallback(), new Delete.DisabledCallback());
        }
    }

    @Test(expected = NotfoundException.class)
    public void testWriteNotFound() throws Exception {
        final LocalSession session = new LocalSession(new Host(new LocalProtocol(), new LocalProtocol().getDefaultHostname()));
        session.open(Proxy.DIRECT, new DisabledHostKeyCallback(), new DisabledLoginCallback(), new DisabledCancelCallback());
        session.login(Proxy.DIRECT, new DisabledLoginCallback(), new DisabledCancelCallback());
        final Path workdir = new LocalHomeFinderFeature().find();
        final Path test = new Path(workdir.getAbsolute() + "/nosuchdirectory/" + UUID.randomUUID().toString(), EnumSet.of(Path.Type.file));
        new LocalWriteFeature(session).write(test, new TransferStatus(), new DisabledConnectionCallback());
    }

    @Test
    public void testWriteContentRange() throws Exception {
        final LocalSession session = new LocalSession(new Host(new LocalProtocol(), new LocalProtocol().getDefaultHostname()));
        session.open(Proxy.DIRECT, new DisabledHostKeyCallback(), new DisabledLoginCallback(), new DisabledCancelCallback());
        session.login(Proxy.DIRECT, new DisabledLoginCallback(), new DisabledCancelCallback());
        final LocalWriteFeature feature = new LocalWriteFeature(session);
        final Path workdir = new LocalHomeFinderFeature().find();
        final Path test = new Path(workdir, UUID.randomUUID().toString(), EnumSet.of(Path.Type.file));
        final byte[] content = RandomUtils.nextBytes(64000);
        {
            final TransferStatus status = new TransferStatus();
            status.setLength(1024L);
            status.setOffset(0L);
            final OutputStream out = feature.write(test, status, new DisabledConnectionCallback());
            // Write first 1024
            new StreamCopier(status, status).withOffset(status.getOffset()).withLimit(status.getLength()).transfer(new ByteArrayInputStream(content), out);
            out.flush();
            out.close();
        }
        assertTrue(new DefaultFindFeature(session).find(test));
        assertEquals(1024L, new DefaultAttributesFinderFeature(session).find(test).getSize());
        {
            // Remaining chunked transfer with offset
            final TransferStatus status = new TransferStatus().exists(true);
            status.setLength(content.length - 1024L);
            status.setOffset(1024L);
            status.setAppend(true);
            final OutputStream out = feature.write(test, status, new DisabledConnectionCallback());
            new StreamCopier(status, status).withOffset(status.getOffset()).withLimit(status.getLength()).transfer(new ByteArrayInputStream(content), out);
            out.flush();
            out.close();
        }
        final ByteArrayOutputStream out = new ByteArrayOutputStream(content.length);
        IOUtils.copy(new LocalReadFeature(session).read(test, new TransferStatus().withLength(content.length), new DisabledConnectionCallback()), out);
        assertArrayEquals(content, out.toByteArray());
        new LocalDeleteFeature(session).delete(Collections.singletonList(test), new DisabledLoginCallback(), new Delete.DisabledCallback());
    }

    @Test
    public void testWriteRangeEndFirst() throws Exception {
        final LocalSession session = new LocalSession(new Host(new LocalProtocol(), new LocalProtocol().getDefaultHostname()));
        session.open(Proxy.DIRECT, new DisabledHostKeyCallback(), new DisabledLoginCallback(), new DisabledCancelCallback());
        session.login(Proxy.DIRECT, new DisabledLoginCallback(), new DisabledCancelCallback());
        final LocalWriteFeature feature = new LocalWriteFeature(session);
        final Path workdir = new LocalHomeFinderFeature().find();
        final Path test = new Path(workdir, UUID.randomUUID().toString(), EnumSet.of(Path.Type.file));
        final byte[] content = RandomUtils.nextBytes(2048);
        {
            // Write end of file first
            final TransferStatus status = new TransferStatus();
            status.setLength(1024L);
            status.setOffset(1024L);
            status.setAppend(true);
            final OutputStream out = feature.write(test, status, new DisabledConnectionCallback());
            new StreamCopier(status, status).withOffset(status.getOffset()).withLimit(status.getLength()).transfer(new ByteArrayInputStream(content), out);
            out.flush();
            out.close();
        }
        assertEquals(2048, new DefaultAttributesFinderFeature(session).find(test).getSize());
        {
            // Write beginning of file up to the last chunk
            final TransferStatus status = new TransferStatus().exists(true);
            status.setOffset(0L);
            status.setLength(1024L);
            status.setAppend(true);
            final OutputStream out = feature.write(test, status, new DisabledConnectionCallback());
            new StreamCopier(status, status).withOffset(status.getOffset()).withLimit(status.getLength()).transfer(new ByteArrayInputStream(content), out);
            out.flush();
            out.close();
        }
        assertEquals(2048, new DefaultAttributesFinderFeature(session).find(test).getSize());
        final ByteArrayOutputStream out = new ByteArrayOutputStream(content.length);
        IOUtils.copy(new LocalReadFeature(session).read(test, new TransferStatus().withLength(content.length), new DisabledConnectionCallback()), out);
        assertArrayEquals(content, out.toByteArray());
        assertTrue(new DefaultFindFeature(session).find(test));
        assertEquals(content.length, new DefaultAttributesFinderFeature(session).find(test).getSize());
        new LocalDeleteFeature(session).delete(Collections.singletonList(test), new DisabledLoginCallback(), new Delete.DisabledCallback());
    }

    @Test
    public void testWriteTildeFilename() throws Exception {
        final LocalSession session = new LocalSession(new Host(new LocalProtocol(), new LocalProtocol().getDefaultHostname()));
        session.open(Proxy.DIRECT, new DisabledHostKeyCallback(), new DisabledLoginCallback(), new DisabledCancelCallback());
        session.login(Proxy.DIRECT, new DisabledLoginCallback(), new DisabledCancelCallback());
        final LocalWriteFeature feature = new LocalWriteFeature(session);
        final Path workdir = new LocalHomeFinderFeature().find();
        final Path test = new Path(workdir, String.format("~$%s", new AsciiRandomStringService().random()), EnumSet.of(Path.Type.file));
        final byte[] content = RandomUtils.nextBytes(2048);
        final TransferStatus status = new TransferStatus().withLength(content.length);
        final OutputStream out = feature.write(test, status, new DisabledConnectionCallback());
        new StreamCopier(status, status).withOffset(status.getOffset()).withLimit(status.getLength()).transfer(new ByteArrayInputStream(content), out);
        out.flush();
        out.close();
        final ByteArrayOutputStream b = new ByteArrayOutputStream(content.length);
        IOUtils.copy(new LocalReadFeature(session).read(test, new TransferStatus().withLength(content.length), new DisabledConnectionCallback()), b);
        assertArrayEquals(content, b.toByteArray());
        assertTrue(new DefaultFindFeature(session).find(test));
        assertEquals(content.length, new DefaultAttributesFinderFeature(session).find(test).getSize());
        new LocalDeleteFeature(session).delete(Collections.singletonList(test), new DisabledLoginCallback(), new Delete.DisabledCallback());
    }
}
