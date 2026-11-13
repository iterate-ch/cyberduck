package ch.cyberduck.core.ssl;

/*
 * Copyright (c) 2002-2026 iterate GmbH. All rights reserved.
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

import ch.cyberduck.core.DisabledCertificateStore;
import ch.cyberduck.core.Host;
import ch.cyberduck.core.TestProtocol;

import org.junit.Test;

import java.net.Socket;

import static org.junit.Assert.assertEquals;

public class PreferencesX509KeyManagerTest {

    @Test
    public void testChooseClientAliasFromBookmark() {
        final Host bookmark = new Host(new TestProtocol(), "localhost");
        bookmark.getCredentials().setCertificate("alias");
        assertEquals("alias", new PreferencesX509KeyManager(bookmark, new DisabledCertificateStore())
            .chooseClientAlias(new String[]{"RSA"}, null, new Socket()));
    }
}
