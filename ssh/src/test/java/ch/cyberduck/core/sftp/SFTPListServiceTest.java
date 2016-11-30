package ch.cyberduck.core.sftp;

/*
 * Copyright (c) 2002-2013 David Kocher. All rights reserved.
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
 * Bug fixes, suggestions and comments should be sent to feedback@cyberduck.ch
 */

import ch.cyberduck.core.*;
import ch.cyberduck.core.cryptomator.impl.CryptoVault;
import ch.cyberduck.core.exception.LoginCanceledException;
import ch.cyberduck.core.exception.NotfoundException;
import ch.cyberduck.core.features.Delete;
import ch.cyberduck.core.features.Touch;
import ch.cyberduck.test.IntegrationTest;

import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.util.Arrays;
import java.util.EnumSet;
import java.util.UUID;

import static org.junit.Assert.*;

@Category(IntegrationTest.class)
public class SFTPListServiceTest {

    @Test
    public void testList() throws Exception {
        final Host host = new Host(new SFTPProtocol(), "test.cyberduck.ch", new Credentials(
                System.getProperties().getProperty("sftp.user"), System.getProperties().getProperty("sftp.password")
        ));
        final SFTPSession session = new SFTPSession(host);
        assertNotNull(session.open(new DisabledHostKeyCallback()));
        assertTrue(session.isConnected());
        assertNotNull(session.getClient());
        session.login(new DisabledPasswordStore(), new DisabledLoginCallback(), new DisabledCancelCallback(), PathCache.empty());
        final Path home = new SFTPHomeDirectoryService(session).find();
        final AttributedList<Path> list = new SFTPListService(session).list(home, new DisabledListProgressListener());
        assertTrue(list.contains(new Path(home, "test", EnumSet.of(Path.Type.file))));
        assertEquals(new Permission(Permission.Action.read_write, Permission.Action.read_write, Permission.Action.read_write),
                list.get(new Path(home, "test", EnumSet.of(Path.Type.file))).attributes().getPermission());
        assertTrue(list.contains(new Path(home, "test.directory", EnumSet.of(Path.Type.directory))));
        assertTrue(list.contains(new Path(home, "test.symlink", EnumSet.of(Path.Type.file, AbstractPath.Type.symboliclink))));
        assertEquals(new Path(home, "test", EnumSet.of(Path.Type.file)), list.get(new Path(home, "test.symlink", EnumSet.of(Path.Type.file, AbstractPath.Type.symboliclink))).getSymlinkTarget());
        assertTrue(list.contains(new Path(home, "test.symlink-absolute", EnumSet.of(Path.Type.file, AbstractPath.Type.symboliclink))));
        assertEquals(new Path(home, "test", EnumSet.of(Path.Type.file)), list.get(new Path(home, "test.symlink-absolute", EnumSet.of(Path.Type.file, AbstractPath.Type.symboliclink))).getSymlinkTarget());
        session.close();
    }

    @Test
    public void testInvalidSymlinkTarget() throws Exception {
        final Host host = new Host(new SFTPProtocol(), "test.cyberduck.ch", new Credentials(
                System.getProperties().getProperty("sftp.user"), System.getProperties().getProperty("sftp.password")
        ));
        final SFTPSession session = new SFTPSession(host);
        new LoginConnectionService(new DisabledLoginCallback(), new DisabledHostKeyCallback(),
                new DisabledPasswordStore(), new DisabledProgressListener(), new DisabledTranscriptListener()).connect(session, PathCache.empty());
        final Path home = new SFTPHomeDirectoryService(session).find();
        final AttributedList<Path> list = new SFTPListService(session).list(home, new DisabledListProgressListener());
        assertTrue(list.contains(new Path(home, "notfound", EnumSet.of(Path.Type.file, Path.Type.symboliclink))));
        assertEquals(new Path(home, "test.symlink-invalid", EnumSet.of(Path.Type.file)),
                list.get(new Path(home, "notfound", EnumSet.of(Path.Type.file, Path.Type.symboliclink))).getSymlinkTarget());
    }

    @Test(expected = NotfoundException.class)
    public void testListNotfound() throws Exception {
        final Host host = new Host(new SFTPProtocol(), "test.cyberduck.ch", new Credentials(
                System.getProperties().getProperty("sftp.user"), System.getProperties().getProperty("sftp.password")
        ));
        final SFTPSession session = new SFTPSession(host);
        session.open(new DisabledHostKeyCallback());
        session.login(new DisabledPasswordStore(), new DisabledLoginCallback(), new DisabledCancelCallback(), PathCache.empty());
        final Path f = new Path(UUID.randomUUID().toString(), EnumSet.of(Path.Type.directory));
        final SFTPListService service = new SFTPListService(session);
        service.list(f, new DisabledListProgressListener());
    }

    @Test(expected = NotfoundException.class)
    public void testListFile() throws Exception {
        final Host host = new Host(new SFTPProtocol(), "test.cyberduck.ch", new Credentials(
                System.getProperties().getProperty("sftp.user"), System.getProperties().getProperty("sftp.password")
        ));
        final SFTPSession session = new SFTPSession(host);
        session.open(new DisabledHostKeyCallback());
        session.login(new DisabledPasswordStore(), new DisabledLoginCallback(), new DisabledCancelCallback(), PathCache.empty());
        final Path home = new SFTPHomeDirectoryService(session).find();
        final Path f = new Path(home, "test", EnumSet.of(Path.Type.directory));
        final SFTPListService service = new SFTPListService(session);
        service.list(f, new DisabledListProgressListener());
    }

    @Test
    public void testListCryptomator() throws Exception {
        final Host host = new Host(new SFTPProtocol(), "test.cyberduck.ch", new Credentials(
                System.getProperties().getProperty("sftp.user"), System.getProperties().getProperty("sftp.password")
        ));
        final SFTPSession session = new SFTPSession(host);
        assertNotNull(session.open(new DisabledHostKeyCallback()));
        assertTrue(session.isConnected());
        assertNotNull(session.getClient());
        final PathCache cache = new PathCache(1);
        session.login(new DisabledPasswordStore(), new DisabledLoginCallback(), new DisabledCancelCallback(), cache);
        final Path home = new SFTPHomeDirectoryService(session).find();
        final Path vault = new Path(home, UUID.randomUUID().toString(), EnumSet.of(Path.Type.directory));
        final Path test = new Path(vault, UUID.randomUUID().toString(), EnumSet.of(Path.Type.file));
        session.withVault(new CryptoVault(session, vault, new DisabledPasswordStore(), new DisabledLoginCallback() {
            @Override
            public void prompt(final Host bookmark, final Credentials credentials, final String title, final String reason, final LoginOptions options) throws LoginCanceledException {
                credentials.setPassword("vault");
            }
        }).create());
        assertTrue(session.getFeature(ListService.class).list(vault, new DisabledListProgressListener()).isEmpty());
        session.getFeature(Touch.class).touch(test);
        assertEquals(test, session.getFeature(ListService.class).list(vault, new DisabledListProgressListener()).get(0));
        // Test list of parent unencrypted
        final AttributedList<Path> parent = session.getFeature(ListService.class).list(home, new DisabledListProgressListener());
        assertFalse(parent.isEmpty());
        assertTrue(parent.contains(vault));
        session.getFeature(Delete.class).delete(Arrays.asList(test, vault), new DisabledLoginCallback(), new Delete.DisabledCallback());
        session.close();
    }
}
