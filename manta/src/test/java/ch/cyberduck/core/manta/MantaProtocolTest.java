package ch.cyberduck.core.manta;

/*
 * Copyright (c) 2002-2017 iterate GmbH. All rights reserved.
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
import ch.cyberduck.core.LoginOptions;
import ch.cyberduck.core.NullLocal;
import ch.cyberduck.core.Profile;
import ch.cyberduck.core.Protocol;
import ch.cyberduck.core.ProtocolFactory;
import ch.cyberduck.core.serializer.impl.dd.ProfilePlistReader;

import org.junit.Test;

import java.util.Collections;
import java.util.HashSet;

import static org.junit.Assert.*;

public class MantaProtocolTest {

    @Test
    public void testFeatures() {
        assertEquals(Protocol.Case.sensitive, new MantaProtocol().getCaseSensitivity());
        assertEquals(Protocol.DirectoryTimestamp.explicit, new MantaProtocol().getDirectoryTimestamp());
    }

    @Test
    public void testPrefix() {
        assertEquals("ch.cyberduck.core.manta.Manta", new MantaProtocol().getPrefix());
    }

    @Test
    public void testDefaultProfile() throws Exception {
        final ProtocolFactory factory = new ProtocolFactory(new HashSet<>(Collections.singleton(new MantaProtocol())));
        final Profile profile = new ProfilePlistReader(factory).read(
                this.getClass().getResourceAsStream("/Joyent Triton Object Storage (us-east).cyberduckprofile"));
        assertTrue(profile.isHostnameConfigurable());
        assertFalse(profile.isPortConfigurable());
        assertTrue(profile.isUsernameConfigurable());
        assertFalse(profile.isCertificateConfigurable());
        assertFalse(profile.isPasswordConfigurable());
    }

    @Test
    public void testValidateCredentials() throws Exception {
        final ProtocolFactory factory = new ProtocolFactory(new HashSet<>(Collections.singleton(new MantaProtocol())));
        final Profile profile = new ProfilePlistReader(factory).read(
                this.getClass().getResourceAsStream("/Joyent Triton Object Storage (us-east).cyberduckprofile"));
        assertFalse(profile.validate(new Credentials(), new LoginOptions(profile)));
        assertFalse(profile.validate(new Credentials("u@domain"), new LoginOptions(profile)));
        assertFalse(profile.validate(new Credentials("u@domain", "p"), new LoginOptions(profile)));
        assertFalse(profile.validate(new Credentials("u@domain").withIdentity(new NullLocal("/f") {
            @Override
            public boolean exists() {
                return false;
            }
        }), new LoginOptions(profile)));
        assertTrue(profile.validate(new Credentials("u@domain").withIdentity(new NullLocal("/f") {
            @Override
            public boolean exists() {
                return true;
            }
        }), new LoginOptions(profile)));
    }
}
