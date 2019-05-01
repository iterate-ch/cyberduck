package ch.cyberduck.core.storegate;

/*
 * Copyright (c) 2002-2019 iterate GmbH. All rights reserved.
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
import ch.cyberduck.core.DisabledConnectionCallback;
import ch.cyberduck.core.DisabledLoginCallback;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.VersionId;
import ch.cyberduck.core.exception.TransferCanceledException;
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

@Category(IntegrationTest.class)
public class StoregateWriteFeatureTest extends AbstractStoregateTest {

    @Test
    public void testReadWrite() throws Exception {
        final StoregateIdProvider nodeid = new StoregateIdProvider(session).withCache(cache);
        final Path room = new StoregateDirectoryFeature(session, nodeid).mkdir(
            new Path(String.format("/Home/mduck/%s", new AlphanumericRandomStringService().random()),
                EnumSet.of(Path.Type.directory, Path.Type.volume)), null, new TransferStatus());
        final byte[] content = RandomUtils.nextBytes(32769);
        final Path test = new Path(room, String.format("%s", new AlphanumericRandomStringService().random()), EnumSet.of(Path.Type.file));
        final VersionId version;
        {
            final TransferStatus status = new TransferStatus();
            status.setLength(content.length);
            final StoregateWriteFeature writer = new StoregateWriteFeature(session, nodeid);
            final HttpResponseOutputStream<VersionId> out = writer.write(test, status, new DisabledConnectionCallback());
            assertNotNull(out);
            new StreamCopier(status, status).transfer(new ByteArrayInputStream(content), out);
            version = out.getStatus();
        }
        assertNotNull(version);
        assertTrue(new DefaultFindFeature(session).find(test));
        final byte[] compare = new byte[content.length];
        final InputStream stream = new StoregateReadFeature(session, nodeid).read(test, new TransferStatus().length(content.length), new DisabledConnectionCallback());
        IOUtils.readFully(stream, compare);
        stream.close();
        assertArrayEquals(content, compare);
        // Overwrite
        {
            final byte[] change = RandomUtils.nextBytes(256);
            final TransferStatus status = new TransferStatus();
            status.setLength(change.length);
            final StoregateWriteFeature writer = new StoregateWriteFeature(session, nodeid);
            final HttpResponseOutputStream<VersionId> out = writer.write(test, status.exists(true), new DisabledConnectionCallback());
            assertNotNull(out);
            new StreamCopier(status, status).transfer(new ByteArrayInputStream(change), out);
        }
        new StoregateDeleteFeature(session, nodeid).delete(Collections.singletonList(room), new DisabledLoginCallback(), new Delete.DisabledCallback());
    }

    @Test(expected = TransferCanceledException.class)
    public void testWriteCancel() throws Exception {
        final StoregateIdProvider nodeid = new StoregateIdProvider(session).withCache(cache);
        final Path room = new StoregateDirectoryFeature(session, nodeid).mkdir(
            new Path(String.format("/Home/mduck/%s", new AlphanumericRandomStringService().random()),
                EnumSet.of(Path.Type.directory, Path.Type.volume)), null, new TransferStatus());

        final byte[] content = RandomUtils.nextBytes(32769);
        final Path test = new Path(room, String.format("{%s", new AlphanumericRandomStringService().random()), EnumSet.of(Path.Type.file));
        final TransferStatus status = new TransferStatus() {
            @Override
            public boolean isCanceled() {
                if(this.getOffset() >= 32768) {
                    return true;
                }
                return super.isCanceled();
            }
        };
        status.setLength(content.length);
        final StoregateWriteFeature writer = new StoregateWriteFeature(session, nodeid);
        final HttpResponseOutputStream<VersionId> out = writer.write(test, status, new DisabledConnectionCallback());
        assertNotNull(out);
        new StreamCopier(status, status).transfer(new ByteArrayInputStream(content), out);
        assertFalse(new DefaultFindFeature(session).find(test));
        out.getStatus();
    }
}
