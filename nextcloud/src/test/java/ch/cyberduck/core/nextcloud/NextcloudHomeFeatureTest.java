package ch.cyberduck.core.nextcloud;

/*
 * Copyright (c) 2002-2022 iterate GmbH. All rights reserved.
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

public class NextcloudHomeFeatureTest {

    @Test
    public void testFindNoUsername() throws Exception {
        final NextcloudHomeFeature feature = new NextcloudHomeFeature(new Host(new NextcloudProtocol()));
        {
            assertEquals(new Path("/ocs/v1.php", EnumSet.of(Path.Type.directory)), feature.find(NextcloudHomeFeature.Context.ocs));
            assertEquals(new Path("/remote.php/webdav", EnumSet.of(Path.Type.directory)), feature.find());
            assertEquals(new Path("/remote.php/webdav", EnumSet.of(Path.Type.directory)), feature.find(NextcloudHomeFeature.Context.files));
            assertEquals(new Path("/remote.php/dav/meta", EnumSet.of(Path.Type.directory)), feature.find(NextcloudHomeFeature.Context.meta));
            assertEquals(new Path("/", EnumSet.of(Path.Type.directory)), feature.find(NextcloudHomeFeature.Context.versions));
        }
    }

    @Test
    public void testFindWithUsername() throws Exception {
        final NextcloudHomeFeature feature = new NextcloudHomeFeature(new Host(new NextcloudProtocol(), new Credentials("u")));
        assertEquals(new Path("/ocs/v1.php", EnumSet.of(Path.Type.directory)), feature.find(NextcloudHomeFeature.Context.ocs));
        assertEquals(new Path("/remote.php/dav/files/u", EnumSet.of(Path.Type.directory)), feature.find(NextcloudHomeFeature.Context.files));
        assertEquals(new Path("/remote.php/dav/meta", EnumSet.of(Path.Type.directory)), feature.find(NextcloudHomeFeature.Context.meta));
        assertEquals(new Path("/remote.php/dav/versions/u", EnumSet.of(Path.Type.directory)), feature.find(NextcloudHomeFeature.Context.versions));
    }

    @Test
    public void testFindWithDefaultPath() throws Exception {
        final Host bookmark = new Host(new NextcloudProtocol(), new Credentials("u"));
        final NextcloudHomeFeature feature = new NextcloudHomeFeature(bookmark);
        for(String s : variants("remote.php/webdav")) {
            bookmark.setDefaultPath(s);
            assertEquals(new Path("/ocs/v1.php", EnumSet.of(Path.Type.directory)), feature.find(NextcloudHomeFeature.Context.ocs));
            assertEquals(new Path("/remote.php/dav/files/u", EnumSet.of(Path.Type.directory)), feature.find(NextcloudHomeFeature.Context.files));
        }
        for(String s : variants("remote.php/webdav/d")) {
            bookmark.setDefaultPath(s);
            assertEquals(new Path("/ocs/v1.php", EnumSet.of(Path.Type.directory)), feature.find(NextcloudHomeFeature.Context.ocs));
            assertEquals(new Path("/remote.php/dav/files/u/d", EnumSet.of(Path.Type.directory)), feature.find(NextcloudHomeFeature.Context.files));
            assertEquals(new Path("/remote.php/dav/meta", EnumSet.of(Path.Type.directory)), feature.find(NextcloudHomeFeature.Context.meta));
            assertEquals(new Path("/remote.php/dav/versions/u", EnumSet.of(Path.Type.directory)), feature.find(NextcloudHomeFeature.Context.versions));
        }
        for(String s : variants("remote.php/dav/files/u")) {
            bookmark.setDefaultPath(s);
            assertEquals(new Path("/ocs/v1.php", EnumSet.of(Path.Type.directory)), feature.find(NextcloudHomeFeature.Context.ocs));
            assertEquals(new Path("/remote.php/dav/files/u/", EnumSet.of(Path.Type.directory)), feature.find(NextcloudHomeFeature.Context.files));
            assertEquals(new Path("/remote.php/dav/meta", EnumSet.of(Path.Type.directory)), feature.find(NextcloudHomeFeature.Context.meta));
            assertEquals(new Path("/remote.php/dav/versions/u", EnumSet.of(Path.Type.directory)), feature.find(NextcloudHomeFeature.Context.versions));
        }
        for(String s : variants("remote.php/dav/files/u/d")) {
            bookmark.setDefaultPath(s);
            assertEquals(new Path("/ocs/v1.php", EnumSet.of(Path.Type.directory)), feature.find(NextcloudHomeFeature.Context.ocs));
            assertEquals(new Path("/remote.php/dav/files/u/d", EnumSet.of(Path.Type.directory)), feature.find(NextcloudHomeFeature.Context.files));
            assertEquals(new Path("/remote.php/dav/meta", EnumSet.of(Path.Type.directory)), feature.find(NextcloudHomeFeature.Context.meta));
            assertEquals(new Path("/remote.php/dav/versions/u", EnumSet.of(Path.Type.directory)), feature.find(NextcloudHomeFeature.Context.versions));
        }
        for(String s : variants("d")) {
            bookmark.setDefaultPath(s);
            assertEquals(new Path("/ocs/v1.php", EnumSet.of(Path.Type.directory)), feature.find(NextcloudHomeFeature.Context.ocs));
            assertEquals(new Path("/remote.php/dav/files/u/d", EnumSet.of(Path.Type.directory)), feature.find(NextcloudHomeFeature.Context.files));
        }
        for(String s : variants("w/remote.php/webdav")) {
            bookmark.setDefaultPath(s);
            assertEquals(new Path("/w/ocs/v1.php", EnumSet.of(Path.Type.directory)), feature.find(NextcloudHomeFeature.Context.ocs));
            assertEquals(new Path("/w/remote.php/dav/files/u", EnumSet.of(Path.Type.directory)), feature.find(NextcloudHomeFeature.Context.files));
        }
        for(String s : variants("w/remote.php/webdav/d")) {
            bookmark.setDefaultPath(s);
            assertEquals(new Path("/w/ocs/v1.php", EnumSet.of(Path.Type.directory)), feature.find(NextcloudHomeFeature.Context.ocs));
            assertEquals(new Path("/w/remote.php/dav/files/u/d", EnumSet.of(Path.Type.directory)), feature.find(NextcloudHomeFeature.Context.files));
        }
        for(String s : variants("w/remote.php/dav/files/u/d")) {
            bookmark.setDefaultPath(s);
            assertEquals(new Path("/w/ocs/v1.php", EnumSet.of(Path.Type.directory)), feature.find(NextcloudHomeFeature.Context.ocs));
            assertEquals(new Path("/w/remote.php/dav/files/u/d", EnumSet.of(Path.Type.directory)), feature.find(NextcloudHomeFeature.Context.files));
        }
    }

    static String[] variants(final String test) {
        return new String[]{test, test + "/", "/" + test, "/" + test + "/"};
    }
}