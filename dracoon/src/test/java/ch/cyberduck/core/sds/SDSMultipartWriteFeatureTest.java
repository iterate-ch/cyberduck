package ch.cyberduck.core.sds;

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

import ch.cyberduck.core.AlphanumericRandomStringService;
import ch.cyberduck.core.DisabledConnectionCallback;
import ch.cyberduck.core.DisabledLoginCallback;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathAttributes;
import ch.cyberduck.core.VersionId;
import ch.cyberduck.core.exception.ConnectionCanceledException;
import ch.cyberduck.core.exception.InteroperabilityException;
import ch.cyberduck.core.exception.NotfoundException;
import ch.cyberduck.core.exception.TransferCanceledException;
import ch.cyberduck.core.features.Delete;
import ch.cyberduck.core.http.HttpResponseOutputStream;
import ch.cyberduck.core.io.MD5ChecksumCompute;
import ch.cyberduck.core.io.StreamCopier;
import ch.cyberduck.core.shared.DefaultFindFeature;
import ch.cyberduck.core.transfer.TransferStatus;
import ch.cyberduck.core.unicode.NFDNormalizer;
import ch.cyberduck.test.IntegrationTest;

import org.apache.commons.io.IOUtils;
import org.apache.commons.io.input.NullInputStream;
import org.apache.commons.lang3.RandomUtils;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Collections;
import java.util.EnumSet;
import java.util.UUID;

import static org.junit.Assert.*;

@Category(IntegrationTest.class)
public class SDSMultipartWriteFeatureTest extends AbstractSDSTest {

