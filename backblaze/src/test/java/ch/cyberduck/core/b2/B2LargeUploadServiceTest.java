package ch.cyberduck.core.b2;

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

import ch.cyberduck.core.Credentials;
import ch.cyberduck.core.DisabledCancelCallback;
import ch.cyberduck.core.DisabledConnectionCallback;
import ch.cyberduck.core.DisabledHostKeyCallback;
import ch.cyberduck.core.DisabledLoginCallback;
import ch.cyberduck.core.DisabledPasswordStore;
import ch.cyberduck.core.DisabledTranscriptListener;
import ch.cyberduck.core.Host;
import ch.cyberduck.core.Local;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.features.Delete;
import ch.cyberduck.core.io.BandwidthThrottle;
import ch.cyberduck.core.io.DisabledStreamListener;
import ch.cyberduck.core.io.StreamCopier;
import ch.cyberduck.core.shared.DefaultFindFeature;
import ch.cyberduck.core.transfer.TransferStatus;
import ch.cyberduck.test.IntegrationTest;

import org.apache.commons.io.IOUtils;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Random;
import java.util.UUID;

import static org.junit.Assert.*;

@Category(IntegrationTest.class)
public class B2LargeUploadServiceTest {

    @Test
    @Ignore
    public void testUpload() throws Exception {
        final B2Session session = new B2Session(
                new Host(new B2Protocol(), new B2Protocol().getDefaultHostname(),
                        new Credentials(
                                System.getProperties().getProperty("b2.user"), System.getProperties().getProperty("b2.key")
                        )));
        final Path bucket = new Path("test-cyberduck", EnumSet.of(Path.Type.directory, Path.Type.volume));
        session.open(new DisabledHostKeyCallback(), new DisabledTranscriptListener());
        session.login(new DisabledPasswordStore(), new DisabledLoginCallback(), new DisabledCancelCallback());

        final Path test = new Path(bucket, UUID.randomUUID().toString() + ".txt", EnumSet.of(Path.Type.file));

        final Local local = new Local(System.getProperty("java.io.tmpdir"), UUID.randomUUID().toString());

        // Each segment, except the last, must be larger than 100MB.
        final byte[] content = new byte[100 * 1024 * 1024 + 1];
        new Random().nextBytes(content);

        final OutputStream out = local.getOutputStream(false);
        IOUtils.write(content, out);
        out.close();
        final TransferStatus status = new TransferStatus();
        status.setLength(content.length);

        final B2LargeUploadService upload = new B2LargeUploadService(session);

        upload.upload(test, local, new BandwidthThrottle(BandwidthThrottle.UNLIMITED), new DisabledStreamListener(),
                status, new DisabledConnectionCallback());
        // Large files do not have a SHA1 checksum. The value will always be "none".
        assertNull(new B2AttributesFeature(session).find(test).getChecksum());

        assertTrue(status.isComplete());
        assertFalse(status.isCanceled());
        assertEquals(content.length, status.getOffset());

        assertTrue(new DefaultFindFeature(session).find(test));
        final InputStream in = new B2ReadFeature(session).read(test, new TransferStatus());
        final ByteArrayOutputStream buffer = new ByteArrayOutputStream(content.length);
        new StreamCopier(status, status).transfer(in, buffer);
        in.close();
        buffer.close();
        assertArrayEquals(content, buffer.toByteArray());
        new B2DeleteFeature(session).delete(Collections.<Path>singletonList(test), new DisabledLoginCallback(), new Delete.Callback() {
            @Override
            public void delete(final Path file) {
            }
        });
        session.close();
    }
}