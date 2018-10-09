package ch.cyberduck.core.b2;

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

import ch.cyberduck.core.DisabledConnectionCallback;
import ch.cyberduck.core.DisabledListProgressListener;
import ch.cyberduck.core.DisabledLoginCallback;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathAttributes;
import ch.cyberduck.core.PathCache;
import ch.cyberduck.core.exception.ChecksumException;
import ch.cyberduck.core.features.Delete;
import ch.cyberduck.core.features.Write;
import ch.cyberduck.core.http.HttpResponseOutputStream;
import ch.cyberduck.core.io.Checksum;
import ch.cyberduck.core.io.SHA1ChecksumCompute;
import ch.cyberduck.core.io.StreamCopier;
import ch.cyberduck.core.transfer.TransferStatus;
import ch.cyberduck.test.IntegrationTest;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.RandomUtils;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Collections;
import java.util.EnumSet;
import java.util.UUID;

import synapticloop.b2.response.BaseB2Response;

import static org.junit.Assert.*;

@Category(IntegrationTest.class)
public class B2WriteFeatureTest extends AbstractB2Test {

    @Test
    public void testWriteChecksumFailure() throws Exception {
        final Path bucket = new Path("test-cyberduck", EnumSet.of(Path.Type.directory, Path.Type.volume));
        final Path file = new Path(bucket, UUID.randomUUID().toString(), EnumSet.of(Path.Type.file));
        final TransferStatus status = new TransferStatus();
        final byte[] content = RandomUtils.nextBytes(1);
        status.setLength(content.length);
        status.setChecksum(Checksum.parse("da39a3ee5e6b4b0d3255bfef95601890afd80709"));
        final HttpResponseOutputStream<BaseB2Response> out = new B2WriteFeature(session, new B2FileidProvider(session).withCache(cache)).write(file, status, new DisabledConnectionCallback());
        IOUtils.write(content, out);
        try {
            out.close();
            fail();
        }
        catch(IOException e) {
            assertTrue(e.getCause() instanceof ChecksumException);
        }
    }

    @Test
    public void testWrite() throws Exception {
        final Path bucket = new Path("test-cyberduck", EnumSet.of(Path.Type.directory, Path.Type.volume));
        final Path test = new Path(bucket, UUID.randomUUID().toString(), EnumSet.of(Path.Type.file));
        final TransferStatus status = new TransferStatus();
        final byte[] content = "test".getBytes("UTF-8");
        status.setLength(content.length);
        status.setChecksum(new SHA1ChecksumCompute().compute(new ByteArrayInputStream(content), status));
        status.setTimestamp(1503654614004L);
        final B2FileidProvider fileid = new B2FileidProvider(session).withCache(cache);
        final OutputStream out = new B2WriteFeature(session, fileid).write(test, status, new DisabledConnectionCallback());
        assertNotNull(out);
        new StreamCopier(new TransferStatus(), new TransferStatus()).transfer(new ByteArrayInputStream(content), out);
        out.close();
        test.attributes().setVersionId(fileid.getFileid(test, new DisabledListProgressListener()));
        assertTrue(new B2FindFeature(session, fileid).find(test, new DisabledListProgressListener()));
        final PathAttributes attributes = new B2ListService(session, fileid).list(test.getParent(), new DisabledListProgressListener()).get(test).attributes();
        assertEquals(content.length, attributes.getSize());
        final Write.Append append = new B2WriteFeature(session, fileid).append(test, status.getLength(), PathCache.empty(), new DisabledListProgressListener());
        assertTrue(append.override);
        assertEquals(content.length, append.size, 0L);
        final byte[] buffer = new byte[content.length];
        final InputStream in = new B2ReadFeature(session, fileid).read(test, new TransferStatus(), new DisabledConnectionCallback());
        IOUtils.readFully(in, buffer);
        in.close();
        assertArrayEquals(content, buffer);
        assertEquals(1503654614004L, new B2AttributesFinderFeature(session, fileid).find(test, new DisabledListProgressListener()).getModificationDate());
        new B2DeleteFeature(session, fileid).delete(Collections.singletonList(test), new DisabledLoginCallback(), new Delete.DisabledCallback());
    }
}
