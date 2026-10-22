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

import ch.cyberduck.core.Path;
import ch.cyberduck.core.URIEncoder;
import ch.cyberduck.core.dav.DAVSession;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.features.AttributesFinder;
import ch.cyberduck.core.nextcloud.NextcloudShareFeature;

import org.apache.commons.lang3.StringUtils;

/**
 * Identify file to share by resource identifier which is unique across spaces
 */
public class OwncloudSpacesShareFeature extends NextcloudShareFeature {

    private final DAVSession session;

    public OwncloudSpacesShareFeature(final DAVSession session) {
        super(session);
        this.session = session;
    }

    @Override
    public boolean isSupported(final Path file, final Type type) {
        if(OwncloudSpaces.isVirtual(file) || new OwncloudSpacesPathContainerService().isContainer(file)) {
            // Root folder of personal space, project space or share
            return false;
        }
        if(file.isChild(OwncloudSpaces.SHARES_FOLDER)) {
            // No resharing
            return false;
        }
        return super.isSupported(file, type);
    }

    @Override
    protected String toReference(final Path file) throws BackgroundException {
        final String id = StringUtils.isNotBlank(file.attributes().getFileId()) ? file.attributes().getFileId() :
                session.getFeature(AttributesFinder.class).find(file).getFileId();
        return String.format("space=%s", URIEncoder.encode(id));
    }
}
