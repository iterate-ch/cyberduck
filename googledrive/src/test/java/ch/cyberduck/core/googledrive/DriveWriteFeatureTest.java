package ch.cyberduck.core.googledrive;

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
import ch.cyberduck.core.features.Delete;
import ch.cyberduck.core.features.Write;
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
import java.io.OutputStream;
import java.util.Collections;
import java.util.EnumSet;
import java.util.UUID;

import static org.junit.Assert.*;

@Category(IntegrationTest.class)
public class DriveWriteFeatureTest extends AbstractDriveTest {

    @Test
    public void testWrite() throws Exception {
        final Path test = new Path(DriveHomeFinderService.MYDRIVE_FOLDER, UUID.randomUUID().toString(), EnumSet.of(Path.Type.file));
        final DriveFileidProvider fileid = new DriveFileidProvider(session);
        {
            final TransferStatus status = new TransferStatus();
            status.setMime("x-application/cyberduck");
            final byte[] content = RandomUtils.nextBytes(2048);
            status.setLength(content.length);
            final OutputStream out = new DriveWriteFeature(session, fileid).write(test, status, new DisabledConnectionCallback());
            assertNotNull(out);
            new StreamCopier(new TransferStatus(), new TransferStatus()).transfer(new ByteArrayInputStream(content), out);
            out.close();
            test.attributes().setVersionId(fileid.getFileid(test, new DisabledListProgressListener()));
            assertTrue(new DefaultFindFeature(session).find(test));
            final PathAttributes attributes = session.list(test.getParent(), new DisabledListProgressListener()).get(test).attributes();
            assertEquals(content.length, attributes.getSize());
            final Write.Append append = new DriveWriteFeature(session, fileid).append(test, status.getLength(), PathCache.empty());
            assertTrue(append.override);
            assertEquals(content.length, append.size, 0L);
            final byte[] buffer = new byte[content.length];
            final InputStream in = new DriveReadFeature(session, fileid).read(test, new TransferStatus(), new DisabledConnectionCallback());
            IOUtils.readFully(in, buffer);
            in.close();
            assertArrayEquals(content, buffer);
            assertEquals("x-application/cyberduck", session.getClient().files().get(test.attributes().getVersionId()).execute().getMimeType());
        }
        {
            final TransferStatus status = new TransferStatus();
            status.setMime("x-application/cyberduck");
            status.setExists(true);
            final byte[] content = RandomUtils.nextBytes(1024);
            status.setLength(content.length);
            final OutputStream out = new DriveWriteFeature(session, fileid).write(test, status, new DisabledConnectionCallback());
            assertNotNull(out);
            new StreamCopier(new TransferStatus(), new TransferStatus()).transfer(new ByteArrayInputStream(content), out);
            out.close();
            final PathAttributes attributes = session.list(test.getParent(), new DisabledListProgressListener()).get(test).attributes();
            assertEquals(content.length, attributes.getSize());
            assertEquals("x-application/cyberduck", session.getClient().files().get(test.attributes().getVersionId()).execute().getMimeType());
        }
        new DriveDeleteFeature(session, fileid).delete(Collections.singletonList(test), new DisabledLoginCallback(), new Delete.DisabledCallback());
    }
}
