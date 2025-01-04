package ch.cyberduck.core.sftp.openssh;

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

import ch.cyberduck.core.Local;
import ch.cyberduck.core.LocalFactory;
import ch.cyberduck.core.sftp.openssh.config.transport.OpenSshConfig;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class OpenSSHIdentityAgentConfigurator {
    private static final Logger log = LogManager.getLogger(OpenSSHIdentityAgentConfigurator.class);

    private final OpenSshConfig configuration;

    public OpenSSHIdentityAgentConfigurator() {
        this(new OpenSshConfig(LocalFactory.get(LocalFactory.get(LocalFactory.get(), ".ssh"), "config")));
    }

    public OpenSSHIdentityAgentConfigurator(final OpenSshConfig configuration) {
        this.configuration = configuration;
    }

    public String getIdentityAgent(final String alias) {
        final Local agent = configuration.lookup(alias).getIdentityAgent();
        if(null == agent) {
            log.debug("No configuration for alias {}", alias);
            return null;
        }
        log.debug("Found configuration {} for alias {}", agent, alias);
        return agent.getAbsolute();
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("OpenSSHIdentityAgentConfigurator{");
        sb.append("configuration=").append(configuration);
        sb.append('}');
        return sb.toString();
    }
}
