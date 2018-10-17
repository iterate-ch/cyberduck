package ch.cyberduck.core.openstack;

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

import ch.cyberduck.core.AlphanumericRandomStringService;
import ch.cyberduck.core.Credentials;
import ch.cyberduck.core.DisabledCancelCallback;
import ch.cyberduck.core.DisabledHostKeyCallback;
import ch.cyberduck.core.DisabledLoginCallback;
import ch.cyberduck.core.DisabledPasswordStore;
import ch.cyberduck.core.Host;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.TranscriptListener;
import ch.cyberduck.core.features.Delete;
import ch.cyberduck.core.proxy.Proxy;
import ch.cyberduck.core.shared.DefaultFindFeature;
import ch.cyberduck.core.transfer.TransferStatus;
import ch.cyberduck.test.IntegrationTest;

import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.util.Collections;
import java.util.EnumSet;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.Assert.*;

@Category(IntegrationTest.class)
public class SwiftDirectoryFeatureTest {

    @Test
    public void testCreateContainer() throws Exception {
        final Host host = new Host(new SwiftProtocol(), "identity.api.rackspacecloud.com", new Credentials(
            System.getProperties().getProperty("rackspace.key"), System.getProperties().getProperty("rackspace.secret")
        ));
        final SwiftSession session = new SwiftSession(host);
        session.open(Proxy.DIRECT, new DisabledHostKeyCallback(), new DisabledLoginCallback());
        session.login(Proxy.DIRECT, new DisabledPasswordStore(), new DisabledLoginCallback(), new DisabledCancelCallback());
        final SwiftDirectoryFeature feature = new SwiftDirectoryFeature(session, new SwiftRegionService(session), new SwiftWriteFeature(session, new SwiftRegionService(session)));
        final Path container = feature.mkdir(new Path(new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory)), "ORD", new TransferStatus());
        assertTrue(new SwiftFindFeature(session).find(container));
        assertEquals(container.attributes(), new SwiftAttributesFinderFeature(session).find(container));
        new SwiftDeleteFeature(session).delete(Collections.singletonList(container), new DisabledLoginCallback(), new Delete.DisabledCallback());
        assertFalse(new SwiftFindFeature(session).find(container));
        session.close();
    }

    @Test
    public void testCreatePlaceholder() throws Exception {
        final Host host = new Host(new SwiftProtocol(), "identity.api.rackspacecloud.com", new Credentials(
            System.getProperties().getProperty("rackspace.key"), System.getProperties().getProperty("rackspace.secret")
        ));
        final SwiftSession session = new SwiftSession(host);
        final AtomicBoolean put = new AtomicBoolean();
        final String name = UUID.randomUUID().toString();
        session.withListener(new TranscriptListener() {
            @Override
            public void log(final Type request, final String message) {
                switch(request) {
                    case request:
                        if(("PUT /v1/MossoCloudFS_59113590-c679-46c3-bf62-9d7c3d5176ee/test-iad-cyberduck/" + name + " HTTP/1.1").equals(message)) {
                            put.set(true);
                        }
                }
            }
        });
        session.open(Proxy.DIRECT, new DisabledHostKeyCallback(), new DisabledLoginCallback());
        session.login(Proxy.DIRECT, new DisabledPasswordStore(), new DisabledLoginCallback(), new DisabledCancelCallback());
        final Path container = new Path("/test-iad-cyberduck", EnumSet.of(Path.Type.volume, Path.Type.directory));
        container.attributes().setRegion("IAD");
        final SwiftDirectoryFeature feature = new SwiftDirectoryFeature(session, new SwiftRegionService(session), new SwiftWriteFeature(session, new SwiftRegionService(session)));
        final Path placeholder = feature.mkdir(new Path(container, name, EnumSet.of(Path.Type.directory)), null, new TransferStatus());
        Thread.sleep(1000L);
        assertTrue(put.get());
        assertTrue(new SwiftFindFeature(session).find(placeholder));
        assertTrue(new DefaultFindFeature(session).find(placeholder));
        assertEquals(placeholder.attributes().getChecksum(), new SwiftAttributesFinderFeature(session).find(placeholder).getChecksum());
        new SwiftDeleteFeature(session).delete(Collections.singletonList(placeholder), new DisabledLoginCallback(), new Delete.DisabledCallback());
        assertFalse(new SwiftFindFeature(session).find(placeholder));
        session.close();
    }
}
