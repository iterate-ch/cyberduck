package ch.cyberduck.core.ftp;

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

import ch.cyberduck.core.Credentials;
import ch.cyberduck.core.DisabledCancelCallback;
import ch.cyberduck.core.DisabledHostKeyCallback;
import ch.cyberduck.core.DisabledLoginCallback;
import ch.cyberduck.core.DisabledPasswordStore;
import ch.cyberduck.core.Host;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathCache;
import ch.cyberduck.core.exception.NotfoundException;
import ch.cyberduck.core.features.Delete;
import ch.cyberduck.core.io.StreamCopier;
import ch.cyberduck.core.shared.DefaultTouchFeature;
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
import java.util.Random;
import java.util.UUID;

import static org.junit.Assert.*;

@Category(IntegrationTest.class)
public class FTPReadFeatureTest {

    @Test(expected = NotfoundException.class)
    public void testReadNotFound() throws Exception {
        final Host host = new Host(new FTPTLSProtocol(), "test.cyberduck.ch", new Credentials(
                System.getProperties().getProperty("ftp.user"), System.getProperties().getProperty("ftp.password")
        ));
        final FTPSession session = new FTPSession(host);
        session.open(new DisabledHostKeyCallback());
        session.login(new DisabledPasswordStore(), new DisabledLoginCallback(), new DisabledCancelCallback(), PathCache.empty());
        final TransferStatus status = new TransferStatus();
        new FTPReadFeature(session).read(new Path(new FTPWorkdirService(session).find(), "nosuchname", EnumSet.of(Path.Type.file)), status);
    }

    @Test
    public void testRead() throws Exception {
        final Host host = new Host(new FTPTLSProtocol(), "test.cyberduck.ch", new Credentials(
                System.getProperties().getProperty("ftp.user"), System.getProperties().getProperty("ftp.password")
        ));
        final FTPSession session = new FTPSession(host);
        session.open(new DisabledHostKeyCallback());
        session.login(new DisabledPasswordStore(), new DisabledLoginCallback(), new DisabledCancelCallback(), PathCache.empty());
        final Path home = new FTPWorkdirService(session).find();
        final Path test = new Path(home, UUID.randomUUID().toString(), EnumSet.of(Path.Type.file));
        new DefaultTouchFeature(session).touch(test, new TransferStatus());
        final byte[] content = new byte[39865];
        new Random().nextBytes(content);
        {
            final TransferStatus status = new TransferStatus().length(content.length);
            final OutputStream out = new FTPWriteFeature(session).write(test, status);
            assertNotNull(out);
            new StreamCopier(status, status).withLimit(new Long(content.length)).transfer(new ByteArrayInputStream(content), out);
            out.close();
        }
        {
            final TransferStatus status = new TransferStatus();
            status.setLength(content.length);
            final InputStream in = new FTPReadFeature(session).read(test, status);
            assertNotNull(in);
            final ByteArrayOutputStream buffer = new ByteArrayOutputStream(content.length);
            new StreamCopier(status, status).withLimit(new Long(content.length)).transfer(in, buffer);
            in.close();
            assertArrayEquals(content, buffer.toByteArray());
        }
        new FTPDeleteFeature(session).delete(Collections.singletonList(test), new DisabledLoginCallback(), new Delete.DisabledCallback());
        session.close();
    }

    @Test
    public void testReadRange() throws Exception {
        final Host host = new Host(new FTPTLSProtocol(), "test.cyberduck.ch", new Credentials(
                System.getProperties().getProperty("ftp.user"), System.getProperties().getProperty("ftp.password")
        ));
        final FTPSession session = new FTPSession(host);
        session.open(new DisabledHostKeyCallback());
        session.login(new DisabledPasswordStore(), new DisabledLoginCallback(), new DisabledCancelCallback(), PathCache.empty());
        final Path test = new Path(new FTPWorkdirService(session).find(), UUID.randomUUID().toString(), EnumSet.of(Path.Type.file));
        new DefaultTouchFeature(session).touch(test, new TransferStatus());
        final byte[] content = RandomUtils.nextBytes(2048);
        final OutputStream out = new FTPWriteFeature(session).write(test, new TransferStatus().length(content.length));
        assertNotNull(out);
        new StreamCopier(new TransferStatus(), new TransferStatus()).transfer(new ByteArrayInputStream(content), out);
        out.close();
        final TransferStatus status = new TransferStatus();
        // Partial read with offset and not full content length
        final long limit = content.length - 100;
        status.setLength(limit);
        status.setAppend(true);
        final long offset = 2L;
        status.setOffset(offset);
        final InputStream in = new FTPReadFeature(session).read(test, status);
        assertNotNull(in);
        final ByteArrayOutputStream download = new ByteArrayOutputStream();
        new StreamCopier(status, status).withLimit(limit).transfer(in, download);
        final byte[] reference = new byte[(int) limit];
        System.arraycopy(content, (int) offset, reference, 0, (int) limit);
        assertArrayEquals(reference, download.toByteArray());
        new FTPDeleteFeature(session).delete(Collections.singletonList(test), new DisabledLoginCallback(), new Delete.DisabledCallback());
        session.close();
    }

