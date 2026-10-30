package ch.cyberduck.core.sftp.openssh;

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

import ch.cyberduck.core.LocalFactory;
import ch.cyberduck.core.sftp.openssh.config.transport.OpenSshConfig;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class OpenSSHSecurityKeyProviderConfigurator {
    private static final Logger log = LogManager.getLogger(OpenSSHSecurityKeyProviderConfigurator.class);

    private final OpenSshConfig configuration;

    public OpenSSHSecurityKeyProviderConfigurator() {
        this(new OpenSshConfig(LocalFactory.get(LocalFactory.get(LocalFactory.get(), ".ssh"), "config")));
    }

    public OpenSSHSecurityKeyProviderConfigurator(final OpenSshConfig configuration) {
        this.configuration = configuration;
    }

    /**
     * A value beginning with <code>$</code> is the name of an environment variable holding the path to the library.
     *
     * @param alias Hostname
     * @return Middleware library configured with <code>SecurityKeyProvider</code> or null
     */
    public String getProvider(final String alias) {
        final String library = configuration.lookup(alias).getSecurityKeyProvider();
        if(null == library) {
            log.debug("No configuration for alias {}", alias);
            return null;
        }
        if(StringUtils.startsWith(library, "$")) {
            final String variable = StringUtils.removeStart(library, "$");
            final String resolved = this.getEnv(variable);
            if(StringUtils.isBlank(resolved)) {
                log.warn("Missing value for environment variable {} referenced in configuration for alias {}",
                        variable, alias);
                return null;
            }
            log.debug("Found configuration {} from environment variable {} for alias {}", resolved, variable, alias);
            return resolved;
        }
        log.debug("Found configuration {} for alias {}", library, alias);
        return library;
    }

    protected String getEnv(final String variable) {
        return System.getenv(variable);
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("OpenSSHSecurityKeyProviderConfigurator{");
        sb.append("configuration=").append(configuration);
        sb.append('}');
        return sb.toString();
    }
}
