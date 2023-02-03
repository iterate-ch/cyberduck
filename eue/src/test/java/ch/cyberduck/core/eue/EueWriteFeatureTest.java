package ch.cyberduck.core.eue;

/*
 * Copyright (c) 2002-2021 iterate GmbH. All rights reserved.
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

import ch.cyberduck.core.AbstractPath;
import ch.cyberduck.core.AlphanumericRandomStringService;
import ch.cyberduck.core.BytecountStreamListener;
import ch.cyberduck.core.DisabledConnectionCallback;
import ch.cyberduck.core.DisabledLoginCallback;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathAttributes;
import ch.cyberduck.core.eue.io.swagger.client.model.UploadType;
import ch.cyberduck.core.exception.ConnectionCanceledException;
import ch.cyberduck.core.exception.QuotaException;
import ch.cyberduck.core.exception.TransferCanceledException;
import ch.cyberduck.core.exception.TransferStatusCanceledException;
import ch.cyberduck.core.features.Delete;
import ch.cyberduck.core.http.HttpResponseOutputStream;
import ch.cyberduck.core.io.Checksum;
import ch.cyberduck.core.io.StatusOutputStream;
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

@Category(IntegrationTest.class)
public class EueWriteFeatureTest extends AbstractEueSessionTest {

    @Test
    public void testMissingChecksum() throws Exception {
        final EueResourceIdProvider fileid = new EueResourceIdProvider(session);
        final EueWriteFeature feature = new EueWriteFeature(session, fileid);
        final Path file = new Path(new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file));
        final byte[] content = RandomUtils.nextBytes(8235);
        final TransferStatus status = new TransferStatus().withLength(content.length);
        status.withChecksum(Checksum.NONE);
        final HttpResponseOutputStream<EueWriteFeature.Chunk> out = feature.write(file, status, new DisabledConnectionCallback());
        final ByteArrayInputStream in = new ByteArrayInputStream(content);
        final TransferStatus progress = new TransferStatus();
        final BytecountStreamListener count = new BytecountStreamListener();
        new StreamCopier(new TransferStatus(), progress).withListener(count).transfer(in, out);
        assertEquals(content.length, count.getSent());
        in.close();
        out.close();
        new EueDeleteFeature(session, fileid).delete(Collections.singletonList(file), new DisabledLoginCallback(), new Delete.DisabledCallback());
    }

    @Test(expected = TransferStatusCanceledException.class)
    public void testWriteCancel() throws Exception {
        final EueResourceIdProvider fileid = new EueResourceIdProvider(session);
        final EueWriteFeature writer = new EueWriteFeature(session, fileid);
        final byte[] content = RandomUtils.nextBytes(32769);
        final Path test = new Path(String.format("{%s", new AlphanumericRandomStringService().random()), EnumSet.of(Path.Type.file));
        {
            final BytecountStreamListener count = new BytecountStreamListener();
            final TransferStatus status = new TransferStatus() {
                @Override
                public void validate() throws ConnectionCanceledException {
                    if(count.getSent() >= 32768) {
                        throw new TransferStatusCanceledException();
                    }
                    super.validate();
                }
            };
            status.setLength(content.length);
            final StatusOutputStream<EueWriteFeature.Chunk> out = writer.write(test, status, new DisabledConnectionCallback());
            assertNotNull(out);
            new StreamCopier(status, status).withListener(count).transfer(new ByteArrayInputStream(content), out);
            assertFalse(new DefaultFindFeature(session).find(test));
            try {
                out.getStatus();
                fail();
            }
            catch(TransferCanceledException e) {
                //
            }
        }
        // Rewrite
        final TransferStatus status = new TransferStatus();
        status.setLength(content.length);
        final StatusOutputStream<EueWriteFeature.Chunk> out = writer.write(test, status, new DisabledConnectionCallback());
        assertNotNull(out);
        new StreamCopier(status, status).transfer(new ByteArrayInputStream(content), out);
        assertFalse(new DefaultFindFeature(session).find(test));
        new EueDeleteFeature(session, fileid).delete(Collections.singletonList(test), new DisabledLoginCallback(), new Delete.DisabledCallback());
    }

    @Test
    public void testWrite() throws Exception {
        final EueResourceIdProvider fileid = new EueResourceIdProvider(session);
        final EueWriteFeature feature = new EueWriteFeature(session, fileid);
        final Path container = new EueDirectoryFeature(session, fileid).mkdir(new Path(new AlphanumericRandomStringService().random(), EnumSet.of(AbstractPath.Type.directory)), new TransferStatus());
        long containerModification = new EueAttributesFinderFeature(session, fileid).find(container).getModificationDate();
        final Path file = new Path(container, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file));
        String resourceId;
        {
            final byte[] content = RandomUtils.nextBytes(8235);
            final long ts = System.currentTimeMillis();
            final TransferStatus status = new TransferStatus().withLength(content.length).withTimestamp(ts);
            final Checksum checksum = feature.checksum(file, status).compute(new ByteArrayInputStream(content), new TransferStatus().withLength(content.length));
            status.withChecksum(checksum);
            final HttpResponseOutputStream<EueWriteFeature.Chunk> out = feature.write(file, status, new DisabledConnectionCallback());
            final ByteArrayInputStream in = new ByteArrayInputStream(content);
            final TransferStatus progress = new TransferStatus();
            final BytecountStreamListener count = new BytecountStreamListener();
            new StreamCopier(new TransferStatus(), progress).withListener(count).transfer(in, out);
            assertEquals(content.length, count.getSent());
            in.close();
            out.close();
            assertEquals(checksum, out.getStatus().getChecksum());
            assertNotEquals(containerModification, new EueAttributesFinderFeature(session, fileid).find(container).getModificationDate());
            resourceId = status.getResponse().getFileId();
            assertTrue(new EueFindFeature(session, fileid).find(file));
            final PathAttributes attributes = new EueAttributesFinderFeature(session, fileid).find(file);
            assertEquals(content.length, attributes.getSize());
            assertEquals(ts, attributes.getModificationDate());
            final byte[] compare = new byte[content.length];
            final InputStream stream = new EueReadFeature(session, fileid).read(file, new TransferStatus().withLength(content.length), new DisabledConnectionCallback());
            IOUtils.readFully(stream, compare);
            stream.close();
            assertArrayEquals(content, compare);
        }
        containerModification = new EueAttributesFinderFeature(session, fileid).find(container).getModificationDate();
        // Override
        {
            final PathAttributes previous = new EueAttributesFinderFeature(session, fileid).find(file);
            final byte[] content = RandomUtils.nextBytes(6231);
            final long ts = System.currentTimeMillis();
            final TransferStatus status = new TransferStatus().withLength(content.length).withTimestamp(ts);
            final Checksum checksum = feature.checksum(file, status).compute(new ByteArrayInputStream(content), new TransferStatus().withLength(content.length));
            status.withChecksum(checksum).exists(true);
            final HttpResponseOutputStream<EueWriteFeature.Chunk> out = feature.write(file, status, new DisabledConnectionCallback());
            final ByteArrayInputStream in = new ByteArrayInputStream(content);
            final TransferStatus progress = new TransferStatus();
            final BytecountStreamListener count = new BytecountStreamListener();
            new StreamCopier(new TransferStatus(), progress).withListener(count).transfer(in, out);
            assertEquals(content.length, count.getSent());
            in.close();
            out.close();
            assertEquals(checksum, out.getStatus().getChecksum());
            assertTrue(new EueFindFeature(session, fileid).find(file));
            assertNotEquals(containerModification, new EueAttributesFinderFeature(session, fileid).find(container).getModificationDate());
            final PathAttributes attributes = new EueAttributesFinderFeature(session, fileid).find(file);
            assertNotEquals(previous, attributes);
            assertNotEquals(previous.getETag(), attributes.getETag());
            assertNotEquals(previous.getRevision(), attributes.getRevision());
            assertEquals(ts, attributes.getModificationDate());
            assertEquals(content.length, attributes.getSize());
            final byte[] compare = new byte[content.length];
            final InputStream stream = new EueReadFeature(session, fileid).read(file, new TransferStatus().withLength(content.length), new DisabledConnectionCallback());
            IOUtils.readFully(stream, compare);
            stream.close();
            assertArrayEquals(content, compare);
        }
        new EueDeleteFeature(session, fileid).delete(Collections.singletonList(container), new DisabledLoginCallback(), new Delete.DisabledCallback());
    }

    @Test
    public void testWriteZeroLength() throws Exception {
        final EueResourceIdProvider fileid = new EueResourceIdProvider(session);
        final EueWriteFeature feature = new EueWriteFeature(session, fileid);
        final byte[] content = RandomUtils.nextBytes(0);
        final Path file = new Path(new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file));
        final TransferStatus status = new TransferStatus().withLength(content.length);
        final Checksum checksum = feature.checksum(file, status).compute(new ByteArrayInputStream(content), new TransferStatus().withLength(content.length));
        status.withChecksum(checksum);
        final HttpResponseOutputStream<EueWriteFeature.Chunk> out = feature.write(file, status, new DisabledConnectionCallback());
        final ByteArrayInputStream in = new ByteArrayInputStream(content);
        assertEquals(content.length, IOUtils.copyLarge(in, out));
        in.close();
        out.close();
        assertTrue(new DefaultFindFeature(session).find(file));
        final PathAttributes attributes = new EueAttributesFinderFeature(session, fileid).find(file);
        assertEquals(content.length, attributes.getSize());
        final byte[] compare = new byte[content.length];
        final InputStream stream = new EueReadFeature(session, fileid).read(file, new TransferStatus().withLength(content.length), new DisabledConnectionCallback());
        IOUtils.readFully(stream, compare);
        stream.close();
        assertArrayEquals(content, compare);
        new EueDeleteFeature(session, fileid).delete(Collections.singletonList(file), new DisabledLoginCallback(), new Delete.DisabledCallback());
    }

    @Test
    public void testOverwriteShadowFile() throws Exception {
        // If the file is already created, but not completely uploaded yet, the entry can be overwritten by setting "forceOverwrite" to true.
        final EueResourceIdProvider fileid = new EueResourceIdProvider(session);
        final EueWriteFeature feature = new EueWriteFeature(session, fileid);
        final Path file = new Path(new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file));
        final byte[] content = RandomUtils.nextBytes(423);
        final TransferStatus status = new TransferStatus().withLength(content.length);
        EueUploadHelper.createResource(session, fileid.getFileId(file.getParent()), file.getName(), status, UploadType.SIMPLE);
        final Checksum checksum = feature.checksum(file, status).compute(new ByteArrayInputStream(content), new TransferStatus().withLength(content.length));
        status.withChecksum(checksum);
        final HttpResponseOutputStream<EueWriteFeature.Chunk> out = feature.write(file, status, new DisabledConnectionCallback());
        final ByteArrayInputStream in = new ByteArrayInputStream(content);
        assertEquals(content.length, IOUtils.copyLarge(in, out));
        in.close();
        out.close();
        assertTrue(new DefaultFindFeature(session).find(file));
        new EueDeleteFeature(session, fileid).delete(Collections.singletonList(file), new DisabledLoginCallback(), new Delete.DisabledCallback());
    }

    @Test(expected = QuotaException.class)
    public void testMaxResourceSizeFailure() throws Exception {
        // If the file is already created, but not completely uploaded yet, the entry can be overwritten by setting "forceOverwrite" to true.
        final EueResourceIdProvider fileid = new EueResourceIdProvider(session);
        final EueWriteFeature feature = new EueWriteFeature(session, fileid);
        final Path file = new Path(new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file));
        final TransferStatus status = new TransferStatus().withLength(Long.MAX_VALUE);
        try {
            feature.write(file, status, new DisabledConnectionCallback());
        }
        catch(QuotaException e) {
            assertEquals("LIMIT_MAX_RESOURCE_SIZE. Please contact your web hosting service provider for assistance.", e.getDetail());
            throw e;
        }
    }
}
