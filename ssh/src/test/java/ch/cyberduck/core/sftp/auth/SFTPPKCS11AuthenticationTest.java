package ch.cyberduck.core.sftp.auth;

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

import ch.cyberduck.core.DisabledLoginCallback;
import ch.cyberduck.core.Host;
import ch.cyberduck.core.TestProtocol;
import ch.cyberduck.core.threading.DisabledCancelCallback;

import org.junit.Test;

import net.schmizz.sshj.SSHClient;

import static org.junit.Assert.assertFalse;

public class SFTPPKCS11AuthenticationTest {

    @Test
    public void testSkipFailureLoadingLibrary() throws Exception {
        final SFTPPKCS11Authentication authentication = new SFTPPKCS11Authentication(new SSHClient(), "n.so");
        assertFalse(authentication.authenticate(new Host(new TestProtocol()), new DisabledLoginCallback(), new DisabledCancelCallback()));
    }
}
