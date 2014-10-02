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

import ch.cyberduck.core.AbstractTestCase;
import ch.cyberduck.core.Credentials;
import ch.cyberduck.core.DescriptiveUrl;
import ch.cyberduck.core.DescriptiveUrlBag;
import ch.cyberduck.core.DisabledCancelCallback;
import ch.cyberduck.core.DisabledHostKeyCallback;
import ch.cyberduck.core.DisabledLoginController;
import ch.cyberduck.core.DisabledPasswordStore;
import ch.cyberduck.core.DisabledProgressListener;
import ch.cyberduck.core.DisabledTranscriptListener;
import ch.cyberduck.core.Host;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.UrlProvider;

import org.junit.Test;

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

/**
 * @version $Id$
 */
public class SwiftUrlProviderTest extends AbstractTestCase {

    @Test
    public void testGet() throws Exception {
        final SwiftSession session = new SwiftSession(new Host(new SwiftProtocol(), "identity.api.rackspacecloud.com",
                new Credentials(properties.getProperty("rackspace.key"), properties.getProperty("rackspace.secret"))
        ));
        session.open(new DisabledHostKeyCallback(), session);
        session.login(new DisabledPasswordStore(), new DisabledLoginController(), new DisabledCancelCallback(), new DisabledTranscriptListener());
        final Path container = new Path("test.cyberduck.ch", EnumSet.of(Path.Type.directory, Path.Type.volume));
        container.attributes().setRegion("DFW");
        assertEquals("https://storage101.dfw1.clouddrive.com/v1/MossoCloudFS_59113590-c679-46c3-bf62-9d7c3d5176ee/test.cyberduck.ch/f",
                new SwiftUrlProvider(session).toUrl(new Path(container, "f", EnumSet.of(Path.Type.file))).find(DescriptiveUrl.Type.provider).getUrl());
        session.close();
    }

    @Test
    public void testSigned() throws Exception {
        final SwiftSession session = new SwiftSession(new Host(new SwiftProtocol(), "identity.api.rackspacecloud.com",
                new Credentials(properties.getProperty("rackspace.key"), properties.getProperty("rackspace.secret"))
        ));
        final UrlProvider provider = new SwiftUrlProvider(session, session.accounts);
        final Path container = new Path("test.cyberduck.ch", EnumSet.of(Path.Type.directory, Path.Type.volume));
        final Path file = new Path(container, UUID.randomUUID().toString(), EnumSet.of(Path.Type.file));
        assertEquals(DescriptiveUrl.EMPTY, provider.toUrl(file).find(DescriptiveUrl.Type.signed));
        session.open(new DisabledHostKeyCallback(), session);
        session.login(new DisabledPasswordStore(), new DisabledLoginController(), new DisabledCancelCallback(), new DisabledTranscriptListener());
        container.attributes().setRegion("DFW");
        new SwiftTouchFeature(session).touch(file);
        final DescriptiveUrlBag list = provider.toUrl(file);
        assertNotNull(list.find(DescriptiveUrl.Type.signed));
        if(session.accounts.containsKey(new SwiftRegionService(session).lookup(container))) {
            if(session.accounts.get(new SwiftRegionService(session).lookup(container)).getTempUrlKey() != null) {
                assertNotEquals(DescriptiveUrl.EMPTY, list.find(DescriptiveUrl.Type.signed));
            }
        }
        new SwiftDeleteFeature(session).delete(Collections.singletonList(file), new DisabledLoginController(), new DisabledProgressListener());
        session.close();
    }

    @Test
    public void testTempUrl() throws Exception {
        final SwiftSession session = new SwiftSession(new Host(new SwiftProtocol(), "identity.api.rackspacecloud.com",
                new Credentials(properties.getProperty("rackspace.key"), properties.getProperty("rackspace.secret"))
        ));
        final Region region = new Region("DFW", URI.create("http://storage101.hkg1.clouddrive.com/v1/MossoCloudFS_59113590-c679-46c3-bf62-9d7c3d5176ee"), URI.create("http://m"));
        Map accounts = new HashMap();
        accounts.put(region, new AccountInfo(1L, 1, "k"));
        final Path container = new Path("test w.cyberduck.ch", EnumSet.of(Path.Type.directory, Path.Type.volume));
        final Path file = new Path(container, "key", EnumSet.of(Path.Type.file));
        final SwiftUrlProvider provider = new SwiftUrlProvider(session, accounts);
        final Iterator<DescriptiveUrl> iterator = provider.createTempUrl(region, file, 1379500716L).iterator();
        assertEquals("http://storage101.hkg1.clouddrive.com/v1/MossoCloudFS_59113590-c679-46c3-bf62-9d7c3d5176ee/test%20w.cyberduck.ch/key?temp_url_sig=0b08dd5b2b48aff5c0269cf4e3ca3afdeaf9c7a5&temp_url_expires=1379500716",
                iterator.next().getUrl());
    }
}
