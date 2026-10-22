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

import ch.cyberduck.core.Path;
import ch.cyberduck.core.dav.DAVPathEncoder;
import ch.cyberduck.core.owncloud.model.Drives;

import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.junit.Test;

import java.util.Collections;
import java.util.EnumSet;
import java.util.List;

import static org.junit.Assert.*;

public class OwncloudSpacesTest {

    private static OwncloudSpaces spaces() throws Exception {
        final OwncloudSpaces spaces = new OwncloudSpaces();
        spaces.withDrives(new OwncloudDrivesResponseHandler().handleEntity(
                new StringEntity(OwncloudDrivesResponseHandlerTest.JSON, ContentType.APPLICATION_JSON)));
        return spaces;
    }

    @Test
    public void testWithDrives() throws Exception {
        final OwncloudSpaces spaces = new OwncloudSpaces();
        final List<Drives.Drive> projects = spaces.withDrives(new OwncloudDrivesResponseHandler().handleEntity(
                new StringEntity(OwncloudDrivesResponseHandlerTest.JSON, ContentType.APPLICATION_JSON)));
        // Disabled space and spaces with clashing names are ignored
        assertEquals(1, projects.size());
        assertEquals("iterate", projects.get(0).name);
        assertEquals("/dav/spaces/storage-users-1$00000000-1111-2222-3333-444444444444", spaces.getPersonal());
        assertEquals("/dav/spaces/a0ca6a90-a365-4782-871e-d44447bbc668$a0ca6a90-a365-4782-871e-d44447bbc668", spaces.getShares());
        assertEquals(1, spaces.getProjects().size());
    }

    @Test
    public void testWithDrivesPersonalRemoved() throws Exception {
        final OwncloudSpaces spaces = spaces();
        assertNotNull(spaces.getPersonal());
        // Personal space disabled or role changed to User Light
        spaces.withDrives(Collections.emptyList());
        assertNull(spaces.getPersonal());
        assertNull(spaces.toServer(OwncloudSpaces.PERSONAL_FOLDER));
        // Fallback for share jail is retained
        assertEquals("/dav/spaces/a0ca6a90-a365-4782-871e-d44447bbc668$a0ca6a90-a365-4782-871e-d44447bbc668", spaces.getShares());
    }

    @Test
    public void testToServer() throws Exception {
        final OwncloudSpaces spaces = spaces();
        assertEquals("/dav/spaces/storage-users-1$00000000-1111-2222-3333-444444444444",
                spaces.toServer(OwncloudSpaces.PERSONAL_FOLDER).getAbsolute());
        assertEquals("/dav/spaces/storage-users-1$00000000-1111-2222-3333-444444444444/a/f",
                spaces.toServer(new Path(OwncloudSpaces.PERSONAL_FOLDER, "a/f", EnumSet.of(Path.Type.file))).getAbsolute());
        assertEquals("/dav/spaces/storage-users-1$aaaaaaaa-0000-0000-0000-000000000001",
                spaces.toServer(new Path(OwncloudSpaces.SPACES_FOLDER, "iterate", EnumSet.of(Path.Type.directory))).getAbsolute());
        assertEquals("/dav/spaces/storage-users-1$aaaaaaaa-0000-0000-0000-000000000001/d",
                spaces.toServer(new Path(OwncloudSpaces.SPACES_FOLDER, "iterate/d", EnumSet.of(Path.Type.directory))).getAbsolute());
        assertEquals("/dav/spaces/a0ca6a90-a365-4782-871e-d44447bbc668$a0ca6a90-a365-4782-871e-d44447bbc668/s/f",
                spaces.toServer(new Path(OwncloudSpaces.SHARES_FOLDER, "s/f", EnumSet.of(Path.Type.file))).getAbsolute());
        // Name prefix of space
        assertNull(spaces.toServer(new Path(OwncloudSpaces.SPACES_FOLDER, "iterate2", EnumSet.of(Path.Type.directory))));
        assertNull(spaces.toServer(new Path(OwncloudSpaces.SPACES_FOLDER, "Clash", EnumSet.of(Path.Type.directory))));
        assertNull(spaces.toServer(OwncloudSpaces.SPACES_FOLDER));
        assertNull(spaces.toServer(new Path("/", EnumSet.of(Path.Type.directory))));
        assertNull(spaces.toServer(new Path("/remote.php/dav/meta/1/v", EnumSet.of(Path.Type.directory))));
    }

