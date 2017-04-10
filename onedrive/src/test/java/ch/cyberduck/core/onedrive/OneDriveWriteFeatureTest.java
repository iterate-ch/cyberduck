package ch.cyberduck.core.onedrive;

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
import ch.cyberduck.core.http.HttpResponseOutputStream;
import ch.cyberduck.core.shared.DefaultFindFeature;
import ch.cyberduck.core.transfer.TransferStatus;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Collections;
import java.util.EnumSet;

import static org.junit.Assert.*;

public class OneDriveWriteFeatureTest extends AbstractOneDriveTest {

    @Test
    public void testWrite() throws Exception {
        final OneDriveWriteFeature feature = new OneDriveWriteFeature(session);
        final Path container = new Path("/587e132bbff8c44a", EnumSet.of(Path.Type.volume));
        final TransferStatus status = new TransferStatus();
        status.setLength(-1L);
        final Path file = new Path(container, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file));
        final HttpResponseOutputStream<Void> out = feature.write(file, status, new DisabledConnectionCallback());
        final byte[] content = RandomStringUtils.random(5 * 1024 * 1024).getBytes("UTF-8");
        final ByteArrayInputStream in = new ByteArrayInputStream(content);
        final byte[] buffer = new byte[1 * 1024];
        assertEquals(content.length, IOUtils.copyLarge(in, out, buffer));
        in.close();
        out.close();
        assertNotNull(out.getStatus());
        assertTrue(new DefaultFindFeature(session).find(file));
        final byte[] compare = new byte[content.length];
        final InputStream stream = new OneDriveReadFeature(session).read(file, new TransferStatus().length(content.length), new DisabledConnectionCallback());
        IOUtils.readFully(stream, compare);
        stream.close();
        assertArrayEquals(content, compare);
        new OneDriveDeleteFeature(session).delete(Collections.singletonList(file), new DisabledLoginCallback(), new Delete.DisabledCallback());
        session.close();
    }
}