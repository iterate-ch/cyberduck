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
import ch.cyberduck.core.DisabledLoginCallback;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathAttributes;
import ch.cyberduck.core.deepbox.io.swagger.client.model.Node;
import ch.cyberduck.core.exception.ConnectionCanceledException;
import ch.cyberduck.core.exception.TransferStatusCanceledException;
import ch.cyberduck.core.features.Delete;
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
import java.io.InputStream;
import java.util.Collections;
import java.util.EnumSet;

import static org.junit.Assert.*;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@Category(IntegrationTest.class)
public class DeepboxWriteFeatureTest extends AbstractDeepboxTest {

    @Test
    public void testOverwrite() throws Exception {
        final DeepboxIdProvider nodeid = new DeepboxIdProvider(session);
        final Path documents = new Path("/ORG 4 - DeepBox Desktop App/ORG 4 - DeepBox Desktop App/ORG3:Box1/Documents/", EnumSet.of(Path.Type.directory, Path.Type.volume));
        final Path file = new Path(documents, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file));
        new DeepboxTouchFeature(session, nodeid).touch(file, new TransferStatus());
        assertTrue(new DefaultFindFeature(session).find(file));
        try {
            final byte[] content = RandomUtils.nextBytes(2047);
            final HttpResponseOutputStream<Node> out = new DeepboxWriteFeature(session, nodeid).write(file,
                    new TransferStatus().exists(true), new DisabledConnectionCallback());
            final ByteArrayInputStream in = new ByteArrayInputStream(content);
            final TransferStatus progress = new TransferStatus();
            final BytecountStreamListener count = new BytecountStreamListener();
            new StreamCopier(progress, progress).withListener(count).transfer(in, out);
            assertEquals(content.length, count.getSent());
            in.close();
            out.close();
            assertTrue(new DefaultFindFeature(session).find(file));
            assertTrue(new DeepboxFindFeature(session, nodeid).find(file));
            assertEquals(content.length, new DeepboxAttributesFinderFeature(session, nodeid).find(file).getSize());
        }
        finally {
            new DeepboxDeleteFeature(session, nodeid).delete(Collections.singletonList(file), new DisabledLoginCallback(), new Delete.DisabledCallback());
        }
    }

    @Test
    public void testInbox() throws Exception {
        final DeepboxIdProvider nodeid = new DeepboxIdProvider(session);
        final Path inbox = new Path("/ORG 4 - DeepBox Desktop App/ORG 4 - DeepBox Desktop App/ORG3:Box1/Inbox/", EnumSet.of(Path.Type.directory, Path.Type.volume));
        final Path file = new Path(inbox, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file));
        try {
            final byte[] content = RandomUtils.nextBytes(1047);
            final HttpResponseOutputStream<Node> out = new DeepboxWriteFeature(session, nodeid).write(file,
                    new TransferStatus(), new DisabledConnectionCallback());
            final ByteArrayInputStream in = new ByteArrayInputStream(content);
            final TransferStatus progress = new TransferStatus();
            final BytecountStreamListener count = new BytecountStreamListener();
            new StreamCopier(progress, progress).withListener(count).transfer(in, out);
            assertEquals(content.length, count.getSent());
            in.close();
            out.close();
            assertTrue(new DefaultFindFeature(session).find(file));
            assertTrue(new DeepboxFindFeature(session, nodeid).find(file));
            assertEquals(content.length, new DeepboxAttributesFinderFeature(session, nodeid).find(file).getSize());
        }
        finally {
            new DeepboxDeleteFeature(session, nodeid).delete(Collections.singletonList(file), new DisabledLoginCallback(), new Delete.DisabledCallback());
        }
    }

    @Test
    public void testNewFile() throws Exception {
        final DeepboxIdProvider nodeid = new DeepboxIdProvider(session);
        final Path documents = new Path("/ORG 4 - DeepBox Desktop App/ORG 4 - DeepBox Desktop App/ORG3:Box1/Documents/", EnumSet.of(Path.Type.directory, Path.Type.volume));
        final Path file = new Path(documents, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file));
        final byte[] content = RandomUtils.nextBytes(2047);
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
        new DeepboxDeleteFeature(session, nodeid).delete(Collections.singletonList(file), new DisabledLoginCallback(), new Delete.DisabledCallback());
    }

    @Test
    public void testReadWrite() throws Exception {
        final DeepboxIdProvider nodeid = new DeepboxIdProvider(session);
        final Path documents = new Path("/ORG 4 - DeepBox Desktop App/ORG 4 - DeepBox Desktop App/ORG3:Box1/Documents/", EnumSet.of(Path.Type.directory, Path.Type.volume));
        final Path room = new DeepboxDirectoryFeature(session, nodeid).mkdir(
                new Path(documents,
                        new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory)), new TransferStatus());
        final long folderTimestamp = new DeepboxAttributesFinderFeature(session, nodeid).find(room).getModificationDate();
        final byte[] content = RandomUtils.nextBytes(32769);
        final Path test = new Path(room, String.format("%s", new AlphanumericRandomStringService().random()), EnumSet.of(Path.Type.file));
        {
            final TransferStatus status = new TransferStatus();
            status.setLength(content.length);
            final DeepboxWriteFeature writer = new DeepboxWriteFeature(session, nodeid);
            final HttpResponseOutputStream<Node> out = writer.write(test, status, new DisabledConnectionCallback());
            assertNotNull(out);
            new StreamCopier(status, status).transfer(new ByteArrayInputStream(content), out);
        }
        assertTrue(new DefaultFindFeature(session).find(test));
        assertEquals(folderTimestamp, new DeepboxAttributesFinderFeature(session, nodeid).find(room).getModificationDate());
        PathAttributes attributes = new DeepboxAttributesFinderFeature(session, nodeid).find(test);
        final String versionId = attributes.getVersionId();
        assertNull(versionId);
        final String nodeId = attributes.getFileId();
        assertNotNull(nodeId);
        assertEquals(new DeepboxAttributesFinderFeature(session, nodeid).find(test), attributes);
        final byte[] compare = new byte[content.length];
        final InputStream stream = new DeepboxReadFeature(session, nodeid).read(test, new TransferStatus().withLength(content.length), new DisabledConnectionCallback());
        IOUtils.readFully(stream, compare);
        stream.close();
        assertArrayEquals(content, compare);
        // Overwrite
        {
            final byte[] change = RandomUtils.nextBytes(256);
            final TransferStatus status = new TransferStatus();
            status.setLength(change.length);
            final DeepboxWriteFeature writer = new DeepboxWriteFeature(session, nodeid);
            final HttpResponseOutputStream<Node> out = writer.write(test, status.exists(true), new DisabledConnectionCallback());
            assertNotNull(out);
            new StreamCopier(status, status).transfer(new ByteArrayInputStream(change), out);
            assertEquals(nodeId, new DeepboxAttributesFinderFeature(session, nodeid).find(test).getFileId());
        }
        test.attributes().setCustom(Collections.emptyMap());
        attributes = new DeepboxAttributesFinderFeature(session, nodeid).find(test);
        assertNotNull(attributes.getFileId());
        assertEquals(nodeId, new DeepboxIdProvider(session).getFileId(test));
        new DeepboxDeleteFeature(session, nodeid).delete(Collections.singletonList(room), new DisabledLoginCallback(), new Delete.DisabledCallback());
    }

    @Test
    public void testWriteSingleByte() throws Exception {
        final DeepboxIdProvider nodeid = new DeepboxIdProvider(session);
        final DeepboxWriteFeature feature = new DeepboxWriteFeature(session, nodeid);
        final Path documents = new Path("/ORG 4 - DeepBox Desktop App/ORG 4 - DeepBox Desktop App/ORG3:Box1/Documents/", EnumSet.of(Path.Type.directory, Path.Type.volume));
        final Path room = new DeepboxDirectoryFeature(session, nodeid).mkdir(
                new Path(documents, new AlphanumericRandomStringService().random(),
                        EnumSet.of(Path.Type.directory, Path.Type.volume)), new TransferStatus());
        final byte[] content = RandomUtils.nextBytes(1);
        final TransferStatus status = new TransferStatus();
        status.setLength(content.length);
        final Path file = new Path(room, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file));
        final HttpResponseOutputStream<Node> out = feature.write(file, status, new DisabledConnectionCallback());
        final ByteArrayInputStream in = new ByteArrayInputStream(content);
        assertEquals(content.length, IOUtils.copyLarge(in, out));
        in.close();
        out.close();
        assertTrue(new DefaultFindFeature(session).find(file));
        final byte[] compare = new byte[content.length];
        final InputStream stream = new DeepboxReadFeature(session, nodeid).read(file, new TransferStatus().withLength(content.length), new DisabledConnectionCallback());
        IOUtils.readFully(stream, compare);
        stream.close();
        assertArrayEquals(content, compare);
        new DeepboxDeleteFeature(session, nodeid).delete(Collections.singletonList(file), new DisabledLoginCallback(), new Delete.DisabledCallback());
        new DeepboxDeleteFeature(session, nodeid).delete(Collections.singletonList(room), new DisabledLoginCallback(), new Delete.DisabledCallback());
    }

    @Test
    public void testWriteCancel() throws Exception {
        final DeepboxIdProvider nodeid = new DeepboxIdProvider(session);
        final Path documents = new Path("/ORG 4 - DeepBox Desktop App/ORG 4 - DeepBox Desktop App/ORG3:Box1/Documents/", EnumSet.of(Path.Type.directory, Path.Type.volume));
        final Path room = new DeepboxDirectoryFeature(session, nodeid).mkdir(
                new Path(documents, new AlphanumericRandomStringService().random(),
                        EnumSet.of(Path.Type.directory, Path.Type.volume)), new TransferStatus());

        final byte[] content = RandomUtils.nextBytes(32769);
        final Path test = new Path(room, String.format("{%s", new AlphanumericRandomStringService().random()), EnumSet.of(Path.Type.file));
        final BytecountStreamListener listener = new BytecountStreamListener();
        final TransferStatus status = new TransferStatus() {
            @Override
            public void validate() throws ConnectionCanceledException {
                if(listener.getSent() >= 32768) {
                    throw new TransferStatusCanceledException();
                }
                super.validate();
            }
        };
        status.setLength(content.length);
        final DeepboxWriteFeature writer = new DeepboxWriteFeature(session, nodeid);
        final HttpResponseOutputStream<Node> out = writer.write(test, status, new DisabledConnectionCallback());
        assertNotNull(out);
        assertThrows(TransferStatusCanceledException.class, () -> new StreamCopier(status, status).withListener(listener).transfer(new ByteArrayInputStream(content), out));
        assertFalse(new DefaultFindFeature(session).find(test));
        assertThrows(TransferStatusCanceledException.class, out::getStatus);
        new DeepboxDeleteFeature(session, nodeid).delete(Collections.singletonList(room), new DisabledLoginCallback(), new Delete.DisabledCallback());
    }
}