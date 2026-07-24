package ch.cyberduck.core.sftp;

/*
 * Copyright (c) 2002-2023 iterate GmbH. All rights reserved.
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
import ch.cyberduck.core.features.Delete;
import ch.cyberduck.core.io.DisabledStreamListener;
import ch.cyberduck.core.transfer.TransferStatus;

import org.junit.Test;

import java.util.Collections;
import java.util.EnumSet;

import static org.junit.Assert.assertTrue;

public class SFTPCopyFeatureTest extends AbstractSFTPTest {

    @Test
    public void testCopy() throws Exception {
        final Path test = new Path(new SFTPHomeDirectoryService(session).find(), new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file));
        new SFTPTouchFeature(session).touch(test, new TransferStatus());
        final Path copy = new Path(new SFTPHomeDirectoryService(session).find(), new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file));
        new SFTPCopyFeature(session).copy(test, copy, new TransferStatus(), new DisabledConnectionCallback(), new DisabledStreamListener());
        assertTrue(new SFTPFindFeature(session).find(test));
        assertTrue(new SFTPFindFeature(session).find(copy));
        new SFTPDeleteFeature(session).delete(Collections.<Path>singletonList(test), new DisabledLoginCallback(), new Delete.DisabledCallback());
        new SFTPDeleteFeature(session).delete(Collections.<Path>singletonList(copy), new DisabledLoginCallback(), new Delete.DisabledCallback());
    }
}