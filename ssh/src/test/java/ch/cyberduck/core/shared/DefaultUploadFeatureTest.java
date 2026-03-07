package ch.cyberduck.core.shared;

/*
 * Copyright (c) 2002-2015 David Kocher. All rights reserved.
 * http://cyberduck.ch/
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
 *
 * Bug fixes, suggestions and comments should be sent to feedback@cyberduck.ch
 */

import ch.cyberduck.core.ConnectionCallback;
import ch.cyberduck.core.Local;
import ch.cyberduck.core.LoginCallback;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.ProgressListener;
import ch.cyberduck.core.features.Delete;
import ch.cyberduck.core.features.Read;
import ch.cyberduck.core.io.BandwidthThrottle;
import ch.cyberduck.core.io.StreamListener;
import ch.cyberduck.core.sftp.AbstractSFTPTest;
import ch.cyberduck.core.sftp.SFTPHomeDirectoryService;
import ch.cyberduck.core.sftp.SFTPWriteFeature;
import ch.cyberduck.core.transfer.TransferStatus;
import ch.cyberduck.test.IntegrationTest;

import org.apache.commons.io.IOUtils;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Random;
import java.util.UUID;

import static org.junit.Assert.assertArrayEquals;

@Category(IntegrationTest.class)
public class DefaultUploadFeatureTest extends AbstractSFTPTest {

    @Test
    public void testTransferAppend() throws Exception {
        final Local local = new Local(System.getProperty("java.io.tmpdir"), UUID.randomUUID().toString());
        final byte[] content = new byte[32770];
        new Random().nextBytes(content);
        final OutputStream out = local.getOutputStream(false);
        IOUtils.write(content, out);
        out.close();
        final Path test = new Path(new SFTPHomeDirectoryService(session).find(), UUID.randomUUID().toString(), EnumSet.of(Path.Type.file));
        {
            final TransferStatus status = new TransferStatus().setLength(content.length / 2);
            new DefaultUploadFeature<Void>(session).upload(
                    new SFTPWriteFeature(session), test, local, new BandwidthThrottle(BandwidthThrottle.UNLIMITED), ProgressListener.noop, StreamListener.noop,
                status,
                    ConnectionCallback.noop);
        }
        {
            final TransferStatus status = new TransferStatus().setLength(content.length / 2).setOffset(content.length / 2).setAppend(true);
            new DefaultUploadFeature<Void>(session).upload(
                    new SFTPWriteFeature(session), test, local, new BandwidthThrottle(BandwidthThrottle.UNLIMITED), ProgressListener.noop, StreamListener.noop,
                status,
                    ConnectionCallback.noop);
        }
        final byte[] buffer = new byte[content.length];
        final Read read = session.getFeature(Read.class);
        final InputStream in = read.read(test, new TransferStatus().setLength(content.length), ConnectionCallback.noop);
        IOUtils.readFully(in, buffer);
        in.close();
        assertArrayEquals(content, buffer);
        final Delete delete = session.getFeature(Delete.class);
        delete.delete(Collections.singletonList(test), LoginCallback.noop, new Delete.DisabledCallback());
        local.delete();
    }
}
