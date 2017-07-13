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
import ch.cyberduck.core.Credentials;
import ch.cyberduck.core.DisabledCancelCallback;
import ch.cyberduck.core.DisabledConnectionCallback;
import ch.cyberduck.core.DisabledHostKeyCallback;
import ch.cyberduck.core.DisabledLoginCallback;
import ch.cyberduck.core.DisabledPasswordStore;
import ch.cyberduck.core.Host;
import ch.cyberduck.core.Local;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathCache;
import ch.cyberduck.core.b2.B2DeleteFeature;
import ch.cyberduck.core.b2.B2Protocol;
import ch.cyberduck.core.b2.B2ReadFeature;
import ch.cyberduck.core.b2.B2Session;
import ch.cyberduck.core.b2.B2TouchFeature;
import ch.cyberduck.core.b2.B2WriteFeature;
import ch.cyberduck.core.features.Delete;
import ch.cyberduck.core.io.BandwidthThrottle;
import ch.cyberduck.core.io.DisabledStreamListener;
import ch.cyberduck.core.io.StreamCopier;
import ch.cyberduck.core.transfer.TransferStatus;
import ch.cyberduck.test.IntegrationTest;

import org.apache.commons.io.IOUtils;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Random;
import java.util.UUID;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertNotNull;

@Category(IntegrationTest.class)
public class DefaultDownloadFeatureTest {

    @Test
    public void testOverwrite() throws Exception {
        final B2Session session = new B2Session(
                new Host(new B2Protocol(), new B2Protocol().getDefaultHostname(),
                        new Credentials(
                                System.getProperties().getProperty("b2.user"), System.getProperties().getProperty("b2.key")
                        )));
        session.open(new DisabledHostKeyCallback());
        session.login(new DisabledPasswordStore(), new DisabledLoginCallback(), new DisabledCancelCallback(), PathCache.empty());

        final Path bucket = new Path("test-cyberduck", EnumSet.of(Path.Type.directory, Path.Type.volume));
        final Path test = new B2TouchFeature(session).touch(
                new Path(bucket, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file)), new TransferStatus());
        final byte[] content = new byte[39864];
        new Random().nextBytes(content);
        {
            final TransferStatus status = new TransferStatus().length(content.length);
            final OutputStream out = new B2WriteFeature(session).write(test, status, new DisabledConnectionCallback());
            assertNotNull(out);
            new StreamCopier(status, status).withLimit(new Long(content.length)).transfer(new ByteArrayInputStream(content), out);
            out.close();
        }
        final Local local = new Local(System.getProperty("java.io.tmpdir"), UUID.randomUUID().toString());
        final TransferStatus status = new TransferStatus().length(content.length);
        new DefaultDownloadFeature(new B2ReadFeature(session)).download(
                test, local, new BandwidthThrottle(BandwidthThrottle.UNLIMITED), new DisabledStreamListener(),
                status,
                new DisabledConnectionCallback());
        final byte[] buffer = new byte[content.length];
        final InputStream in = local.getInputStream();
        IOUtils.readFully(in, buffer);
        in.close();
        assertArrayEquals(content, buffer);
        new B2DeleteFeature(session).delete(Collections.singletonList(test), new DisabledLoginCallback(), new Delete.DisabledCallback());
        session.close();
    }

    @Test
    public void testTransferUnknownSize() throws Exception {
        final B2Session session = new B2Session(
                new Host(new B2Protocol(), new B2Protocol().getDefaultHostname(),
                        new Credentials(
                                System.getProperties().getProperty("b2.user"), System.getProperties().getProperty("b2.key")
                        )));
        session.open(new DisabledHostKeyCallback());
        session.login(new DisabledPasswordStore(), new DisabledLoginCallback(), new DisabledCancelCallback(), PathCache.empty());

        final Path bucket = new Path("test-cyberduck", EnumSet.of(Path.Type.directory, Path.Type.volume));
        final Path test = new Path(bucket, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file));
        new B2TouchFeature(session).touch(test, new TransferStatus());
        final byte[] content = new byte[1];
        new Random().nextBytes(content);
        {
            final TransferStatus status = new TransferStatus().length(content.length);
            final OutputStream out = new B2WriteFeature(session).write(test, status, new DisabledConnectionCallback());
            assertNotNull(out);
            new StreamCopier(status, status).withLimit(new Long(content.length)).transfer(new ByteArrayInputStream(content), out);
            out.close();
        }
        final Local local = new Local(System.getProperty("java.io.tmpdir"), UUID.randomUUID().toString());
        {
            final TransferStatus status = new TransferStatus().length(-1L);
            new DefaultDownloadFeature(new B2ReadFeature(session)).download(
                    test, local, new BandwidthThrottle(BandwidthThrottle.UNLIMITED), new DisabledStreamListener(),
                    status,
                    new DisabledConnectionCallback());
        }
        final byte[] buffer = new byte[content.length];
        final InputStream in = local.getInputStream();
        IOUtils.readFully(in, buffer);
        in.close();
        assertArrayEquals(content, buffer);
        new B2DeleteFeature(session).delete(Collections.singletonList(test), new DisabledLoginCallback(), new Delete.DisabledCallback());
        session.close();
    }
}