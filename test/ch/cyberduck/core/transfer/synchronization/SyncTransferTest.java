package ch.cyberduck.core.transfer.synchronization;

/*
 * Copyright (c) 2012 David Kocher. All rights reserved.
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
 * dkocher@cyberduck.ch
 */

import ch.cyberduck.core.AbstractTestCase;
import ch.cyberduck.core.Host;
import ch.cyberduck.core.NullSession;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.Protocol;
import ch.cyberduck.core.sftp.SFTPSession;
import ch.cyberduck.core.transfer.Transfer;
import ch.cyberduck.core.transfer.TransferAction;
import ch.cyberduck.core.transfer.synchronisation.SyncTransfer;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotSame;

/**
 * @version $Id$
 */
public class SyncTransferTest extends AbstractTestCase {

    @Test
    public void testSerialize() throws Exception {
        Transfer t = new SyncTransfer(new NullSession(new Host("t")), new Path("t", Path.FILE_TYPE)) {
            @Override
            public long getSize() {
                return 4L;
            }

            @Override
            public long getTransferred() {
                return 3L;
            }
        };
        final SyncTransfer serialized = new SyncTransfer(t.getAsDictionary(), new SFTPSession(new Host(Protocol.SFTP, "t")));
        assertNotSame(t, serialized);
        assertEquals(t.getRoots(), serialized.getRoots());
        assertEquals(t.getBandwidth(), serialized.getBandwidth());
        assertEquals(4L, serialized.getSize());
        assertEquals(3L, serialized.getTransferred());
    }

    @Test
    public void testFilter() throws Exception {
        final Path p = new Path("t", Path.FILE_TYPE);
        Transfer t = new SyncTransfer(new NullSession(new Host("t")), p);
        t.filter(null, TransferAction.ACTION_OVERWRITE);
    }

    @Test
    public void testChildren() throws Exception {

    }

    @Test
    public void testIsSkipped() throws Exception {

    }
}
