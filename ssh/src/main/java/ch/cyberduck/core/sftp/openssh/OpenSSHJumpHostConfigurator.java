package ch.cyberduck.core.sftp.openssh;

/*
 * Copyright (c) 2002-2020 iterate GmbH. All rights reserved.
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
import ch.cyberduck.core.HostParser;
import ch.cyberduck.core.JumphostConfigurator;
import ch.cyberduck.core.LocalFactory;
import ch.cyberduck.core.ProtocolFactory;
import ch.cyberduck.core.exception.HostParserException;
import ch.cyberduck.core.exception.LoginCanceledException;
import ch.cyberduck.core.sftp.SFTPProtocol;
import ch.cyberduck.core.sftp.openssh.config.transport.OpenSshConfig;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Collections;

public class OpenSSHJumpHostConfigurator implements JumphostConfigurator {
    private static final Logger log = LogManager.getLogger(OpenSSHJumpHostConfigurator.class);

    private final OpenSshConfig configuration;
    private final OpenSSHHostnameConfigurator hostname;
    private final OpenSSHCredentialsConfigurator credentials;

    public OpenSSHJumpHostConfigurator() {
        this(new OpenSshConfig(LocalFactory.get(LocalFactory.get(LocalFactory.get(), ".ssh"), "config")));
    }

    public OpenSSHJumpHostConfigurator(final OpenSshConfig configuration) {
        this.configuration = configuration;
        this.hostname = new OpenSSHHostnameConfigurator(configuration);
        this.credentials = new OpenSSHCredentialsConfigurator(configuration);
    }

    @Override
    public Host getJumphost(final String alias) {
        if(StringUtils.isBlank(alias)) {
            return null;
        }
        final String proxyJump = configuration.lookup(alias).getProxyJump();
        if(StringUtils.isBlank(proxyJump)) {
            return null;
        }
        if(log.isInfoEnabled()) {
            log.info("Found jump host configuration {} from {}", proxyJump, configuration);
        }
        try {
            final Host host = new HostParser(new ProtocolFactory(Collections.singleton(new SFTPProtocol())), new SFTPProtocol()).get(proxyJump);
            // Resolve credentials
            host.setCredentials(credentials.configure(host));
            // Resolve alias if any
            host.setPort(hostname.getPort(host.getHostname()));
            host.setHostname(hostname.getHostname(host.getHostname()));
            return host;
        }
        catch(HostParserException e) {
            log.warn("Failure parsing JumpHost directive {}", proxyJump);
            return null;
        }
    }

    @Override
    public JumphostConfigurator reload() throws LoginCanceledException {
        hostname.reload();
        credentials.reload();
        return this;
    }
}
