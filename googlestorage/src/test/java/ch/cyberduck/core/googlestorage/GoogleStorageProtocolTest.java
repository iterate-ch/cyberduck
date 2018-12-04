package ch.cyberduck.core.googlestorage;

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

import ch.cyberduck.core.Local;
import ch.cyberduck.core.Profile;
import ch.cyberduck.core.ProtocolFactory;
import ch.cyberduck.core.Scheme;
import ch.cyberduck.core.serializer.impl.dd.ProfilePlistReader;

import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;

import static org.junit.Assert.*;

public class GoogleStorageProtocolTest {

    @Test
    public void testPrefix() {
        assertEquals("ch.cyberduck.core.googlestorage.GoogleStorage", new GoogleStorageProtocol().getPrefix());
    }

    @Test
    public void testPassword() {
        assertFalse(new GoogleStorageProtocol().isPasswordConfigurable());
    }

    @Test
    public void testConfigurable() {
        assertFalse(new GoogleStorageProtocol().isHostnameConfigurable());
        assertFalse(new GoogleStorageProtocol().isPortConfigurable());
    }

    @Test
    public void testSchemes() {
        assertTrue(Arrays.asList(new GoogleStorageProtocol().getSchemes()).contains(Scheme.https.name()));
    }

    @Test
    public void testDefaultProfile() throws Exception {
        final ProtocolFactory factory = new ProtocolFactory(new HashSet<>(Collections.singleton(new GoogleStorageProtocol())));
        final Profile profile = new ProfilePlistReader(factory).read(
            new Local("../profiles/default/Google Cloud Storage.cyberduckprofile"));
        assertFalse(profile.isHostnameConfigurable());
        assertFalse(profile.isPortConfigurable());
        assertTrue(profile.isUsernameConfigurable());
        assertFalse(profile.isPasswordConfigurable());
    }
}
