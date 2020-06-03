package ch.cyberduck.core.onedrive.features.sharepoint;

/*
 * Copyright (c) 2002-2020 iterate GmbH. All rights reserved.
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

import ch.cyberduck.core.DisabledListProgressListener;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathAttributes;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.features.IdProvider;
import ch.cyberduck.core.onedrive.AbstractListService;
import ch.cyberduck.core.onedrive.SharepointSession;

import org.nuxeo.onedrive.client.Sites;
import org.nuxeo.onedrive.client.resources.Site;

import java.util.EnumSet;
import java.util.Iterator;

public class SitesListService extends AbstractListService<Site.Metadata> {
    private final SharepointSession session;
    private final IdProvider idProvider;

    public SitesListService(final SharepointSession session, final IdProvider idProvider) {
        this.session = session;
        this.idProvider = idProvider;
    }

    @Override
    protected Iterator<Site.Metadata> getIterator(final Path directory) throws BackgroundException {
        if (directory.getParent().isRoot()) {
            return Sites.getSites(session.getClient(), "*");
        }
        final String siteId = idProvider.getFileid(directory.getParent(), new DisabledListProgressListener());
        final Site site = new Site(session.getClient(), siteId);
        return Sites.getSites(site);
    }


    @Override
    protected boolean isFiltering(final Path directory) {
        return directory.getParent().isRoot();
    }

    @Override
    protected boolean filter(final Site.Metadata metadata) {
        return null != metadata.getRoot();
    }

    @Override
    protected Path toPath(final Site.Metadata metadata, final Path directory) {
        final PathAttributes attributes = new PathAttributes();
        attributes.setVersionId(metadata.getId());
        attributes.setDisplayname(metadata.getDisplayName());

        return new Path(directory, metadata.getName(), EnumSet.of(Path.Type.directory, Path.Type.volume, Path.Type.placeholder), attributes);
    }
}
