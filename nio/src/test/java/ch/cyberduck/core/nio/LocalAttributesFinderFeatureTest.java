package ch.cyberduck.core.nio;

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

import ch.cyberduck.core.DisabledCancelCallback;
import ch.cyberduck.core.DisabledHostKeyCallback;
import ch.cyberduck.core.DisabledLoginCallback;
import ch.cyberduck.core.DisabledPasswordStore;
import ch.cyberduck.core.Host;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathCache;
import ch.cyberduck.core.features.Delete;
import ch.cyberduck.core.preferences.TemporarySupportDirectoryFinder;
import ch.cyberduck.core.transfer.TransferStatus;

import org.junit.Test;

import java.nio.file.Files;
import java.nio.file.attribute.PosixFileAttributes;
import java.nio.file.attribute.PosixFilePermissions;
import java.util.Collections;
import java.util.EnumSet;
import java.util.UUID;

import static org.junit.Assert.assertEquals;

public class LocalAttributesFinderFeatureTest {

    @Test
    public void testConvert() throws Exception {
        final LocalSession session = new LocalSession(new Host(new LocalProtocol(), new LocalProtocol().getDefaultHostname()));
        session.open(new DisabledHostKeyCallback());
        session.login(new DisabledPasswordStore(), new DisabledLoginCallback(), new DisabledCancelCallback(), PathCache.empty());
        final Path file = new Path(new Path(new TemporarySupportDirectoryFinder().find().getAbsolute(),
                EnumSet.of(Path.Type.directory)), UUID.randomUUID().toString(), EnumSet.of(Path.Type.file));
        new LocalTouchFeature(session).touch(file, new TransferStatus());
        final java.nio.file.Path local = session.getClient().getPath(file.getAbsolute());
        final PosixFileAttributes posixAttributes = Files.readAttributes(local, PosixFileAttributes.class);
        final LocalAttributesFinderFeature finder = new LocalAttributesFinderFeature(session);
        assertEquals(PosixFilePermissions.toString(posixAttributes.permissions()), finder.find(file).getPermission().getSymbol());
        Files.setPosixFilePermissions(local, PosixFilePermissions.fromString("rw-------"));
        assertEquals("rw-------", finder.find(file).getPermission().getSymbol());
        Files.setPosixFilePermissions(local, PosixFilePermissions.fromString("rwxrwxrwx"));
        assertEquals("rwxrwxrwx", finder.find(file).getPermission().getSymbol());
        Files.setPosixFilePermissions(local, PosixFilePermissions.fromString("rw-rw----"));
        assertEquals("rw-rw----", finder.find(file).getPermission().getSymbol());
        assertEquals(posixAttributes.size(), finder.find(file).getSize());
        assertEquals(posixAttributes.lastModifiedTime().toMillis(), finder.find(file).getModificationDate());
        assertEquals(posixAttributes.creationTime().toMillis(), finder.find(file).getCreationDate());
        assertEquals(posixAttributes.lastAccessTime().toMillis(), finder.find(file).getAccessedDate());
        new LocalDeleteFeature(session).delete(Collections.singletonList(file), new DisabledLoginCallback(), new Delete.DisabledCallback());
    }
}