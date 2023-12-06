package ch.cyberduck.core.importer;

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

import ch.cyberduck.core.Local;
import ch.cyberduck.core.ProtocolFactory;
import ch.cyberduck.core.Scheme;
import ch.cyberduck.core.TestProtocol;
import ch.cyberduck.core.exception.AccessDeniedException;

import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;

import static org.junit.Assert.assertEquals;

public class TotalCommanderBookmarkCollectionTest {

    @Test(expected = AccessDeniedException.class)
    public void testParseNotFound() throws Exception {
        new TotalCommanderBookmarkCollection().parse(new ProtocolFactory(Collections.emptySet()), new Local(System.getProperty("java.io.tmpdir"), "f"));
    }

    @Test
    public void testParse() throws Exception {
        TotalCommanderBookmarkCollection c = new TotalCommanderBookmarkCollection();
        assertEquals(0, c.size());
        c.parse(new ProtocolFactory(new HashSet<>(Arrays.asList(new TestProtocol(Scheme.ftp), new TestProtocol(Scheme.ftps), new TestProtocol(Scheme.sftp)))), new Local("src/test/resources/wcx_ftp.ini"));
        assertEquals(2, c.size());
        assertEquals("sudo.ch", c.get(0).getHostname());
        assertEquals("fo|cyberduck.io session bookmark", c.get(1).getNickname());  /* pragma: codespell-ignore */
        assertEquals("cyberduck.io", c.get(1).getHostname());
        assertEquals("/remote", c.get(1).getDefaultPath());
    }
}
