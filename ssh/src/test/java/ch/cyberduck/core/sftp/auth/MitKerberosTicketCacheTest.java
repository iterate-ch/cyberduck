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

import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

import static org.junit.Assert.*;

public class MitKerberosTicketCacheTest {

    @Test
    public void testParseDefaultPrincipal() {
        assertEquals("bug@UPENN.EDU", MitKerberosTicketCache.parseDefaultPrincipal(
                "Ticket cache: API:Initial default ccache\r\nDefault principal: bug@UPENN.EDU\r\n\r\n" +
                        "Valid starting     Expires            Service principal\r\n"));
    }

    @Test
    public void testParseDefaultPrincipalNoTicket() {
        assertNull(MitKerberosTicketCache.parseDefaultPrincipal("klist: No credentials cache found\r\n"));
        assertNull(MitKerberosTicketCache.parseDefaultPrincipal("Default principal: bug\r\n"));
    }

    @Test
    public void testEmptyCache() throws IOException {
        final DataInputStream in = new DataInputStream(new ByteArrayInputStream(
                MitKerberosTicketCache.emptyCache("bug@UPENN.EDU")));
        assertEquals(0x0504, in.readUnsignedShort());
        assertEquals(12, in.readUnsignedShort());
        assertEquals(1, in.readUnsignedShort());
        assertEquals(8, in.readUnsignedShort());
        assertEquals(0, in.readInt());
        assertEquals(0, in.readInt());
        assertEquals(1, in.readInt());
        assertEquals(1, in.readInt());
        assertEquals("UPENN.EDU", read(in));
        assertEquals("bug", read(in));
        assertEquals(0, in.available());
    }

    @Test
    public void testEmptyCacheMultipleComponents() throws IOException {
        final DataInputStream in = new DataInputStream(new ByteArrayInputStream(
                MitKerberosTicketCache.emptyCache("bug/admin@UPENN.EDU")));
        in.skipBytes(2 + 2 + 2 + 2 + 4 + 4 + 4);
        assertEquals(2, in.readInt());
        assertEquals("UPENN.EDU", read(in));
        assertEquals("bug", read(in));
        assertEquals("admin", read(in));
        assertEquals(0, in.available());
    }

    @Test
    public void testLocateNotFound() {
        assertNull(MitKerberosTicketCache.locate("C:\\does\\not\\exist"));
    }

    private static String read(final DataInputStream in) throws IOException {
        final byte[] bytes = new byte[in.readInt()];
        in.readFully(bytes);
        return new String(bytes, StandardCharsets.UTF_8);
    }
}
