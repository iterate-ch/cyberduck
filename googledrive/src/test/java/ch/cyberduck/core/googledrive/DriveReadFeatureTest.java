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
import ch.cyberduck.core.DisabledLoginCallback;
import ch.cyberduck.core.DisabledPasswordCallback;
import ch.cyberduck.core.Local;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.exception.NotfoundException;
import ch.cyberduck.core.features.Delete;
import ch.cyberduck.core.io.BandwidthThrottle;
import ch.cyberduck.core.io.DisabledStreamListener;
import ch.cyberduck.core.io.StreamCopier;
import ch.cyberduck.core.transfer.TransferStatus;
import ch.cyberduck.test.IntegrationTest;

import org.apache.commons.io.IOUtils;
import org.apache.commons.text.RandomStringGenerator;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Collections;
import java.util.EnumSet;
import java.util.UUID;

import static org.junit.Assert.*;

@Category(IntegrationTest.class)
public class DriveReadFeatureTest extends AbstractDriveTest {

    @Test
    public void testAppend() throws Exception {
        assertTrue(new DriveReadFeature(null).offset(new Path("/", EnumSet.of(Path.Type.file))));
    }

    @Test(expected = NotfoundException.class)
    public void testReadNotFound() throws Exception {
        final TransferStatus status = new TransferStatus();
        new DriveReadFeature(session).read(new Path(new DriveHomeFinderService(session).find(), "nosuchname", EnumSet.of(Path.Type.file)), status, new DisabledConnectionCallback(), new DisabledPasswordCallback());
    }

    @Test
    public void testReadRange() throws Exception {

        final String name = "ä-" + UUID.randomUUID().toString();
        final Path test = new Path(new DriveHomeFinderService(session).find(), name, EnumSet.of(Path.Type.file));
        final Local local = new Local(System.getProperty("java.io.tmpdir"), name);
        final byte[] content = new RandomStringGenerator.Builder().build().generate(1000).getBytes();
        final OutputStream out = local.getOutputStream(false);
        assertNotNull(out);
        IOUtils.write(content, out);
        out.close();
        new DriveUploadFeature(new DriveWriteFeature(session)).upload(
                test, local, new BandwidthThrottle(BandwidthThrottle.UNLIMITED), new DisabledStreamListener(),
                new TransferStatus().length(content.length),
                new DisabledConnectionCallback());
        final TransferStatus status = new TransferStatus();
        status.setLength(content.length);
        status.setAppend(true);
        status.setOffset(100L);
        final InputStream in = new DriveReadFeature(session).read(test, status.length(content.length - 100), new DisabledConnectionCallback(), new DisabledPasswordCallback());
        assertNotNull(in);
        final ByteArrayOutputStream buffer = new ByteArrayOutputStream(content.length - 100);
        new StreamCopier(status, status).transfer(in, buffer);
        final byte[] reference = new byte[content.length - 100];
        System.arraycopy(content, 100, reference, 0, content.length - 100);
        assertArrayEquals(reference, buffer.toByteArray());
        in.close();
        new DriveDeleteFeature(session).delete(Collections.<Path>singletonList(test), new DisabledLoginCallback(), new Delete.DisabledCallback());
    }
}
