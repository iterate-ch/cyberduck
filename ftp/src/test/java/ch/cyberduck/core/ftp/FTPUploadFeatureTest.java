package ch.cyberduck.core.ftp;

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
import ch.cyberduck.core.DisabledLoginCallback;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.features.Delete;
import ch.cyberduck.core.transfer.TransferStatus;

import org.junit.Test;

import java.util.Collections;
import java.util.EnumSet;

import static org.junit.Assert.assertTrue;

public class FTPUploadFeatureTest extends AbstractFTPTest {

    @Test
    public void testAppend() throws Exception {
        final Path f = new Path(new FTPWorkdirService(session).find(), new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file));
        new FTPTouchFeature(session).touch(f, new TransferStatus());
        assertTrue(new FTPUploadFeature(session).append(f, new TransferStatus().exists(true).withLength(0L).withRemote(new FTPAttributesFinderFeature(session).find(f))).append);
        new FTPDeleteFeature(session).delete(Collections.singletonList(f), new DisabledLoginCallback(), new Delete.DisabledCallback());
    }
}