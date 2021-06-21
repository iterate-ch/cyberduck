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
import ch.cyberduck.core.ListProgressListener;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathAttributes;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.onedrive.SharepointSiteSession;
import ch.cyberduck.core.onedrive.features.GraphAttributesFinderFeature;
import ch.cyberduck.core.onedrive.features.GraphFileIdProvider;

import org.nuxeo.onedrive.client.types.Site;

import java.io.IOException;

public class SharepointSiteAttributesFinder extends GraphAttributesFinderFeature {
    private final SharepointSiteSession session;

    public SharepointSiteAttributesFinder(final SharepointSiteSession session, final GraphFileIdProvider fileid) {
        super(session, fileid);
        this.session = session;
    }

    private Site.Metadata getSite(final Path file) throws IOException {
        final Site hostSite = Site.byHostname(session.getClient(), session.getHost().getHostname());
        if(file.isRoot()) {
            return hostSite.getMetadata();
        }
        return Site.byPath(hostSite, file.getAbsolute()).getMetadata();
    }

    @Override
    public PathAttributes find(final Path file, final ListProgressListener listener) throws BackgroundException {
        if(session.isHome(file)) {
            final Site.Metadata site;
            try {
                site = getSite(file);
            }
            catch(IOException exception) {
                throw new DefaultIOExceptionMappingService().map("Failure to read attributes of {0}", exception, file);
            }
            return new PathAttributes().withFileId(site.getId());
        }

        return super.find(file, listener);
    }
}
