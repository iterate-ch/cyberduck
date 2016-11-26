package ch.cyberduck.core.openstack;

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
import ch.cyberduck.core.DisabledLoginCallback;
import ch.cyberduck.core.DisabledPasswordStore;
import ch.cyberduck.core.DisabledTranscriptListener;
import ch.cyberduck.core.Host;
import ch.cyberduck.core.PathCache;
import ch.cyberduck.core.features.Quota;
import ch.cyberduck.test.IntegrationTest;

import org.junit.Test;
import org.junit.experimental.categories.Category;

import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;

@Category(IntegrationTest.class)
public class SwiftQuotaFeatureTest {
    @Test
    public void testGet() throws Exception {
        final Host host = new Host(new SwiftProtocol(), "identity.api.rackspacecloud.com", new Credentials(
                System.getProperties().getProperty("rackspace.key"), System.getProperties().getProperty("rackspace.secret")
        ));
        final SwiftSession session = new SwiftSession(host).withAccountPreload(false).withCdnPreload(false).withContainerPreload(false);
        session.open(new DisabledHostKeyCallback(), new DisabledTranscriptListener());
        session.login(new DisabledPasswordStore(), new DisabledLoginCallback(), new DisabledCancelCallback(), PathCache.empty());
        final Quota.Space quota = new SwiftQuotaFeature(session).get();
        assertNotNull(quota.available);
        assertNotNull(quota.used);
        assertNotEquals(0L, quota.available, 0L);
        assertNotEquals(0L, quota.used, 0L);
        session.close();
    }
}