    @Test
    public void testAbortNoRead() throws Exception {
        final Host host = new Host(new FTPTLSProtocol(), "test.cyberduck.ch", new Credentials(
                System.getProperties().getProperty("ftp.user"), System.getProperties().getProperty("ftp.password")
        ));
        final FTPSession session = new FTPSession(host);
        session.open(new DisabledHostKeyCallback());
        session.login(new DisabledPasswordStore(), new DisabledLoginCallback(), new DisabledCancelCallback(), PathCache.empty());
        final Path test = new Path(new FTPWorkdirService(session).find(), UUID.randomUUID().toString(), EnumSet.of(Path.Type.file));
        new DefaultTouchFeature(session).touch(test, new TransferStatus());
        final TransferStatus status = new TransferStatus();
        status.setLength(5L);
        final Path workdir = new FTPWorkdirService(session).find();
        final InputStream in = new FTPReadFeature(session).read(new Path(workdir, "test", EnumSet.of(Path.Type.file)), status);
        assertNotNull(in);
        // Send ABOR because stream was not read completly
        in.close();
        // Make sure subsequent PWD command works
        assertEquals(workdir, new FTPWorkdirService(session).find());
        new FTPDeleteFeature(session).delete(Collections.singletonList(test), new DisabledLoginCallback(), new Delete.DisabledCallback());
        session.close();
    }

    @Test
    public void testAbortPartialRead() throws Exception {
        final Host host = new Host(new FTPTLSProtocol(), "test.cyberduck.ch", new Credentials(
                System.getProperties().getProperty("ftp.user"), System.getProperties().getProperty("ftp.password")
        ));
        final FTPSession session = new FTPSession(host);
        session.open(new DisabledHostKeyCallback());
        session.login(new DisabledPasswordStore(), new DisabledLoginCallback(), new DisabledCancelCallback(), PathCache.empty());
        final Path test = new Path(new FTPWorkdirService(session).find(), UUID.randomUUID().toString(), EnumSet.of(Path.Type.file));
        new DefaultTouchFeature(session).touch(test, new TransferStatus());
        final OutputStream out = new FTPWriteFeature(session).write(test, new TransferStatus().length(20L));
        assertNotNull(out);
        final byte[] content = RandomUtils.nextBytes(2048);
        new StreamCopier(new TransferStatus(), new TransferStatus()).transfer(new ByteArrayInputStream(content), out);
        out.close();
        final TransferStatus status = new TransferStatus();
        status.setLength(20L);
        final Path workdir = new FTPWorkdirService(session).find();
        final InputStream in = new FTPReadFeature(session).read(test, status);
        assertNotNull(in);
        assertTrue(in.read() > 0);
        // Send ABOR because stream was not read completly
        in.close();
        // Make sure subsequent PWD command works
        assertEquals(workdir, new FTPWorkdirService(session).find());
        new FTPDeleteFeature(session).delete(Collections.singletonList(test), new DisabledLoginCallback(), new Delete.DisabledCallback());
        session.close();
    }

    @Test
    public void testDoubleCloseStream() throws Exception {
        final Host host = new Host(new FTPTLSProtocol(), "test.cyberduck.ch", new Credentials(
                System.getProperties().getProperty("ftp.user"), System.getProperties().getProperty("ftp.password")
        ));
        final FTPSession session = new FTPSession(host);
        session.open(new DisabledHostKeyCallback());
        session.login(new DisabledPasswordStore(), new DisabledLoginCallback(), new DisabledCancelCallback(), PathCache.empty());
        final Path test = new Path(new FTPWorkdirService(session).find(), UUID.randomUUID().toString(), EnumSet.of(Path.Type.file));
        new DefaultTouchFeature(session).touch(test, new TransferStatus());
        final TransferStatus status = new TransferStatus();
        status.setLength(5L);
        final Path workdir = new FTPWorkdirService(session).find();
        final InputStream in = new FTPReadFeature(session).read(new Path(workdir, "test", EnumSet.of(Path.Type.file)), status);
        assertNotNull(in);
        // Read 226 reply
        in.close();
        // Read timeout
        in.close();
        session.close();
    }
}
