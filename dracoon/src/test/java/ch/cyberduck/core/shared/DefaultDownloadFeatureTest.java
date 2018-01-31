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
import ch.cyberduck.core.DisabledPasswordCallback;
import ch.cyberduck.core.DisabledPasswordStore;
import ch.cyberduck.core.Host;
import ch.cyberduck.core.Local;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.VersionId;
import ch.cyberduck.core.features.Delete;
import ch.cyberduck.core.io.BandwidthThrottle;
import ch.cyberduck.core.io.DisabledStreamListener;
import ch.cyberduck.core.io.StatusOutputStream;
import ch.cyberduck.core.io.StreamCopier;
import ch.cyberduck.core.sds.SDSDeleteFeature;
import ch.cyberduck.core.sds.SDSDirectoryFeature;
import ch.cyberduck.core.sds.SDSProtocol;
import ch.cyberduck.core.sds.SDSReadFeature;
import ch.cyberduck.core.sds.SDSSession;
import ch.cyberduck.core.sds.SDSTouchFeature;
import ch.cyberduck.core.sds.SDSWriteFeature;
import ch.cyberduck.core.ssl.DefaultX509KeyManager;
import ch.cyberduck.core.ssl.DisabledX509TrustManager;
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

import static org.junit.Assert.*;

@Category(IntegrationTest.class)
public class DefaultDownloadFeatureTest {

    @Test
    public void testTransferAppend() throws Exception {
        final Host host = new Host(new SDSProtocol(), "duck.ssp-europe.eu", new Credentials(
            System.getProperties().getProperty("sds.user"), System.getProperties().getProperty("sds.key")
        ));
        final SDSSession session = new SDSSession(host, new DisabledX509TrustManager(), new DefaultX509KeyManager());
        session.open(new DisabledHostKeyCallback(), new DisabledLoginCallback());
        session.login(new DisabledPasswordStore(), new DisabledLoginCallback(), new DisabledCancelCallback());

        final Path room = new SDSDirectoryFeature(session).mkdir(new Path(new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory, Path.Type.volume)), null, new TransferStatus());
        final Path test = new SDSTouchFeature(session).touch(
            new Path(room, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file)), new TransferStatus());
        final byte[] content = new byte[39864];
        new Random().nextBytes(content);
        {
            final TransferStatus status = new TransferStatus().length(content.length);
            final StatusOutputStream<VersionId> out = new SDSWriteFeature(session).write(test, status, new DisabledConnectionCallback());
            assertNotNull(out);
            new StreamCopier(status, status).withLimit(new Long(content.length)).transfer(new ByteArrayInputStream(content), out);
            out.close();
            assertNotEquals(test.attributes().getVersionId(), out.getStatus().id);
            test.attributes().setVersionId(out.getStatus().id);
        }
        final Local local = new Local(System.getProperty("java.io.tmpdir"), UUID.randomUUID().toString());
        {
            final TransferStatus status = new TransferStatus().length(content.length / 2);
            new DefaultDownloadFeature(new SDSReadFeature(session)).download(
                test, local, new BandwidthThrottle(BandwidthThrottle.UNLIMITED), new DisabledStreamListener(),
                status,
                new DisabledConnectionCallback(), new DisabledPasswordCallback());
        }
        {
            final TransferStatus status = new TransferStatus().length(content.length / 2).skip(content.length / 2).append(true);
            new DefaultDownloadFeature(new SDSReadFeature(session)).download(
                test, local, new BandwidthThrottle(BandwidthThrottle.UNLIMITED), new DisabledStreamListener(),
                status,
                new DisabledConnectionCallback(), new DisabledPasswordCallback());
        }
        final byte[] buffer = new byte[content.length];
        final InputStream in = local.getInputStream();
        IOUtils.readFully(in, buffer);
        in.close();
        assertArrayEquals(content, buffer);
        new SDSDeleteFeature(session).delete(Collections.singletonList(room), new DisabledLoginCallback(), new Delete.DisabledCallback());
        session.close();
    }

    @Test
    public void testTransferUnknownSize() throws Exception {
        final Host host = new Host(new SDSProtocol(), "duck.ssp-europe.eu", new Credentials(
            System.getProperties().getProperty("sds.user"), System.getProperties().getProperty("sds.key")
        ));
        final SDSSession session = new SDSSession(host, new DisabledX509TrustManager(), new DefaultX509KeyManager());
        session.open(new DisabledHostKeyCallback(), new DisabledLoginCallback());
        session.login(new DisabledPasswordStore(), new DisabledLoginCallback(), new DisabledCancelCallback());

        final Path room = new SDSDirectoryFeature(session).mkdir(new Path(new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory, Path.Type.volume)), null, new TransferStatus());
        final Path test = new Path(room, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file));
        new SDSTouchFeature(session).touch(test, new TransferStatus());
        final byte[] content = new byte[1];
        new Random().nextBytes(content);
        {
            final TransferStatus status = new TransferStatus().length(content.length);
            status.setExists(true);
            final OutputStream out = new SDSWriteFeature(session).write(test, status, new DisabledConnectionCallback());
            assertNotNull(out);
            new StreamCopier(status, status).withLimit(new Long(content.length)).transfer(new ByteArrayInputStream(content), out);
            out.close();
        }
        final Local local = new Local(System.getProperty("java.io.tmpdir"), UUID.randomUUID().toString());
        {
            final TransferStatus status = new TransferStatus().length(-1L);
            status.setExists(true);
            new DefaultDownloadFeature(new SDSReadFeature(session)).download(
                test, local, new BandwidthThrottle(BandwidthThrottle.UNLIMITED), new DisabledStreamListener(),
                status,
                new DisabledConnectionCallback(), new DisabledPasswordCallback());
        }
        final byte[] buffer = new byte[content.length];
        final InputStream in = local.getInputStream();
        IOUtils.readFully(in, buffer);
        in.close();
        assertArrayEquals(content, buffer);
        new SDSDeleteFeature(session).delete(Collections.singletonList(room), new DisabledLoginCallback(), new Delete.DisabledCallback());
        session.close();
    }
}
