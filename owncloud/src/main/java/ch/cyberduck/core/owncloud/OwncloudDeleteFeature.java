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

import ch.cyberduck.core.LocaleFactory;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.exception.AccessDeniedException;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.nextcloud.NextcloudDeleteFeature;

import java.text.MessageFormat;

/**
 * Deny deleting virtual folders and root folder of spaces and shares
 */
public class OwncloudDeleteFeature extends NextcloudDeleteFeature {

    private final OwncloudPathContainerService containerService = new OwncloudPathContainerService();

    private final OwncloudSession session;

    public OwncloudDeleteFeature(final OwncloudSession session) {
        super(session);
        this.session = session;
    }

    @Override
    public void preflight(final Path file) throws BackgroundException {
        if(session.ocs.spaces) {
            if(containerService.isVirtual(file) || containerService.isContainer(file)) {
                throw new AccessDeniedException(MessageFormat.format(LocaleFactory.localizedString(
                        "Cannot delete {0}", "Error"), file.getName())).withFile(file);
            }
        }
        super.preflight(file);
    }
}
