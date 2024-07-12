package ch.cyberduck.core.deepbox;

/*
 * Copyright (c) 2002-2024 iterate GmbH. All rights reserved.
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
import ch.cyberduck.core.DisabledConnectionCallback;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.deepbox.io.swagger.client.model.Node;
import ch.cyberduck.core.exception.NotfoundException;
import ch.cyberduck.core.http.HttpResponseOutputStream;
import ch.cyberduck.core.io.StreamCopier;
import ch.cyberduck.core.shared.DefaultFindFeature;
import ch.cyberduck.core.transfer.TransferStatus;
import ch.cyberduck.test.IntegrationTest;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.RandomUtils;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.EnumSet;

import static org.junit.Assert.*;

@Category(IntegrationTest.class)
public class DeepboxReadFeatureTest extends AbstractDeepboxTest {

    @Test
    public void testRead() throws Exception {
        final TransferStatus status = new TransferStatus();
        final DeepboxIdProvider nodeid = new DeepboxIdProvider(session);
        final Path file = new Path("/ORG 4 - DeepBox Desktop App/ORG3:Box1/Documents/Invoices : Receipts/RE-IN - Copy1.pdf", EnumSet.of(Path.Type.file));
        final String s = IOUtils.toString(new DeepboxReadFeature(session, nodeid).read(file, status, new DisabledConnectionCallback()));
        assertNotNull(s);
    }

    @Test
    public void testReadNotFound() throws Exception {
        final TransferStatus status = new TransferStatus();
        final DeepboxIdProvider nodeid = new DeepboxIdProvider(session);
        final Path documents = new Path("/ORG 4 - DeepBox Desktop App/ORG3:Box1/Documents/", EnumSet.of(Path.Type.directory, Path.Type.volume));
        final Path test = new DeepboxDirectoryFeature(session, nodeid).mkdir(
                new Path(documents, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory)), new TransferStatus());
        assertThrows(NotfoundException.class, () -> new DeepboxReadFeature(session, nodeid).read(new Path(test, "nosuchname", EnumSet.of(Path.Type.file)), status, new DisabledConnectionCallback()));
        deleteAndPurge(test);
    }

    @Test
    public void testReadRange() throws Exception {
        final DeepboxIdProvider nodeid = new DeepboxIdProvider(session);
        final Path documents = new Path("/ORG 4 - DeepBox Desktop App/ORG3:Box1/Documents/", EnumSet.of(Path.Type.directory, Path.Type.volume));
        final Path file = new Path(documents, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file));
        final byte[] content = RandomUtils.nextBytes(2047);
        {
            final HttpResponseOutputStream<Node> out = new DeepboxWriteFeature(session, nodeid).write(file, new TransferStatus(), new DisabledConnectionCallback());
            final ByteArrayInputStream in = new ByteArrayInputStream(content);
            final TransferStatus progress = new TransferStatus();
            final BytecountStreamListener count = new BytecountStreamListener();
            new StreamCopier(progress, progress).withListener(count).transfer(in, out);
            assertEquals(content.length, count.getSent());
            in.close();
            out.close();
            assertTrue(new DefaultFindFeature(session).find(file));
            assertTrue(new DeepboxFindFeature(session, nodeid).find(file));
        }
        final TransferStatus status = new TransferStatus();
        status.setLength(content.length);
        status.setAppend(true);
        status.setOffset(100L);
        final InputStream in = new DeepboxReadFeature(session, nodeid).read(file, status.withLength(content.length - 100), new DisabledConnectionCallback());
        assertNotNull(in);
        final ByteArrayOutputStream buffer = new ByteArrayOutputStream(content.length - 100);
        new StreamCopier(status, status).transfer(in, buffer);
        final byte[] reference = new byte[content.length - 100];
        System.arraycopy(content, 100, reference, 0, content.length - 100);
        assertArrayEquals(reference, buffer.toByteArray());
        in.close();
        deleteAndPurge(file);
    }

    @Test
    public void testReadRangeUnknownLength() throws Exception {
        final DeepboxIdProvider nodeid = new DeepboxIdProvider(session);
        final Path documents = new Path("/ORG 4 - DeepBox Desktop App/ORG3:Box1/Documents/", EnumSet.of(Path.Type.directory, Path.Type.volume));
        final Path file = new Path(documents, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file));
        final byte[] content = RandomUtils.nextBytes(2047);
        {
            final HttpResponseOutputStream<Node> out = new DeepboxWriteFeature(session, nodeid).write(file, new TransferStatus(), new DisabledConnectionCallback());
            final ByteArrayInputStream in = new ByteArrayInputStream(content);
            final TransferStatus progress = new TransferStatus();
            final BytecountStreamListener count = new BytecountStreamListener();
            new StreamCopier(progress, progress).withListener(count).transfer(in, out);
            assertEquals(content.length, count.getSent());
            in.close();
            out.close();
            assertTrue(new DefaultFindFeature(session).find(file));
            assertTrue(new DeepboxFindFeature(session, nodeid).find(file));
        }
        final TransferStatus status = new TransferStatus();
        status.setLength(-1L);
        status.setAppend(true);
        status.setOffset(100L);
        final InputStream in = new DeepboxReadFeature(session, nodeid).read(file, status.withLength(content.length - 100), new DisabledConnectionCallback());
        assertNotNull(in);
        final ByteArrayOutputStream buffer = new ByteArrayOutputStream(content.length - 100);
        new StreamCopier(status, status).transfer(in, buffer);
        final byte[] reference = new byte[content.length - 100];
        System.arraycopy(content, 100, reference, 0, content.length - 100);
        assertArrayEquals(reference, buffer.toByteArray());
        in.close();
        deleteAndPurge(file);
    }
}
