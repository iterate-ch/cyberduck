package ch.cyberduck.core.irods;

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

import ch.cyberduck.core.BytecountStreamListener;
import ch.cyberduck.core.Credentials;
import ch.cyberduck.core.DisabledCancelCallback;
import ch.cyberduck.core.DisabledConnectionCallback;
import ch.cyberduck.core.DisabledHostKeyCallback;
import ch.cyberduck.core.DisabledLoginCallback;
import ch.cyberduck.core.DisabledProgressListener;
import ch.cyberduck.core.Host;
import ch.cyberduck.core.Local;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.Profile;
import ch.cyberduck.core.ProtocolFactory;
import ch.cyberduck.core.exception.ConnectionCanceledException;
import ch.cyberduck.core.features.Delete;
import ch.cyberduck.core.io.BandwidthThrottle;
import ch.cyberduck.core.io.DisabledStreamListener;
import ch.cyberduck.core.proxy.DisabledProxyFinder;
import ch.cyberduck.core.serializer.impl.dd.ProfilePlistReader;
import ch.cyberduck.core.transfer.TransferStatus;
import ch.cyberduck.test.TestcontainerTest;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.RandomUtils;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.UUID;

import static org.junit.Assert.*;

@Category(TestcontainerTest.class)
public class IRODSUploadFeatureTest extends IRODSDockerComposeManager {

    @Test
    @Ignore
    public void testAppend() throws Exception {
        final ProtocolFactory factory = new ProtocolFactory(new HashSet<>(Collections.singleton(new IRODSProtocol())));
        final Profile profile = new ProfilePlistReader(factory).read(
                this.getClass().getResourceAsStream("/iRODS.cyberduckprofile"));
        final Host host = new Host(profile, profile.getDefaultHostname(), new Credentials(
                PROPERTIES.get("irods.key"), PROPERTIES.get("irods.secret")
        ));

        final IRODSSession session = new IRODSSession(host);
        session.open(new DisabledProxyFinder(), new DisabledHostKeyCallback(), new DisabledLoginCallback(), new DisabledCancelCallback());
        session.login(new DisabledLoginCallback(), new DisabledCancelCallback());
        final Local local = new Local(System.getProperty("java.io.tmpdir"), UUID.randomUUID().toString());
        final int length = 32770;
        final byte[] content = RandomUtils.nextBytes(length);
        final OutputStream out = local.getOutputStream(false);
        IOUtils.write(content, out);
        out.close();
        final Path test = new Path(new IRODSHomeFinderService(session).find(), UUID.randomUUID().toString(), EnumSet.of(Path.Type.file));
        {
            final TransferStatus status = new TransferStatus().setLength(content.length / 2);
            final BytecountStreamListener count = new BytecountStreamListener();
            new IRODSUploadFeature(session).upload(
                    new IRODSWriteFeature(session), test, local, new BandwidthThrottle(BandwidthThrottle.UNLIMITED), new DisabledProgressListener(), count,
                    status,
                    new DisabledConnectionCallback());
            assertEquals(content.length / 2, count.getSent());
        }
        {
            final TransferStatus status = new TransferStatus().setLength(content.length / 2).setOffset(content.length / 2).setAppend(true);
            new IRODSUploadFeature(session).upload(
                    new IRODSWriteFeature(session), test, local, new BandwidthThrottle(BandwidthThrottle.UNLIMITED), new DisabledProgressListener(), new DisabledStreamListener(),
                    status,
                    new DisabledConnectionCallback());
            assertEquals(content.length / 2, status.getOffset());
        }
        final byte[] buffer = new byte[content.length];
        final InputStream in = new IRODSReadFeature(session).read(test, new TransferStatus().setLength(content.length), new DisabledConnectionCallback());
        IOUtils.readFully(in, buffer);
        in.close();
        assertArrayEquals(content, buffer);
        new IRODSDeleteFeature(session).delete(Collections.singletonList(test), new DisabledLoginCallback(), new Delete.DisabledCallback());
        local.delete();
        session.close();
    }

    @Test
    public void testWrite() throws Exception {
        final ProtocolFactory factory = new ProtocolFactory(new HashSet<>(Collections.singleton(new IRODSProtocol())));
        final Profile profile = new ProfilePlistReader(factory).read(
                this.getClass().getResourceAsStream("/iRODS.cyberduckprofile"));
        final Host host = new Host(profile, profile.getDefaultHostname(), new Credentials(
                PROPERTIES.get("irods.key"), PROPERTIES.get("irods.secret")
        ));

        final IRODSSession session = new IRODSSession(host);
        session.open(new DisabledProxyFinder(), new DisabledHostKeyCallback(), new DisabledLoginCallback(), new DisabledCancelCallback());
        session.login(new DisabledLoginCallback(), new DisabledCancelCallback());
        final Local local = new Local(System.getProperty("java.io.tmpdir"), UUID.randomUUID().toString());
        final int length = 32770;
        final byte[] content = RandomUtils.nextBytes(length);
        final OutputStream out = local.getOutputStream(false);
        IOUtils.write(content, out);
        out.close();
        final Path test = new Path(new IRODSHomeFinderService(session).find(), UUID.randomUUID().toString(), EnumSet.of(Path.Type.file));
        final TransferStatus status = new TransferStatus().setLength(content.length);
        final BytecountStreamListener count = new BytecountStreamListener();
        new IRODSUploadFeature(session).upload(
                new IRODSWriteFeature(session), test, local, new BandwidthThrottle(BandwidthThrottle.UNLIMITED), new DisabledProgressListener(), count, status, new DisabledConnectionCallback());
        assertTrue(status.isComplete());
        assertEquals(content.length, count.getSent());
        final byte[] buffer = new byte[content.length];
        final InputStream in = new IRODSReadFeature(session).read(test, new TransferStatus().setLength(content.length), new DisabledConnectionCallback());
        IOUtils.readFully(in, buffer);
        in.close();
        assertArrayEquals(content, buffer);
        new IRODSDeleteFeature(session).delete(Collections.singletonList(test), new DisabledLoginCallback(), new Delete.DisabledCallback());
        session.close();
    }

