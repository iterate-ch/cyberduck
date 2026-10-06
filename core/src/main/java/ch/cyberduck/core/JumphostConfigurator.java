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

/**
 * Configurator for resolving jump host configuration for bookmark
 */
public interface JumphostConfigurator {

    Host getJumphost(String alias);

    /**
     * @param alias Hostname of bookmark used as alias in configuration
     * @return Command to execute to connect to the server through a proxy such as an OpenSSH {@code ProxyCommand}
     * directive. The returned value may still contain unresolved tokens such as {@code %h}, {@code %p} and {@code %r}.
     * Null if no proxy command is configured.
     */
    default String getProxyCommand(String alias) {
        return null;
    }

    JumphostConfigurator reload() throws LoginCanceledException;

    JumphostConfigurator DISABLED = new JumphostConfigurator() {
        @Override
        public Host getJumphost(final String alias) {
            return null;
        }

        @Override
        public JumphostConfigurator reload() {
            return this;
        }
    };
}
