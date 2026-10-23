package ch.cyberduck.core.owncloud;

/*
 * Copyright (c) 2002-2022 iterate GmbH. All rights reserved.
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

import ch.cyberduck.core.Host;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathNormalizer;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.features.Home;
import ch.cyberduck.core.nextcloud.NextcloudHomeFeature;
import ch.cyberduck.core.ocs.OcsCapabilities;
import ch.cyberduck.core.preferences.HostPreferencesFactory;
import ch.cyberduck.core.shared.DefaultPathHomeFeature;
import ch.cyberduck.core.shared.WorkdirHomeFeature;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.text.MessageFormat;

public class OwncloudHomeFeature extends NextcloudHomeFeature {
    private static final Logger log = LogManager.getLogger(OwncloudHomeFeature.class);

    private final Host bookmark;
    private final OcsCapabilities ocs;
    private final OwncloudPathMapper mapper;
    private final OwncloudPathContainerService containerService = new OwncloudPathContainerService();

    public OwncloudHomeFeature(final Host bookmark) {
        this(bookmark, OcsCapabilities.none, null);
    }

    public OwncloudHomeFeature(final Host bookmark, final OcsCapabilities ocs, final OwncloudPathMapper mapper) {
        super(bookmark);
        this.bookmark = bookmark;
        this.ocs = ocs;
        this.mapper = mapper;
    }

    /**
     * @return Workdir or default path. Resolved to virtual folder of storage space when spaces are supported
     */
    @Override
    public Path find() throws BackgroundException {
        if(!ocs.spaces) {
            final Path workdir = new WorkdirHomeFeature(bookmark).find();
            if(null != workdir) {
                return workdir;
            }
            return super.find();
        }
        for(Home feature : new Home[]{new WorkdirHomeFeature(bookmark), new DefaultPathHomeFeature(bookmark)}) {
            final Path home = feature.find();
            if(null == home) {
                continue;
            }
            if(containerService.isVirtual(home) || null != mapper.toServer(home)) {
                // Path within virtual folder
                return home;
            }
            final Path virtual = mapper.toVirtual(home.getAbsolute(),
                    HostPreferencesFactory.get(bookmark).getProperty("nextcloud.root.webdav.default"),
                    StringUtils.isBlank(bookmark.getCredentials().getUsername()) ? null :
                            MessageFormat.format(HostPreferencesFactory.get(bookmark).getProperty("nextcloud.root.webdav.user"),
                                    Context.files.name(), bookmark.getCredentials().getUsername()));
            if(null != virtual) {
                log.debug("Map {} to {}", home, virtual);
                return virtual;
            }
            log.warn("No space found for {}", home);
        }
        if(StringUtils.isNotBlank(bookmark.getDefaultPath()) && !StringUtils.startsWith(bookmark.getDefaultPath(), String.valueOf(Path.DELIMITER))
                && null != mapper.getPersonal()) {
            // Relative to personal space
            return PathNormalizer.compose(OwncloudPathMapper.PERSONAL_FOLDER, bookmark.getDefaultPath());
        }
        // Virtual folders in root
        return Home.root();
    }
}
