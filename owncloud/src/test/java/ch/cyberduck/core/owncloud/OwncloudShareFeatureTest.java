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

import ch.cyberduck.core.Credentials;
import ch.cyberduck.core.DefaultPathAttributes;
import ch.cyberduck.core.Host;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.features.Share;
import ch.cyberduck.core.ssl.DefaultX509KeyManager;
import ch.cyberduck.core.ssl.DisabledX509TrustManager;

import org.junit.Test;

import java.util.EnumSet;

import static org.junit.Assert.*;

public class OwncloudShareFeatureTest {

    private final OwncloudSession session = new OwncloudSession(new Host(new OwncloudProtocol(), "example.com", new Credentials("u")),
            new DisabledX509TrustManager(), new DefaultX509KeyManager());
    private final OwncloudShareFeature feature = new OwncloudShareFeature(session);

    {
        session.ocs.withSpaces(true);
    }

    private final Path space = new Path(OwncloudPathMapper.SPACES_FOLDER, "iterate", EnumSet.of(Path.Type.directory));
    private final Path share = new Path(OwncloudPathMapper.SHARES_FOLDER, "s", EnumSet.of(Path.Type.directory));

    @Test
    public void testIsSupported() {
        assertTrue(feature.isSupported(new Path(OwncloudPathMapper.PERSONAL_FOLDER, "f", EnumSet.of(Path.Type.file)), Share.Type.download));
        assertTrue(feature.isSupported(new Path(space, "f", EnumSet.of(Path.Type.file)), Share.Type.download));
        assertTrue(feature.isSupported(new Path(space, "d", EnumSet.of(Path.Type.directory)), Share.Type.upload));
        assertFalse(feature.isSupported(new Path(space, "f", EnumSet.of(Path.Type.file)), Share.Type.upload));
        // Virtual folders
        assertFalse(feature.isSupported(new Path("/", EnumSet.of(Path.Type.directory)), Share.Type.download));
        assertFalse(feature.isSupported(OwncloudPathMapper.SPACES_FOLDER, Share.Type.download));
        assertFalse(feature.isSupported(OwncloudPathMapper.SHARES_FOLDER, Share.Type.download));
        // Root folder of spaces
        assertFalse(feature.isSupported(OwncloudPathMapper.PERSONAL_FOLDER, Share.Type.download));
        assertFalse(feature.isSupported(space, Share.Type.download));
        assertFalse(feature.isSupported(share, Share.Type.download));
        // No resharing
        assertFalse(feature.isSupported(new Path(share, "f", EnumSet.of(Path.Type.file)), Share.Type.download));
    }

    @Test
    public void testToReference() throws Exception {
        assertEquals("space=storage-users-1%24aaaaaaaa-0000-0000-0000-000000000001%21bbbbbbbb-0000-0000-0000-000000000001",
                feature.toReference(new Path(space, "f", EnumSet.of(Path.Type.file),
                        new DefaultPathAttributes().setFileId("storage-users-1$aaaaaaaa-0000-0000-0000-000000000001!bbbbbbbb-0000-0000-0000-000000000001"))));
    }
}
