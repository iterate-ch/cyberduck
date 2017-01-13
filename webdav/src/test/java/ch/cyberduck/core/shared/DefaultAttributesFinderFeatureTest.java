package ch.cyberduck.core.shared;

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

import ch.cyberduck.core.AttributedList;
import ch.cyberduck.core.Attributes;
import ch.cyberduck.core.Credentials;
import ch.cyberduck.core.DisabledCancelCallback;
import ch.cyberduck.core.DisabledHostKeyCallback;
import ch.cyberduck.core.DisabledLoginCallback;
import ch.cyberduck.core.DisabledPasswordStore;
import ch.cyberduck.core.Host;
import ch.cyberduck.core.ListProgressListener;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathCache;
import ch.cyberduck.core.dav.DAVDeleteFeature;
import ch.cyberduck.core.dav.DAVProtocol;
import ch.cyberduck.core.dav.DAVSSLProtocol;
import ch.cyberduck.core.dav.DAVSession;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.exception.NotfoundException;
import ch.cyberduck.core.features.Delete;
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
public class DefaultAttributesFinderFeatureTest {

    @Test(expected = NotfoundException.class)
    public void testNotFound() throws Exception {
        final Host host = new Host(new DAVProtocol(), "test.cyberduck.ch", new Credentials(
                System.getProperties().getProperty("webdav.user"), System.getProperties().getProperty("webdav.password")
        ));
        host.setDefaultPath("/dav/basic");
        final DAVSession session = new DAVSession(host);
        session.open(new DisabledHostKeyCallback());
        session.login(new DisabledPasswordStore(), new DisabledLoginCallback(), new DisabledCancelCallback(), PathCache.empty());
        new DefaultAttributesFinderFeature(session).find(new Path(new DefaultHomeFinderService(session).find(), UUID.randomUUID().toString(), EnumSet.of(Path.Type.file)));
    }

    @Test
    public void testAttributes() throws Exception {
        final Host host = new Host(new DAVProtocol(), "test.cyberduck.ch", new Credentials(
                System.getProperties().getProperty("webdav.user"), System.getProperties().getProperty("webdav.password")
        ));
        host.setDefaultPath("/dav/basic");
        final AtomicBoolean set = new AtomicBoolean();
        final DAVSession session = new DAVSession(host) {
            @Override
            public AttributedList<Path> list(final Path file, final ListProgressListener listener) throws BackgroundException {
                final AttributedList<Path> list = super.list(file, listener);
                set.set(true);
                return list;
            }
        };
        session.open(new DisabledHostKeyCallback());
        session.login(new DisabledPasswordStore(), new DisabledLoginCallback(), new DisabledCancelCallback(), PathCache.empty());
        final PathCache cache = new PathCache(1);
        final DefaultAttributesFinderFeature f = new DefaultAttributesFinderFeature(session).withCache(cache);
        final String name = UUID.randomUUID().toString();
        final Path file = new Path(new DefaultHomeFinderService(session).find(), name, EnumSet.of(Path.Type.file));
        new DefaultTouchFeature(session).touch(file, new TransferStatus());
        final Attributes attributes = f.find(file);
        assertEquals(0L, attributes.getSize());
        // Test cache
        assertEquals(0L, f.find(file).getSize());
        assertTrue(cache.containsKey(file.getParent()));
        // Test wrong type
        try {
            f.find(new Path(new DefaultHomeFinderService(session).find(), name, EnumSet.of(Path.Type.directory)));
            fail();
        }
        catch(NotfoundException e) {
            // Expected
        }
        new DAVDeleteFeature(session).delete(Collections.singletonList(file), new DisabledLoginCallback(), new Delete.DisabledCallback());
        session.close();
    }

    @Test
    public void testFindNoWebDAV() throws Exception {
        final Host host = new Host(new DAVSSLProtocol(), "update.cyberduck.io");
        final AtomicBoolean set = new AtomicBoolean();
        final DAVSession session = new DAVSession(host) {
            @Override
            public AttributedList<Path> list(final Path file, final ListProgressListener listener) throws BackgroundException {
                final AttributedList<Path> list = super.list(file, listener);
                set.set(true);
                return list;
            }
        };
        session.open(new DisabledHostKeyCallback());
        session.login(new DisabledPasswordStore(), new DisabledLoginCallback(), new DisabledCancelCallback(), PathCache.empty());
        final DefaultAttributesFinderFeature f = new DefaultAttributesFinderFeature(session);
        final Path file = new Path("/robots.txt", EnumSet.of(Path.Type.file));
        final Attributes attributes = f.find(file);
        assertNotNull(attributes);
        session.close();
    }
}
