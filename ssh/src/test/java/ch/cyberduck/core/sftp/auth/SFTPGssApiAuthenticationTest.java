package ch.cyberduck.core.sftp.auth;

/*
 * Copyright (c) 2002-2024 iterate GmbH. All rights reserved.
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
import ch.cyberduck.core.LoginCallback;
import ch.cyberduck.core.sftp.SFTPProtocol;
import ch.cyberduck.core.threading.CancelCallback;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.PrintWriter;

import static org.junit.Assert.*;

public class SFTPGssApiAuthenticationTest {

    private String savedKrb5Conf;

    @Before
    public void saveKrb5Conf() {
        savedKrb5Conf = System.getProperty("java.security.krb5.conf");
    }

    @After
    public void restoreKrb5Conf() {
        if(savedKrb5Conf != null) {
            System.setProperty("java.security.krb5.conf", savedKrb5Conf);
        }
        else {
            System.clearProperty("java.security.krb5.conf");
        }
    }

    @Test
    public void testGetMethod() {
        assertEquals("gssapi-with-mic", new SFTPGssApiAuthentication(null).getMethod());
    }

    @Test
    public void testAuthenticateNoTgtReturnsFalseWithPresetKrb5Conf() throws Exception {
        // When java.security.krb5.conf is already set, the code must leave it unchanged on exit —
        // even when loginContext.login() fails because no TGT is available.
        final File sentinel = File.createTempFile("cyberduck-test-krb5-", ".conf");
        sentinel.deleteOnExit();
        try(PrintWriter w = new PrintWriter(sentinel)) {
            w.println("[libdefaults]");
            w.println("    default_realm = NONEXISTENT.INVALID");
        }
        System.setProperty("java.security.krb5.conf", sentinel.getAbsolutePath());

        final Host host = new Host(new SFTPProtocol(), "test.nonexistent.invalid", new Credentials("user", ""));
        // No TGT exists for NONEXISTENT.INVALID; login fails and authenticate() must return false.
        assertFalse(new SFTPGssApiAuthentication(null).authenticate(host, LoginCallback.noop, CancelCallback.noop));
        assertEquals("java.security.krb5.conf must be unchanged after login failure",
                sentinel.getAbsolutePath(), System.getProperty("java.security.krb5.conf"));
        sentinel.delete();
    }

    @Test
    public void testAuthenticateNoTgtClearsTemporaryKrb5Conf() throws Exception {
        // When no java.security.krb5.conf is set, the code writes a temp file and sets the property.
        // After login failure the property must be cleared so subsequent auth methods see a clean state.
        System.clearProperty("java.security.krb5.conf");

        final Host host = new Host(new SFTPProtocol(), "test.nonexistent.invalid", new Credentials("user", ""));
        assertFalse(new SFTPGssApiAuthentication(null).authenticate(host, LoginCallback.noop, CancelCallback.noop));
        assertNull("java.security.krb5.conf must be cleared after login failure",
                System.getProperty("java.security.krb5.conf"));
    }
}
