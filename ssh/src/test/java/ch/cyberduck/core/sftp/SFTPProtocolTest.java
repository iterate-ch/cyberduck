package ch.cyberduck.core.sftp;

/*
 * Copyright (c) 2002-2014 David Kocher. All rights reserved.
 * http://cyberduck.io/
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
 * feedback@cyberduck.io
 */

import ch.cyberduck.core.Credentials;
import ch.cyberduck.core.LoginOptions;

import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class SFTPProtocolTest {

    @Test
    public void testValidateToken() throws Exception {
        assertTrue(new SFTPProtocol().validate(new Credentials(null, "123414"), new LoginOptions().user(false).publickey(false).keychain(false)));
        assertFalse(new SFTPProtocol().validate(new Credentials("", "123414"), new LoginOptions().user(true).publickey(false).keychain(false)));
    }
}