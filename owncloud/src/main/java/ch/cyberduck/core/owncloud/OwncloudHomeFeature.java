package ch.cyberduck.core.owncloud;

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

import ch.cyberduck.core.Host;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.features.Home;
import ch.cyberduck.core.nextcloud.NextcloudHomeFeature;
import ch.cyberduck.core.preferences.HostPreferences;
import ch.cyberduck.core.shared.DefaultPathHomeFeature;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class OwncloudHomeFeature extends NextcloudHomeFeature {
    private static final Logger log = LogManager.getLogger(OwncloudHomeFeature.class);

    public OwncloudHomeFeature(final Host bookmark) {
        this(new DefaultPathHomeFeature(bookmark), bookmark);
    }

    public OwncloudHomeFeature(final Home delegate, final Host bookmark) {
        this(delegate, bookmark, new HostPreferences(bookmark).getProperty("owncloud.root.default"));
    }

    public OwncloudHomeFeature(final Home delegate, final Host bookmark, final String root) {
        super(delegate, bookmark, root);
    }

    public Path find(final Context context) throws BackgroundException {
        switch(context) {
            case versions:
                return super.find(Context.meta);
        }
        return super.find(context);
    }
}
