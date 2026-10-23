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

import ch.cyberduck.core.owncloud.model.Drives;

import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.*;

public class OwncloudDrivesResponseHandlerTest {

    public static final String JSON = "{\"value\":[" +
            "{\"driveAlias\":\"personal/jane\",\"driveType\":\"personal\",\"id\":\"storage-users-1$00000000-1111-2222-3333-444444444444\"," +
            "\"name\":\"Jane Doe\",\"quota\":{\"remaining\":1000,\"total\":0,\"used\":10}," +
            "\"root\":{\"id\":\"storage-users-1$00000000-1111-2222-3333-444444444444\"," +
            "\"webDavUrl\":\"https://example.com/dav/spaces/storage-users-1$00000000-1111-2222-3333-444444444444\"}}," +
            "{\"driveAlias\":\"project/iterate\",\"driveType\":\"project\",\"id\":\"storage-users-1$aaaaaaaa-0000-0000-0000-000000000001\"," +
            "\"name\":\"iterate\",\"root\":{\"id\":\"storage-users-1$aaaaaaaa-0000-0000-0000-000000000001\"," +
            "\"webDavUrl\":\"https://example.com/dav/spaces/storage-users-1$aaaaaaaa-0000-0000-0000-000000000001\"}}," +
            "{\"driveAlias\":\"project/archive\",\"driveType\":\"project\",\"id\":\"storage-users-1$aaaaaaaa-0000-0000-0000-000000000002\"," +
            "\"name\":\"Archive\",\"root\":{\"id\":\"storage-users-1$aaaaaaaa-0000-0000-0000-000000000002\",\"deleted\":{\"state\":\"trashed\"}," +
            "\"webDavUrl\":\"https://example.com/dav/spaces/storage-users-1$aaaaaaaa-0000-0000-0000-000000000002\"}}," +
            "{\"driveAlias\":\"project/clash\",\"driveType\":\"project\",\"id\":\"storage-users-1$aaaaaaaa-0000-0000-0000-000000000003\"," +
            "\"name\":\"Clash\",\"root\":{\"id\":\"storage-users-1$aaaaaaaa-0000-0000-0000-000000000003\"," +
            "\"webDavUrl\":\"https://example.com/dav/spaces/storage-users-1$aaaaaaaa-0000-0000-0000-000000000003\"}}," +
            "{\"driveAlias\":\"project/clash\",\"driveType\":\"project\",\"id\":\"storage-users-1$aaaaaaaa-0000-0000-0000-000000000004\"," +
            "\"name\":\"Clash\",\"root\":{\"id\":\"storage-users-1$aaaaaaaa-0000-0000-0000-000000000004\"," +
            "\"webDavUrl\":\"https://example.com/dav/spaces/storage-users-1$aaaaaaaa-0000-0000-0000-000000000004\"}}," +
            "{\"driveAlias\":\"virtual/shares\",\"driveType\":\"virtual\",\"id\":\"a0ca6a90-a365-4782-871e-d44447bbc668$a0ca6a90-a365-4782-871e-d44447bbc668\"," +
            "\"name\":\"Shares\",\"root\":{\"id\":\"a0ca6a90-a365-4782-871e-d44447bbc668$a0ca6a90-a365-4782-871e-d44447bbc668\"," +
            "\"webDavUrl\":\"https://example.com/dav/spaces/a0ca6a90-a365-4782-871e-d44447bbc668$a0ca6a90-a365-4782-871e-d44447bbc668\"}}" +
            "]}";

    @Test
    public void testHandleEntity() throws Exception {
        final List<Drives.Drive> drives = new OwncloudDrivesResponseHandler().handleEntity(new StringEntity(JSON, ContentType.APPLICATION_JSON));
        assertEquals(6, drives.size());
        assertEquals("personal", drives.get(0).driveType);
        assertEquals("https://example.com/dav/spaces/storage-users-1$00000000-1111-2222-3333-444444444444", drives.get(0).root.webDavUrl);
        assertNull(drives.get(1).root.deleted);
        assertEquals("trashed", drives.get(2).root.deleted.state);
    }

    @Test
    public void testHandleEntityEmpty() throws Exception {
        assertTrue(new OwncloudDrivesResponseHandler().handleEntity(new StringEntity("{}", ContentType.APPLICATION_JSON)).isEmpty());
    }
}
