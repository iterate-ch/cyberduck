package ch.cyberduck.core.transfer.move;

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
import ch.cyberduck.core.NullPath;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.Protocol;
import ch.cyberduck.core.sftp.SFTPSession;
import ch.cyberduck.core.transfer.Transfer;

import org.junit.Test;

import java.util.Collections;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotSame;

/**
 * @version $Id$
 */
public class MoveTransferTest extends AbstractTestCase {

    @Test
    public void testSerialize() throws Exception {
        Transfer t = new MoveTransfer(Collections.<Path, Path>singletonMap(new NullPath("t", Path.FILE_TYPE), new NullPath("d", Path.FILE_TYPE)));
        t.addSize(4L);
        t.addTransferred(3L);
        final MoveTransfer serialized = new MoveTransfer(t.getAsDictionary(), new SFTPSession(new Host(Protocol.SFTP, "t")));
        assertNotSame(t, serialized);
        assertEquals(t.getRoots(), serialized.getRoots());
        assertEquals(t.getBandwidth(), serialized.getBandwidth());
        assertEquals(4L, serialized.getSize());
        assertEquals(3L, serialized.getTransferred());
    }

    @Test
    public void testChildren() throws Exception {

    }

    @Test
    public void testFilter() throws Exception {

    }
}
