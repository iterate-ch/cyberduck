package ch.cyberduck.core.openstack;

/*
 * Copyright (c) 2026 AiNET and Blomp. All rights reserved.
 * https://ai.net/
 * https://www.blomp.com/
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 */

import ch.cyberduck.core.Host;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.preferences.HostPreferencesFactory;
import ch.cyberduck.core.shared.AbstractHomeFeature;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.text.MessageFormat;
import java.util.EnumSet;

/**
 * Home directory for services that give every user a single container in a shared account and deny listing the
 * account. Configured with {@code openstack.container.home} where {@code {0}} is replaced with the username.
 */
public class SwiftHomeFeature extends AbstractHomeFeature {
    private static final Logger log = LogManager.getLogger(SwiftHomeFeature.class);

    private final Host host;

    public SwiftHomeFeature(final Host host) {
        this.host = host;
    }

    @Override
    public Path find() {
        final String pattern = HostPreferencesFactory.get(host).getProperty("openstack.container.home");
        if(StringUtils.isBlank(pattern)) {
            return null;
        }
        final String user = SwiftAuthenticationService.toUser(host.getCredentials().getUsername());
        if(StringUtils.isBlank(user)) {
            log.warn("Missing username to determine home container from pattern {}", pattern);
            return null;
        }
        final String container = MessageFormat.format(pattern, user);
        if(StringUtils.isBlank(container) || StringUtils.contains(container, Path.DELIMITER)) {
            log.warn("Invalid home container {} from pattern {}", container, pattern);
            return null;
        }
        return new Path(String.valueOf(Path.DELIMITER) + container, EnumSet.of(Path.Type.volume, Path.Type.directory));
    }
}
