package ch.cyberduck.core;

/*
 * Copyright (c) 2012 David Kocher. All rights reserved.
 * http://cyberduck.ch/
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
 *
 * Bug fixes, suggestions and comments should be sent to:
 * dkocher@cyberduck.ch
 */

import ch.cyberduck.core.exception.LoginCanceledException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public final class CredentialsConfiguratorFactory {
    private static final Logger log = LogManager.getLogger(CredentialsConfiguratorFactory.class);

    private CredentialsConfiguratorFactory() {
        //
    }

    /**
     * @param protocol Protocol
     * @return Configurator for default settings
     */
    public static CredentialsConfigurator get(final Protocol protocol) {
        final CredentialsConfigurator finder = protocol.getFeature(CredentialsConfigurator.class);
        try {
            return finder.reload();
        }
        catch(LoginCanceledException e) {
            log.warn("Failure {} reloading credentials from {}", e, finder);
            return CredentialsConfigurator.DISABLED;
        }
    }
}
