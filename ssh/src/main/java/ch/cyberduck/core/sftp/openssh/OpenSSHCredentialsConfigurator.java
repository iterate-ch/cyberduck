package ch.cyberduck.core.sftp.openssh;

/*
 * Copyright (c) 2002-2017 iterate GmbH. All rights reserved.
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

import ch.cyberduck.core.Credentials;
import ch.cyberduck.core.CredentialsConfigurator;
import ch.cyberduck.core.Host;
import ch.cyberduck.core.Local;
import ch.cyberduck.core.LocalFactory;
import ch.cyberduck.core.LoginOptions;
import ch.cyberduck.core.preferences.HostPreferences;
import ch.cyberduck.core.sftp.openssh.config.transport.OpenSshConfig;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class OpenSSHCredentialsConfigurator implements CredentialsConfigurator {
    private static final Logger log = LogManager.getLogger(OpenSSHCredentialsConfigurator.class);

    private final OpenSshConfig configuration;

    public OpenSSHCredentialsConfigurator() {
        this(new OpenSshConfig(LocalFactory.get(LocalFactory.get(LocalFactory.get(), ".ssh"), "config")));
    }

    public OpenSSHCredentialsConfigurator(final OpenSshConfig configuration) {
        this.configuration = configuration;
    }

    @Override
    public Credentials configure(final Host host) {
        if(StringUtils.isNotBlank(host.getHostname())) {
            final Credentials credentials = new Credentials(host.getCredentials());
            configuration.refresh();
            // Update this host credentials from the OpenSSH configuration file in ~/.ssh/config
            final OpenSshConfig.Host entry = configuration.lookup(host.getHostname());
            if(StringUtils.isNotBlank(entry.getUser())) {
                if(!credentials.validate(host.getProtocol(), new LoginOptions(host.getProtocol()).password(false))) {
                    if(log.isInfoEnabled()) {
                        log.info(String.format("Using username %s from %s", entry, configuration));
                    }
                    credentials.setUsername(entry.getUser());
                }
            }
            if(!credentials.isPublicKeyAuthentication()) {
                if(null != entry.getIdentityFile()) {
                    if(log.isInfoEnabled()) {
                        log.info(String.format("Using identity %s from %s", entry, configuration));
                    }
                    credentials.setIdentity(entry.getIdentityFile());
                }
                else {
                    // No custom public key authentication configuration
                    if(new HostPreferences(host).getBoolean("ssh.authentication.publickey.default.enable")) {
                        final Local rsa = LocalFactory.get(new HostPreferences(host).getProperty("ssh.authentication.publickey.default.rsa"));
                        if(rsa.exists()) {
                            if(log.isInfoEnabled()) {
                                log.info(String.format("Using RSA default host key %s from %s", rsa, configuration));
                            }
                            credentials.setIdentity(rsa);
                        }
                        else {
                            final Local dsa = LocalFactory.get(new HostPreferences(host).getProperty("ssh.authentication.publickey.default.dsa"));
                            if(dsa.exists()) {
                                if(log.isInfoEnabled()) {
                                    log.info(String.format("Using DSA default host key %s from %s", dsa, configuration));
                                }
                                credentials.setIdentity(dsa);
                            }
                        }
                    }
                }
            }
            return credentials;
        }
        return host.getCredentials();
    }

    @Override
    public CredentialsConfigurator reload() {
        configuration.refresh();
        return this;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("OpenSSHCredentialsConfigurator{");
        sb.append("configuration=").append(configuration);
        sb.append('}');
        return sb.toString();
    }
}
