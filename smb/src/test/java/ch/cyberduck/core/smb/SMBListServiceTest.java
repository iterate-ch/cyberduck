package ch.cyberduck.core.smb;

/*
 * Copyright (c) 2002-2023 iterate GmbH. All rights reserved.
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

import ch.cyberduck.core.AttributedList;
import ch.cyberduck.core.Credentials;
import ch.cyberduck.core.DisabledCancelCallback;
import ch.cyberduck.core.DisabledHostKeyCallback;
import ch.cyberduck.core.DisabledListProgressListener;
import ch.cyberduck.core.DisabledLoginCallback;
import ch.cyberduck.core.Host;
import ch.cyberduck.core.ListService;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.features.Home;
import ch.cyberduck.core.proxy.Proxy;
import ch.cyberduck.core.shared.DefaultHomeFinderService;
import ch.cyberduck.core.transfer.TransferStatus;
import ch.cyberduck.test.TestcontainerTest;

import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.util.EnumSet;

import static org.junit.Assert.*;

@Category(TestcontainerTest.class)
public class SMBListServiceTest extends AbstractSMBTest {

    @Test
    public void testListShareFromContext() throws Exception {
        final Path root = Home.ROOT;
        final AttributedList<Path> result = new SMBListService(session).list(root, new DisabledListProgressListener());
        assertEquals(1, result.size());
        for(Path f : result) {
            assertTrue(f.isVolume());
            assertNotEquals(TransferStatus.UNKNOWN_LENGTH, f.attributes().getSize());
            assertNotEquals(TransferStatus.UNKNOWN_LENGTH, f.attributes().getQuota());
        }
    }

    @Test
    public void testListAllShares() throws Exception {
        final Host host = new Host(new SMBProtocol(), container.getHost(), container.getMappedPort(445));
        host.setCredentials(new Credentials("smbj", "pass"));
        final SMBSession session = new SMBSession(host);
        session.open(Proxy.DIRECT, new DisabledHostKeyCallback(), new DisabledLoginCallback(), new DisabledCancelCallback());
        session.login(Proxy.DIRECT, new DisabledLoginCallback(), new DisabledCancelCallback());
        final Path root = Home.ROOT;
        final AttributedList<Path> result = session.getFeature(ListService.class).list(root, new DisabledListProgressListener());
        assertEquals(4, result.size());
        for(Path f : result) {
            assertTrue(f.isVolume());
            assertNotEquals(TransferStatus.UNKNOWN_LENGTH, f.attributes().getSize());
            assertNotEquals(TransferStatus.UNKNOWN_LENGTH, f.attributes().getQuota());
        }
        session.close();
    }

    @Test
    public void testList() throws Exception {
        final Path home = new DefaultHomeFinderService(session).find();
        final Path testFolder = new Path(home, "folder", EnumSet.of(Path.Type.directory));
        final Path testFile = new Path(testFolder, "L0-file.txt", EnumSet.of(Path.Type.file));
        final Path innerFolder = new Path(testFolder, "L1", EnumSet.of(Path.Type.directory));
        final AttributedList<Path> result = new SMBListService(session).list(testFolder, new DisabledListProgressListener());
        assertEquals(2, result.size());
        assertTrue(result.contains(testFile));
        assertTrue(result.contains(innerFolder));
    }

    @Test
    public void testListEmptyFolder() throws Exception {
        final Path home = new DefaultHomeFinderService(session).find();
        final Path emptyFolder = new Path(home, "empty_folder", EnumSet.of(Path.Type.directory));
        final AttributedList<Path> result = new SMBListService(session).list(emptyFolder, new DisabledListProgressListener());
        assertEquals(0, result.size());
    }
}
