package ch.cyberduck.core.irods;

import java.util.EnumSet;

import org.apache.commons.lang3.StringUtils;

/*
 * Copyright (c) 2002-2016 iterate GmbH. All rights reserved.
 * https://cyberduck.io/
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 */

import ch.cyberduck.core.Credentials;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.shared.AbstractHomeFeature;

public class IRODSHomeFinderService extends AbstractHomeFeature {

    private final IRODSSession session;

    public IRODSHomeFinderService(final IRODSSession session) {
        this.session = session;
    }

    @Override
    public Path find() throws BackgroundException {
        final String user;
        final Credentials credentials = session.getHost().getCredentials();
        if(StringUtils.contains(credentials.getUsername(), ':')) {
            user = StringUtils.splitPreserveAllTokens(credentials.getUsername(), ':')[1];
        }
        else {
            user = credentials.getUsername();
        }
        return new Path(new StringBuilder()
            .append(Path.DELIMITER).append(session.getRegion())
            .append(Path.DELIMITER).append("home")
            .append(Path.DELIMITER).append(user)
            .toString(), EnumSet.of(Path.Type.directory, Path.Type.volume));
    }
}
