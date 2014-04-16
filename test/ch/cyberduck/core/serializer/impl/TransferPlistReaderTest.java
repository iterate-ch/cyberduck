package ch.cyberduck.core.serializer.impl;

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

import ch.cyberduck.core.AbstractTestCase;
import ch.cyberduck.core.Protocol;
import ch.cyberduck.core.TransferReaderFactory;
import ch.cyberduck.core.local.FinderLocal;
import ch.cyberduck.core.transfer.Transfer;
import ch.cyberduck.core.transfer.UploadTransfer;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * @version $Id:$
 */
public class TransferPlistReaderTest extends AbstractTestCase {

    @Test
    public void testDeserialize() throws Exception {
        final Transfer t = TransferReaderFactory.get().read(
                new FinderLocal("test/ch/cyberduck/core/serializer/impl/c44b5120-8dfe-41af-acd3-da99d87b811f.cyberducktransfer")
        );
        assertTrue(t instanceof UploadTransfer);
        assertEquals("identity.api.rackspacecloud.com", t.getHost().getHostname());
        assertEquals(Protocol.Type.swift, t.getHost().getProtocol().getType());
        assertEquals("/test.cyberduck.ch/bookmarks_en.png", t.getRoot().remote.getAbsolute());
        assertEquals("C:\\Users\\Yves Langisch\\Pictures\\bookmarks_en.png", t.getRoot().local.getAbsolute());
    }
}
