package ch.cyberduck.core.sftp;

/*
 * Copyright (c) 2002-2026 iterate GmbH. All rights reserved.
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
import ch.cyberduck.core.ConnectionCallback;
import ch.cyberduck.core.DisabledConnectionCallback;
import ch.cyberduck.core.DisabledLoginCallback;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.features.Copy;
import ch.cyberduck.core.features.Delete;
import ch.cyberduck.core.io.DisabledStreamListener;
import ch.cyberduck.core.transfer.TransferStatus;

import org.junit.Test;

import java.util.Collections;
import java.util.EnumSet;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class SFTPCopyDataFeatureTest extends AbstractSFTPTest {

    @Test
    public void testCopy() throws Exception {
        final Path test = new Path(new SFTPHomeDirectoryService(session).find(), new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file));
        new SFTPTouchFeature(session).touch(new SFTPWriteFeature(session), test, new TransferStatus());
        final byte[] content = new byte[2048];
        new java.util.Random().nextBytes(content);
        final java.io.OutputStream out = new SFTPWriteFeature(session).write(test, new TransferStatus().setLength(content.length), ConnectionCallback.noop);
        out.write(content);
        out.close();
        final Path copy = new Path(new SFTPHomeDirectoryService(session).find(), new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file));
        final Path result = new SFTPCopyDataFeature(session).copy(test, copy, new TransferStatus(), new DisabledConnectionCallback(), new DisabledStreamListener());
        assertTrue(new SFTPFindFeature(session).find(test));
        assertTrue(new SFTPFindFeature(session).find(copy));
        assertEquals(content.length, result.attributes().getSize());
        new SFTPDeleteFeature(session).delete(java.util.Arrays.asList(test, copy), new DisabledLoginCallback(), new Delete.DisabledCallback());
    }

    @Test
    public void testFeatureSelection() {
        final Copy feature = session.getFeature(Copy.class);
        assertNotNull(feature);
        assertTrue(feature instanceof SFTPCopyDataFeature);
    }
}
