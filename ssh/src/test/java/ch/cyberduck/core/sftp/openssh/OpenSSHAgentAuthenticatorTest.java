package ch.cyberduck.core.sftp.openssh;

/*
 * Copyright (c) 2002-2014 David Kocher. All rights reserved.
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
 * Bug fixes, suggestions and comments should be sent to:
 * feedback@cyberduck.ch
 */

import org.junit.Ignore;
import org.junit.Test;

import java.util.Collection;

import com.jcraft.jsch.agentproxy.Identity;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;

public class OpenSSHAgentAuthenticatorTest {

    @Test
    @Ignore
    public void testGetIdentities() throws Exception {
        final OpenSSHAgentAuthenticator authenticator = new OpenSSHAgentAuthenticator();
        final Collection<Identity> identities = authenticator.getIdentities();
        assertNotNull(authenticator.getProxy());
        assertFalse(identities.isEmpty());
    }
}
