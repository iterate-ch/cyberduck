package ch.cyberduck.core.sftp;

/*
 * Copyright (c) 2013 David Kocher. All rights reserved.
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
import ch.cyberduck.core.Credentials;
import ch.cyberduck.core.DefaultHostKeyController;
import ch.cyberduck.core.DisabledLoginController;
import ch.cyberduck.core.DisabledPasswordStore;
import ch.cyberduck.core.Host;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.exception.NotfoundException;
import ch.cyberduck.core.shared.DefaultHomeFinderService;
import ch.cyberduck.core.shared.DefaultTouchFeature;
import ch.cyberduck.core.transfer.TransferStatus;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.Test;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.Collections;
import java.util.UUID;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertNotNull;

/**
 * @version $Id$
 */
public class SFTPReadFeatureTest extends AbstractTestCase {

    @Test(expected = NotfoundException.class)
    public void testReadNotFound() throws Exception {
        final Host host = new Host(new SFTPProtocol(), "test.cyberduck.ch", new Credentials(
                properties.getProperty("sftp.user"), properties.getProperty("sftp.password")
        ));
        final SFTPSession session = new SFTPSession(host);
        session.open(new DefaultHostKeyController());
        session.login(new DisabledPasswordStore(), new DisabledLoginController());
        final TransferStatus status = new TransferStatus();
        new SFTPReadFeature(session).read(new Path(session.workdir(), "nosuchname", Path.FILE_TYPE), status);
    }

    @Test
    public void testReadRange() throws Exception {
        final Host host = new Host(new SFTPProtocol(), "test.cyberduck.ch", new Credentials(
                properties.getProperty("sftp.user"), properties.getProperty("sftp.password")
        ));
        final SFTPSession session = new SFTPSession(host);
        session.open(new DefaultHostKeyController());
        session.login(new DisabledPasswordStore(), new DisabledLoginController());
        final Path test = new Path(new DefaultHomeFinderService(session).find(), UUID.randomUUID().toString(), Path.FILE_TYPE);
        new DefaultTouchFeature(session).touch(test);
        final byte[] content = RandomStringUtils.random(1000).getBytes();
        final OutputStream out = new SFTPWriteFeature(session).write(test, new TransferStatus().length(content.length));
        assertNotNull(out);
        IOUtils.write(content, out);
        IOUtils.closeQuietly(out);
        final TransferStatus status = new TransferStatus();
        status.setLength(content.length);
        status.setAppend(true);
        status.setCurrent(100L);
        final Path workdir = session.workdir();
        final InputStream in = new SFTPReadFeature(session).read(test, status);
        assertNotNull(in);
        final byte[] download = new byte[content.length - 100];
        IOUtils.readFully(in, download);
        final byte[] reference = new byte[content.length - 100];
        System.arraycopy(content, 100, reference, 0, content.length - 100);
        assertArrayEquals(reference, download);
        in.close();
        new SFTPDeleteFeature(session).delete(Collections.<Path>singletonList(test), new DisabledLoginController());
        session.close();
    }
}
