package ch.cyberduck.core.onedrive;

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
import ch.cyberduck.core.Host;
import ch.cyberduck.core.ListService;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.features.IdProvider;
import ch.cyberduck.core.ssl.X509KeyManager;
import ch.cyberduck.core.ssl.X509TrustManager;

import org.apache.commons.lang3.StringUtils;
import org.nuxeo.onedrive.client.types.GroupItem;
import org.nuxeo.onedrive.client.types.Site;

import java.util.Objects;

public class SharepointSiteSession extends AbstractSharepointSession {
    public SharepointSiteSession(final Host host, final X509TrustManager trust, final X509KeyManager key) {
        super(host, trust, key);
    }

    @Override
    public boolean isSingleSite() {
        return true;
    }

    @Override
    public Site getSite(final Path file) throws BackgroundException {
        final Path parent = file.getParent();
        if (parent.isRoot()) {
            String path = host.getDefaultPath();
            if (StringUtils.isBlank(path) || "/".equals(path)) {
                return Site.byId(getClient(), "root");
            }
            if (!path.startsWith("/")) {
                path = "/" + path;
            }
            return Site.byPath(Site.byHostname(getClient(), host.getHostname()), path);
        }
        return Site.byId(getClient(), fileIdProvider.getFileid(parent, new DisabledListProgressListener()));
    }

    @Override
    public GroupItem getGroup(final Path file) throws BackgroundException {
        return null;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T _getFeature(final Class<T> type) {
        if(type == ListService.class) {
            return (T) new SharepointSiteListService(this, this.getFeature(IdProvider.class));
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

        return SharepointListService.DRIVES_ID.equals(containerPath.getParent().attributes().getVersionId())
            && (container || !containerPath.equals(file));
    }
}