    @Test
    public void testWriteUnknownContentLength() throws Exception {
        final SDSNodeIdProvider nodeid = new SDSNodeIdProvider(session);
        final Path room = new SDSDirectoryFeature(session, nodeid).mkdir(
            new Path(new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory, Path.Type.volume, Path.Type.triplecrypt)), null, new TransferStatus());
        final byte[] content = RandomUtils.nextBytes(32769);
        final TransferStatus status = new TransferStatus();
        status.setLength(-1L);
        status.setMime("text/plain");
        final Path test = new Path(room, UUID.randomUUID().toString(), EnumSet.of(Path.Type.file));
        final SDSMultipartWriteFeature writer = new SDSMultipartWriteFeature(session, nodeid);
        final HttpResponseOutputStream<VersionId> out = writer.write(test, status, new DisabledConnectionCallback());
        assertNotNull(out);
        new StreamCopier(status, status).transfer(new ByteArrayInputStream(content), out);
        final VersionId version = out.getStatus();
        assertNotNull(version);
        assertTrue(new DefaultFindFeature(session).find(test));
        final byte[] compare = new byte[content.length];
        final InputStream stream = new SDSReadFeature(session, nodeid).read(test, new TransferStatus().length(content.length), new DisabledConnectionCallback());
        IOUtils.readFully(stream, compare);
        stream.close();
        assertArrayEquals(content, compare);
        new SDSDeleteFeature(session, nodeid).delete(Collections.singletonList(room), new DisabledLoginCallback(), new Delete.DisabledCallback());
    }

    @Test
    public void testReadWrite() throws Exception {
        final SDSNodeIdProvider nodeid = new SDSNodeIdProvider(session);
        final Path room = new SDSDirectoryFeature(session, nodeid).mkdir(
            new Path(new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory, Path.Type.volume, Path.Type.triplecrypt)), null, new TransferStatus());
        final byte[] content = RandomUtils.nextBytes(32769);
        final Path test = new Path(room, new NFDNormalizer().normalize(String.format("ä%s", new AlphanumericRandomStringService().random())).toString(), EnumSet.of(Path.Type.file));
        final VersionId version;
        {
            final SDSMultipartWriteFeature writer = new SDSMultipartWriteFeature(session, nodeid);
            final TransferStatus status = new TransferStatus();
            status.setLength(content.length);
            status.setChecksum(new MD5ChecksumCompute().compute(new ByteArrayInputStream(content), new TransferStatus()));
            status.setMime("text/plain");
            status.setTimestamp(System.currentTimeMillis());
            final HttpResponseOutputStream<VersionId> out = writer.write(test, status, new DisabledConnectionCallback());
            assertNotNull(out);
            new StreamCopier(status, status).transfer(new ByteArrayInputStream(content), out);
            version = out.getStatus();
        }
        assertNotNull(version);
        assertTrue(new DefaultFindFeature(session).find(test));
        assertTrue(new SDSFindFeature(nodeid).find(test));
        assertEquals(version.id, new SDSAttributesFinderFeature(session, nodeid).find(test).getVersionId());
        final byte[] compare = new byte[content.length];
        final InputStream stream = new SDSReadFeature(session, nodeid).read(test, new TransferStatus().length(content.length), new DisabledConnectionCallback());
        IOUtils.readFully(stream, compare);
        stream.close();
        assertArrayEquals(content, compare);
        // Overwrite
        {
            final byte[] change = RandomUtils.nextBytes(256);
            final TransferStatus status = new TransferStatus();
            status.setLength(change.length);
            final SDSMultipartWriteFeature writer = new SDSMultipartWriteFeature(session, nodeid);
            final HttpResponseOutputStream<VersionId> out = writer.write(test, status.exists(true), new DisabledConnectionCallback());
            assertNotNull(out);
            new StreamCopier(status, status).transfer(new ByteArrayInputStream(change), out);
            assertNotEquals(version, out.getStatus());
            new SDSReadFeature(session, nodeid).read(test.withAttributes(new PathAttributes().withVersionId(out.getStatus().id)), new TransferStatus(), new DisabledConnectionCallback()).close();
        }
        // Read with previous version must fail
        try {
            new SDSReadFeature(session, nodeid).read(new Path(test.getAbsolute(), test.getType(), new PathAttributes().withVersionId(version.id)), new TransferStatus(), new DisabledConnectionCallback());
            fail();
        }
        catch(NotfoundException e) {
            // Expected
        }
        new SDSDeleteFeature(session, nodeid).delete(Collections.singletonList(room), new DisabledLoginCallback(), new Delete.DisabledCallback());
    }

    @Test(expected = TransferCanceledException.class)
    public void testWriteCancel() throws Exception {
        final SDSNodeIdProvider nodeid = new SDSNodeIdProvider(session);
        final Path room = new SDSDirectoryFeature(session, nodeid).mkdir(
            new Path(new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory, Path.Type.volume, Path.Type.triplecrypt)), null, new TransferStatus());
        final byte[] content = RandomUtils.nextBytes(32769);
        final Path test = new Path(room, String.format("{%s", new AlphanumericRandomStringService().random()), EnumSet.of(Path.Type.file));
        final TransferStatus status = new TransferStatus() {
            @Override
            public void validate() throws ConnectionCanceledException {
                if(this.getOffset() >= 32768) {
                    throw new TransferCanceledException();
                }
                super.validate();
            }
        };
        status.setLength(content.length);
        final SDSMultipartWriteFeature writer = new SDSMultipartWriteFeature(session, nodeid);
        final HttpResponseOutputStream<VersionId> out = writer.write(test, status, new DisabledConnectionCallback());
        assertNotNull(out);
        new StreamCopier(status, status).transfer(new ByteArrayInputStream(content), out);
        assertFalse(new DefaultFindFeature(session).find(test));
        out.getStatus();
    }

    @Test(expected = InteroperabilityException.class)
    public void testWriteRoot() throws Exception {
        final SDSNodeIdProvider nodeid = new SDSNodeIdProvider(session);
        final Path test = new Path(UUID.randomUUID().toString(), EnumSet.of(Path.Type.file));
        final TransferStatus status = new TransferStatus();
        final SDSMultipartWriteFeature writer = new SDSMultipartWriteFeature(session, nodeid);
        final HttpResponseOutputStream<VersionId> out = writer.write(test, status, new DisabledConnectionCallback());
    }

    @Test
    public void testWriteZeroSingleByte() throws Exception {
        final SDSNodeIdProvider nodeid = new SDSNodeIdProvider(session);
        final Path room = new SDSDirectoryFeature(session, nodeid).mkdir(
            new Path(new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory, Path.Type.volume, Path.Type.triplecrypt)), null, new TransferStatus());
        final byte[] content = RandomUtils.nextBytes(2);
        final TransferStatus status = new TransferStatus().length(content.length);
        final Path test = new Path(room, UUID.randomUUID().toString(), EnumSet.of(Path.Type.file));
        final SDSMultipartWriteFeature writer = new SDSMultipartWriteFeature(session, nodeid);
        final HttpResponseOutputStream<VersionId> out = writer.write(test, status, new DisabledConnectionCallback());
        assertNotNull(out);
        new StreamCopier(status, status).transfer(new ByteArrayInputStream(content), out);
        final VersionId version = out.getStatus();
        assertNotNull(version);
        assertTrue(new DefaultFindFeature(session).find(test));
        new SDSDeleteFeature(session, nodeid).delete(Collections.singletonList(room), new DisabledLoginCallback(), new Delete.DisabledCallback());
    }

    @Test
    public void testWriteZeroLength() throws Exception {
        final SDSNodeIdProvider nodeid = new SDSNodeIdProvider(session);
        final Path room = new SDSDirectoryFeature(session, nodeid).mkdir(
            new Path(new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory, Path.Type.volume, Path.Type.triplecrypt)), null, new TransferStatus());
        final TransferStatus status = new TransferStatus();
        final Path test = new Path(room, UUID.randomUUID().toString(), EnumSet.of(Path.Type.file));
        final SDSMultipartWriteFeature writer = new SDSMultipartWriteFeature(session, nodeid);
        final HttpResponseOutputStream<VersionId> out = writer.write(test, status, new DisabledConnectionCallback());
        assertNotNull(out);
        new StreamCopier(status, status).transfer(new NullInputStream(0L), out);
        final VersionId version = out.getStatus();
        assertNotNull(version);
        assertTrue(new DefaultFindFeature(session).find(test));
        new SDSDeleteFeature(session, nodeid).delete(Collections.singletonList(room), new DisabledLoginCallback(), new Delete.DisabledCallback());
    }
}
