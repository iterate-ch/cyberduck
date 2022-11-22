package ch.cyberduck.core.hubic;

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

import ch.cyberduck.core.Profile;
import ch.cyberduck.core.Protocol;
import ch.cyberduck.core.ProtocolFactory;
import ch.cyberduck.core.serializer.impl.dd.ProfilePlistReader;

import org.junit.Test;

import java.util.Collections;
import java.util.HashSet;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

public class HubicProtocolTest {

    @Test
    public void testFeatures() {
        assertEquals(Protocol.Case.sensitive, new HubicProtocol().getCaseSensitivity());
        assertEquals(Protocol.DirectoryTimestamp.explicit, new HubicProtocol().getDirectoryTimestamp());
    }

    @Test
    public void testPrefix() {
        assertEquals("ch.cyberduck.core.hubic.Hubic", new HubicProtocol().getPrefix());
    }

    @Test
    public void testDefaultProfile() throws Exception {
        final ProtocolFactory factory = new ProtocolFactory(new HashSet<>(Collections.singleton(new HubicProtocol())));
        final Profile profile = new ProfilePlistReader(factory).read(
                this.getClass().getResourceAsStream("/hubiC.cyberduckprofile"));
        assertFalse(profile.isHostnameConfigurable());
        assertFalse(profile.isPortConfigurable());
        assertFalse(profile.isUsernameConfigurable());
        assertFalse(profile.isPasswordConfigurable());
    }
}
