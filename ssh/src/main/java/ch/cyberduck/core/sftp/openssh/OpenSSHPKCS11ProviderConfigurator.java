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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class OpenSSHPKCS11ProviderConfigurator {
    private static final Logger log = LogManager.getLogger(OpenSSHPKCS11ProviderConfigurator.class);

    private final OpenSshConfig configuration;

    public OpenSSHPKCS11ProviderConfigurator() {
        this(new OpenSshConfig(LocalFactory.get(LocalFactory.get(LocalFactory.get(), ".ssh"), "config")));
    }

    public OpenSSHPKCS11ProviderConfigurator(final OpenSshConfig configuration) {
        this.configuration = configuration;
    }

    /**
     * @param alias Hostname
     * @return Native PKCS#11 library configured with <code>PKCS11Provider</code> or null
     */
    public String getProvider(final String alias) {
        final String library = configuration.lookup(alias).getPKCS11Provider();
        if(null == library) {
            log.debug("No configuration for alias {}", alias);
            return null;
        }
        log.debug("Found configuration {} for alias {}", library, alias);
        return library;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("OpenSSHPKCS11ProviderConfigurator{");
        sb.append("configuration=").append(configuration);
        sb.append('}');
        return sb.toString();
    }
}
