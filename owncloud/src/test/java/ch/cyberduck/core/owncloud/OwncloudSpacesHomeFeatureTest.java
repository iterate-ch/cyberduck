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
import ch.cyberduck.core.Host;
import ch.cyberduck.core.Path;

import org.junit.Test;

import java.util.EnumSet;

import static org.junit.Assert.assertEquals;

public class OwncloudSpacesHomeFeatureTest {

    private final OwncloudSpaces spaces = new OwncloudSpaces("/dav/spaces/s$p", "/dav/spaces/j$j");

    private Path find(final String defaultPath) throws Exception {
        return this.find(spaces, defaultPath);
    }

    private Path find(final OwncloudSpaces spaces, final String defaultPath) throws Exception {
        final Host host = new Host(new OwncloudProtocol(), "example.com", new Credentials("jane"));
        host.setDefaultPath(defaultPath);
        return new OwncloudSpacesHomeFeature(host, spaces).find();
    }

    @Test
    public void testFind() throws Exception {
        assertEquals("/", this.find(null).getAbsolute());
        assertEquals(OwncloudSpaces.PERSONAL_FOLDER.getAbsolute(), this.find("/remote.php/webdav/").getAbsolute());
        assertEquals(new Path(OwncloudSpaces.PERSONAL_FOLDER, "d", EnumSet.of(Path.Type.directory)).getAbsolute(), this.find("/remote.php/dav/files/jane/d").getAbsolute());
        assertEquals(new Path(OwncloudSpaces.PERSONAL_FOLDER, "d", EnumSet.of(Path.Type.directory)).getAbsolute(), this.find("/dav/spaces/s$p/d").getAbsolute());
        assertEquals(new Path(OwncloudSpaces.SHARES_FOLDER, "s", EnumSet.of(Path.Type.directory)).getAbsolute(), this.find("/dav/spaces/j$j/s").getAbsolute());
        assertEquals(new Path(OwncloudSpaces.PERSONAL_FOLDER, "d", EnumSet.of(Path.Type.directory)).getAbsolute(), this.find("d").getAbsolute());
        assertEquals(OwncloudSpaces.SHARES_FOLDER.getAbsolute(), this.find(OwncloudSpaces.SHARES_FOLDER.getAbsolute()).getAbsolute());
        assertEquals("/", this.find("/unknown").getAbsolute());
    }

    @Test
    public void testFindNoPersonalSpace() throws Exception {
        // Role User Light
        final OwncloudSpaces spaces = new OwncloudSpaces(null, "/dav/spaces/j$j");
        assertEquals("/", this.find(spaces, "/remote.php/webdav/").getAbsolute());
        assertEquals("/", this.find(spaces, "/remote.php/dav/files/jane/d").getAbsolute());
        assertEquals("/", this.find(spaces, "d").getAbsolute());
        assertEquals("/", this.find(spaces, OwncloudSpaces.PERSONAL_FOLDER.getAbsolute()).getAbsolute());
        assertEquals(new Path(OwncloudSpaces.SHARES_FOLDER, "s", EnumSet.of(Path.Type.directory)).getAbsolute(), this.find(spaces, "/dav/spaces/j$j/s").getAbsolute());
    }
}
