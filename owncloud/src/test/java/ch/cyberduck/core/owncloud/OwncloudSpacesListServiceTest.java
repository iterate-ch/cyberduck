package ch.cyberduck.core.owncloud;

/*
 * Copyright (c) 2002-2026 iterate GmbH. All rights reserved.
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
import ch.cyberduck.core.DisabledListProgressListener;
import ch.cyberduck.core.Host;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.owncloud.model.Drives;
import ch.cyberduck.core.ssl.DefaultX509KeyManager;
import ch.cyberduck.core.ssl.DisabledX509TrustManager;

import org.junit.Test;

import java.util.Collections;
import java.util.EnumSet;

import static org.junit.Assert.*;

public class OwncloudSpacesListServiceTest {

    private static Drives.Drive drive(final String type, final String id) {
        final Drives.Drive drive = new Drives.Drive();
        drive.id = id;
        drive.name = id;
        drive.driveType = type;
        drive.root = new Drives.Root();
        drive.root.id = id;
        drive.root.webDavUrl = String.format("https://example.com/dav/spaces/%s", id);
        return drive;
    }

    private static OwncloudSession session() {
        final OwncloudSession session = new OwncloudSession(new Host(new OwncloudProtocol(), "example.com", new Credentials("u")),
                new DisabledX509TrustManager(), new DefaultX509KeyManager());
        session.ocs.withSpaces(true).withProjects(true).withSharejail(true);
        return session;
    }

    @Test
    public void testListRoot() throws Exception {
        final OwncloudSession session = session();
        session.spaces.withDrives(Collections.singletonList(drive(OwncloudSpaces.DRIVE_TYPE_PERSONAL, "p")));
        final AttributedList<Path> list = new OwncloudSpacesListService(session, null).list(
                new Path("/", EnumSet.of(Path.Type.directory)), new DisabledListProgressListener());
        assertEquals(3, list.size());
        assertTrue(list.contains(OwncloudSpaces.PERSONAL_FOLDER));
        assertTrue(list.contains(OwncloudSpaces.SPACES_FOLDER));
        assertTrue(list.contains(OwncloudSpaces.SHARES_FOLDER));
    }

    @Test
    public void testListRootNoPersonalSpace() throws Exception {
        // Role User Light
        final OwncloudSession session = session();
        session.spaces.withDrives(Collections.singletonList(drive(OwncloudSpaces.DRIVE_TYPE_PROJECT, "iterate")));
        final AttributedList<Path> list = new OwncloudSpacesListService(session, null).list(
                new Path("/", EnumSet.of(Path.Type.directory)), new DisabledListProgressListener());
        assertEquals(2, list.size());
        assertFalse(list.contains(OwncloudSpaces.PERSONAL_FOLDER));
    }
}
