package ch.cyberduck.core.dav;

/*
 * Copyright (c) 2002-2013 David Kocher. All rights reserved.
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
import ch.cyberduck.core.exception.AccessDeniedException;
import ch.cyberduck.core.features.Delete;
import ch.cyberduck.core.io.BandwidthThrottle;
import ch.cyberduck.core.io.DisabledStreamListener;
import ch.cyberduck.core.local.DefaultLocalTouchFeature;
import ch.cyberduck.core.shared.DefaultHomeFinderService;
import ch.cyberduck.core.transfer.TransferStatus;
import ch.cyberduck.test.IntegrationTest;

import org.apache.commons.io.IOUtils;
import org.apache.commons.io.input.NullInputStream;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Random;
import java.util.UUID;

import static org.junit.Assert.*;

@Category(IntegrationTest.class)
public class DAVUploadFeatureTest {

    @Test
    public void testDecorate() throws Exception {
        final NullInputStream n = new NullInputStream(1L);
        final DAVSession session = new DAVSession(new Host(new DAVProtocol(), "h"));
        assertSame(NullInputStream.class, new DAVUploadFeature(new DAVWriteFeature(session)).decorate(n, null).getClass());
    }

    @Test
    public void testDigest() throws Exception {
        final DAVSession session = new DAVSession(new Host(new DAVProtocol(), "h"));
        assertNull(new DAVUploadFeature(new DAVWriteFeature(session)).digest());
    }

    @Test(expected = AccessDeniedException.class)
    public void testAccessDenied() throws Exception {
        final Host host = new Host(new DAVProtocol(), "test.cyberduck.ch", new Credentials(
                System.getProperties().getProperty("webdav.user"), System.getProperties().getProperty("webdav.password")
        ));
        host.setDefaultPath("/dav/basic");
        final DAVSession session = new DAVSession(host);
        session.open(new DisabledHostKeyCallback());
        session.login(new DisabledPasswordStore(), new DisabledLoginCallback(), new DisabledCancelCallback(), PathCache.empty());
        final TransferStatus status = new TransferStatus();
        final Local local = new Local(System.getProperty("java.io.tmpdir"), UUID.randomUUID().toString());
        new DefaultLocalTouchFeature().touch(local);
        final Path test = new Path(new Path("/dav/accessdenied", EnumSet.of(Path.Type.directory)), "nosuchname", EnumSet.of(Path.Type.file));
        try {
            new DAVUploadFeature(new DAVWriteFeature(session)).upload(
                    test, local, new BandwidthThrottle(BandwidthThrottle.UNLIMITED), new DisabledStreamListener(),
                    status,
                    new DisabledConnectionCallback());
        }
        catch(AccessDeniedException e) {
            assertEquals("Unexpected response (403 Forbidden). Please contact your web hosting service provider for assistance.", e.getDetail());
            throw e;
        }
        finally {
            local.delete();
            session.close();
        }
    }

    @Test
    public void testAppend() throws Exception {
        final Host host = new Host(new DAVProtocol(), "test.cyberduck.ch", new Credentials(
                System.getProperties().getProperty("webdav.user"), System.getProperties().getProperty("webdav.password")
        ));
        host.setDefaultPath("/dav/basic");
        final DAVSession session = new DAVSession(host);
        session.open(new DisabledHostKeyCallback());
        session.login(new DisabledPasswordStore(), new DisabledLoginCallback(), new DisabledCancelCallback(), PathCache.empty());
        final Local local = new Local(System.getProperty("java.io.tmpdir"), UUID.randomUUID().toString());
        final byte[] content = new byte[32770];
        new Random().nextBytes(content);
        final OutputStream out = local.getOutputStream(false);
        IOUtils.write(content, out);
        out.close();
        final Path test = new Path(new DefaultHomeFinderService(session).find(), UUID.randomUUID().toString(), EnumSet.of(Path.Type.file));
        {
            final TransferStatus status = new TransferStatus().length(content.length / 2);
            new DAVUploadFeature(new DAVWriteFeature(session)).upload(
                    test, local, new BandwidthThrottle(BandwidthThrottle.UNLIMITED), new DisabledStreamListener(),
                    status,
                    new DisabledConnectionCallback());
        }
        {
            final TransferStatus status = new TransferStatus().length(content.length / 2).skip(content.length / 2).append(true);
            new DAVUploadFeature(new DAVWriteFeature(session)).upload(
                    test, local, new BandwidthThrottle(BandwidthThrottle.UNLIMITED), new DisabledStreamListener(),
                    status,
                    new DisabledConnectionCallback());
        }
        final byte[] buffer = new byte[content.length];
        final InputStream in = new DAVReadFeature(session).read(test, new TransferStatus().length(content.length), new DisabledConnectionCallback());
        IOUtils.readFully(in, buffer);
        in.close();
        assertArrayEquals(content, buffer);
        new DAVDeleteFeature(session).delete(Collections.singletonList(test), new DisabledLoginCallback(), new Delete.DisabledCallback());
        local.delete();
        session.close();
    }
}
