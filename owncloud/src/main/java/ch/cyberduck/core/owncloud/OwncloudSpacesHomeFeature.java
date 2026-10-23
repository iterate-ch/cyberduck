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

import ch.cyberduck.core.Host;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathNormalizer;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.features.Home;
import ch.cyberduck.core.nextcloud.NextcloudHomeFeature;
import ch.cyberduck.core.preferences.HostPreferencesFactory;
import ch.cyberduck.core.shared.AbstractHomeFeature;
import ch.cyberduck.core.shared.DefaultPathHomeFeature;
import ch.cyberduck.core.shared.WorkdirHomeFeature;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.text.MessageFormat;

/**
 * Resolve workdir or default path to virtual folder of storage space. Defaults to root with virtual folders.
 */
public class OwncloudSpacesHomeFeature extends AbstractHomeFeature {
    private static final Logger log = LogManager.getLogger(OwncloudSpacesHomeFeature.class);

    private final Host host;
    private final OwncloudSpaces spaces;
    private final OwncloudSpacesPathContainerService containerService = new OwncloudSpacesPathContainerService();

    public OwncloudSpacesHomeFeature(final Host host, final OwncloudSpaces spaces) {
        this.host = host;
        this.spaces = spaces;
    }

    @Override
    public Path find() throws BackgroundException {
        for(Home feature : new Home[]{new WorkdirHomeFeature(host), new DefaultPathHomeFeature(host)}) {
            final Path home = feature.find();
            if(null == home) {
                continue;
            }
            if(containerService.isVirtual(home) || null != spaces.toServer(home)) {
                // Path within virtual folder
                return home;
            }
            final Path virtual = spaces.toVirtual(home.getAbsolute(),
                    HostPreferencesFactory.get(host).getProperty("nextcloud.root.webdav.default"),
                    StringUtils.isBlank(host.getCredentials().getUsername()) ? null :
                            MessageFormat.format(HostPreferencesFactory.get(host).getProperty("nextcloud.root.webdav.user"),
                                    NextcloudHomeFeature.Context.files.name(), host.getCredentials().getUsername()));
            if(null != virtual) {
                log.debug("Map {} to {}", home, virtual);
                return virtual;
            }
            log.warn("No space found for {}", home);
        }
        if(StringUtils.isNotBlank(host.getDefaultPath()) && !StringUtils.startsWith(host.getDefaultPath(), String.valueOf(Path.DELIMITER))
                && null != spaces.getPersonal()) {
            // Relative to personal space
            return PathNormalizer.compose(OwncloudSpaces.PERSONAL_FOLDER, host.getDefaultPath());
        }
        // Virtual folders in root
        return Home.root();
    }
}
