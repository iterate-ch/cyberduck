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

import ch.cyberduck.core.Credentials;
import ch.cyberduck.core.CredentialsConfigurator;
import ch.cyberduck.core.Host;
import ch.cyberduck.core.Local;
import ch.cyberduck.core.LocalFactory;
import ch.cyberduck.core.Preferences;
import ch.cyberduck.core.sftp.openssh.config.transport.OpenSshConfig;

import org.apache.commons.lang3.StringUtils;

/**
 * @version $Id$
 */
public class OpenSSHCredentialsConfigurator implements CredentialsConfigurator {

    private OpenSshConfig configuration;

    public OpenSSHCredentialsConfigurator() {
        this(new OpenSshConfig(LocalFactory.createLocal(Local.HOME, ".ssh/config")));
    }

    public OpenSSHCredentialsConfigurator(final OpenSshConfig configuration) {
        this.configuration = configuration;
    }

    @Override
    public Credentials configure(final Host host) {
        final Credentials credentials = host.getCredentials();
        if(!credentials.isPublicKeyAuthentication()) {
            if(StringUtils.isNotBlank(host.getHostname())) {
                // Update this host credentials from the OpenSSH configuration file in ~/.ssh/config
                final OpenSshConfig.Host entry = configuration.lookup(host.getHostname());
                if(StringUtils.isNotBlank(entry.getUser())) {
                    credentials.setUsername(entry.getUser());
                }
                if(null != entry.getIdentityFile()) {
                    credentials.setIdentity(entry.getIdentityFile());
                }
                else {
                    // No custom public key authentication configuration
                    if(Preferences.instance().getBoolean("ssh.authentication.publickey.default.enable")) {
                        final Local rsa = LocalFactory.createLocal(Preferences.instance().getProperty("ssh.authentication.publickey.default.rsa"));
                        if(rsa.exists()) {
                            credentials.setIdentity(rsa);
                        }
                        else {
                            final Local dsa = LocalFactory.createLocal(Preferences.instance().getProperty("ssh.authentication.publickey.default.dsa"));
                            if(dsa.exists()) {
                                credentials.setIdentity(dsa);
                            }
                        }
                    }
                }
            }
        }
        return credentials;
    }
}
