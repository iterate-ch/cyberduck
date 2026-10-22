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
import ch.cyberduck.core.exception.AccessDeniedException;
import ch.cyberduck.core.exception.UnsupportedException;
import ch.cyberduck.core.ssl.DefaultX509KeyManager;
import ch.cyberduck.core.ssl.DisabledX509TrustManager;

import org.junit.Test;

import java.util.EnumSet;
import java.util.Optional;

public class OwncloudSpacesMoveFeatureTest {

    private final OwncloudSpacesMoveFeature feature = new OwncloudSpacesMoveFeature(
            new OwncloudSession(new Host(new OwncloudProtocol(), "example.com", new Credentials("u")), new DisabledX509TrustManager(), new DefaultX509KeyManager()));

    private final Path space = new Path(OwncloudSpaces.SPACES_FOLDER, "iterate", EnumSet.of(Path.Type.directory));

    @Test
    public void testPreflightWithinSpace() throws Exception {
        feature.preflight(new Path(space, "a", EnumSet.of(Path.Type.file)), Optional.of(new Path(space, "d/b", EnumSet.of(Path.Type.file))));
        feature.preflight(new Path(OwncloudSpaces.PERSONAL_FOLDER, "a", EnumSet.of(Path.Type.file)),
                Optional.of(new Path(OwncloudSpaces.PERSONAL_FOLDER, "b", EnumSet.of(Path.Type.file))));
    }

    @Test(expected = UnsupportedException.class)
    public void testPreflightBetweenSpaces() throws Exception {
        feature.preflight(new Path(space, "a", EnumSet.of(Path.Type.file)),
                Optional.of(new Path(OwncloudSpaces.PERSONAL_FOLDER, "a", EnumSet.of(Path.Type.file))));
    }

    @Test(expected = AccessDeniedException.class)
    public void testPreflightRenameSpace() throws Exception {
        feature.preflight(space, Optional.of(new Path(OwncloudSpaces.SPACES_FOLDER, "renamed", EnumSet.of(Path.Type.directory))));
    }

    @Test(expected = AccessDeniedException.class)
    public void testPreflightTargetVirtual() throws Exception {
        feature.preflight(new Path(OwncloudSpaces.PERSONAL_FOLDER, "a", EnumSet.of(Path.Type.directory)),
                Optional.of(new Path(OwncloudSpaces.SHARES_FOLDER, "a", EnumSet.of(Path.Type.directory))));
    }
}
