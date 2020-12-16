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

import ch.cyberduck.core.DisabledListProgressListener;
import ch.cyberduck.core.Host;
import ch.cyberduck.core.ListService;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.features.IdProvider;
import ch.cyberduck.core.ssl.X509KeyManager;
import ch.cyberduck.core.ssl.X509TrustManager;

import org.apache.log4j.Logger;
import org.nuxeo.onedrive.client.types.GroupItem;
import org.nuxeo.onedrive.client.types.Site;

public class SharepointSession extends AbstractSharepointSession {
    private static final Logger log = Logger.getLogger(SharepointSession.class);

    public SharepointSession(final Host host, final X509TrustManager trust, final X509KeyManager key) {
        super(host, trust, key);
    }

    @Override
    public boolean isSingleSite() {
        return false;
    }

    @Override
    public Site getSite(final Path file) throws BackgroundException {
        return Site.byId(getClient(), fileIdProvider.getFileid(file, new DisabledListProgressListener()));
    }

    @Override
    public GroupItem getGroup(final Path file) throws BackgroundException {
        return new GroupItem(getClient(), fileIdProvider.getFileid(file, new DisabledListProgressListener()));
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T _getFeature(final Class<T> type) {
        if(type == ListService.class) {
            return (T) new SharepointListService(this, this.getFeature(IdProvider.class));
        }
        return super._getFeature(type);
    }

    @Override
    public boolean isAccessible(final Path file, final boolean container) {
        if(file.isRoot()) {
            return false;
        }

        final Path containerPath = getContainer(file);
        if(containerPath.isRoot()) {
            return false;
        }

        final Path parent = containerPath.getParent();
        if(parent.isRoot()) {
            return false;
        }

        if(SharepointListService.GROUPS_CONTAINER.equals(parent.getParent().getName())) {
            return container || !containerPath.equals(file);
        }
        return SharepointListService.DRIVES_CONTAINER.equals(containerPath.getParent().getName())
            && (container || !containerPath.equals(file));
    }
}
