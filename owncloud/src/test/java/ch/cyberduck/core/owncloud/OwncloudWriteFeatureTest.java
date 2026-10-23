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
import ch.cyberduck.core.nextcloud.NextcloudWriteFeature;
import ch.cyberduck.core.ssl.DefaultX509KeyManager;
import ch.cyberduck.core.ssl.DisabledX509TrustManager;

import org.junit.Test;

import java.util.EnumSet;

public class OwncloudWriteFeatureTest {

    private final OwncloudSession session = new OwncloudSession(new Host(new OwncloudProtocol(), "example.com", new Credentials("u")),
            new DisabledX509TrustManager(), new DefaultX509KeyManager());
    private final OwncloudWriteFeature<Void> feature = new OwncloudWriteFeature<>(session, new NextcloudWriteFeature(session));

    {
        session.ocs.withSpaces(true);
    }

    @Test
    public void testPreflight() throws Exception {
        feature.preflight(new Path(OwncloudPathContainerService.PERSONAL_FOLDER, "f", EnumSet.of(Path.Type.file)));
        feature.preflight(new Path(new Path(OwncloudPathContainerService.SPACES_FOLDER, "iterate", EnumSet.of(Path.Type.directory)), "f", EnumSet.of(Path.Type.file)));
        feature.preflight(new Path(new Path(OwncloudPathContainerService.SHARES_FOLDER, "s", EnumSet.of(Path.Type.directory)), "f", EnumSet.of(Path.Type.file)));
    }

    @Test(expected = AccessDeniedException.class)
    public void testPreflightRoot() throws Exception {
        feature.preflight(new Path("/f", EnumSet.of(Path.Type.file)));
    }

    @Test(expected = AccessDeniedException.class)
    public void testPreflightSpaces() throws Exception {
        feature.preflight(new Path(OwncloudPathContainerService.SPACES_FOLDER, "f", EnumSet.of(Path.Type.file)));
    }

    @Test(expected = AccessDeniedException.class)
    public void testPreflightShares() throws Exception {
        feature.preflight(new Path(OwncloudPathContainerService.SHARES_FOLDER, "f", EnumSet.of(Path.Type.file)));
    }

    @Test
    public void testPreflightNoSpaces() throws Exception {
        session.ocs.withSpaces(false);
        feature.preflight(new Path(OwncloudPathContainerService.SPACES_FOLDER, "f", EnumSet.of(Path.Type.file)));
    }
}
