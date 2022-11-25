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
import ch.cyberduck.core.nextcloud.NextcloudHomeFeature;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.EnumSet;

public class OwncloudHomeFeature extends NextcloudHomeFeature {
    private static final Logger log = LogManager.getLogger(OwncloudHomeFeature.class);

    public OwncloudHomeFeature(final Host bookmark) {
        super(bookmark);
    }

    public Path find(final Context context) {
        switch(context) {
            case versions:
                final Path workdir = new Path("/remote.php/dav/meta", EnumSet.of(Path.Type.directory));
                if(log.isDebugEnabled()) {
                    log.debug(String.format("Use home directory %s", workdir));
                }
                return workdir;
        }
        return super.find(context);
    }
}
