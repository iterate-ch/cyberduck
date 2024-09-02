package ch.cyberduck.core.diagnostics;

/*
 * Copyright (c) 2002-2024 iterate GmbH. All rights reserved.
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
import ch.cyberduck.core.HostnameConfigurator;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.exception.ResolveFailedException;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class HostnameReachability implements Reachability {
    private static final Logger log = LogManager.getLogger(HostnameReachability.class);

    @Override
    public void test(final Host bookmark) throws BackgroundException {
        final HostnameConfigurator configurator = bookmark.getProtocol().getFeature(HostnameConfigurator.class);
        final String hostname = configurator.getHostname(bookmark.getHostname());
        if(StringUtils.isBlank(hostname)) {
            if(log.isWarnEnabled()) {
                log.warn(String.format("Missing hostname in %s", bookmark));
            }
            throw new ResolveFailedException();
        }
    }

    @Override
    public Monitor monitor(final Host bookmark, final Callback callback) {
        return Monitor.disabled;
    }
}
