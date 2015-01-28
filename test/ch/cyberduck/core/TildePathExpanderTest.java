package ch.cyberduck.core;

/*
 * Copyright (c) 2002-2015 David Kocher. All rights reserved.
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

import ch.cyberduck.core.exception.ConnectionCanceledException;
import ch.cyberduck.core.sftp.SFTPProtocol;
import ch.cyberduck.core.sftp.SFTPSession;

import org.junit.Test;

import java.util.EnumSet;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;

public class TildePathExpanderTest extends AbstractTestCase {

    @Test(expected = ConnectionCanceledException.class)
    public void testExpandNotConnected() throws Exception {
        final Host host = new Host(new SFTPProtocol(), "test.cyberduck.ch", new Credentials(
                properties.getProperty("sftp.user"), properties.getProperty("sftp.password")
        ));
        final SFTPSession session = new SFTPSession(host);
        new TildePathExpander(session).expand(new Path("~/f", EnumSet.of(Path.Type.file)));
    }

    @Test
    public void testExpand() throws Exception {
        final Host host = new Host(new SFTPProtocol(), "test.cyberduck.ch", new Credentials(
                properties.getProperty("sftp.user"), properties.getProperty("sftp.password")
        ));
        final SFTPSession session = new SFTPSession(host);
        session.open(new DisabledHostKeyCallback(), new DisabledTranscriptListener());
        session.login(new DisabledPasswordStore(), new DisabledLoginCallback(), new DisabledCancelCallback());
        final Path expanded = new TildePathExpander(session).expand(new Path("~/f", EnumSet.of(Path.Type.file)));
        assertEquals(new Path("/home/jenkins/f", EnumSet.of(Path.Type.file)), expanded);
        assertEquals(new Path("/home/jenkins", EnumSet.of(Path.Type.directory)), expanded.getParent());
        session.close();
    }

    @Test
    public void testExpandPathWithDirectory() throws Exception {
        final Host host = new Host(new SFTPProtocol(), "test.cyberduck.ch", new Credentials(
                properties.getProperty("sftp.user"), properties.getProperty("sftp.password")
        ));
        final SFTPSession session = new SFTPSession(host);
        session.open(new DisabledHostKeyCallback(), new DisabledTranscriptListener());
        session.login(new DisabledPasswordStore(), new DisabledLoginCallback(), new DisabledCancelCallback());
        final Path expanded = new TildePathExpander(session).expand(new Path("/~/f/s", EnumSet.of(Path.Type.file)));
        assertEquals(new Path("/home/jenkins/f/s", EnumSet.of(Path.Type.file)), expanded);
        assertEquals(new Path("/home/jenkins/f", EnumSet.of(Path.Type.directory)), expanded.getParent());
        session.close();
    }

    @Test
    public void testNoExpand() throws Exception {
        final Host host = new Host(new SFTPProtocol(), "test.cyberduck.ch", new Credentials(
                properties.getProperty("sftp.user"), properties.getProperty("sftp.password")
        ));
        final SFTPSession session = new SFTPSession(host);
        session.open(new DisabledHostKeyCallback(), new DisabledTranscriptListener());
        session.login(new DisabledPasswordStore(), new DisabledLoginCallback(), new DisabledCancelCallback());
        final Path f = new Path("/f", EnumSet.of(Path.Type.file));
        assertSame(f, new TildePathExpander(session).expand(f));
        session.close();
    }
}