package ch.cyberduck.core.onedrive.features.sharepoint;

/*
 * Copyright (c) 2002-2021 iterate GmbH. All rights reserved.
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

import ch.cyberduck.core.DefaultIOExceptionMappingService;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.onedrive.SharepointSiteSession;
import ch.cyberduck.core.onedrive.features.GraphFileIdProvider;

import org.nuxeo.onedrive.client.types.Site;

import java.io.IOException;

public class SharepointSiteFileIdProvider extends GraphFileIdProvider {
    private final SharepointSiteSession session;

    public SharepointSiteFileIdProvider(final SharepointSiteSession session) {
        super(session);
        this.session = session;
    }

    private Site.Metadata getSite(final Path file) throws IOException {
        final Site hostSite = Site.byHostname(session.getClient(), session.getHost().getHostname());
        if(file.isRoot()) {
            return hostSite.getMetadata(null); // query: null: Default return set.
        }
        return Site.byPath(hostSite, file.getAbsolute()).getMetadata(null); // query: null: Default return set.
    }

    @Override
    public String getFileId(final Path file) throws BackgroundException {
        if(session.isHome(file)) {
            final Site.Metadata site;
            try {
                site = getSite(file);
            }
            catch(IOException exception) {
                throw new DefaultIOExceptionMappingService().map("Failure to read attributes of {0}", exception, file);
            }
            return site.getId();
        }
        return super.getFileId(file);
    }
}
