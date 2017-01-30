package ch.cyberduck.core.dropbox;

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
import ch.cyberduck.core.DisabledHostKeyCallback;
import ch.cyberduck.core.DisabledListProgressListener;
import ch.cyberduck.core.DisabledLoginCallback;
import ch.cyberduck.core.DisabledPasswordStore;
import ch.cyberduck.core.DisabledProgressListener;
import ch.cyberduck.core.DisabledTranscriptListener;
import ch.cyberduck.core.Host;
import ch.cyberduck.core.LoginConnectionService;
import ch.cyberduck.core.LoginOptions;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathCache;
import ch.cyberduck.core.Scheme;
import ch.cyberduck.core.exception.LoginCanceledException;
import ch.cyberduck.core.features.Delete;
import ch.cyberduck.core.features.Find;
import ch.cyberduck.core.io.StreamCopier;
import ch.cyberduck.core.shared.DefaultHomeFinderService;
import ch.cyberduck.core.ssl.DefaultX509KeyManager;
import ch.cyberduck.core.ssl.DisabledX509TrustManager;
import ch.cyberduck.core.transfer.TransferStatus;
import ch.cyberduck.test.IntegrationTest;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.RandomUtils;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Collections;
import java.util.EnumSet;
import java.util.UUID;

import static org.junit.Assert.*;

@Category(IntegrationTest.class)
public class DropboxWriteFeatureTest {

    @Test
    public void testReadWrite() throws Exception {
        final DropboxSession session = new DropboxSession(new Host(new DropboxProtocol(), new DropboxProtocol().getDefaultHostname()),
                new DisabledX509TrustManager(), new DefaultX509KeyManager());
        new LoginConnectionService(new DisabledLoginCallback() {
            @Override
            public void prompt(final Host bookmark, final Credentials credentials, final String title, final String reason, final LoginOptions options) throws LoginCanceledException {
                fail(reason);
            }
        }, new DisabledHostKeyCallback(),
                new DisabledPasswordStore() {
                    @Override
                    public String getPassword(Scheme scheme, int port, String hostname, String user) {
                        return System.getProperties().getProperty("dropbox.accesstoken");
                    }

                    @Override
                    public String getPassword(String hostname, String user) {
                        return System.getProperties().getProperty("dropbox.accesstoken");
                    }
                }, new DisabledProgressListener(), new DisabledTranscriptListener())
                .connect(session, PathCache.empty(), new DisabledCancelCallback());

        final DropboxWriteFeature write = new DropboxWriteFeature(session);

        final TransferStatus status = new TransferStatus();
        final byte[] content = RandomUtils.nextBytes(66800);
        status.setLength(content.length);
        final Path test = new Path(new DefaultHomeFinderService(session).find(), UUID.randomUUID().toString(), EnumSet.of(Path.Type.file));
        final OutputStream out = write.write(test, status);
        assertNotNull(out);
        new StreamCopier(new TransferStatus(), new TransferStatus()).transfer(new ByteArrayInputStream(content), out);
        test.attributes().setVersionId(new DropboxIdProvider(session).getFileid(test));
        assertTrue(session.getFeature(Find.class).find(test));
        assertEquals(content.length, session.list(test.getParent(), new DisabledListProgressListener()).get(test).attributes().getSize());
        assertEquals(content.length, write.append(test, status.getLength(), PathCache.empty()).size, 0L);
        {
            final InputStream in = new DropboxReadFeature(session).read(test, new TransferStatus().length(content.length));
            final ByteArrayOutputStream buffer = new ByteArrayOutputStream(content.length);
            new StreamCopier(status, status).transfer(in, buffer);
            in.close();
            assertArrayEquals(content, buffer.toByteArray());
        }
        {
            final byte[] buffer = new byte[content.length - 1];
            final InputStream in = new DropboxReadFeature(session).read(test, new TransferStatus().length(content.length).append(true).skip(1L));
            IOUtils.readFully(in, buffer);
            in.close();
            final byte[] reference = new byte[content.length - 1];
            System.arraycopy(content, 1, reference, 0, content.length - 1);
            assertArrayEquals(reference, buffer);
        }
        new DropboxDeleteFeature(session).delete(Collections.singletonList(test), new DisabledLoginCallback(), new Delete.Callback() {
            @Override
            public void delete(final Path file) {
            }
        });
        session.close();
    }

    @Test
    public void testWriteAppendChunks() throws Exception {
        final DropboxSession session = new DropboxSession(new Host(new DropboxProtocol(), new DropboxProtocol().getDefaultHostname()),
                new DisabledX509TrustManager(), new DefaultX509KeyManager());
        new LoginConnectionService(new DisabledLoginCallback() {
            @Override
            public void prompt(final Host bookmark, final Credentials credentials, final String title, final String reason, final LoginOptions options) throws LoginCanceledException {
                fail(reason);
            }
        }, new DisabledHostKeyCallback(),
                new DisabledPasswordStore() {
                    @Override
                    public String getPassword(Scheme scheme, int port, String hostname, String user) {
                        return System.getProperties().getProperty("dropbox.accesstoken");
                    }

                    @Override
                    public String getPassword(String hostname, String user) {
                        return System.getProperties().getProperty("dropbox.accesstoken");
                    }
                }, new DisabledProgressListener(), new DisabledTranscriptListener())
                .connect(session, PathCache.empty(), new DisabledCancelCallback());

        final DropboxWriteFeature write = new DropboxWriteFeature(session, 44000L);

        final TransferStatus status = new TransferStatus();
        final byte[] content = RandomUtils.nextBytes(290000);
        status.setLength(content.length);
        final Path test = new Path(new DefaultHomeFinderService(session).find(), UUID.randomUUID().toString(), EnumSet.of(Path.Type.file));
        final OutputStream out = write.write(test, status);
        assertNotNull(out);
        new StreamCopier(new TransferStatus(), new TransferStatus()).transfer(new ByteArrayInputStream(content), out);
        test.attributes().setVersionId(new DropboxIdProvider(session).getFileid(test));
        assertTrue(session.getFeature(Find.class).find(test));
        assertEquals(content.length, session.list(test.getParent(), new DisabledListProgressListener()).get(test).attributes().getSize());
        assertEquals(content.length, write.append(test, status.getLength(), PathCache.empty()).size, 0L);
        {
            final InputStream in = new DropboxReadFeature(session).read(test, new TransferStatus().length(content.length));
            final ByteArrayOutputStream buffer = new ByteArrayOutputStream(content.length);
            new StreamCopier(status, status).transfer(in, buffer);
            in.close();
            assertArrayEquals(content, buffer.toByteArray());
        }
        {
            final byte[] buffer = new byte[content.length - 1];
            final InputStream in = new DropboxReadFeature(session).read(test, new TransferStatus().length(content.length).append(true).skip(1L));
            IOUtils.readFully(in, buffer);
            in.close();
            final byte[] reference = new byte[content.length - 1];
            System.arraycopy(content, 1, reference, 0, content.length - 1);
            assertArrayEquals(reference, buffer);
        }
        new DropboxDeleteFeature(session).delete(Collections.singletonList(test), new DisabledLoginCallback(), new Delete.Callback() {
            @Override
            public void delete(final Path file) {
            }
        });
        session.close();
    }
}
