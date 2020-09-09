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
import ch.cyberduck.core.Profile;
import ch.cyberduck.core.editor.EditSaveWorker;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.exception.NotfoundException;
import ch.cyberduck.core.features.IdProvider;
import ch.cyberduck.core.ssl.X509KeyManager;
import ch.cyberduck.core.ssl.X509TrustManager;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.nuxeo.onedrive.client.types.GroupItem;
import org.nuxeo.onedrive.client.types.Site;
import org.slf4j.LoggerFactory;

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
        return Site.byId(getClient(), fileIdProvider.getFileid(file.getParent(), new DisabledListProgressListener()));
    }

    @Override
    public GroupItem getGroup(final Path file) throws BackgroundException {
        return new GroupItem(getClient(), fileIdProvider.getFileid(file.getParent(), new DisabledListProgressListener()));
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
        if(file.isChild(SharepointListService.DEFAULT_NAME)) {
            // handles /Default_Name
            if(SharepointListService.DEFAULT_NAME.equals(file)) {
                return false;
            }
            // handles /Default_Name/Drive-ID
            if(!container && SharepointListService.DEFAULT_NAME.equals(file.getParent())) {
                return false;
            }
        }
        else if(file.isChild(SharepointListService.GROUPS_NAME)) {
            // Handles /Groups_Name and /Groups_Name/Group
            if(SharepointListService.GROUPS_NAME.equals(file) || SharepointListService.GROUPS_NAME.equals(file.getParent())) {
                return false;
            }
            // handles /Groups_Name/Group/Drive-ID
            if(!container && SharepointListService.GROUPS_NAME.equals(file.getParent().getParent())) {
                return false;
            }
        }
        else {
            log.warn(String.format("File %s is neither in %s nor in %s", file, SharepointListService.DEFAULT_NAME, SharepointListService.GROUPS_NAME));
            // This should never happen.
            return false;
        }

        return super.isAccessible(file, container);
    }
}
