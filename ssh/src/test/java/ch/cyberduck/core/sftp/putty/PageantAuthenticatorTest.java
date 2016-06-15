package ch.cyberduck.core.sftp.putty;

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

import ch.cyberduck.core.Factory;

import org.junit.Test;

import java.util.Collection;

import com.jcraft.jsch.agentproxy.Identity;

import static org.junit.Assert.*;

public class PageantAuthenticatorTest {

    @Test
    public void testGetIdentities() throws Exception {
        final PageantAuthenticator authenticator = new PageantAuthenticator();
        final Collection<Identity> identities = authenticator.getIdentities();
        switch(Factory.Platform.getDefault()) {
            case windows:
                assertNotNull(authenticator.getProxy());
                break;
            default:
                assertNull(authenticator.getProxy());
                assertTrue(identities.isEmpty());
                break;
        }
    }
}
