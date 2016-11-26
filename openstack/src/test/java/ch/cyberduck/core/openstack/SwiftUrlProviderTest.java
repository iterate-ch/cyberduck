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

import ch.cyberduck.core.Credentials;
import ch.cyberduck.core.DescriptiveUrl;
import ch.cyberduck.core.DescriptiveUrlBag;
import ch.cyberduck.core.DisabledCancelCallback;
import ch.cyberduck.core.DisabledHostKeyCallback;
import ch.cyberduck.core.DisabledLoginCallback;
import ch.cyberduck.core.DisabledPasswordStore;
import ch.cyberduck.core.Host;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathCache;
import ch.cyberduck.core.UrlProvider;
import ch.cyberduck.core.features.Delete;
import ch.cyberduck.test.IntegrationTest;

import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.net.URI;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;

import ch.iterate.openstack.swift.model.AccountInfo;
import ch.iterate.openstack.swift.model.Region;

import static org.junit.Assert.*;

@Category(IntegrationTest.class)
public class SwiftUrlProviderTest {

    @Test
    public void testGet() throws Exception {
        final Host host = new Host(new SwiftProtocol(), "identity.api.rackspacecloud.com",
                new Credentials(System.getProperties().getProperty("rackspace.key"), System.getProperties().getProperty("rackspace.secret"))
        );
        final SwiftSession session = new SwiftSession(host);
        session.open(new DisabledHostKeyCallback());
        session.login(new DisabledPasswordStore(), new DisabledLoginCallback(), new DisabledCancelCallback(), PathCache.empty());
        final Path container = new Path("test.cyberduck.ch", EnumSet.of(Path.Type.directory, Path.Type.volume));
        container.attributes().setRegion("DFW");
        assertEquals("https://storage101.dfw1.clouddrive.com/v1/MossoCloudFS_59113590-c679-46c3-bf62-9d7c3d5176ee/test.cyberduck.ch/f",
                new SwiftUrlProvider(session).toUrl(new Path(container, "f", EnumSet.of(Path.Type.file))).find(DescriptiveUrl.Type.provider).getUrl());
        session.close();
    }

    @Test
    public void testSigned() throws Exception {
        final Host host = new Host(new SwiftProtocol(), "identity.api.rackspacecloud.com",
                new Credentials(System.getProperties().getProperty("rackspace.key"), System.getProperties().getProperty("rackspace.secret"))
        );
        final SwiftSession session = new SwiftSession(host);
        final UrlProvider provider = new SwiftUrlProvider(session, session.accounts);
        final Path container = new Path("test.cyberduck.ch", EnumSet.of(Path.Type.directory, Path.Type.volume));
        final Path file = new Path(container, UUID.randomUUID().toString(), EnumSet.of(Path.Type.file));
        session.open(new DisabledHostKeyCallback());
        session.login(new DisabledPasswordStore(), new DisabledLoginCallback(), new DisabledCancelCallback(), PathCache.empty());
        while(session.accounts.isEmpty()) {
            // Account information loaded in thread pool
            Thread.sleep(1000L);
        }
        container.attributes().setRegion("DFW");
        new SwiftTouchFeature(session).touch(file);
        final DescriptiveUrlBag list = provider.toUrl(file);
        final DescriptiveUrl signed = list.find(DescriptiveUrl.Type.signed);
        assertNotNull(signed);
        assertNotEquals(DescriptiveUrl.EMPTY, signed);
        assertFalse(list.filter(DescriptiveUrl.Type.signed).isEmpty());
        for(DescriptiveUrl s : list.filter(DescriptiveUrl.Type.signed)) {
            assertNotNull(s);
            assertNotEquals(DescriptiveUrl.EMPTY, s);
        }
        new SwiftDeleteFeature(session).delete(Collections.singletonList(file), new DisabledLoginCallback(), new Delete.DisabledCallback());
        session.close();
    }

    @Test
    public void testTempUrl() throws Exception {
        final SwiftSession session = new SwiftSession(new Host(new SwiftProtocol(), "identity.api.rackspacecloud.com",
                new Credentials(System.getProperties().getProperty("rackspace.key"), System.getProperties().getProperty("rackspace.secret"))
        ));
        final Region region = new Region("DFW", URI.create("http://storage101.hkg1.clouddrive.com/v1/MossoCloudFS_59113590-c679-46c3-bf62-9d7c3d5176ee"), URI.create("http://m"));
        Map<Region, AccountInfo> accounts = new HashMap<Region, AccountInfo>();
        accounts.put(region, new AccountInfo(1L, 1, "k"));
        final Path container = new Path("test w.cyberduck.ch", EnumSet.of(Path.Type.directory, Path.Type.volume));
        final Path file = new Path(container, "key f", EnumSet.of(Path.Type.file));
        final Path file2 = new Path(file, "key2", EnumSet.of(Path.Type.file));
        final SwiftUrlProvider provider = new SwiftUrlProvider(session, accounts);
        final Iterator<DescriptiveUrl> iterator = provider.sign(region, file2, 1379500716).iterator();
        assertEquals("http://storage101.hkg1.clouddrive.com/v1/MossoCloudFS_59113590-c679-46c3-bf62-9d7c3d5176ee/test%20w.cyberduck.ch/key%20f/key2?temp_url_sig=a079831228bfea78853f1951e4d10f2599782219&temp_url_expires=1379500716",
                iterator.next().getUrl());
    }
}
