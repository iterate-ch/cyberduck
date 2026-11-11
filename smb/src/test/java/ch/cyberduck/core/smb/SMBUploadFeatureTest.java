package ch.cyberduck.core.smb;

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
import ch.cyberduck.core.LoginCallback;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.features.Delete;
import ch.cyberduck.core.features.Upload;
import ch.cyberduck.core.features.Write;
import ch.cyberduck.core.shared.DefaultHomeFinderService;
import ch.cyberduck.core.transfer.TransferStatus;
import ch.cyberduck.test.TestcontainerTest;

import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.util.Collections;
import java.util.EnumSet;

import static org.junit.Assert.*;

@Category(TestcontainerTest.class)
public class SMBUploadFeatureTest extends AbstractSMBTest {

    @Test
    public void testAppend() throws Exception {
        final Path home = new DefaultHomeFinderService(session).find();
        final Path test = new Path(home, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file));
        new SMBTouchFeature(session).touch(new SMBWriteFeature(session), test, new TransferStatus());
        assertTrue(session._getFeature(Upload.class) instanceof SMBUploadFeature);
        final SMBUploadFeature feature = new SMBUploadFeature();
        final Write.Append append = feature.append(test, new TransferStatus().setExists(true).setLength(1L)
                .setRemote(new SMBAttributesFinderFeature(session).find(test)));
        assertTrue(append.append);
        assertEquals(0L, append.offset.longValue());
        assertFalse(feature.append(test, new TransferStatus().setExists(false).setLength(1L)).append);
        new SMBDeleteFeature(session).delete(Collections.singletonList(test), LoginCallback.noop, new Delete.DisabledCallback());
    }
}
