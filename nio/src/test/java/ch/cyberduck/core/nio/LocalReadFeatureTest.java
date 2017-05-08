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

import ch.cyberduck.core.DisabledCancelCallback;
import ch.cyberduck.core.DisabledConnectionCallback;
import ch.cyberduck.core.DisabledHostKeyCallback;
import ch.cyberduck.core.DisabledLoginCallback;
import ch.cyberduck.core.DisabledPasswordStore;
import ch.cyberduck.core.Host;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathCache;
import ch.cyberduck.core.exception.NotfoundException;
import ch.cyberduck.core.features.Delete;
import ch.cyberduck.core.io.StreamCopier;
import ch.cyberduck.core.preferences.TemporarySupportDirectoryFinder;
import ch.cyberduck.core.transfer.TransferStatus;
import ch.cyberduck.test.IntegrationTest;

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

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertNotNull;

@Category(IntegrationTest.class)
public class LocalReadFeatureTest {

    @Test(expected = NotfoundException.class)
    public void testReadNotFound() throws Exception {
        final LocalSession session = new LocalSession(new Host(new LocalProtocol(), new LocalProtocol().getDefaultHostname()));
        session.open(new DisabledHostKeyCallback());
        session.login(new DisabledPasswordStore(), new DisabledLoginCallback(), new DisabledCancelCallback(), PathCache.empty());
        final TransferStatus status = new TransferStatus();
        final Path workdir = new Path(new TemporarySupportDirectoryFinder().find().getAbsolute(), EnumSet.of(Path.Type.directory));
        new LocalReadFeature(session).read(new Path(workdir, "nosuchname", EnumSet.of(Path.Type.file)), status, new DisabledConnectionCallback());
    }

    @Test
    public void testRead() throws Exception {
        final LocalSession session = new LocalSession(new Host(new LocalProtocol(), new LocalProtocol().getDefaultHostname()));
        session.open(new DisabledHostKeyCallback());
        session.login(new DisabledPasswordStore(), new DisabledLoginCallback(), new DisabledCancelCallback(), PathCache.empty());
        final Path workdir = new Path(new TemporarySupportDirectoryFinder().find().getAbsolute(), EnumSet.of(Path.Type.directory));
        final Path test = new Path(workdir, UUID.randomUUID().toString(), EnumSet.of(Path.Type.file));
        new LocalTouchFeature(session).touch(test, new TransferStatus());
        final byte[] content = new byte[39865];
        new Random().nextBytes(content);
        {
            final TransferStatus status = new TransferStatus().length(content.length).exists(true);
            final OutputStream out = new LocalWriteFeature(session).write(test, status, new DisabledConnectionCallback());
            assertNotNull(out);
            new StreamCopier(status, status).withLimit(new Long(content.length)).transfer(new ByteArrayInputStream(content), out);
            out.close();
        }
        {
            final TransferStatus status = new TransferStatus();
            status.setLength(content.length);
            final InputStream in = new LocalReadFeature(session).read(test, status, new DisabledConnectionCallback());
            assertNotNull(in);
            final ByteArrayOutputStream buffer = new ByteArrayOutputStream(content.length);
            new StreamCopier(status, status).withLimit(new Long(content.length)).transfer(in, buffer);
            in.close();
            assertArrayEquals(content, buffer.toByteArray());
        }
        new LocalDeleteFeature(session).delete(Collections.singletonList(test), new DisabledLoginCallback(), new Delete.DisabledCallback());
        session.close();
    }

    @Test
    public void testReadRange() throws Exception {
        final LocalSession session = new LocalSession(new Host(new LocalProtocol(), new LocalProtocol().getDefaultHostname()));
        session.open(new DisabledHostKeyCallback());
        session.login(new DisabledPasswordStore(), new DisabledLoginCallback(), new DisabledCancelCallback(), PathCache.empty());
        final Path workdir = new Path(new TemporarySupportDirectoryFinder().find().getAbsolute(), EnumSet.of(Path.Type.directory));
        final Path test = new Path(workdir, UUID.randomUUID().toString(), EnumSet.of(Path.Type.file));
        new LocalTouchFeature(session).touch(test, new TransferStatus());
        final byte[] content = new byte[1048576];
        new Random().nextBytes(content);
        {
            final TransferStatus status = new TransferStatus().length(content.length).exists(true);
            final OutputStream out = new LocalWriteFeature(session).write(test, status, new DisabledConnectionCallback());
            assertNotNull(out);
            new StreamCopier(status, status).withLimit(new Long(content.length)).transfer(new ByteArrayInputStream(content), out);
            out.close();
        }
        {
            final TransferStatus status = new TransferStatus();
            status.setLength(content.length);
            status.setAppend(true);
            status.setOffset(100L);
            final InputStream in = new LocalReadFeature(session).read(test, status, new DisabledConnectionCallback());
            assertNotNull(in);
            final ByteArrayOutputStream buffer = new ByteArrayOutputStream(content.length - 100);
            new StreamCopier(status, status).withLimit(new Long(content.length - 100)).transfer(in, buffer);
            in.close();
            final byte[] reference = new byte[content.length - 100];
            System.arraycopy(content, 100, reference, 0, content.length - 100);
            assertArrayEquals(reference, buffer.toByteArray());
        }
        new LocalDeleteFeature(session).delete(Collections.singletonList(test), new DisabledLoginCallback(), new Delete.DisabledCallback());
        session.close();
    }
}