    @Test
    public void testEncode() {
        final DAVPathEncoder encoder = new DAVPathEncoder(new OwncloudSpaces("/dav/spaces/s$p", "/dav/spaces/j$j"));
        assertEquals("/dav/spaces/s%24p/", encoder.encode(OwncloudSpaces.PERSONAL_FOLDER));
        assertEquals("/dav/spaces/s%24p/a%20b/", encoder.encode(new Path(OwncloudSpaces.PERSONAL_FOLDER, "a b", EnumSet.of(Path.Type.directory))));
        assertEquals("/dav/spaces/s%24p/a%20b/f", encoder.encode(new Path(OwncloudSpaces.PERSONAL_FOLDER, "a b/f", EnumSet.of(Path.Type.file))));
        assertEquals("/dav/spaces/j%24j/s/f", encoder.encode(new Path(OwncloudSpaces.SHARES_FOLDER, "s/f", EnumSet.of(Path.Type.file))));
        // Unknown space
        assertEquals(String.format("%s/x/f", OwncloudSpaces.SPACES_FOLDER.getAbsolute()),
                encoder.encode(new Path(OwncloudSpaces.SPACES_FOLDER, "x/f", EnumSet.of(Path.Type.file))));
        // Path outside of virtual folders
        assertEquals("/remote.php/dav/meta/1/v/2", encoder.encode(new Path("/remote.php/dav/meta/1/v/2", EnumSet.of(Path.Type.file))));
    }

    @Test
    public void testToVirtual() throws Exception {
        final OwncloudSpaces spaces = spaces();
        assertEquals(OwncloudSpaces.PERSONAL_FOLDER.getAbsolute(),
                spaces.toVirtual("/dav/spaces/storage-users-1$00000000-1111-2222-3333-444444444444/").getAbsolute());
        assertEquals(new Path(OwncloudSpaces.SPACES_FOLDER, "iterate/d", EnumSet.of(Path.Type.directory)).getAbsolute(),
                spaces.toVirtual("/dav/spaces/storage-users-1$aaaaaaaa-0000-0000-0000-000000000001/d").getAbsolute());
        assertEquals(new Path(OwncloudSpaces.PERSONAL_FOLDER, "d", EnumSet.of(Path.Type.directory)).getAbsolute(),
                spaces.toVirtual("/remote.php/dav/files/jane/d", "remote.php/webdav", "remote.php/dav/files/jane").getAbsolute());
        assertEquals(OwncloudSpaces.PERSONAL_FOLDER.getAbsolute(),
                spaces.toVirtual("/remote.php/webdav/", "remote.php/webdav").getAbsolute());
        assertNull(spaces.toVirtual("/remote.php/webdav"));
        // Alias from workaround with nextcloud.root.webdav.user=/dav/spaces in bookmark
        assertEquals(new Path(OwncloudSpaces.SPACES_FOLDER, "iterate/d", EnumSet.of(Path.Type.directory)).getAbsolute(),
                spaces.toVirtual("/dav/spaces/storage-users-1$aaaaaaaa-0000-0000-0000-000000000001/d", "/dav/spaces").getAbsolute());
        assertNull(spaces.toVirtual("/dav/spaces/storage-users-1$aaaaaaaa-0000-0000-0000-000000000003"));
    }

    @Test
    public void testIsVirtual() {
        assertTrue(OwncloudSpaces.isVirtual(new Path("/", EnumSet.of(Path.Type.directory))));
        assertTrue(OwncloudSpaces.isVirtual(OwncloudSpaces.SPACES_FOLDER));
        assertTrue(OwncloudSpaces.isVirtual(OwncloudSpaces.SHARES_FOLDER));
        assertFalse(OwncloudSpaces.isVirtual(OwncloudSpaces.PERSONAL_FOLDER));
    }
}
