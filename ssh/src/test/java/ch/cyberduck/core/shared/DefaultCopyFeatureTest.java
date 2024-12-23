package ch.cyberduck.core.shared;

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
import ch.cyberduck.core.features.Delete;
import ch.cyberduck.core.io.DisabledStreamListener;
import ch.cyberduck.core.io.StreamCopier;
import ch.cyberduck.core.sftp.AbstractSFTPTest;
import ch.cyberduck.core.sftp.SFTPDeleteFeature;
import ch.cyberduck.core.sftp.SFTPHomeDirectoryService;
import ch.cyberduck.core.sftp.SFTPTouchFeature;
import ch.cyberduck.core.sftp.SFTPWriteFeature;
import ch.cyberduck.core.transfer.TransferStatus;
import ch.cyberduck.test.IntegrationTest;

import org.apache.commons.lang3.RandomUtils;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.io.ByteArrayInputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.Optional;

import static org.junit.Assert.*;

@Category(IntegrationTest.class)
public class DefaultCopyFeatureTest extends AbstractSFTPTest {

    @Test
    public void testSupported() throws Exception {
        final Path source = new Path(new DefaultHomeFinderService(session).find(), new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file));
        final Path target = new Path(new DefaultHomeFinderService(session).find(), new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file));
        assertTrue(new DefaultCopyFeature(session).isSupported(source, Optional.of(target)));
    }

    @Test
    public void testCopy() throws Exception {
        final Path source = new Path(new SFTPHomeDirectoryService(session).find(), new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file));
        final Path target = new Path(new SFTPHomeDirectoryService(session).find(), new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file));
        new SFTPTouchFeature(session).touch(source, new TransferStatus());
        final byte[] content = RandomUtils.nextBytes(524);
        final TransferStatus status = new TransferStatus().withLength(content.length);
        final OutputStream out = new SFTPWriteFeature(session).write(source, status, new DisabledConnectionCallback());
        assertNotNull(out);
        new StreamCopier(status, status).withLimit(new Long(content.length)).transfer(new ByteArrayInputStream(content), out);
        out.close();
        new DefaultCopyFeature(session).copy(source, target, new TransferStatus(), new DisabledConnectionCallback(), new DisabledStreamListener());
        assertTrue(new DefaultFindFeature(session).find(source));
        assertTrue(new DefaultFindFeature(session).find(target));
        assertEquals(content.length, new DefaultAttributesFinderFeature(session).find(target).getSize());
        new SFTPDeleteFeature(session).delete(Arrays.asList(source, target), new DisabledLoginCallback(), new Delete.DisabledCallback());
    }
}
