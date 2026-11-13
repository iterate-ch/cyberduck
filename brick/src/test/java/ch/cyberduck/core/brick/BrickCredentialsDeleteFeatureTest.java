package ch.cyberduck.core.brick;

/*
 * Copyright (c) 2002-2026 iterate GmbH. All rights reserved.
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

import ch.cyberduck.core.Credentials;
import ch.cyberduck.core.Host;
import ch.cyberduck.core.HostPasswordStore;

import org.junit.Test;

import static org.mockito.Mockito.*;

public class BrickCredentialsDeleteFeatureTest {
    @Test
    public void testExplicitDeletionCleansBothCredentialKinds() throws Exception {
        final Host host = new Host(new BrickProtocol(), "customer.files.com", new Credentials("jane"));
        final HostPasswordStore store = mock(HostPasswordStore.class);
        new BrickCredentialsDeleteFeature(store).delete(host);
        verify(store).deleteLoginPassword(host);
        verify(store).delete(host);
    }

    @Test
    public void testLegacyUsernameLessPasswordSlotIsRemovedLocally() throws Exception {
        final Host host = new Host(new BrickProtocol(), "customer.files.com");
        final HostPasswordStore store = mock(HostPasswordStore.class);
        new BrickCredentialsDeleteFeature(store).delete(host);
        verify(store).deletePassword(host.getProtocol().getScheme(), host.getPort(), host.getHostname(), host.getProtocol().getPasswordPlaceholder());
        verify(store).delete(host);
    }
}
