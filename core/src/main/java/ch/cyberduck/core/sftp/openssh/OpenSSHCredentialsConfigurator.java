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
import ch.cyberduck.core.preferences.Preferences;
import ch.cyberduck.core.preferences.PreferencesFactory;
import ch.cyberduck.core.sftp.openssh.config.transport.OpenSshConfig;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

public class OpenSSHCredentialsConfigurator implements CredentialsConfigurator {
    private static final Logger log = Logger.getLogger(OpenSSHCredentialsConfigurator.class);

    private static final Local file
            = LocalFactory.get(Local.HOME, ".ssh/config");

    private OpenSshConfig configuration;

    private Preferences preferences
            = PreferencesFactory.get();

    public OpenSSHCredentialsConfigurator() {
        this(new OpenSshConfig(file));
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
                    if(log.isInfoEnabled()) {
                        log.info(String.format("Using username %s from %s", entry, file));
                    }
                    credentials.setUsername(entry.getUser());
                }
                if(null != entry.getIdentityFile()) {
                    if(log.isInfoEnabled()) {
                        log.info(String.format("Using identity %s from %s", entry, file));
                    }
                    credentials.setIdentity(entry.getIdentityFile());
                }
                else {
                    // No custom public key authentication configuration
                    if(preferences.getBoolean("ssh.authentication.publickey.default.enable")) {
                        final Local rsa = LocalFactory.get(preferences.getProperty("ssh.authentication.publickey.default.rsa"));
                        if(rsa.exists()) {
                            if(log.isInfoEnabled()) {
                                log.info(String.format("Using RSA default host key %s from %s", rsa, file));
                            }
                            credentials.setIdentity(rsa);
                        }
                        else {
                            final Local dsa = LocalFactory.get(preferences.getProperty("ssh.authentication.publickey.default.dsa"));
                            if(dsa.exists()) {
                                if(log.isInfoEnabled()) {
                                    log.info(String.format("Using DSA default host key %s from %s", dsa, file));
                                }
                                credentials.setIdentity(dsa);
                            }
                        }
                    }
                }
            }
        }
        return credentials;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("OpenSSHCredentialsConfigurator{");
        sb.append("configuration=").append(configuration);
        sb.append('}');
        return sb.toString();
    }
}
