package ch.cyberduck.core.smb;

/*
 * Copyright (c) 2002-2023 iterate GmbH. All rights reserved.
 * https://cyberduck.io/
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 */

import ch.cyberduck.core.DisabledCancelCallback;
import ch.cyberduck.core.DisabledHostKeyCallback;
import ch.cyberduck.core.DisabledLoginCallback;
import ch.cyberduck.core.Host;
import ch.cyberduck.core.ListService;
import ch.cyberduck.core.PathContainerService;
import ch.cyberduck.core.cdn.DistributionConfiguration;
import ch.cyberduck.core.exception.ConnectionRefusedException;
import ch.cyberduck.core.features.AttributesFinder;
import ch.cyberduck.core.features.Copy;
import ch.cyberduck.core.features.Delete;
import ch.cyberduck.core.features.Move;
import ch.cyberduck.core.features.Quota;
import ch.cyberduck.core.features.Read;
import ch.cyberduck.core.features.Touch;
import ch.cyberduck.core.features.UnixPermission;
import ch.cyberduck.core.features.Write;
import ch.cyberduck.core.proxy.Proxy;
import ch.cyberduck.test.TestcontainerTest;

import org.junit.Test;
import org.junit.experimental.categories.Category;

import static org.junit.Assert.*;

@Category(TestcontainerTest.class)
public class SMBSessionTest extends AbstractSMBTest {

    @Test
    public void testConnectRefused() {
        final Host host = new Host(new SMBProtocol(), session.getHost().getHostname(), 135)
                .withCredentials(session.getHost().getCredentials());
        final SMBSession session = new SMBSession(host);
        assertThrows(ConnectionRefusedException.class, () -> session.open(Proxy.DIRECT, new DisabledHostKeyCallback(), new DisabledLoginCallback(), new DisabledCancelCallback()));
    }

    @Test
    public void testFeatures() {
        assertNull(session.getFeature(UnixPermission.class));
        assertNull(session.getFeature(DistributionConfiguration.class));
        assertNotNull(session.getFeature(Touch.class));
        assertNotNull(session.getFeature(AttributesFinder.class));
        assertNotNull(session.getFeature(Copy.class));
        assertNotNull(session.getFeature(Move.class));
        assertNotNull(session.getFeature(Delete.class));
        assertNotNull(session.getFeature(ListService.class));
        assertSame(session.getFeature(ListService.class), session.getFeature(ListService.class));
        assertNotNull(session.getFeature(PathContainerService.class));
        assertNotNull(session.getFeature(Quota.class));
        assertNotNull(session.getFeature(Read.class));
        assertNotNull(session.getFeature(Write.class));
    }
}
