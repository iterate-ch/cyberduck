package ch.cyberduck.core.sds;

/*
 * Copyright (c) 2002-2017 iterate GmbH. All rights reserved.
 * https://cyberduck.io/
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
 */

import ch.cyberduck.core.AlphanumericRandomStringService;
import ch.cyberduck.core.Credentials;
import ch.cyberduck.core.DisabledCancelCallback;
import ch.cyberduck.core.DisabledHostKeyCallback;
import ch.cyberduck.core.DisabledLoginCallback;
import ch.cyberduck.core.DisabledPasswordStore;
import ch.cyberduck.core.Host;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathCache;
import ch.cyberduck.core.features.Delete;
import ch.cyberduck.core.shared.DefaultFindFeature;
import ch.cyberduck.core.shared.DefaultTouchFeature;
import ch.cyberduck.core.shared.DefaultUploadFeature;
import ch.cyberduck.core.ssl.DefaultX509KeyManager;
import ch.cyberduck.core.ssl.DisabledX509TrustManager;
import ch.cyberduck.core.transfer.TransferStatus;
import ch.cyberduck.test.IntegrationTest;

import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.util.Collections;
import java.util.EnumSet;
import java.util.UUID;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

@Category(IntegrationTest.class)
public class SDSDeleteFeatureTest {

    @Test
    public void testDeleteFile() throws Exception {
        final Host host = new Host(new SDSProtocol(), "duck.ssp-europe.eu", new Credentials(
                System.getProperties().getProperty("sds.user"), System.getProperties().getProperty("sds.key")
        ));
        final SDSSession session = new SDSSession(host, new DisabledX509TrustManager(), new DefaultX509KeyManager());
        session.open(new DisabledHostKeyCallback());
        session.login(new DisabledPasswordStore(), new DisabledLoginCallback(), new DisabledCancelCallback(), PathCache.empty());
        final Path room = new Path("CD-TEST-" + new AlphanumericRandomStringService().random(),
                EnumSet.of(Path.Type.directory, Path.Type.volume));
        new SDSDirectoryFeature(session).mkdir(room, null, new TransferStatus());
        final Path fileInRoom = new Path(room, UUID.randomUUID().toString(), EnumSet.of(Path.Type.file));
        new DefaultTouchFeature(new DefaultUploadFeature(new SDSWriteFeature(session))).touch(fileInRoom, new TransferStatus());
        assertTrue(new DefaultFindFeature(session).find(fileInRoom));
        new SDSDeleteFeature(session).delete(Collections.singletonList(fileInRoom), new DisabledLoginCallback(), new Delete.DisabledCallback());
        assertFalse(new DefaultFindFeature(session).find(fileInRoom));
        new SDSDeleteFeature(session).delete(Collections.singletonList(room), new DisabledLoginCallback(), new Delete.DisabledCallback());
        session.close();
    }

    @Test
    public void testDeleteFolderRoomWithContent() throws Exception {
        final Host host = new Host(new SDSProtocol(), "duck.ssp-europe.eu", new Credentials(
                System.getProperties().getProperty("sds.user"), System.getProperties().getProperty("sds.key")
        ));
        final SDSSession session = new SDSSession(host, new DisabledX509TrustManager(), new DefaultX509KeyManager());
        session.open(new DisabledHostKeyCallback());
        session.login(new DisabledPasswordStore(), new DisabledLoginCallback(), new DisabledCancelCallback(), PathCache.empty());
        final Path room = new Path("CD-TEST-" + new AlphanumericRandomStringService().random(),
                EnumSet.of(Path.Type.directory, Path.Type.volume));
        new SDSDirectoryFeature(session).mkdir(room, null, new TransferStatus());
        final Path folder = new Path(room, UUID.randomUUID().toString(), EnumSet.of(Path.Type.directory));
        new SDSDirectoryFeature(session).mkdir(folder, null, new TransferStatus());
        assertTrue(new DefaultFindFeature(session).find(folder));
        final Path file = new Path(folder, UUID.randomUUID().toString(), EnumSet.of(Path.Type.file));
        new DefaultTouchFeature(new DefaultUploadFeature(new SDSWriteFeature(session))).touch(file, new TransferStatus());
        assertTrue(new DefaultFindFeature(session).find(file));
        new SDSDeleteFeature(session).delete(Collections.singletonList(folder), new DisabledLoginCallback(), new Delete.DisabledCallback());
        assertFalse(new DefaultFindFeature(session).find(folder));
        new SDSDeleteFeature(session).delete(Collections.singletonList(room), new DisabledLoginCallback(), new Delete.DisabledCallback());
        assertFalse(new DefaultFindFeature(session).find(room));
        session.close();
    }
}