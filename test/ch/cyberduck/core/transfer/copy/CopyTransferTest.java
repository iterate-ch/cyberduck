package ch.cyberduck.core.transfer.copy;

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

import ch.cyberduck.core.*;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.ftp.FTPSession;
import ch.cyberduck.core.sftp.SFTPSession;
import ch.cyberduck.core.transfer.Transfer;
import ch.cyberduck.core.transfer.TransferAction;
import ch.cyberduck.core.transfer.TransferOptions;
import ch.cyberduck.core.transfer.TransferPrompt;
import ch.cyberduck.core.transfer.TransferStatus;

import org.junit.Test;

import java.util.Collections;
import java.util.UUID;

import static org.junit.Assert.*;

/**
 * @version $Id$
 */
public class CopyTransferTest extends AbstractTestCase {

    @Test
    public void testSerialize() throws Exception {
        final Path test = new Path("t", Path.FILE_TYPE);
        CopyTransfer t = new CopyTransfer(new SFTPSession(new Host(Protocol.SFTP, "t")),
                new FTPSession(new Host(Protocol.FTP, "t")), Collections.<Path, Path>singletonMap(test, new Path("d", Path.FILE_TYPE)));
        TransferStatus saved = new TransferStatus();
        saved.setLength(4L);
        saved.setCurrent(3L);
        t.save(test, saved);
        final CopyTransfer serialized = new CopyTransfer(t.serialize(SerializerFactory.get()),
                new SFTPSession(new Host(Protocol.SFTP, "t")));
        assertNotSame(t, serialized);
        assertEquals(t.getRoots(), serialized.getRoots());
        assertEquals(t.files, serialized.files);
        assertEquals(t.getBandwidth(), serialized.getBandwidth());
        assertEquals(4L, serialized.getSize());
        assertEquals(3L, serialized.getTransferred());
    }

    @Test
    public void testAction() throws Exception {
        final Path test = new Path("t", Path.FILE_TYPE);
        CopyTransfer t = new CopyTransfer(new SFTPSession(new Host(Protocol.SFTP, "t")),
                new FTPSession(new Host(Protocol.FTP, "t")), Collections.<Path, Path>singletonMap(test, new Path("d", Path.FILE_TYPE)));
        assertEquals(TransferAction.ACTION_OVERWRITE, t.action(false, true));
    }

    @Test
    public void testDuplicate() throws Exception {
        final SFTPSession session = new SFTPSession(new Host(Protocol.SFTP, "test.cyberduck.ch", new Credentials(
                properties.getProperty("sftp.user"), properties.getProperty("sftp.password")
        )));
        session.open(new DefaultHostKeyController());
        session.login(new DisabledPasswordStore(), new DisabledLoginController());

        final Path test = session.list(session.workdir(), new DisabledListProgressListener()).get(
                new Path(session.workdir(), "test", Path.FILE_TYPE).getReference());
        assertNotEquals(-1, test.attributes().getSize());

        final Path copy = new Path(session.workdir(), UUID.randomUUID().toString(), Path.FILE_TYPE);
        final Transfer t = new CopyTransfer(session, session,
                Collections.<Path, Path>singletonMap(test, copy));

        t.start(new TransferPrompt() {
            @Override
            public TransferAction prompt() throws BackgroundException {
                return TransferAction.ACTION_CANCEL;
            }
        }, new TransferOptions());
        assertTrue(t.isComplete());
        assertNotNull(t.getTimestamp());

        session.close();
    }

    @Test
    public void testCopyBetweenHosts() throws Exception {
        final SFTPSession session = new SFTPSession(new Host(Protocol.SFTP, "test.cyberduck.ch", new Credentials(
                properties.getProperty("sftp.user"), properties.getProperty("sftp.password")
        )));
        session.open(new DefaultHostKeyController());
        session.login(new DisabledPasswordStore(), new DisabledLoginController());

        final FTPSession destination = new FTPSession(new Host(Protocol.FTP_TLS, "test.cyberduck.ch", new Credentials(
                properties.getProperty("ftp.user"), properties.getProperty("ftp.password")
        )));
        destination.open(new DefaultHostKeyController());
        destination.login(new DisabledPasswordStore(), new DisabledLoginController());

        final Path test = session.list(session.workdir(), new DisabledListProgressListener()).get(
                new Path(session.workdir(), "test", Path.FILE_TYPE).getReference());
        assertNotEquals(-1, test.attributes().getSize());

        final Path copy = new Path(destination.workdir(), UUID.randomUUID().toString(), Path.FILE_TYPE);
        final Transfer t = new CopyTransfer(session, destination,
                Collections.<Path, Path>singletonMap(test, copy));

        t.start(new TransferPrompt() {
            @Override
            public TransferAction prompt() throws BackgroundException {
                return TransferAction.ACTION_CANCEL;
            }
        }, new TransferOptions());
        assertTrue(t.isComplete());
        assertNotNull(t.getTimestamp());

        session.close();
        destination.close();
    }
}
