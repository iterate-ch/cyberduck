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

import org.ietf.jgss.GSSManager;
import org.ietf.jgss.GSSName;
import org.junit.Test;

import javax.security.auth.Subject;

import static org.junit.Assert.*;

public class KerberosRealmGSSManagerTest {

    @Test
    public void testToPrincipalConfiguredRealm() {
        assertEquals("host/server.nonexistent.invalid@EXAMPLE.COM",
                new KerberosRealmGSSManager(GSSManager.getInstance(), " EXAMPLE.COM ", new Subject())
                        .toPrincipal("host@server.nonexistent.invalid"));
    }

    @Test
    public void testToPrincipalNoHost() {
        assertNull(new KerberosRealmGSSManager(GSSManager.getInstance(), "EXAMPLE.COM", new Subject()).toPrincipal("host"));
        assertNull(new KerberosRealmGSSManager(GSSManager.getInstance(), "EXAMPLE.COM", new Subject()).toPrincipal("@server"));
    }

    @Test
    public void testCreateNameConfiguredRealm() throws Exception {
        final GSSName name = new KerberosRealmGSSManager(GSSManager.getInstance(), "EXAMPLE.COM", new Subject())
                .createName("host@server.nonexistent.invalid", GSSName.NT_HOSTBASED_SERVICE);
        assertEquals("host/server.nonexistent.invalid@EXAMPLE.COM", name.toString());
        assertEquals(KerberosRealmGSSManager.NT_KRB5_PRINCIPAL_NAME, name.getStringNameType());
    }

    @Test
    public void testCreateNameUserName() throws Exception {
        final GSSName name = new KerberosRealmGSSManager(GSSManager.getInstance(), "EXAMPLE.COM", new Subject())
                .createName("user@OTHER.ORG", GSSName.NT_USER_NAME);
        assertEquals("user@OTHER.ORG", name.toString());
        assertEquals(GSSName.NT_USER_NAME, name.getStringNameType());
    }
}