    @Test
    public void testInterruptStatusForSmallFiles() throws Exception {
        final ProtocolFactory factory = new ProtocolFactory(new HashSet<>(Collections.singleton(new IRODSProtocol())));
        final Profile profile = new ProfilePlistReader(factory).read(
                this.getClass().getResourceAsStream("/iRODS.cyberduckprofile"));
        final Host host = new Host(profile, profile.getDefaultHostname(), new Credentials(
                PROPERTIES.get("irods.key"), PROPERTIES.get("irods.secret")
        ));

        final IRODSSession session = new IRODSSession(host);
        session.open(new DisabledProxyFinder(), new DisabledHostKeyCallback(), new DisabledLoginCallback(), new DisabledCancelCallback());
        session.login(new DisabledLoginCallback(), new DisabledCancelCallback());
        final Local local = new Local(System.getProperty("java.io.tmpdir"), UUID.randomUUID().toString());
        final int length = 32770;
        final byte[] content = RandomUtils.nextBytes(length);
        final OutputStream out = local.getOutputStream(false);
        IOUtils.write(content, out);
        out.close();
        final Path test = new Path(new IRODSHomeFinderService(session).find(), UUID.randomUUID().toString(), EnumSet.of(Path.Type.file));
        final TransferStatus status = new TransferStatus().setLength(content.length);
        new IRODSUploadFeature(session).upload(
                new IRODSWriteFeature(session), test, local,
                new BandwidthThrottle(BandwidthThrottle.UNLIMITED),
                new DisabledProgressListener(),
                new DisabledStreamListener() {
                    @Override
                    public void sent(final long bytes) {
                        super.sent(bytes);
                        status.setCanceled();
                    }
                },
                status,
                new DisabledConnectionCallback());
        try {
            status.validate();
            fail();
        }
        catch(ConnectionCanceledException e) {
            //
        }
        assertFalse(status.isComplete());
        new IRODSDeleteFeature(session).delete(Collections.singletonList(test), new DisabledLoginCallback(), new Delete.DisabledCallback());
        session.close();
    }

    @Test
    public void testInterruptStatusForLargeFiles() throws Exception {
        final ProtocolFactory factory = new ProtocolFactory(new HashSet<>(Collections.singleton(new IRODSProtocol())));
        final Profile profile = new ProfilePlistReader(factory).read(
                this.getClass().getResourceAsStream("/iRODS.cyberduckprofile"));
        final Host host = new Host(profile, profile.getDefaultHostname(), new Credentials(
                PROPERTIES.get("irods.key"), PROPERTIES.get("irods.secret")
        ));

        final IRODSSession session = new IRODSSession(host);
        session.open(new DisabledProxyFinder(), new DisabledHostKeyCallback(), new DisabledLoginCallback(), new DisabledCancelCallback());
        session.login(new DisabledLoginCallback(), new DisabledCancelCallback());
        final Local local = new Local(System.getProperty("java.io.tmpdir"), UUID.randomUUID().toString());
        final int length = 33 * 1024 * 1024; // Triggers parallel transfer
        final byte[] content = RandomUtils.nextBytes(length);
        final OutputStream out = local.getOutputStream(false);
        IOUtils.write(content, out);
        out.close();
        final Path test = new Path(new IRODSHomeFinderService(session).find(), UUID.randomUUID().toString(), EnumSet.of(Path.Type.file));
        final TransferStatus status = new TransferStatus().setLength(content.length);
        new IRODSUploadFeature(session).upload(
                new IRODSWriteFeature(session), test, local,
                new BandwidthThrottle(BandwidthThrottle.UNLIMITED),
                new DisabledProgressListener(),
                new DisabledStreamListener() {
                    @Override
                    public void sent(final long bytes) {
                        super.sent(bytes);
                        status.setCanceled();
                    }
                },
                status,
                new DisabledConnectionCallback());
        try {
            status.validate();
            fail();
        }
        catch(ConnectionCanceledException e) {
            //
        }
        assertFalse(status.isComplete());
        new IRODSDeleteFeature(session).delete(Collections.singletonList(test), new DisabledLoginCallback(), new Delete.DisabledCallback());
        session.close();
    }
}
