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

import ch.cyberduck.core.dav.DAVQuotaFeature;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.features.Quota;

/**
 * Quota of personal space when spaces are supported
 */
public class OwncloudQuotaFeature implements Quota {

    private final OwncloudSession session;

    public OwncloudQuotaFeature(final OwncloudSession session) {
        this.session = session;
    }

    @Override
    public Space get() throws BackgroundException {
        if(!session.ocs.spaces) {
            return new DAVQuotaFeature(session).get();
        }
        if(null == session.drives.getPersonal()) {
            // No personal space for users with role User Light
            return Quota.unknown;
        }
        // Home is root with virtual folders
        return new DAVQuotaFeature(session, () -> OwncloudPathContainerService.PERSONAL_FOLDER).get();
    }
}
