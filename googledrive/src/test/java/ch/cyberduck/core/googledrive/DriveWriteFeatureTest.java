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

import ch.cyberduck.core.*;
import ch.cyberduck.core.exception.LoginCanceledException;
import ch.cyberduck.core.features.Delete;
import ch.cyberduck.core.features.Find;
import ch.cyberduck.core.http.HttpConnectionPoolBuilder;
import ch.cyberduck.core.http.HttpUploadFeature;
import ch.cyberduck.core.io.BandwidthThrottle;
import ch.cyberduck.core.io.DisabledStreamListener;
import ch.cyberduck.core.proxy.DisabledProxyFinder;
import ch.cyberduck.core.ssl.DefaultX509KeyManager;
import ch.cyberduck.core.ssl.DefaultX509TrustManager;
import ch.cyberduck.core.ssl.DisabledX509TrustManager;
import ch.cyberduck.core.ssl.ThreadLocalHostnameDelegatingTrustManager;
import ch.cyberduck.core.transfer.TransferStatus;
import ch.cyberduck.test.IntegrationTest;

import org.apache.commons.io.IOUtils;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.Collections;
import java.util.EnumSet;
import java.util.UUID;

import static org.junit.Assert.*;

@Category(IntegrationTest.class)
public class DriveWriteFeatureTest {

    @Test
    public void testWrite() throws Exception {
        final Host host = new Host(new DriveProtocol(), "www.googleapis.com", new Credentials());
        final DriveSession session = new DriveSession(host, new DefaultX509TrustManager(), new DefaultX509KeyManager());
        new LoginConnectionService(new DisabledLoginCallback() {
            @Override
            public void prompt(final Host bookmark, final Credentials credentials, final String title, final String reason, final LoginOptions options) throws LoginCanceledException {
                fail(reason);
            }
        }, new DisabledHostKeyCallback(),
                new DisabledPasswordStore() {
                    @Override
                    public String getPassword(Scheme scheme, int port, String hostname, String user) {
                        if(user.equals("Google Drive OAuth2 Access Token")) {
                            return System.getProperties().getProperty("googledrive.accesstoken");
                        }
                        if(user.equals("Google Drive OAuth2 Refresh Token")) {
                            return System.getProperties().getProperty("googledrive.refreshtoken");
                        }
                        fail();
                        return null;
                    }

                    @Override
                    public String getPassword(String hostname, String user) {
                        return super.getPassword(hostname, user);
                    }
                }, new DisabledProgressListener(),
                new DisabledTranscriptListener()).connect(session, PathCache.empty());
        final TransferStatus status = new TransferStatus();
        final Local local = new Local(System.getProperty("java.io.tmpdir"), UUID.randomUUID().toString());
        final byte[] content = "test".getBytes("UTF-8");
        final OutputStream out = local.getOutputStream(false);
        IOUtils.write(content, out);
        IOUtils.closeQuietly(out);
        status.setLength(content.length);
        final Path test = new Path(new DriveHomeFinderService(session).find(), UUID.randomUUID().toString(), EnumSet.of(Path.Type.file));
        final HttpConnectionPoolBuilder builder = new HttpConnectionPoolBuilder(host, new ThreadLocalHostnameDelegatingTrustManager(
                new DisabledX509TrustManager(), host.getHostname()), new DefaultX509KeyManager(), new DisabledProxyFinder()
        );
        final HttpUploadFeature upload = new DriveUploadFeature(session);
        upload.upload(test, local, new BandwidthThrottle(BandwidthThrottle.UNLIMITED), new DisabledStreamListener(),
                status, new DisabledConnectionCallback());
        test.attributes().setVersionId(new DriveFileidProvider(session).getFileid(test));
        assertTrue(session.getFeature(Find.class).find(test));
        assertEquals(content.length, session.list(test.getParent(), new DisabledListProgressListener()).get(test).attributes().getSize(), 0L);
        assertEquals(content.length, new DriveWriteFeature(session).append(test, status.getLength(), PathCache.empty()).size, 0L);
        {
            final byte[] buffer = new byte[content.length];
            IOUtils.readFully(new DriveReadFeature(session).read(test, new TransferStatus()), buffer);
            assertArrayEquals(content, buffer);
        }
        {
            final byte[] buffer = new byte[content.length - 1];
            final InputStream in = new DriveReadFeature(session).read(test, new TransferStatus().length(content.length).append(true).skip(1L));
            IOUtils.readFully(in, buffer);
            IOUtils.closeQuietly(in);
            final byte[] reference = new byte[content.length - 1];
            System.arraycopy(content, 1, reference, 0, content.length - 1);
            assertArrayEquals(reference, buffer);
        }
        new DriveDeleteFeature(session).delete(Collections.singletonList(test), new DisabledLoginCallback(),
                new Delete.Callback() {
                    @Override
                    public void delete(final Path file) {
                        //
                    }
                });
        session.close();
    }
}
