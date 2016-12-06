package ch.cyberduck.core.serializer.impl.dd;

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
import ch.cyberduck.core.TestProtocol;
import ch.cyberduck.core.transfer.DownloadTransfer;
import ch.cyberduck.core.transfer.Transfer;
import ch.cyberduck.core.transfer.UploadTransfer;

import org.junit.Test;

import java.util.Collections;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class TransferPlistReaderTest {

    @Test
    public void testDeserializeUpload() throws Exception {
        final Transfer t = new TransferPlistReader(new ProtocolFactory(Collections.singleton(new TestProtocol()))).read(
                new Local("src/test/resources/c44b5120-8dfe-41af-acd3-da99d87b811f.cyberducktransfer")
        );
        assertTrue(t instanceof UploadTransfer);
        assertEquals("identity.api.rackspacecloud.com", t.getSource().getHostname());
        assertEquals("/test.cyberduck.ch/bookmarks_en.png", t.getRoot().remote.getAbsolute());
        assertEquals("C:\\Users\\Yves Langisch\\Pictures\\bookmarks_en.png", t.getRoot().local.getAbsolute());
    }

    @Test
    public void testDeserializeDownload() throws Exception {
        final Transfer t = new TransferPlistReader(new ProtocolFactory(Collections.singleton(new TestProtocol()))).read(
                new Local("src/test/resources/fcea1809-1d75-42f1-92b5-99b38bc1d63e.cyberducktransfer")
        );
        assertTrue(t instanceof DownloadTransfer);
        assertEquals("s3.amazonaws.com", t.getSource().getHostname());
        assertEquals("/cyberduck/Cyberduck-3.3.zip", t.getRoot().remote.getAbsolute());
        assertEquals("C:\\Users\\Yves Langisch\\Desktop\\Cyberduck-3.3.zip", t.getRoot().local.getAbsolute());
    }
}
