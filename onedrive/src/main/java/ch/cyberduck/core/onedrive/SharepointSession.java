package ch.cyberduck.core.onedrive;

/*
 * Copyright (c) 2002-2018 iterate GmbH. All rights reserved.
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

import ch.cyberduck.core.Host;
import ch.cyberduck.core.ListService;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathAttributes;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.ssl.ThreadLocalHostnameDelegatingTrustManager;
import ch.cyberduck.core.ssl.X509KeyManager;
import ch.cyberduck.core.ssl.X509TrustManager;

import org.nuxeo.onedrive.client.OneDriveItem;

import java.util.EnumSet;

public class SharepointSession extends GraphSession {
    public static final String DEFAULT_ID = "DEFAULT_NAME";
    public static final String GROUPS_ID = "GROUPS_NAME";

    public static final Path DEFAULT_NAME = new Path("/Default", EnumSet.of(Path.Type.placeholder, Path.Type.directory), new PathAttributes().withVersionId(DEFAULT_ID));
    public static final Path GROUPS_NAME = new Path("/Groups", EnumSet.of(Path.Type.placeholder, Path.Type.directory), new PathAttributes().withVersionId(GROUPS_ID));

    public SharepointSession(final Host host, final X509TrustManager trust, final X509KeyManager key) {
        super(host, new ThreadLocalHostnameDelegatingTrustManager(trust, host.getHostname()), key);
    }

    @Override
    public OneDriveItem toItem(final Path currentPath, final boolean resolveLastItem) throws BackgroundException {
        return null;
    }

    @Override
    public <T> T _getFeature(final Class<T> type) {
        if(type == ListService.class) {
            return (T) new SharepointListService(this, fileIdProvider());
        }
        return super._getFeature(type);
    }
}
