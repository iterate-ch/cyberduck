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
import ch.cyberduck.core.Permission;
import ch.cyberduck.core.StaticPermission;
import ch.cyberduck.core.exception.ConflictException;
import ch.cyberduck.core.features.Delete;
import ch.cyberduck.core.io.DisabledStreamListener;
import ch.cyberduck.core.transfer.TransferStatus;

import org.junit.Test;

import java.util.EnumSet;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;
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
    public void testCopyAttributesFromStatus() throws Exception {
        final Path test = new Path(new SFTPHomeDirectoryService(session).find(), new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file));
        new SFTPTouchFeature(session).touch(new SFTPWriteFeature(session), test, new TransferStatus());
        final Permission permission = new StaticPermission("750");
        final long modified = 1_700_000_000_000L;
        final Path copy = new Path(new SFTPHomeDirectoryService(session).find(), new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file));
        final Path result = new SFTPCopyDataFeature(session).copy(test, copy, new TransferStatus().setPermission(permission).setModified(modified), new DisabledConnectionCallback(), new DisabledStreamListener());
        assertEquals(permission, result.attributes().getPermission());
        assertEquals(modified, result.attributes().getModificationDate());
        assertEquals(permission, new SFTPAttributesFinderFeature(session).find(copy).getPermission());
        new SFTPDeleteFeature(session).delete(java.util.Arrays.asList(test, copy), new DisabledLoginCallback(), new Delete.DisabledCallback());
    }

    @Test
    public void testCopyTargetExistsStatusNotSet() throws Exception {
        final Path test = new Path(new SFTPHomeDirectoryService(session).find(), new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file));
        new SFTPTouchFeature(session).touch(new SFTPWriteFeature(session), test, new TransferStatus());
        final Path copy = new Path(new SFTPHomeDirectoryService(session).find(), new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file));
        new SFTPTouchFeature(session).touch(new SFTPWriteFeature(session), copy, new TransferStatus());
        // Target exists but exists flag is not set
        assertThrows(ConflictException.class, () -> new SFTPCopyDataFeature(session).copy(test, copy, new TransferStatus(), new DisabledConnectionCallback(), new DisabledStreamListener()));
        new SFTPDeleteFeature(session).delete(java.util.Arrays.asList(test, copy), new DisabledLoginCallback(), new Delete.DisabledCallback());
    }
}
