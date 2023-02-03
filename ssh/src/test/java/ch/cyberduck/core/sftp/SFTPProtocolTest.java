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
import ch.cyberduck.core.Protocol;

import org.junit.Test;

import static org.junit.Assert.*;

public class SFTPProtocolTest {

    @Test
    public void testFeatures() {
        assertEquals(Protocol.Case.sensitive, new SFTPProtocol().getCaseSensitivity());
        assertEquals(Protocol.DirectoryTimestamp.implicit, new SFTPProtocol().getDirectoryTimestamp());
    }

    @Test
    public void testValidateToken() {
        assertTrue(new SFTPProtocol().validate(new Credentials(null, "123414"), new LoginOptions().user(false).publickey(false).keychain(false)));
        assertFalse(new SFTPProtocol().validate(new Credentials("", "123414"), new LoginOptions().user(true).publickey(false).keychain(false)));
    }

    @Test
    public void testValidateCredentialsEmpty() {
        Credentials c = new Credentials("user", "");
        assertTrue(c.validate(new SFTPProtocol(), new LoginOptions(new SFTPProtocol())));
    }

    @Test
    public void testValidateCredentialsBlank() {
        Credentials c = new Credentials("user", " ");
        assertTrue(c.validate(new SFTPProtocol(), new LoginOptions(new SFTPProtocol())));
    }
}
