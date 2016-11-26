/*
 * Copyright (c) 2002-2016 iterate GmbH. All rights reserved.
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

package ch.cyberduck.core.openstack;

import ch.cyberduck.core.Credentials;
import ch.cyberduck.core.DisabledCancelCallback;
import ch.cyberduck.core.DisabledHostKeyCallback;
import ch.cyberduck.core.DisabledLoginCallback;
import ch.cyberduck.core.DisabledPasswordStore;
import ch.cyberduck.core.DisabledTranscriptListener;
import ch.cyberduck.core.Host;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathCache;
import ch.cyberduck.core.features.Delete;
import ch.cyberduck.core.transfer.TransferStatus;
import ch.cyberduck.test.IntegrationTest;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Collections;
import java.util.EnumSet;
import java.util.UUID;

import static org.junit.Assert.*;

@Category(IntegrationTest.class)
public class SwiftLargeUploadWriteFeatureTest {

    @Test
    public void testWriteUploadLargeBuffer() throws Exception {
        final Host host = new Host(new SwiftProtocol(), "identity.api.rackspacecloud.com", new Credentials(
                System.getProperties().getProperty("rackspace.key"), System.getProperties().getProperty("rackspace.secret")
        ));
        final SwiftSession session = new SwiftSession(host).withAccountPreload(false).withCdnPreload(false).withContainerPreload(false);
        session.open(new DisabledHostKeyCallback(), new DisabledTranscriptListener());
        session.login(new DisabledPasswordStore(), new DisabledLoginCallback(), new DisabledCancelCallback(), PathCache.empty());
        final SwiftRegionService regionService = new SwiftRegionService(session);
        final SwiftLargeUploadWriteFeature feature = new SwiftLargeUploadWriteFeature(session, regionService,
                new SwiftSegmentService(session, ".segments-test/"));
        final Path container = new Path("test.cyberduck.ch", EnumSet.of(Path.Type.directory, Path.Type.volume));
        container.attributes().setRegion("DFW");
        final TransferStatus status = new TransferStatus();
        status.setLength(-1L);
        final Path file = new Path(container, UUID.randomUUID().toString(), EnumSet.of(Path.Type.file));
        final OutputStream out = feature.write(file, status);
        final byte[] content = RandomStringUtils.random(6 * 1024 * 1024).getBytes("UTF-8");
        final ByteArrayInputStream in = new ByteArrayInputStream(content);
        // Adjust buffer to be 5MB. This will write parts with 5MB size which is the minimum allowed
        final byte[] buffer = new byte[5 * 1024 * 1024];
        assertEquals(content.length, IOUtils.copyLarge(in, out, buffer));
        in.close();
        out.close();
        assertTrue(new SwiftFindFeature(session).find(file));
        final byte[] compare = new byte[content.length];
        final InputStream stream = new SwiftReadFeature(session, regionService).read(file, new TransferStatus().length(content.length));
        IOUtils.readFully(stream, compare);
        stream.close();
        assertArrayEquals(content, compare);
        new SwiftDeleteFeature(session).delete(Collections.singletonList(file), new DisabledLoginCallback(), new Delete.DisabledCallback());
        session.close();
    }

    @Test
    public void testWriteUploadSmallBuffer() throws Exception {
        final Host host = new Host(new SwiftProtocol(), "identity.api.rackspacecloud.com", new Credentials(
                System.getProperties().getProperty("rackspace.key"), System.getProperties().getProperty("rackspace.secret")
        ));
        final SwiftSession session = new SwiftSession(host).withAccountPreload(false).withCdnPreload(false).withContainerPreload(false);
        session.open(new DisabledHostKeyCallback(), new DisabledTranscriptListener());
        session.login(new DisabledPasswordStore(), new DisabledLoginCallback(), new DisabledCancelCallback(), PathCache.empty());
        final SwiftRegionService regionService = new SwiftRegionService(session);
        final SwiftLargeUploadWriteFeature feature = new SwiftLargeUploadWriteFeature(session, regionService,
                new SwiftSegmentService(session, ".segments-test/"));
        final Path container = new Path("test.cyberduck.ch", EnumSet.of(Path.Type.directory, Path.Type.volume));
        container.attributes().setRegion("DFW");
        final TransferStatus status = new TransferStatus();
        status.setLength(-1L);
        final Path file = new Path(container, UUID.randomUUID().toString(), EnumSet.of(Path.Type.file));
        final OutputStream out = feature.write(file, status);
        final byte[] content = RandomStringUtils.random(5 * 1024 * 1024).getBytes("UTF-8");
        final ByteArrayInputStream in = new ByteArrayInputStream(content);
        // Adjust buffer to be 5MB. This will write parts with 5MB size which is the minimum allowed
        final byte[] buffer = new byte[1 * 1024 * 1024];
        assertEquals(content.length, IOUtils.copyLarge(in, out, buffer));
        in.close();
        out.close();
        assertTrue(new SwiftFindFeature(session).find(file));
        final byte[] compare = new byte[content.length];
        final InputStream stream = new SwiftReadFeature(session, regionService).read(file, new TransferStatus().length(content.length));
        IOUtils.readFully(stream, compare);
        stream.close();
        assertArrayEquals(content, compare);
        new SwiftDeleteFeature(session).delete(Collections.singletonList(file), new DisabledLoginCallback(), new Delete.DisabledCallback());
        session.close();
    }
}